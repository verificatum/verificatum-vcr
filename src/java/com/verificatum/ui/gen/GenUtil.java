
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
