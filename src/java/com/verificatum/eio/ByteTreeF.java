
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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.verificatum.crypto.Hashdigest;


/**
 * This class is part of an implementation of a byte oriented
 * intermediate data format. Documentation is provided in
 * {@link ByteTreeBasic}.
 *
 * @author Douglas Wikstrom
 */
public final class ByteTreeF extends ByteTreeBasic {

    /**
     * Number of bytes in buffer used for computing digests.
     */
    static final int DIGEST_BUFFER_SIZE = 4096;

    /**
     * Number of bytes in buffer used for converting data on file to a
     * byte[].
     */
    static final int BUFFER_SIZE = 4096;

    /**
     * File holding the data of this instance.
     */
    public final File file;

    /**
     * Constructs an instance with the given data. Note that the data
     * on file is <em>not</em> copied. It is the responsibility of the
     * programmer to make sure that the underlying file remains intact
     * during the life time of this instance. It is also the
     * responsibility of the programmer to remove the underlying file
     * when this is no longer needed.
     *
     * @param file File containing a byte array representation of a
     * byte tree.
     */
    public ByteTreeF(final File file) {
        this.file = file;
    }

    // Documented in ByteTreeBasic.java.

    @Override
    public ByteTreeReader getByteTreeReader() {
        return new ByteTreeReaderF(null, this);
    }

    @Override
    public void update(final Hashdigest digest) {
        FileInputStream fis = null;

        try {

            fis = new FileInputStream(file);

            final byte[] buf = new byte[DIGEST_BUFFER_SIZE];

            int len = fis.read(buf);
            while (len >= 0) {
                digest.update(buf, 0, len);
                len = fis.read(buf);
            }

        } catch (final IOException ioe) {
            throw new EIOError("Internal error!", ioe);
        } finally {
            ExtIO.strictClose(fis);
        }
    }

    /*
     * Overrides method in ByteTreeBasic.java
     */
    @Override
    public void writeTo(final DataOutputStream dos) throws EIOException {
        try {

            ExtIO.copy(file, dos);

        } catch (final FileNotFoundException fnfe) {
            throw new EIOException("File not found!", fnfe);
        } catch (final SecurityException se) {
            throw new EIOException("Not allowed to open file!", se);
        } catch (final IOException ioe) {
            throw new EIOException("Unable to write file!", ioe);
        }
    }

    @Override
    public void writeTo(final File file) throws EIOException {
        try {
            ExtIO.copyFile(this.file, file);
        } catch (final IOException ioe) {
            throw new EIOException("Can not write file!", ioe);
        }
    }

    @Override
    public long totalByteSize() {
        return file.length();
    }

    @Override
    public int toByteArray(final byte[] result, final int offset) {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);

            final byte[] buf = new byte[BUFFER_SIZE];

            int tmpOffset = offset;
            int len = fis.read(buf);
            while (len >= 0) {
                System.arraycopy(buf, 0, result, tmpOffset, len);
                tmpOffset += len;
                len = fis.read(buf);
            }

            return tmpOffset - offset;

        } catch (final IOException ioe) {
            throw new EIOError("Unable to convert to byte[]!", ioe);
        } finally {
            ExtIO.strictClose(fis);
        }
    }

    // ###############################################################

    /**
     * Returns true or false depending on if the contents of the given
     * file is a valid byte tree or not. This provides the first
     * shield against malformed inputs.
     *
     * @param file File to verify.
     * @param maximalRecursiveDepth Maximal recursion depth of the
     * given byte tree.
     * @return true or false depending on if the contents of the given
     *         file is a valid byte tree or not.
     */
    public static boolean verifyFormat(final File file,
                                       final int maximalRecursiveDepth) {

        DataInputStream dis = null;
        boolean res = true;
        try {

            final FileInputStream fis = new FileInputStream(file);
            final BufferedInputStream bis = new BufferedInputStream(fis);

            dis = new DataInputStream(bis);

            // Check that there is a properly constructed byte tree.
            verifyFormat(dis, maximalRecursiveDepth);

            // Check that there is nothing more.
            if (dis.read() != -1) {
                res = false;
            }

        } catch (final IOException ioe) {
            res = false;
        } catch (final EIOException eioe) {
            res = false;
        } finally {
            ExtIO.strictClose(dis);
        }
        return res;
    }

    /**
     * Returns true or false depending on if the contents of the given
     * stream is a valid byte tree or not. This provides the first
     * shield against malformed inputs.
     *
     * @param dis Stream to verify.
     * @param maximalRecursiveDepth Maximal recursion depth of the
     * given byte tree.
     * @throws IOException If the stream can not be read.
     * @throws EIOException If the format of the input file is
     *  incorrect.
     */
    public static void verifyFormat(final DataInputStream dis,
                                    final int maximalRecursiveDepth)
        throws IOException, EIOException {

        final int type = dis.readByte();
        final int length = dis.readInt();

        if (type == ByteTreeBasic.LEAF) {

            // If we are supposed to be a leaf, we attempt to skip a
            // suitable number of bytes.
            int len = length;
            while (len > 0) {
                len -= dis.skipBytes(len);
            }

        } else if (type == ByteTreeBasic.NODE) {

            // If a we are a node, then we attempt to call ourselves
            // recursively to read the correct number of children.
            if (maximalRecursiveDepth == 0) {
                throw new EIOException("Too deep recursion!");
            }

            for (int i = 0; i < length; i++) {

                verifyFormat(dis, maximalRecursiveDepth - 1);

            }
        } else {
            throw new EIOException("Malformed type!");
        }
    }

    /**
     * Free resources allocated by this instance.
     */
    public void free() {
        TempFile.delete(file);
    }
}
