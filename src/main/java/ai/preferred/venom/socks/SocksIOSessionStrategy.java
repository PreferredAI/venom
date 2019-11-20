package ai.preferred.venom.socks;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ssl.SSLIOSession;

import java.io.IOException;

/**
 * Socks + TSL/SSL layering strategy.
 */
public class SocksIOSessionStrategy implements SchemeIOSessionStrategy {

  private final SSLIOSessionStrategy sslioSessionStrategy;

  /**
   * @param sslioSessionStrategy TSL/SSL strategy
   */
  public SocksIOSessionStrategy(final SSLIOSessionStrategy sslioSessionStrategy) {
    this.sslioSessionStrategy = sslioSessionStrategy;
  }

  @Override
  public IOSession upgrade(final HttpHost host, final IOSession session) throws IOException {
    final HttpRoute route = (HttpRoute) session.getAttribute(IOSession.ATTACHMENT_KEY);

    final SocksIOSession socksSession = new SocksIOSession(session);
    socksSession.initialize();

    if ("https".equals(route.getTargetHost().getSchemeName())) {
      final SSLIOSession wrappedSocksSession = sslioSessionStrategy.upgrade(route.getTargetHost(), socksSession);
      wrappedSocksSession.setAttribute(SocksIOSession.SESSION_KEY, socksSession);
      return wrappedSocksSession;
    }

    return socksSession;
  }

  @Override
  public boolean isLayeringRequired() {
    return true;
  }

}
