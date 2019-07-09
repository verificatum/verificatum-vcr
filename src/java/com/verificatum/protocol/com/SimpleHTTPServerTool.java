
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

package com.verificatum.protocol.com;

import java.io.File;
import java.util.Arrays;

import com.verificatum.ui.opt.Opt;
import com.verificatum.ui.opt.OptException;
import com.verificatum.vcr.VCR;


/**
 * Command line interface to {@link SimpleHTTPServerTool}.
 *
 * @author Douglas Wikstrom
 */
public final class SimpleHTTPServerTool {

    /**
     * Method to prevent instantiation of this class.
     */
    private SimpleHTTPServerTool() {
    }

    /**
     * Generates an option instance representing the various ways the
     * HTTP server can be invoked.
     *
     * @param commandName Name of the command executed by the user to
     * invoke this protocol, i.e., the name of the shell script
     * wrapper.
     * @return Option instance representing how this protocol can be
     * invoked.
     */
    protected static Opt opt(final String commandName) {

        final String defaultErrorString =
            "Invalid usage form, please use \"" + commandName
            + " -h\" for usage information!";

        final Opt opt = new Opt(commandName, defaultErrorString);

        opt.addParameter("directory", "Root directory of server.");
        opt.addParameter("hostname", "Hostname of server.");
        opt.addParameter("port", "Port number.");
        opt.addParameter("backlog", "Backlog number.");

        opt.addOption("-h", "", "Print usage information.");
        opt.addOption("-version", "", "Print the package version.");

        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h###");

        opt.addUsageForm();
        opt.appendToUsageForm(1, "##directory,hostname,port,backlog#");

        opt.addUsageForm();
        opt.appendToUsageForm(2, "-version###");


        final String s = "Runs a simple HTTP server.";

        opt.appendDescription(s);

        return opt;
    }

    /**
     * Allows executing this server as a standalone application. No
     * verifications of command line arguments are performed.
     *
     * <p>
     *
     * <b>WARNING!</b> This is only meant to be used for debugging.
     *
     * @param args Command line arguments.
     */
    public static void main(final String[] args) {

        if (args.length < 1) {
            System.err.println("Failed to pass the name of the wrapper!");
            System.exit(1);
        }

        final String commandName = args[0];
        final Opt opt = opt(commandName);

        try {

            opt.parse(Arrays.copyOfRange(args, 1, args.length));

            // Output usage info.
            if (opt.getBooleanValue("-h")) {

                System.out.println(opt.usage());
                return;
            }

            // Output version.
            if (opt.getBooleanValue("-version")) {

                System.out.println(VCR.version());
                return;
            }

            final File directory = new File(opt.getStringValue("directory"));

            final SimpleHTTPServer shttps =
                new SimpleHTTPServer(directory,
                                     opt.getStringValue("hostname"),
                                     opt.getIntValue("port"),
                                     opt.getIntValue("backlog"));
            shttps.start();

        } catch (final OptException oe) {
            System.err.println(oe.getMessage());
        }
    }
}
