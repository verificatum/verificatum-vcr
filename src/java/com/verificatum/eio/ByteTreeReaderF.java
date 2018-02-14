
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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A reader of {@link ByteTreeF} instances.
 *
 * @author Douglas Wikstrom
 */
public final class ByteTreeReaderF extends ByteTreeReader {

    /**
     * Size of buffer for underlying {@link BufferedInputStream}.
     */
    public static final int BUFFER_SIZE = 16384;

    /**
     * Source of data.
     */
    DataInputStream dis;

    /**
     * Indicates if this instance points to a leaf or not.
     */
    boolean isLeaf;

    /**
     * Indicates that this instance opened the underlying stream.
     */
    boolean opener;

    /**
     * Creates an instance with the given parent and underlying byte
     * tree.
     *
     * @param parent Instance that spawned this one.
     * @param bt Underlying byte tree.
     */
    public ByteTreeReaderF(final ByteTreeReader parent, final ByteTreeF bt) {
        try {

            final FileInputStream fis = new FileInputStream(bt.file);
            final BufferedInputStream bis =
                new BufferedInputStream(fis, BUFFER_SIZE);
            this.dis = new DataInputStream(bis);
            final ByteTreeReaderF btr = new ByteTreeReaderF(parent, dis);

            this.parent = parent;
            this.isLeaf = btr.isLeaf;
            this.remaining = btr.remaining;
            this.opener = true;

        } catch (final FileNotFoundException fnfe) {
            throw new EIOError("File not found!", fnfe);
        }
    }

    /**
     * Creates a reader of the byte tree on the given file.
     *
     * @param file Representation of byte tree.
     */
    public ByteTreeReaderF(final File file) {
        final ByteTreeReaderF tmp =
            (ByteTreeReaderF) (new ByteTreeF(file)).getByteTreeReader();
        this.dis = tmp.dis;
        this.parent = tmp.parent;
        this.isLeaf = tmp.isLeaf;
        this.remaining = tmp.remaining;
        this.opener = tmp.opener;
    }

    /**
     * Creates an instance with the given parent and reading from the
     * given data source.
     *
     * @param parent Instance that spawned this one.
     * @param dis Source of data.
     */
    protected ByteTreeReaderF(final ByteTreeReader parent,
                              final DataInputStream dis) {
        try {

            this.parent = parent;
            this.dis = dis;
            this.isLeaf = dis.readByte() == ByteTreeBasic.LEAF;

            this.remaining = dis.readInt();

            this.opener = false;

        } catch (final IOException ioe) {
            throw new EIOError("Could not read!", ioe);
        }
    }

    // Documented in ByteTreeReader.java.

    @Override
    public boolean isLeaf() {
        return isLeaf;
    }

    @Override
    protected ByteTreeReader getNextChildInner() {
        return new ByteTreeReaderF(this, dis);
    }

    @Override
    protected void readInner(final byte[] destination,
                             final int offset,
                             final int length) {
        try {

            int currentOffset = offset;
            final int end = currentOffset + length;
            while (currentOffset < end) {
                currentOffset +=
                    dis.read(destination, currentOffset, end - currentOffset);
            }
        } catch (final IOException ioe) {
            throw new EIOError("Unable to read!", ioe);
        }
    }

    @Override
    public void close() {
        if (opener) {
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (final IOException ioe) {
                throw new EIOError("Unable to close stream!", ioe);
            }
        }
    }
}
