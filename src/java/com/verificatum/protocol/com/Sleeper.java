
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

package com.verificatum.protocol.com;

/**
 * A simple sleeping thread that sleeps at for a given amount of time
 * unless interrupted prematurely.
 *
 * @author Douglas Wikstrom
 */
public final class Sleeper extends Thread {

    /**
     * Maximum time this instance sleeps when executed as a thread.
     */
    long waitTime;

    /**
     * Creates an instance that waits for at most the given amount of
     * time when started as a thread.
     *
     * @param waitTime Maximum time this instance sleeps when executed
     * as a thread.
     */
    Sleeper(final long waitTime) {
        this.waitTime = waitTime;
    }

    // Documented in Thread.java

    @Override
    public void run() {
        try {
            Thread.sleep(waitTime);
        } catch (final InterruptedException ie) {
        }
    }
}
