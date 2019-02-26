package ai.preferred.venom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadedWorkerManagerTest {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ThreadedWorkerManagerTest.class);

  private void submit(Worker worker) throws ExecutionException, InterruptedException {

    final Future<Boolean> futureCallable = worker.submit(() -> true);
    Assertions.assertTrue(futureCallable.get());

    final AtomicBoolean sendBool = new AtomicBoolean(false);
    final Future<AtomicBoolean> future = worker.submit(() -> sendBool.set(true), sendBool);
    final AtomicBoolean returnBool = future.get();
    Assertions.assertTrue(returnBool.get());

    final AtomicBoolean runnableBool = new AtomicBoolean(false);
    final Future<?> futureRunnable = worker.submit(() -> runnableBool.set(true));
    futureRunnable.get();
    Assertions.assertTrue(runnableBool.get());

    final AtomicBoolean blockingBool = new AtomicBoolean(false);
    worker.executeBlockingIO(() -> blockingBool.set(true));
    Assertions.assertTrue(returnBool.get());
  }

  @Test
  public void testDefaultWorker() throws ExecutionException, InterruptedException {
    try (final ThreadedWorkerManager threadedWorkerManager = new ThreadedWorkerManager(new InlineExecutorService())) {
      final Worker worker = threadedWorkerManager.getWorker();
      Assertions.assertTrue(worker instanceof ThreadedWorkerManager.DefaultWorker);

      submit(worker);
    } catch (InterruptedException e) {
      LOGGER.error("Interrupted.");
      throw e;
    } catch (ExecutionException e) {
      LOGGER.error("Execution failure.");
      throw e;
    }
  }

  @Test
  public void testForkJoinWorker() throws ExecutionException, InterruptedException {
    try (final ThreadedWorkerManager threadedWorkerManager = new ThreadedWorkerManager(null)) {
      final Worker worker = threadedWorkerManager.getWorker();
      Assertions.assertTrue(worker instanceof ThreadedWorkerManager.ForkJoinWorker);

      submit(worker);
    } catch (InterruptedException e) {
      LOGGER.error("Interrupted.");
      throw e;
    } catch (ExecutionException e) {
      LOGGER.error("Execution failure.");
      throw e;
    }
  }

  @Test
  public void testInvokeNull() {
    try (final ThreadedWorkerManager threadedWorkerManager = new ThreadedWorkerManager(null)) {
      final Worker worker = threadedWorkerManager.getWorker();
      Assertions.assertTrue(worker instanceof ThreadedWorkerManager.ForkJoinWorker);
      Assertions.assertThrows(NullPointerException.class, () -> worker.executeBlockingIO(null));
    }
  }
}
