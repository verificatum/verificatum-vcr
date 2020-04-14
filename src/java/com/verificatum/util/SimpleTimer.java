
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
 * Implements a simple timer based on
 * {@link System#currentTimeMillis()}.
 *
 * @author Douglas Wikstrom
 */
public class SimpleTimer {

    /**
     * Time at which this instance was created.
     */
    long startTime;

    /**
     * Create timer that automatically starts.
     */
    public SimpleTimer() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Returns a string representation of the input millisecond in the
     * form of a string of hours, minutes, and seconds.
     *
     * @param millisecs Milliseconds to convert.
     * @return String representation of input in terms of hour,
     * minutes, and seconds.
     */
    public static String toString(final long millisecs) {

        final long secs = millisecs / 1000;
        final long minutes = secs / 60;
        final long remainingSecs = secs % 60;
        final long hours = minutes / 60;
        final long remainingMinutes = minutes % 60;

        return String.format("%2dh %2dm %2ds",
                             hours,
                             remainingMinutes,
                             remainingSecs);
    }

    /**
     * Returns the elapsed time.
     *
     * @return Elapsed time.
     */
    public long elapsed() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Outputs a string that describes the time elapsed since this
     * timer was created.
     *
     * @return String describing the time elapsed since this instance
     *         was created.
     */
    @Override
    public final String toString() {
        return toString(elapsed());
    }
}
