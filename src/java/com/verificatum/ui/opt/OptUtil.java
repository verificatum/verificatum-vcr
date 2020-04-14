
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

package com.verificatum.ui.opt;

import com.verificatum.vcr.VCR;

/**
 * Utility functions for command line options.
 *
 * @author Douglas Wikstrom
 */
public final class OptUtil {

    /**
     * Method to prevent instantiation of this class.
     */
    private OptUtil() {
    }

    /**
     * Process help and version flags in a standard way and return the
     * description.
     *
     * @param opt Parsed command line parameters.
     * @return String describing usage information or version, or null
     * if no flag was given in the command line parameters.
     */
    public static String processHelpAndVersionString(final Opt opt) {

        // Output usage info.
        if (opt.getBooleanValue("-h")) {
            return opt.usage();
        }

        // Output version.
        if (opt.getBooleanValue("-version")) {
            return VCR.version();
        }
        return null;
    }

    /**
     * Process help and version flags in a standard way and print the
     * description.
     *
     * @param opt Parsed command line parameters.
     */
    public static void processHelpAndVersion(final Opt opt) {

        final String res = processHelpAndVersionString(opt);

        if (res != null) {
            System.out.println(res);
            System.exit(0);
        }
    }
}
