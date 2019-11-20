package ai.preferred.venom.socks;

import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;

import java.io.InterruptedIOException;
import java.util.concurrent.ThreadFactory;

/**
 * This IOReactor makes sure that the supplied {@link IOEventDispatch} is decorated with {@link SocksIOEventDispatch}.
 */
public class SocksConnectingIOReactor extends DefaultConnectingIOReactor {

  /**
   * Creates an instance of SocksConnectingIOReactor with the given configuration.
   *
   * @param config        I/O reactor configuration.
   * @param threadFactory the factory to create threads.
   *                      Can be {@code null}.
   * @throws IOReactorException in case if a non-recoverable I/O error.
   */
  public SocksConnectingIOReactor(IOReactorConfig config, ThreadFactory threadFactory) throws IOReactorException {
    super(config, threadFactory);
  }

  /**
   * Creates an instance of SocksConnectingIOReactor with the given configuration.
   *
   * @param config I/O reactor configuration.
   *               Can be {@code null}.
   * @throws IOReactorException in case if a non-recoverable I/O error.
   */
  public SocksConnectingIOReactor(IOReactorConfig config) throws IOReactorException {
    super(config);
  }

  /**
   * Creates an instance of SocksConnectingIOReactor with default configuration.
   *
   * @throws IOReactorException in case if a non-recoverable I/O error.
   */
  public SocksConnectingIOReactor() throws IOReactorException {
    super();
  }

  @Override
  public void execute(final IOEventDispatch eventDispatch) throws InterruptedIOException, IOReactorException {
    super.execute(new SocksIOEventDispatch(eventDispatch));
  }

}
