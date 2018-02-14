
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
 * A reader of {@link ByteTree} instances.
 *
 * @author Douglas Wikstrom
 */
public final class ByteTreeReaderBT extends ByteTreeReader {

    /**
     * Underlying byte tree.
     */
    ByteTree bt;

    /**
     * Creates a reader of the given byte tree with the given parent.
     *
     * @param bt Underlying byte tree.
     */
    public ByteTreeReaderBT(final ByteTree bt) {
        this(null, bt);
    }

    /**
     * Creates a reader of the given byte tree with the given parent.
     *
     * @param parent Instance that spawned this one.
     * @param bt Underlying byte tree.
     */
    public ByteTreeReaderBT(final ByteTreeReader parent, final ByteTree bt) {
        super(parent, getRemaining(bt));
        this.bt = bt;
    }

    /**
     * Returns the number of children/bytes in the given byte tree.
     *
     * @param bt Underlying byte tree.
     * @return Number of children/bytes
     */
    private static int getRemaining(final ByteTree bt) {
        if (bt.value == null) {
            return bt.children.length;
        } else {
            return bt.value.length;
        }
    }

    // Documented in ByteTreeReader.java

    @Override
    public boolean isLeaf() {
        return bt.value != null;
    }

    @Override
    protected ByteTreeReader getNextChildInner() {
        final ByteTree child = bt.children[bt.children.length - remaining];
        return new ByteTreeReaderBT(this, child);
    }

    @Override
    protected void readInner(final byte[] destination,
                             final int offset,
                             final int length) {
        System.arraycopy(bt.value, bt.value.length - remaining, destination,
                         offset, length);
    }

    @Override
    public void close() {

        // Potentially this helps the garbage collect.
        this.bt = null;
    }
}
