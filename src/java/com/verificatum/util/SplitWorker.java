
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
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Provides a simple way of dividing work for operations that provide
 * a single output.
 *
 * @param <T> Return type of a thread.
 *
 * @author Douglas Wikstrom
 */
public abstract class SplitWorker<T> {

    /**
     * Total number of operations.
     */
    protected int totalOperations;

    /**
     * Creates an instance with the total number of operations.
     *
     * @param totalOperations Total number of operations to perform.
     */
    public SplitWorker(final int totalOperations) {
        this.totalOperations = totalOperations;
    }

    /**
     * Determines if the operation is threaded or not. This should be
     * replaced by subclasses that determines to thread or not based
     * on the cost of an operation.
     *
     * @return True or false depending on if an operation should be
     * threaded or not.
     */
    public boolean divide() {
        return true;
    }

    /**
     * Performs the work delegated to a given core.
     *
     * @param operations Number of operations delegated to the active
     * core.
     * @return T Result of the computations of one thread.
     */
    public abstract T work(int operations);

    /**
     * Performs the work of this instance.
     *
     * @return List of results of all threads.
     */
    public List<T> work() {
        return SplitWorker.divideWork(this, totalOperations);
    }

    /**
     * Determines the number of cores on the machine and divides the
     * work encapsulated in <code>worker</code> on this number of
     * threads.
     *
     * @param <F> Return type of each thread.
     * @param worker Encapsulation of work to be done.
     * @param totalOperations Total number of operations performed.
     * @return List of results of all threads.
     */
    private static <F> List<F> divideWork(final SplitWorker<F> worker,
                                          final int totalOperations) {
        if (worker.divide()) {

            // Fetch a pool of threads.
            final UniformExecutor executor = UniformExecutors.get();

            // Determine how many cores to use and divide the work.
            final int usedCores =
                Math.min(executor.getCores(), totalOperations);
            final int operations = totalOperations / usedCores;
            int remaining = totalOperations;

            // Create a list of callable to work independently.
            final List<SplitWorkerCallable<F>> callables =
                new ArrayList<SplitWorkerCallable<F>>(usedCores);
            for (int l = 0; l < usedCores; l++) {
                final int ops = Math.min(operations, remaining);
                callables.add(new SplitWorkerCallable<F>(worker, ops));
                remaining -= ops;
            }

            // Run the threads until they complete and return the
            // results as a list.
            try {
                return executor.invoke(callables);
            } catch (final InterruptedException ie) {
                throw new UtilError("Interrupted threaded computatation!", ie);
            }

        } else {

            final List<F> result = new ArrayList<F>(1);
            result.add(worker.work(totalOperations));
            return result;
        }
    }
}

/**
 * Callable processing a number of operations.
 *
 * @param <T> Return type of callable.
 *
 * @author Douglas Wikstrom
 */
class SplitWorkerCallable<T> implements Callable<T> {

    /**
     * Number of operations performed by this callable.
     */
    private final int operations;

    /**
     * Underlying worker.
     */
    private final SplitWorker<T> worker;

    /**
     * Creates a callable that will perform the given number of
     * operations.
     *
     * @param worker Underlying worker.
     * @param operations Number of operations performed by this
     * worker.
     */
    SplitWorkerCallable(final SplitWorker<T> worker,
                        final int operations) {
        this.worker = worker;
        this.operations = operations;
    }

    @Override
    public T call() {
        return worker.work(operations);
    }
}
