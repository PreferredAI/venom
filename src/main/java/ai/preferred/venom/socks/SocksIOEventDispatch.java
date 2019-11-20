package ai.preferred.venom.socks;

import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.NHttpClientEventHandler;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutor;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOSession;

import java.io.IOException;

/**
 * This class wraps and handles IO dispatch related to {@link SocksIOSession}.
 */
public class SocksIOEventDispatch implements IOEventDispatch {

  private final IOEventDispatch dispatch;

  /**
   * Decorates {@link IOEventDispatch}.
   *
   * @param dispatch delegated IO dispatch
   */
  public SocksIOEventDispatch(IOEventDispatch dispatch) {
    this.dispatch = dispatch;
  }

  @Override
  public void connected(IOSession session) {
    dispatch.connected(session);
  }

  @Override
  public void inputReady(IOSession session) {
    try {
      if (initializeSocksSession(session)) {
        dispatch.inputReady(session);
      }
    } catch (RuntimeException e) {
      session.shutdown();
      throw e;
    }
  }

  @Override
  public void outputReady(IOSession session) {
    try {
      if (initializeSocksSession(session)) {
        dispatch.outputReady(session);
      }
    } catch (RuntimeException e) {
      session.shutdown();
      throw e;
    }
  }

  @Override
  public void timeout(IOSession session) {
    try {
      dispatch.timeout(session);
      final SocksIOSession socksIOSession = getSocksSession(session);
      if (socksIOSession != null) {
        socksIOSession.shutdown();
      }
    } catch (RuntimeException e) {
      session.shutdown();
      throw e;
    }
  }

  @Override
  public void disconnected(IOSession session) {
    dispatch.disconnected(session);
  }

  private boolean initializeSocksSession(IOSession session) {
    final SocksIOSession socksSession = getSocksSession(session);
    if (socksSession != null) {
      try {
        try {
          if (!socksSession.isInitialized()) {
            return socksSession.initialize();
          }
        } catch (final IOException e) {
          onException(socksSession, e);
          throw new RuntimeException(e);
        }
      } catch (final RuntimeException e) {
        socksSession.shutdown();
        throw e;
      }
    }
    return true;
  }

  private void onException(IOSession session, Exception ex) {
    final NHttpClientConnection conn = getConnection(session);
    if (conn != null) {
      final NHttpClientEventHandler handler = getEventHandler(conn);
      if (handler != null) {
        handler.exception(conn, ex);
      }
    }
  }

  private SocksIOSession getSocksSession(IOSession session) {
    return (SocksIOSession) session.getAttribute(SocksIOSession.SESSION_KEY);
  }

  private NHttpClientConnection getConnection(IOSession session) {
    return (NHttpClientConnection) session.getAttribute(IOEventDispatch.CONNECTION_KEY);
  }

  private NHttpClientEventHandler getEventHandler(NHttpConnection conn) {
    return (NHttpClientEventHandler) conn.getContext().getAttribute(HttpAsyncRequestExecutor.HTTP_HANDLER);
  }

}
