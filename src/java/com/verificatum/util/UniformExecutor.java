
/*
 * Copyright 2008-2018 Douglas Wikstrom
 *
 * This file is part of Verificatum Core Routines (VCR).
 *
 * VCR is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * VCR is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with VCR. If not, see <http://www.gnu.org/licenses/>.
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
