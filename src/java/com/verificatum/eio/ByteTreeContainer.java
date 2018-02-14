
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
