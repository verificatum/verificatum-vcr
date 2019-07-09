
/* Copyright 2008-2019 Douglas Wikstrom
 *
 * This file is part of Verificatum Core Routines (VCR).
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.verificatum.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * Wrapper of {@link ThreadPoolExecutor} that holds the exact number
 * of threads equal to the number of cores on the machine. This is not
 * meant to be used directly. Use, e.g., {@link SplitWorker} or {@link
 * ArrayWorker} instead. The only way to invoke this is to hand the
 * exactly the same number of tasks as there are cores.
 *
 * @author Douglas Wikstrom
 */
class UniformExecutor {

    /**
     * Number of cores available.
     */
    private final int cores;

    /**
     * Underlying executor.
     */
    private final ThreadPoolExecutor executor;

    /**
     * Creates an executor that maps computations uniformly onto
     * multiple cores.
     *
     * @param cores Number of cores on the physical machine.
     */
    UniformExecutor(final int cores) {
        this.cores = cores;
        executor = new ThreadPoolExecutor(cores, cores,
                                          0, TimeUnit.SECONDS,
                                          new LinkedBlockingQueue<Runnable>(),
                                          new PriorityThreadFactory());
        executor.prestartAllCoreThreads();
    }

    /**
     * Returns the number of threads of this executor.
     *
     * @return Number of cores on this machine.
     */
    int getCores() {
        return cores;
    }

    /**
     * Invokes each of a list of callable instances, waits for them to
     * complete, and returns their results as a list.
     *
     * @param <T> Return type of each thread.
     * @param callables List of callable instances.
     * @return List of results of the callables.
     *
     * @throws InterruptedException If the execution was interrupted.
     */
    <T> List<T> invoke(final Collection<? extends Callable<T>> callables)
        throws InterruptedException {
        if (callables.size() > cores) {
            throw new UtilError("Number of callables larger than number of "
                                + "cores! (" + callables.size() + " != "
                                + cores + ")");
        } else {
            final List<Future<T>> futures = executor.invokeAll(callables);
            final List<T> results = new ArrayList<T>(cores);
            for (final Future<T> future : futures) {
                try {
                    results.add(future.get());
                } catch (final ExecutionException ee) {
                    throw new UtilError("Unable to get result from thread!",
                                        ee);
                }
            }
            UniformExecutors.put(this);
            return results;
        }
    }
}

/**
 * Thread factory that always creates threads with maximal priority.
 */
class PriorityThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setPriority(java.lang.Thread.MAX_PRIORITY);
        return thread;
    }
}
