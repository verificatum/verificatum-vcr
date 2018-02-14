
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

package com.verificatum.eio;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.verificatum.crypto.RandomDevice;
import com.verificatum.ui.gen.GenUtil;
import com.verificatum.ui.opt.Opt;
import com.verificatum.ui.opt.OptException;
import com.verificatum.ui.opt.OptUtil;


/**
 * Command line tool for printing byte trees.
 *
 * @author Douglas Wikstrom
 */
public final class ByteTreeTool {

    /**
     * Avoid instantiation.
     */
    private ByteTreeTool() {
    }

    /**
     * Generates an option instance representing the various ways the
     * mix-net can be invoked.
     *
     * @param commandName Name of the command executed by the user to
     * invoke this protocol, i.e., the name of the shell script
     * wrapper.
     * @return Option instance representing how this tool can be
     * invoked.
     */
    protected static Opt opt(final String commandName) {

        final String defaultErrorString =
            "Invalid usage form, please use \"" + commandName
            + " -h\" for usage information!";

        final Opt opt = new Opt(commandName, defaultErrorString);

        opt.addParameter("file", "File containing byte tree.");

        opt.addOption("-h", "", "Print usage information.");
        opt.addOption("-e", "", "Print exception trace upon error.");
        opt.addOption("-wd", "dir",
                      "Directory for temporary files (default is "
                      + "a unique subdirectory of /tmp/com.verificatum). "
                      + "This directory is deleted on exit.");
        opt.addOption("-cerr", "",
                      "Print error messages as clean strings without any "
                      + "error prefix or newlines.");
        opt.addOption("-hex", "",
                      "Indicates that the input byte array is represented as "
                      + "a hexadecimal string, possibly with a prefix ending "
                      + "with \"::\".");

        opt.addOption("-version", "", "Print the package version.");

        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h###");

        opt.addUsageForm();
        opt.appendToUsageForm(1,
                              "#-hex,-e,-cerr,-wd##file");

        opt.addUsageForm();
        opt.appendToUsageForm(2, "-version###");


        final String s =
            "Reads byte tree data and prints it as a nested JSON array or "
            + "reads data and verifies that it is a valid byte tree. "
            + "\n\n"
            + "In both cases the source of the data can be a file or standard "
            + "input. The format of the input can be a binary representation "
            + "of a byte tree or a hexadecimal encoding thereof. The default "
            + "source of data is standard input.";

        opt.appendDescription(s);

        return opt;
    }

    /**
     * Parses the command line.
     *
     * @param args Command line arguments.
     * @return Parsed command line arguments.
     * @throws EIOException If the command line arguments
     * can not be parsed.
     */
    private static Opt parseCommandLine(final String[] args)
        throws EIOException {

        if (args.length == 0) {
            throw new EIOException("Missing command name!");
        }

        final String commandName = args[0];
        final Opt opt = opt(commandName);

        final String[] newargs = Arrays.copyOfRange(args, 1, args.length);

        try {

            opt.parse(newargs);
            return opt;

        } catch (final OptException oe) {
            throw new EIOException(oe.getMessage(), oe);
        }
    }

    /**
     * Allows a user to invoke this protocol from the command line.
     *
     * @param args Command line arguments.
     */
    public static void main(final String[] args) {

        // We must treat the flags -e and -cerr in an ad hoc way to
        // make sure that they work even when parsing the command line
        // fails.
        final boolean cerrFlag = GenUtil.specialFlag("-cerr", args);
        final boolean eFlag = GenUtil.specialFlag("-e", args);

        try {

            final Opt opt = parseCommandLine(args);

            // If help or version flags are given we act accordingly.
            OptUtil.processHelpAndVersion(opt);

            TempFile.init(opt.getStringValue("-wd", ""), new RandomDevice());

            File file = null;
            if (opt.valueIsGiven("file")) {
                file = new File(opt.getStringValue("file"));
            } else {
                file = TempFile.getFile();
                try {
                    ExtIO.copy(System.in, file);
                } catch (final IOException ioe) {
                    throw new EIOException(ioe.getMessage(), ioe);
                }
            }

            // If the input is hexadecimally coded, then we decode it
            // first.
            if (opt.getBooleanValue("-hex")) {
                final File tmpFile = TempFile.getFile();
                try {
                    Hex.toByteArray(file, tmpFile);
                } catch (final IOException ioe) {
                    throw new EIOException(ioe.getMessage(), ioe);
                }
                file = tmpFile;
            }

            // Print the byte tree.
            final ByteTreeBasic btb = new ByteTreeF(file);
            final DataOutputStream dos = new DataOutputStream(System.out);
            btb.prettyWriteTo(dos);
            ExtIO.strictClose(dos);

        // PMD does not understand this.
        } catch (final EIOException pfe) { // NOPMD

            GenUtil.processErrors(pfe, cerrFlag, eFlag);

        } finally {
            TempFile.free();
        }
    }
}
