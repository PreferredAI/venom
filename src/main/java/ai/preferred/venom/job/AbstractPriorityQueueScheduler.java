/*
 * Copyright (c) 2019 Preferred.AI
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

package ai.preferred.venom.job;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class AbstractPriorityQueueScheduler extends AbstractQueueScheduler {

  /**
   * Constructs an instance of AbstractQueueScheduler.
   */
  protected AbstractPriorityQueueScheduler() {
    super(new PriorityBlockingQueue<>(11,
        Comparator.comparing(o -> ((PriorityJobAttribute) o.getJobAttribute(PriorityJobAttribute.class)))));
  }

  protected final Job ensurePriorityJobAttribute(final Job job) {
    if (job.getJobAttribute(PriorityJobAttribute.class) == null) {
      job.addJobAttribute(new PriorityJobAttribute());
    }
    return job;
  }

  @Override
  public final void put(final @Nonnull Job job) throws InterruptedException {
    ensurePriorityJobAttribute(job);
    getQueue().put(job);
  }

  @Override
  public final boolean offer(final Job job, final long timeout, final @Nonnull TimeUnit unit)
      throws InterruptedException {
    ensurePriorityJobAttribute(job);
    return getQueue().offer(job, timeout, unit);
  }

  @Override
  public final boolean offer(final @Nonnull Job job) {
    ensurePriorityJobAttribute(job);
    return getQueue().offer(job);
  }


}
