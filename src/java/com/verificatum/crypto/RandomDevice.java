
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

package com.verificatum.crypto;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.ExtIO;
import com.verificatum.ui.Util;
import com.verificatum.util.Functions;


/**
 * Wrapper class for random sources implemented as random devices in
 * the operating system. This could be a physical source of true
 * randomness, or some heuristically secure source such as
 * <code>/dev/urandom</code> on some Un*xes. It is assumed that the
 * underlying source contains sufficiently many bytes.
 *
 * <p>
 *
 * <b>WARNING!</b> It is prudent to <b>never</b> use a normal file as
 * a random device, due to the risk of reuse.
 *
 * <p>
 *
 * <b>WARNING!</b> Do not create too many instances of this class,
 * since there is no way to release the underlying file descriptor
 * with certainty.
 *
 * @author Douglas Wikstrom
 */
public final class RandomDevice extends RandomSource {

    /**
     * Random device from where we read random bits.
     */
    private File file;

    /**
     * Stream from the random device.
     */
    private BufferedInputStream bis;

    /**
     * Constructs an instance corresponding to the input.
     *
     * @param btr Representation of an instance.
     * @return Random device represented by the input.
     * @throws CryptoFormatException If the input does not represent
     *  an instance.
     */
    public static RandomDevice newInstance(final ByteTreeReader btr)
        throws CryptoFormatException {
        return new RandomDevice(btr);
    }

    /**
     * Turns a file into an input stream.
     *
     * @param file File representing the random device.
     */
    private void setupDevice(final File file) {
        synchronized (this) {
            try {
                bis = new BufferedInputStream(new FileInputStream(file));
            } catch (final FileNotFoundException fnfe) {
                throw new CryptoError("File not found!", fnfe);
            } catch (final SecurityException se) {
                throw new CryptoError("Not allowed to open device!", se);
            }
        }
    }

    /**
     * Returns an instance that extracts its output from
     * <code>/dev/urandom</code>.
     */
    public RandomDevice() {
        this(new File("/dev/urandom"));
    }

    /**
     * Constructs an instance reading from the given random device.
     *
     * @param file Path to a random device.
     */
    public RandomDevice(final File file) {
        this.file = file;
        setupDevice(file);
    }

    /**
     * Constructs an instance reading from a device described in the
     * input.
     *
     * @param btr Representation of a random device.
     * @throws CryptoFormatException If the input does not represent
     *  an instance.
     */
    public RandomDevice(final ByteTreeReader btr) throws CryptoFormatException {
        try {
            final String path = btr.readString();
            this.file = new File(path);
            setupDevice(file);
        } catch (final EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        } catch (final CryptoError ce) {
            throw new CryptoFormatException("Unable to create device!", ce);
        }
    }

    // Documented in com.verificatum.crypto.RandomSource.

    @Override
    public void getBytes(final byte[] array) {
        synchronized (this) {
            try {
                int index = 0;
                final int len = array.length;

                while (index < len) {
                    index += bis.read(array, index, len - index);
                }
            } catch (final IOException ioe) {
                throw new CryptoError("Unable to read from random device!",
                                      ioe);
            }
        }
    }

    // Documented in Marshalizable.java

    @Override
    public ByteTree toByteTree() {
        final byte[] bytes = ExtIO.getBytes(file.getPath());
        return new ByteTree(bytes);
    }

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose) + "(" + this.file.toString() + ")";
    }

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RandomDevice)) {
            return false;
        }
        return file.equals(((RandomDevice) obj).file);
    }
}
