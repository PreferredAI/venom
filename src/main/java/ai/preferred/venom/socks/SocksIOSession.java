package ai.preferred.venom.socks;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.SessionBufferStatus;
import org.apache.http.util.Asserts;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;

/**
 * The class establishes Socks4a connection and delegates the interface calls to a decorated {@link IOSession}.
 */
public class SocksIOSession implements IOSession {

  /**
   * SOCKS session key.
   */
  public static final String SESSION_KEY = "http.session.socks";

  private static final String DEFAULT_USER_ID = "USER";

  private static final byte SOCKS_VERSION = 4;
  private static final byte CONNECT = 1;
  private static final byte NULL = 0;

  private static final byte[] DIST_IP_LOOKUP_REQUEST = new byte[]{0, 0, 0, 1};

  private final IOSession innerSession;
  private final HttpHost targetHost;
  private final String userId;

  private final SocketAddress remoteAddress;

  private final ByteBuffer replyBuf = ByteBuffer.allocate(8);

  private volatile int status = IOSession.ACTIVE;

  private volatile boolean initialized;

  private boolean connectRequested;
  private boolean connectReceived;

  private boolean endOfStream = false;

  /**
   * Decorates {@link IOSession}, sets default user ID for a SOCKS proxy.
   *
   * @param innerSession decorated session
   */
  public SocksIOSession(final IOSession innerSession) {
    this(innerSession, DEFAULT_USER_ID);
  }

  /**
   * Decorates {@link IOSession}, allows to specify user ID.
   *
   * @param innerSession decorated session
   * @param userId       user id as in SOCKS4a specification
   */
  public SocksIOSession(final IOSession innerSession, final String userId) {
    final HttpRoute route = (HttpRoute) innerSession.getAttribute(IOSession.ATTACHMENT_KEY);

    this.innerSession = innerSession;
    this.targetHost = route.getTargetHost();
    this.userId = userId;

    if (targetHost.getAddress() != null) {
      remoteAddress = new InetSocketAddress(targetHost.getAddress(), targetHost.getPort());
    } else {
      remoteAddress = InetSocketAddress.createUnresolved(targetHost.getHostName(), targetHost.getPort());
    }

    innerSession.setAttribute(SESSION_KEY, this);
  }

  /**
   * Initializes Socks IO session.
   *
   * @return true if Socks IO session is successfully initialized, false - otherwise.
   * @throws IOException session IO exceptions
   */
  synchronized boolean initialize() throws IOException {
    Asserts.check(!this.initialized, "Socks I/O session already initialized");

    if (innerSession.getStatus() >= IOSession.CLOSING) {
      return false;
    }

    if (!connectRequested) {
      sendConnectRequest();
    }

    if (!connectReceived) {
      initialized = receiveConnectReply();
    }

    return initialized;
  }

  private void sendConnectRequest() throws IOException {
    Asserts.check(!this.connectRequested, "Socks CONNECT already sent");

    final boolean isIPv4 = InetAddressUtils.isIPv4Address(targetHost.getHostName());

    final byte[] userId = this.userId.getBytes(StandardCharsets.ISO_8859_1);
    final byte[] host = targetHost.getHostName().getBytes(StandardCharsets.ISO_8859_1);

    final int size = 9 + userId.length + (isIPv4 ? 0 : host.length + 1);

    final ByteBuffer buf = ByteBuffer.allocate(size);
    buf.put(SOCKS_VERSION);
    buf.put(CONNECT);
    buf.put((byte) ((targetHost.getPort() >> 8) & 0xff));
    buf.put((byte) (targetHost.getPort() & 0xff));
    buf.put(isIPv4 ? Inet4Address.getByName(targetHost.getHostName()).getAddress() : DIST_IP_LOOKUP_REQUEST);
    buf.put(userId);
    buf.put(NULL);
    if (!isIPv4) {
      buf.put(host);
      buf.put(NULL);
    }
    buf.flip();

    if (innerSession.channel().write(buf) != size) {
      throw new IOException("Could not flush the buffer");
    }

    connectRequested = true;
  }

  private boolean receiveConnectReply() throws IOException {
    final int read = innerSession.channel().read(replyBuf);
    if (!endOfStream && read == -1) {
      endOfStream = true;
      close();
      throw new IOException("IO channel closed before connection established");
    }

    if (replyBuf.position() < 8) {
      return false;
    }

    replyBuf.flip();

    processConnectReply();

    return true;
  }

  private void processConnectReply() throws IOException {
    Asserts.check(!this.connectReceived, "CONNECT reply has been already received");
    Asserts.check(replyBuf.limit() == 8, "Response is expected of 8 bytes, but got {}", replyBuf.limit());

    byte vn = replyBuf.get();

    Asserts.check(vn == 0 || vn == 4, "Invalid socks version {}", vn);

    IOException ex = null;
    final byte cd = replyBuf.get();
    switch (cd) {
      case 90:
        break;
      case 91:
        ex = new IOException("SOCKS request rejected");
        break;
      case 92:
        ex = new IOException("SOCKS server couldn't reach destination");
        break;
      case 93:
        ex = new IOException("SOCKS authentication failed");
        break;
      default:
        ex = new IOException("Reply from SOCKS server contains bad status");
        break;
    }

    if (ex != null) {
      close();
      throw ex;
    }

    connectReceived = true;
  }

  /**
   * Checks if the session has been initialized.
   *
   * @return true if Socks IO session is successfully initialized, false - otherwise.
   */
  boolean isInitialized() {
    return initialized;
  }

  @Override
  public ByteChannel channel() {
    return innerSession.channel();
  }

  @Override
  public SocketAddress getRemoteAddress() {
    return remoteAddress;
  }

  @Override
  public SocketAddress getLocalAddress() {
    return innerSession.getLocalAddress();
  }

  @Override
  public int getEventMask() {
    return innerSession.getEventMask();
  }

  @Override
  public void setEventMask(int ops) {
    innerSession.setEventMask(ops);
  }

  @Override
  public void setEvent(int op) {
    innerSession.setEvent(op);
  }

  @Override
  public void clearEvent(int op) {
    innerSession.clearEvent(op);
  }

  @Override
  public synchronized void close() {
    if (status >= IOSession.CLOSING) {
      return;
    }
    status = IOSession.CLOSED;
    innerSession.close();
  }

  @Override
  public synchronized void shutdown() {
    if (status >= IOSession.CLOSING) {
      return;
    }
    status = IOSession.CLOSED;
    innerSession.shutdown();
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public boolean isClosed() {
    return innerSession.isClosed();
  }

  @Override
  public int getSocketTimeout() {
    return innerSession.getSocketTimeout();
  }

  @Override
  public void setSocketTimeout(int timeout) {
    innerSession.setSocketTimeout(timeout);
  }

  @Override
  public void setBufferStatus(SessionBufferStatus status) {
    innerSession.setBufferStatus(status);
  }

  @Override
  public boolean hasBufferedInput() {
    return innerSession.hasBufferedInput();
  }

  @Override
  public boolean hasBufferedOutput() {
    return innerSession.hasBufferedOutput();
  }

  @Override
  public void setAttribute(final String name, final Object obj) {
    innerSession.setAttribute(name, obj);
  }

  @Override
  public Object getAttribute(final String name) {
    return innerSession.getAttribute(name);
  }

  @Override
  public Object removeAttribute(final String name) {
    return innerSession.removeAttribute(name);
  }

}
