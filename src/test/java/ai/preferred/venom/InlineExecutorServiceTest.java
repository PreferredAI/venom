/*
 * Copyright 2018 Preferred.AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.preferred.venom;

import ai.preferred.venom.utils.InlineExecutorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class InlineExecutorServiceTest {

  @Test
  public void testExecute() {
    final InlineExecutorService inlineExecutorService = new InlineExecutorService();
    final AtomicBoolean executed = new AtomicBoolean(false);

    Assertions.assertFalse(inlineExecutorService.isShutdown());
    Assertions.assertFalse(inlineExecutorService.isTerminated());

    inlineExecutorService.execute(() -> executed.set(true));
    Assertions.assertTrue(executed.get());

    Assertions.assertFalse(inlineExecutorService.isShutdown());
    Assertions.assertFalse(inlineExecutorService.isTerminated());

    inlineExecutorService.shutdown();
    Assertions.assertTrue(inlineExecutorService.isShutdown());
    Assertions.assertTrue(inlineExecutorService.isTerminated());
  }

  @Test
  public void testShutdownExecute() throws InterruptedException {
    final InlineExecutorService inlineExecutorService = new InlineExecutorService();
    final AtomicBoolean executed = new AtomicBoolean(false);

    inlineExecutorService.shutdown();
    Assertions.assertTrue(inlineExecutorService.isShutdown());

    inlineExecutorService.awaitTermination(1, TimeUnit.NANOSECONDS);
    Assertions.assertTrue(inlineExecutorService.isTerminated());

    Assertions.assertThrows(RejectedExecutionException.class,
        () -> inlineExecutorService.execute(() -> executed.set(true)));
    Assertions.assertFalse(executed.get());

    Assertions.assertTrue(inlineExecutorService.isShutdown());

    inlineExecutorService.awaitTermination(1, TimeUnit.NANOSECONDS);
    Assertions.assertTrue(inlineExecutorService.isTerminated());
  }

  @Test
  public void testShutdownNow() {
    final InlineExecutorService inlineExecutorService = new InlineExecutorService();
    final AtomicBoolean executed = new AtomicBoolean(false);

    final List<Runnable> runnables = inlineExecutorService.shutdownNow();
    Assertions.assertTrue(runnables.isEmpty());
    Assertions.assertTrue(inlineExecutorService.isShutdown());
    Assertions.assertTrue(inlineExecutorService.isTerminated());

    Assertions.assertThrows(RejectedExecutionException.class,
        () -> inlineExecutorService.execute(() -> executed.set(true)));
    Assertions.assertFalse(executed.get());

    Assertions.assertTrue(inlineExecutorService.isShutdown());
    Assertions.assertTrue(inlineExecutorService.isTerminated());
  }

}
