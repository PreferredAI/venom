package ai.preferred.venom.socks;

import com.google.common.annotations.Beta;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.protocol.HttpContext;

/**
 * This route planners ensures that the connection to https server via socks proxy works. It prevents http client from
 * tunnelling the IO session twice ({@link SocksIOSessionStrategy} upgrades {@link SocksIOSession} to
 * {@link org.apache.http.nio.reactor.ssl.SSLIOSession} when necessary).
 */
@Beta
public class SocksHttpRoutePlanner implements HttpRoutePlanner {

  private final HttpRoutePlanner rp;

  /**
   * Decorates {@link HttpRoutePlanner}.
   *
   * @param rp decorated route planner
   */
  public SocksHttpRoutePlanner(final HttpRoutePlanner rp) {
    this.rp = rp;
  }

  @Override
  public HttpRoute determineRoute(HttpHost host, HttpRequest request, HttpContext context) throws HttpException {
    final HttpRoute route = rp.determineRoute(host, request, context);
    final boolean secure = "https".equalsIgnoreCase(route.getTargetHost().getSchemeName());
    if (secure && route.getProxyHost() != null && "socks".equalsIgnoreCase(route.getProxyHost().getSchemeName())) {
      return new HttpRoute(route.getTargetHost(), route.getLocalAddress(), route.getProxyHost(), false);
    }
    return route;
  }

}
