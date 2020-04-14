
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

/**
 * Keeps {@link UniformExecutor} instances containing exactly the same
 * number of threads as there are cores on the machine. This is a
 * package level class and is not intended to be used
 * directly. Instead {@link SplitWorker} and {@link ArrayWorker} uses
 * this class to get a thread pool to use during a single
 * multi-threaded computation.
 *
 * <p>
 *
 * The idea is to hide the fact that threads are re-used from the rest
 * of the applications, i.e., instances of the mentioned classes could
 * have been implemented such that threads were created on the fly.
 *
 * @author Douglas Wikstrom
 */
public final class UniformExecutors {

    /**
     * Prevents accidental instantiation.
     */
    private UniformExecutors() {
    }

    /**
     * Stores created executors. For a typical single story-line
     * applications, there will be at most one executor in this list
     * at any time.
     */
    private static List<UniformExecutor> executors =
        new ArrayList<UniformExecutor>();

    static {
        executors.add(get());
    }

    /**
     * Returns an executor with threads with maximum priority.
     *
     * @return Executor with threads of maximum priority.
     */
    static UniformExecutor get() {
        synchronized (executors) {

            if (executors.isEmpty()) {

                final int cores = Runtime.getRuntime().availableProcessors();
                return new UniformExecutor(cores);

            } else {

                return executors.remove(0);
            }
        }
    }

    /**
     * Returns an executor after use.
     *
     * @param executor A uniform executor ready to be re-used.
     */
    static void put(final UniformExecutor executor) {
        synchronized (executors) {
            executors.add(executor);
        }
    }
}
