
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
