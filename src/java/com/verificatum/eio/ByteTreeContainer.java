
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

import java.io.DataOutputStream;
import java.io.IOException;

import com.verificatum.crypto.Hashdigest;


/**
 * This class is part of an implementation of a byte oriented
 * intermediate data format. Documentation is provided in
 * {@link ByteTreeBasic}.
 *
 * @author Douglas Wikstrom
 */
public final class ByteTreeContainer extends ByteTreeBasic {

    /**
     * Stores the data of this container.
     */
    ByteTreeBasic[] children;

    /**
     * Creates an instance storing the given contents as children.
     * This does not copy the input array.
     *
     * @param children Children of this instance.
     */
    public ByteTreeContainer(final ByteTreeBasic... children) {
        this.children = children;
    }

    /**
     * Creates an instance storing the given contents as children.
     *
     * @param convChildren Instances that can be converted to byte
     * trees.
     */
    public ByteTreeContainer(final ByteTreeConvertible... convChildren) {
        children = new ByteTreeBasic[convChildren.length];
        for (int i = 0; i < children.length; i++) {
            children[i] = convChildren[i].toByteTree();
        }
    }

    // Documented in ByteTreeBasic.java.

    @Override
    public ByteTreeReader getByteTreeReader() {
        return new ByteTreeReaderC(null, this);
    }

    @Override
    public void update(final Hashdigest digest) {
        final byte[] prefix = new byte[5];
        prefix[0] = NODE;
        ExtIO.writeInt(prefix, 1, children.length);
        digest.update(prefix);

        for (int i = 0; i < children.length; i++) {
            children[i].update(digest);
        }
    }

    @Override
    public void writeTo(final DataOutputStream dos) throws EIOException {
        try {
            dos.writeByte(NODE);
            dos.writeInt(children.length);

            for (int i = 0; i < children.length; i++) {
                children[i].writeTo(dos);
            }
        } catch (final IOException ioe) {
            throw new EIOException("Can not write byte tree to stream!", ioe);
        }
    }

    @Override
    public long totalByteSize() {
        long total = 5;
        for (int i = 0; i < children.length; i++) {
            total += children[i].totalByteSize();
        }
        return total;
    }

    @Override
    public int toByteArray(final byte[] result, final int offset) {

        int tmpOffset = offset;

        result[tmpOffset++] = NODE;
        ExtIO.writeInt(result, tmpOffset, children.length);
        tmpOffset += 4;

        for (int i = 0; i < children.length; i++) {
            tmpOffset += children[i].toByteArray(result, tmpOffset);
        }
        return tmpOffset - offset;
    }
}
