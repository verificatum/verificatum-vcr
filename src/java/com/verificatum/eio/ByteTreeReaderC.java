
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

/**
 * A reader of {@link ByteTreeContainer} instances.
 *
 * @author Douglas Wikstrom
 */
public final class ByteTreeReaderC extends ByteTreeReader {

    /**
     * Byte tree from which this instance reads.
     */
    ByteTreeContainer btc;

    /**
     * Position of reader within the underlying byte tree.
     */
    int index;

    /**
     * Creates an instance that reads from the given byte tree and
     * with the given parent.
     *
     * @param parent Instance that spawned this one.
     * @param btc Byte tree from which this instance reads.
     */
    protected ByteTreeReaderC(final ByteTreeReader parent,
                              final ByteTreeContainer btc) {
        super(parent, btc.children.length);
        this.btc = btc;
        this.index = 0;
    }

    // Documented in ByteTreeReader.java

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    protected ByteTreeReader getNextChildInner() {
        final ByteTreeReader btr = btc.children[index++].getByteTreeReader();
        btr.parent = this;
        return btr;
    }

    @Override
    protected void readInner(final byte[] destination,
                             final int offset,
                             final int length) {
        throw new EIOError("A ByteTreeContainer is always a node!");
    }

    @Override
    public void close() {
        btc = null;
    }
}
