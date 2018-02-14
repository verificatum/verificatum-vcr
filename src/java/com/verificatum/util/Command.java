
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

import java.io.InputStream;
import java.io.IOException;

import com.verificatum.eio.ExtIO;

/**
 * Wrapper of command execution functionality in JVM.
 *
 * @author Douglas Wikstrom
 */
public final class Command {

    /**
     * Avoid accidental instantiation.
     */
    private Command() { }

    /**
     * Execute a command and return a pair of the exit code and
     * output as a string.
     *
     * @param command Command to be executed.
     * @return Pair consisting of the exit code and output of the
     * command.
     * @throws UtilException If the execution of the command fails.
     */
    public static Pair<Integer, String> execute(final String ... command)
        throws UtilException {
        try {
            Process process = null;
            InputStream is = null;
            try {
                final ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                process = pb.start();

                // final Runtime runTime = Runtime.getRuntime();
                // process = runTime.exec(command);
                is = process.getInputStream();

                final String output = ExtIO.readString(is);
                try {
                    process.waitFor();
                } catch (final InterruptedException ie) {
                    throw new UtilException("Native call was interrupted!", ie);
                }
                return new Pair<Integer, String>(process.exitValue(), output);
            } finally {
                ExtIO.strictClose(is);
            }
        } catch (final IOException ioe) {
            throw new UtilException("Failed to execute command!", ioe);
        }
    }

    /**
     * Execute a command and return the output.
     *
     * @param command Command to be executed.
     * @return Pair consisting of the exit code and output of the
     * command.
     * @throws UtilException If the execution of the command fails, or
     * if the exit code is non-zero.
     */
    public static String checkedExecute(final String ... command)
        throws UtilException {
        final Pair<Integer, String> output = execute(command);
        if (output.first == 0) {
            return output.second;
        } else {
            throw new UtilException("Non-zero exit code! ("
                                    + output.first + ")");
        }
    }
}
