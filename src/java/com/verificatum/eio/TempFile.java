
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.verificatum.crypto.RandomSource;


/**
 * Manages temporary files. The reason that this is not implemented
 * using {@link File#createTempFile(String,String, File)} is that we
 * need to control where the files are stored.
 *
 * @author Douglas Wikstrom
 */
public final class TempFile {

    /**
     * Avoid accidental instantiation.
     */
    private TempFile() { }

    /**
     * Directory used to store temporary files.
     */
    private static File storageDir;

    /**
     * Counter used to name files unambigously.
     */
    private static int fileNameCounter;

    /**
     * Indicates if debugging is enabled.
     */
    private static boolean debug = false;

    /**
     * Enable debugging of the temporary file system.
     */
    public static void debug() {
        debug = true;
    }

    /**
     * Initializes the storage directory. Any existing files in the
     * storage directory may be overwritten. It is the responsibility
     * of the user to not call this repeatedly without calling {@link
     * #free()} first.
     *
     * @param theStorageDir Storage directory.
     */
    public static void init(final File theStorageDir) {
        storageDir = theStorageDir;
        fileNameCounter = 0;
    }

    /**
     * Setup a working directory.
     *
     * @param filename Name of working directory in the form of an
     * absolute path, a path relative to /tmp/com.verificatum, or the
     * empty string indicating that a randomly named subdirectory of
     * /tmp/com.verificatum must be generated.
     * @param randomSource Source of randomness.
     * @throws EIOException If the directory can not be created.
     */
    public static void init(final String filename,
                            final RandomSource randomSource)
        throws EIOException {

        File wd;

        final File defaultRoot = new File("/tmp/com.verificatum/");

        if ("".equals(filename)) {
            final byte[] subdirbytes = randomSource.getBytes(10);
            wd = new File(defaultRoot, Hex.toHexString(subdirbytes));
        } else if (filename.charAt(0) == '/') {
            wd = new File(filename);
        } else {
            wd = new File(defaultRoot, filename);
        }

        try {
            ExtIO.mkdirs(wd);
        } catch (final EIOException eioe) {
            throw new EIOException("Unable to create working directory!", eioe);
        }
        TempFile.init(wd);
    }

    /**
     * Dump an exception file that allows tracing the location of the
     * allocation of a temporary file.
     *
     * @param fileName File name of temporary file.
     * @param e Exception tracing the location of the allocation of
     * the temporary file.
     */
    private static void dumpException(final String fileName,
                                      final Exception e) {
        try {

            final String excFileName = fileName + "_exc";
            final File fileExc = new File(storageDir, excFileName);
            final PrintWriter pw = new PrintWriter(fileExc, "UTF-8");

            final String header = String.format("%nFile: %s", fileName);
            pw.println(header);

            final String delim =
                "############################################################";
            pw.println(delim);

            e.printStackTrace(pw);
            pw.close();

        } catch (final UnsupportedEncodingException uee) {
            throw new EIOError("Failed to give valid encoding!", uee);
        } catch (final FileNotFoundException fnfe) {
            throw new EIOError("Failed to set up exception file!", fnfe);
        }
    }

    /**
     * Returns a uniquely named temporary file.
     *
     * @return Uniquely named temporary file.
     */
    public static File getFile() {

        synchronized (storageDir) {

            final String fileName = String.format("%08d", fileNameCounter);
            final File file = new File(storageDir, fileName);

            // This can be used to track down where temporary files were
            // created if they are not deleted.

            if (debug) {
                try {

                    // We deliberately throw this exception to trace
                    // the location in the code where the temporary
                    // file was allocated.
                    throw new EIOException("Debug exception!");

                } catch (final EIOException eioe) {

                    dumpException(fileName, eioe);

                }
            }

            fileNameCounter++;

            return file;
        }
    }

    /**
     * Delete temporary file.
     *
     * @param file Temporary file to be deleted.
     */
    public static void delete(final File file) {
        if (!ExtIO.delete(file)) {
            final String e = "Unable to delete temporary file! (" + file + ")";
            throw new EIOError(e);
        }
        if (debug) {
            final File parent = file.getParentFile();
            final String fileName = file.getName();
            final File fileExc = new File(parent, fileName + "_exc");
            if (!ExtIO.delete(fileExc)) {
                final String e =
                    "Unable to delete exception file! (" + fileExc + ")";
                throw new EIOError(e);
            }
        }
    }

    /**
     * Removes all files in the storage directory.
     */
    public static void free() {

        // If we are debugging we need to keep all the temporary files
        // that were not deleted separately.
        if (!debug
            && storageDir != null
            && !ExtIO.delete(storageDir)) {

            throw new EIOError("Unable to delete storage directory!");
        }
    }
}
