package ai.preferred.venom;

import ai.preferred.venom.job.Scheduler;
import ai.preferred.venom.request.Request;
import ai.preferred.venom.response.VResponse;

/**
 * This class defines fatal runtime exception for {@link Handler}.
 * If {@link Handler#handle(Request, VResponse, Scheduler, Session, Worker)} encounters unexpected situation,
 * it can signal {@link Crawler} to stop execution by throwing this exception.
 *
 * @author Maksim Tkachenko
 */
public class FatalHandlerException extends RuntimeException {

  public FatalHandlerException() {
    super();
  }

  public FatalHandlerException(String message) {
    super(message);
  }

  public FatalHandlerException(String message, Throwable cause) {
    super(message, cause);
  }

  public FatalHandlerException(Throwable cause) {
    super(cause);
  }

}
