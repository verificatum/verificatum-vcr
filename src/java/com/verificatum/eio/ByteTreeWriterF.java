
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

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * A writer of byte tree instances to file. The functionality of this
 * class does not match that of {@link ByteTreeReader}. This class
 * should be used inside in classes operating on files internally.
 *
 * @author Douglas Wikstrom
 */
public final class ByteTreeWriterF implements Closeable {

    /**
     * Destination of instances.
     */
    DataOutputStream dos;

    /**
     * Creates an instance with the given number of children/bytes to
     * be written.
     *
     * @param remaining Number of children/bytes (supposedly)
     * remaining to be written.
     * @param file Destination of instances.
     *
     * @throws IOException If the output file can not be opened or not
     * written.
     */
    public ByteTreeWriterF(final int remaining, final File file)
        throws IOException {
        final FileOutputStream fos = new FileOutputStream(file);
        final BufferedOutputStream bos = new BufferedOutputStream(fos);
        this.dos = new DataOutputStream(bos);
        dos.writeByte(ByteTreeBasic.NODE);
        dos.writeInt(remaining);
    }

    /**
     * Creates an instance with the given number of children/bytes to
     * be written.
     *
     * @param remaining Number of children/bytes (supposedly)
     * remaining to be written.
     * @param file Destination of instances.
     * @return Byte tree writer.
     *
     * @throws EIOError If the output file can not be opened or not
     * written.
     */
    public static ByteTreeWriterF unsafeByteTreeWriterF(final int remaining,
                                                        final File file)
        throws EIOError {
        try {
            return new ByteTreeWriterF(remaining, file);
        } catch (final FileNotFoundException fnfe) {
            throw new EIOError("Unable to create writer!", fnfe);
        } catch (final IOException ioe) {
            throw new EIOError("Unable to create writer!", ioe);
        }
    }

    /**
     * Writes a byte tree to the underlying file.
     *
     * @param bt Byte tree to be written.
     */
    public void unsafeWrite(final ByteTreeBasic bt) {
        bt.unsafeWriteTo(dos);
    }

    /**
     * Writes a byte tree to the underlying file.
     *
     * @param bt Byte tree to be written.
     * @throws EIOException If writing fails.
     */
    public void write(final ByteTreeBasic bt) throws EIOException {
        bt.writeTo(dos);
    }

    /**
     * Writes byte-tree convertible object to the underlying file.
     *
     * @param btc Byte tree convertible objects to be written.
     * @throws EIOException If writing fails.
     */
    public void write(final ByteTreeConvertible... btc) throws EIOException {
        for (int i = 0; i < btc.length; i++) {
            write(btc[i].toByteTree());
        }
    }

    /**
     * Writes byte-tree convertible object to the underlying file.
     *
     * @param btc Byte tree convertible objects to be written.
     */
    public void unsafeWrite(final ByteTreeConvertible... btc) {
        try {
            write(btc);
        } catch (final EIOException eioe) {
            throw new EIOError("Unable to write!", eioe);
        }
    }

    /**
     * Closes the underlying file.
     */
    public void close() {
        ExtIO.strictClose(dos);
    }
}
