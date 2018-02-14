
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.verificatum.eio.ExtIO;

/**
 * Stores a string representation of a PID and when it was first
 * written to file measured in seconds since epoch. An instance can
 * either be: (1) read from file, or (2) created almost simultaneously
 * with writing it to file. This class is portable if the underlying
 * programs vrunning and vchild are portable.
 *
 * <p>
 *
 * The file contains a triple matching the regular expression
 *
 * <p>
 *
 * pid starttime yy-MM-dd:hh:mm:ss
 *
 * <p>
 *
 * where "pid" is a process id string, "starttime" is an integer in
 * seconds since epoch, and the rest is a human readable
 * representation of starttime that should be considered a comment. It
 * is ignored when instantiating this class from the contents of a PID
 * file. This class is oblivious of the format of the PID, so any
 * platform independent issues must be handled in the required native
 * programs.
 *
 * @author Douglas Wikstrom
 */
public class PID {

    /**
     * String representation of a platform dependent PID string.
     */
    private final String pidString;

    /**
     * Time in seconds since epoch when the PID was written to file.
     */
    private long starttime;

    /**
     * Constructs an instance and writes the PID string of this JVM
     * along with start time to file as explained above.
     *
     * <p>
     *
     * This constructor assumes that the given PID string represents
     * the parent of this JVM. Thus, this targets situations where the
     * JVM is invoked from a shell script that runs in the foreground
     * and runs a Java application that is given the PID string of the
     * shell script as input.
     *
     * @param parentPidString PID string of the parent process of this
     * JVM.
     * @param pidFile File to which the PID string information is
     * written upon instantiation.
     *
     * @throws UtilException If the PID string of this JVM can not be
     * written to the given file.
     */
    public PID(final String parentPidString, final File pidFile)
        throws UtilException {
        this.pidString = getChildPIDString(parentPidString);
        this.starttime = System.currentTimeMillis() / 1000;
        try {
            ExtIO.writeString(pidFile, getContent());
        } catch (final IOException ioe) {
            throw new UtilError("Failed to write PID file!", ioe);
        }
    }

    /**
     * Constructs an instance from the contents of the given file.
     *
     * @param pidFile File containing PID content as explained above.
     *
     * @throws UtilException If the contents of the PID file are not
     * formatted properly.
     */
    public PID(final File pidFile) throws UtilException {
        String pidContent;
        try {
            pidContent = ExtIO.readString(pidFile);
        } catch (final IOException ioe) {
            throw new UtilError("Failed to write PID contents! ("
                                + pidFile.toString() + ")", ioe);
        }
        final String[] parts = pidContent.split(" ");
        if (parts.length != 3) {
            final String e =
                "Badly formatted PID file does not have three parts! ("
                + pidContent + ")";
            throw new UtilException(e);
        }
        this.pidString = parts[0];
        try {
            this.starttime = Long.parseLong(parts[1]);
        } catch (final NumberFormatException nfe) {
            throw new UtilException("Start time must be a long integer! ("
                                    + parts[1] + ")", nfe);
        }
    }

    /**
     * Returns the PID content of this instance.
     *
     * @return PID content of this instance.
     */
    public String getContent() {
        return String.format("%s %s %s",
                             this.pidString,
                             this.starttime,
                             formatTime(this.starttime * 1000));
    }

    /**
     * Returns the PID string stored in this instance.
     *
     * @return PID string stored in this instance.
     */
    public String getPIDString() {
        return pidString;
    }

    /**
     * Returns the start time in seconds from epoch since this PID was
     * written to file.
     *
     * @return Start time in seconds from epoch since this PID was
     * written to file.
     */
    public long getStartTime() {
        return starttime;
    }

    /**
     * Returns true or false depending on if the process with this PID
     * string is running or not.
     *
     * <p>
     *
     * This is determined by checking that the process with the PID
     * string is running in the operating system and was started
     * before the start time of this PID, so it does not merely verify
     * that <em>some</em> process is running with the given PID
     * string.
     *
     * @return True if and only if the process with this PID is
     * running.
     *
     * @throws UtilException If the underlying system call fails.
     */
    public boolean isRunning() throws UtilException {
        final long now = System.currentTimeMillis() / 1000;
        final String etime = Command.checkedExecute("vrunning", pidString);
        if ("".equals(etime)) {
            return false;
        } else {
            final long elapsed = etimeToLong(etime);
            return elapsed > now - this.starttime;
        }
    }

    /**
     * Extracts the PID string of this JVM from the PID string of the
     * parent process.
     *
     * @param parentPidString PID string of the parent process of this
     * JVM.
     * @return PID of child process of the process with the given PID.
     *
     * @throws UtilException If the parent PID is not the PID of an
     * existing process for which the JVM is permitted to extract the
     * PIDs of child processes, or if there is more than one child
     * process.
     */
    private static String getChildPIDString(final String parentPidString)
        throws UtilException {
        final String pidString =
            Command.checkedExecute("vchild", parentPidString);
        if ("".equals(pidString)) {
            throw new UtilException("Invalid parent PID! ("
                                    + parentPidString + ")");
        } else if (pidString.split(" ").length > 1) {
            throw new UtilException("More than one child process! ("
                                    + pidString + ")");
        } else {
            return pidString;
        }
    }

    /**
     * Converts a string representation of an integer in [0,60) to the
     * integer.
     *
     * @param numberString String representation of an integer in [0,60).
     * @return Represented number.
     * @throws UtilException If the input is not on the required form.
     */
    private static int timeNumber(final String numberString)
        throws UtilException {
        try {
            final int number = Integer.parseInt(numberString);
            if (0 <= number && number < 60) {
                return number;
            } else {
                throw new UtilException("Number is not in [0,60)! ("
                                        + number + ")");
            }
        } catch (final NumberFormatException nfe) {
            throw new UtilException("Failed to parse number!", nfe);
        }
    }

    /**
     * Converts a time string of the format [dd-]hh:mm:ss to a long
     * representing the number of elapsed seconds.
     *
     * @param etime Time represented in etime format.
     * @return Time in seconds since epoch.
     *
     * @throws UtilException If the input does not satisfy the etime format.
     */
    private static long etimeToLong(final String etime) throws UtilException {

        final String[] dayTime = etime.split("-");
        if (dayTime.length > 2) {
            throw new UtilException("The etime has more than two parts!");
        }
        int dhours = 0;
        int index = 0;
        if (dayTime.length > 1) {
            dhours = Integer.parseInt(dayTime[0]) * 24;
            if (dhours <= 0) {
                throw new UtilException("Number of days is non-positive!");
            }
            index++;
        }
        final String[] hhmmss = dayTime[index].split(":");
        if (hhmmss.length != 3) {
            throw new UtilException("The time does not have three parts!");
        }
        final int hours = timeNumber(hhmmss[0]);
        final int minutes = timeNumber(hhmmss[1]);
        final int seconds = timeNumber(hhmmss[2]);

        return (dhours + hours) * 60 + minutes * 60 + seconds;
    }

    /**
     * Converts a time in milliseconds from epoch to a human readable
     * string of the form yyyy-MM-dd:HH:mm:ss.
     *
     * @param milliseconds Time in milliseconds.
     * @return Time formatted as a string for the current timezone.
     */
    private static String formatTime(final long milliseconds) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);

        final String format = "yyyy-MM-dd:HH:mm:ss";
        final SimpleDateFormat sdf = new SimpleDateFormat(format);

        return sdf.format(calendar.getTime());
    }
}

