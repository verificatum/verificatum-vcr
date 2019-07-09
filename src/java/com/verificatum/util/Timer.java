
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

/**
 * Simplistic timer that allows setting a point in time in the future
 * and then later querying if this future time has become now.
 *
 * @author Douglas Wikstrom
 */
public final class Timer {

    /**
     * Time from which this timer reports that the time is up.
     */
    private long endTime;

    /**
     * Creates an instance.
     *
     * @param timeToEnd Total time to wait.
     */
    public Timer(final long timeToEnd) {
        if (timeToEnd >= 0) {
            endTime = System.currentTimeMillis() + timeToEnd;
        } else {
            endTime = -1;
        }
    }

    /**
     * Returns true when the time is up.
     *
     * @return <code>true</code> if the time is up and
     * <code>false</code> otherwise.
     */
    public boolean timeIsUp() {
        if (endTime < 0) {
            return false;
        } else {
            return remainingTime() == 0;
        }
    }

    /**
     * Returns the remaining number of milliseconds until the time is
     * up, or zero if the time is already up.
     *
     * @return Remaining time until the time is up.
     */
    public long remainingTime() {
        if (endTime < 0) {
            return -1;
        } else {

            final long r = endTime - System.currentTimeMillis();

            if (r <= 0) {
                return 0;
            } else {
                return r;
            }
        }
    }
}
