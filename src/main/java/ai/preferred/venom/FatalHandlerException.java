package ai.preferred.venom;

/**
 * This class defines fatal runtime exception for {@link Handler}.
 * If {@link Handler#handle(ai.preferred.venom.request.Request,
 * ai.preferred.venom.response.VResponse, ai.preferred.venom.job.Scheduler,
 * Session, Worker)} encounters unexpected situation, it can signal
 * {@link Crawler} to stop execution by throwing this exception.
 *
 * @author Maksim Tkachenko
 */
public class FatalHandlerException extends RuntimeException {

  /**
   * Constructs a new fatal handler exception with {@code null} as its
   * detail message.  The cause is not initialized, and may subsequently be
   * initialized by a call to {@link #initCause}.
   */
  public FatalHandlerException() {
    super();
  }

  /**
   * Constructs a new fatal handler exception with the specified detail message.
   * The cause is not initialized, and may subsequently be initialized by a
   * call to {@link #initCause}.
   *
   * @param message the detail message. The detail message is saved for
   *                later retrieval by the {@link #getMessage()} method.
   */
  public FatalHandlerException(final String message) {
    super(message);
  }

  /**
   * Constructs a new fatal handler exception with the specified detail message and
   * cause.  <p>Note that the detail message associated with
   * {@code cause} is <i>not</i> automatically incorporated in
   * this runtime exception's detail message.
   *
   * @param message the detail message (which is saved for later retrieval
   *                by the {@link #getMessage()} method).
   * @param cause   the cause (which is saved for later retrieval by the
   *                {@link #getCause()} method).  (A <tt>null</tt> value is
   *                permitted, and indicates that the cause is nonexistent or
   *                unknown.)
   * @since 1.4
   */
  public FatalHandlerException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new fatal handler exception with the specified cause and a
   * detail message of <tt>(cause==null ? null : cause.toString())</tt>
   * (which typically contains the class and detail message of
   * <tt>cause</tt>).  This constructor is useful for runtime exceptions
   * that are little more than wrappers for other throwables.
   *
   * @param cause the cause (which is saved for later retrieval by the
   *              {@link #getCause()} method).  (A <tt>null</tt> value is
   *              permitted, and indicates that the cause is nonexistent or
   *              unknown.)
   * @since 1.4
   */
  public FatalHandlerException(final Throwable cause) {
    super(cause);
  }

}
