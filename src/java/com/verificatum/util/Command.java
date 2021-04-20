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
                    Thread.currentThread().interrupt();
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
