
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

package com.verificatum.ui.gen;

import com.verificatum.eio.TempFile;

/**
 * Utility functions.
 *
 * @author Douglas Wikstrom
 */
public final class GenUtil {

    /**
     * Method to prevent instantiation of this class.
     */
    private GenUtil() {
    }

    /**
     * Returns true or false depending on if the flag occurs on the
     * command line.
     *
     * @param flag Flag to search for.
     * @param args Command line parameters.
     * @return True or false depending on if the flag occurs on the
     * command line.
     */
    public static boolean specialFlag(final String flag, final String[] args) {
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals(flag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Process errors and print suitable error parameters.
     *
     * @param throwable Throwable to process.
     * @param cerrFlag If true, then a clean error message string is
     * printed without any formatting.
     * @param eFlag If true, then the exception causing the execution
     * is printed as a stack trace.
     */
    public static void processErrors(final Throwable throwable,
                                     final boolean cerrFlag,
                                     final boolean eFlag) {
        if (cerrFlag) {
            System.err.print(throwable.getMessage());
        } else {
            final String e = "\n" + "ERROR: " + throwable.getMessage() + "\n";
            System.err.println(e);
        }

        if (eFlag) {
            throwable.printStackTrace();
        }

        TempFile.free();
        System.exit(1);
    }
}
