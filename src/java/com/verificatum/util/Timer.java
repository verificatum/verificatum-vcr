
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
