
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
