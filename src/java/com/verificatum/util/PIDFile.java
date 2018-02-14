
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

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.verificatum.eio.ExtIO;

/**
 * Provides functionality to determine the PID of the JVM and storing
 * it in a PID file. PID files are often used to allow other processes
 * to monitor and kill other processes. This is dangerous if done
 * incorrectly. PIDs may be reused, so checking the availability of a
 * process with a given PID, or killing it may give unexpected
 * results.
 *
 * <p>
 *
 * There is no robust and platform independent way to determine the
 * PID from within a Java application. Instead we assume that the JVM
 * has been started from a shell script wrapper and given this parent
 * PID as input. Determining a child PID from a parent PID is easy.
 *
 * <p>
 *
 * To avoid the problem with re-use of PIDs in a robust way we dump
 * the time when the PID was written to file on file as well. A
 * monitoring process can then look up the process using the PID file
 * and verify that the start time of the process predates the time the
 * PID was dumped. If this is done early in the life of the JVM
 * process, the probability of failure is negligible.
 *
 * <p>
 *
 * We store the time on file instead of checking the time of creation
 * of the file, since the creation time provided by
 * java.nio.Files.readAttributes is platform independent. We use the
 * format on the format "epoch YYYY-MM-DD:00:00:000", since the first
 * component makes it easy for software to compare start times and the
 * second makes manual inspection trivial (except in the rare case it
 * is a leap year or the beginning/end of daylight savings).
 *
 * @author Douglas Wikstrom
 */
public final class PIDFile {

    /**
     * Avoid accidental instantiation.
     */
    private PIDFile() { }

    /**
     * String representation of the PID of this JVM.
     */
    private static String pidString = null;

    /**
     * String representation of the PID of this JVM.
     */
    private static final Object PID_STRING_LOCK = new Object();

    /**
     * Given the parent PID of this JVM, this determines and sets its
     * PID for future use. This is meant to be used by wrapper scripts
     * that feed the PID of the script to the JVM.
     *
     * @param parentPidString String representation of the parent of
     * this JVM.
     */
    public static void setPID(final String parentPidString) {
        synchronized (PID_STRING_LOCK) {
            if (pidString == null) {
                final String command = "ps --pid " + parentPidString;

                Process process = null;
                InputStream is = null;
                try {

                    final Runtime runTime = Runtime.getRuntime();
                    process = runTime.exec(command);
                    is = process.getInputStream();
                    pidString = ExtIO.readString(is);

                } catch (final IOException ioe) {
                    throw new UtilError("Unable to determine PID of JVM!", ioe);
                } finally {
                    ExtIO.strictClose(is);
                    if (process != null) {
                        process.destroy();
                    }
                }
            } else {
                final String e =
                    String.format("Attempting to reset pid! (%s -> %s)",
                                  pidString, pidString);
                throw new UtilError(e);
            }
        }
    }

    /**
     * Returns the PID of this JVM if it has been initialized.
     *
     * @return String representation of the PID of this JVM.
     */
    public static String getPID() {
        return pidString;
    }

    /**
     * Stores the PID of this JVM on one file and the start time on a
     * human-readable format on another.
     *
     * @param pidFile File storing PID of this JVM.
     * @param startTimeFile File storing the start time of this JVM.
     */
    public static void writePIDandStartTime(final File pidFile,
                                            final File startTimeFile) {
        final long epoch = System.currentTimeMillis();

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epoch);

        final String format = "yyyy-MM-dd:HH:mm:ss.SSS";
        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        final String startTime = String.format("%s %s", epoch, sdf.toString());

        try {
            ExtIO.writeString(pidFile, pidString);
            ExtIO.writeString(startTimeFile, startTime);
        } catch (final IOException ioe) {
            throw new UtilError("Failed to write PID file!", ioe);
        }
    }

    /**
     * Reads a PID from one file and the time the file was written
     * from another as an epoch.
     *
     * @param pidFile File storing PID of this JVM.
     * @param startTimeFile File storing the start time of this JVM.
     * @return Pair of PID and start time.
     */
    public static Pair<String, Long>
        readPIDandStartTime(final File pidFile,
                            final File startTimeFile) {
        try {

            final String pidString = ExtIO.readString(pidFile);
            final String startTimeString = ExtIO.readString(startTimeFile);
            final long startTime = Long.parseLong(startTimeString);

            return new Pair<String, Long>(pidString, startTime);

        } catch (final IOException ioe) {
            throw new UtilError("Failed to write PID file!", ioe);
        }
    }
}

