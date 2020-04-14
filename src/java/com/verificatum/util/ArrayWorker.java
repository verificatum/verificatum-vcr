
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
 * Provides a simple way of dividing work performed component-wise on
 * one or more arrays on multiple cores. {@link
 * com.verificatum.arithm.LargeInteger} contains examples of how this
 * is used.
 *
 * @author Douglas Wikstrom
 */
public abstract class ArrayWorker {

    /**
     * Size of input arrays.
     */
    protected int size;

    /**
     * Creates an instance with the given size.
     *
     * @param size Size of instance.
     */
    public ArrayWorker(final int size) {
        this.size = size;
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
     * @param start Starting index for the work delegated to the
     * active core.
     * @param end Ending index for the work delegated to the active
     * core.
     */
    public abstract void work(int start, int end);

    /**
     * Performs the work of this instance.
     */
    public void work() {
        ArrayWorker.divideWork(this, size);
    }

    /**
     * Determines the number of cores on the machine and divides the
     * work encapsulated in <code>worker</code> on this number of
     * threads.
     *
     * @param worker Encapsulation of work to be done.
     * @param size Length of arrays.
     */
    private static void divideWork(final ArrayWorker worker, final int size) {
        if (worker.divide()) {

            // Fetch a pool of threads.
            final UniformExecutor executor = UniformExecutors.get();

            // Determine how many cores to use and divide the work.
            final int usedCores = Math.min(executor.getCores(), size);
            final int perCore = size / usedCores;

            // Create a list of callable to work independently.
            final List<ArrayWorkerCallable> callables =
                new ArrayList<ArrayWorkerCallable>(usedCores);
            for (int l = 0; l < usedCores; l++) {

                final int start = l * perCore;
                int end = (l + 1) * perCore;
                if (l == usedCores - 1) {
                    end = size;
                }
                callables.add(new ArrayWorkerCallable(worker, start, end));
            }

            // Run the threads until they complete and return the
            // results as a list.
            try {
                executor.invoke(callables);
            } catch (final InterruptedException ie) {
                throw new UtilError("Interrupted threaded computatation!", ie);
            }

        } else {

            worker.work(0, size);
        }
    }
}

/**
 * Callable processing a segment of arrays.
 *
 * @author Douglas Wikstrom
 */
class ArrayWorkerCallable implements Callable<Object> {

    /**
     * Start index for processing.
     */
    private final int start;

    /**
     * End index for processing.
     */
    private final int end;

    /**
     * Underlying worker.
     */
    private final ArrayWorker worker;

    /**
     * Creates a callable that will perform the given number of
     * operations.
     *
     * @param worker Underlying worker.
     * @param start First index of an element to process (inclusive).
     * @param end Last index of an element to process (exclusive).
     */
    ArrayWorkerCallable(final ArrayWorker worker,
                        final int start,
                        final int end) {
        this.worker = worker;
        this.start = start;
        this.end = end;
    }

    @Override
    public Object call() {
        worker.work(start, end);
        return null;
    }
}
