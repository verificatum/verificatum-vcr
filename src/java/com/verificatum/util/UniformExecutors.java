
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
