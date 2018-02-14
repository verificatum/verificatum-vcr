
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
