
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

import java.io.Closeable;
import java.io.UnsupportedEncodingException;

/**
 * Abstract class of a reader of {@link ByteTree} instances. Reading
 * corresponds to a depth-first traversal of the tree, and such a
 * traversal is enforced. Public methods are provided for accessing
 * children and data of a byte tree. If such a method throws a
 * {@link EIOException}, then the underlying resources are deallocated
 * and further processing is illegal (undefined behavior). This poses
 * no restriction, since an application can query an instance to
 * traverse the tree without any exceptions.
 *
 * @author Douglas Wikstrom
 */
public abstract class ByteTreeReader implements Closeable {

    /**
     * Number of children/bytes remaining to be read.
     */
    protected int remaining;

    /**
     * Instance that spawned this one (null if this is the root).
     */
    protected ByteTreeReader parent;

    /**
     * Indicates that there is an active child, i.e., there is more to
     * read in the child that was most recently returned, so it is
     * illegal to ask for another child.
     */
    protected boolean activeChild;

    /**
     * Creates an uninitialized instance.
     */
    protected ByteTreeReader() {
        super();
    }

    /**
     * Creates an instance with the given parent and number of
     * children/bytes to read.
     *
     * @param parent Parent of this instance.
     * @param remaining Number of children/bytes remaining to be read.
     */
    protected ByteTreeReader(final ByteTreeReader parent, final int remaining) {
        this.parent = parent;
        this.remaining = remaining;
        this.activeChild = false;
    }

    /**
     * Returns true or false depending on if this reader points to a
     * leaf or not.
     *
     * @return true or false depending on if this reader points to a
     * leaf or not.
     */
    public abstract boolean isLeaf();

    /**
     * Returns a reader of the next child to read. Subclasses
     * implementing this method may assume that there is a next child
     * to read and that previous children of this instance have
     * already been processed fully.
     *
     * @return Next child to read.
     */
    protected abstract ByteTreeReader getNextChildInner();

    /**
     * Reads data into the given array. Subclasses implementing this
     * method may assume that the requested number of bytes is less or
     * equal to the number of remaining bytes and that the result fits
     * in the given array, and that all previous children have been
     * processed fully.
     *
     * @param destination Destination array of data.
     * @param offset Where to start writing in destination array.
     * @param length Number of bytes to read.
     */
    protected abstract void readInner(byte[] destination,
                                      int offset,
                                      int length);

    /**
     * Deallocates any resources allocated by this instance, e.g.,
     * opened files. This must be called if reading is interrupted due
     * to bad formatting, and otherwise it is called automatically
     * upon the end of reading. Spurious calls to close must be
     * ignored.
     */
    public abstract void close();

    // Implemented in terms of the above.

    /**
     * Returns a reader of the next child to read.
     *
     * @return Next child to read.
     *
     * @throws EIOException If there are no more children to read or
     *  if data remains to be read in the child preceeding
     *  the requested one in a depth-first traversal of the
     *  underlying byte tree.
     */
    public ByteTreeReader getNextChild() throws EIOException {

        // These problems may occur if reading a maliciously
        // constructed byte tree. This should not give a fatal error.
        if (isLeaf()) {
            close();
            throw new EIOException("Requesting child from leaf!");
        }
        if (remaining == 0) {
            close();
            throw new EIOException("There are no more children!");
        }

        // This problem can only occur due to bad programming.
        if (activeChild) {
            throw new EIOError("Violating depth-first traversal!");
        }

        activeChild = true;
        final ByteTreeReader res = getNextChildInner();

        remaining--;
        return res;
    }

    /**
     * Returns a reader of the next child to read.
     *
     * @return Next child to read.
     *
     * @throws EIOError If there are no more children to read or
     *  if data remains to be read in the child preceeding
     *  the requested one in a depth-first traversal of the
     *  underlying byte tree.
     */
    public ByteTreeReader unsafeGetNextChild() throws EIOError {
        try {
            return getNextChild();
        } catch (final EIOException eioe) {
            throw new EIOError("Failed to read child!", eioe);
        }
    }

    /**
     * Skips one child when reading.
     *
     * @throws EIOException If there is no file to be skipped.
     */
    public void skipChild() throws EIOException {
        final ByteTreeReader btr = getNextChild();
        if (btr.isLeaf()) {
            btr.read();
        } else {
            while (btr.getRemaining() > 0) {
                btr.skipChild();
            }
        }
    }

    /**
     * Skips a number of children when reading.
     *
     * @param n Number of children to skip.
     *
     * @throws EIOException If there are not n files that can be skipped.
     */
    public void skipChildren(final int n) throws EIOException {
        for (int i = 0; i < n; i++) {
            skipChild();
        }
    }

    /**
     * Skips a number of children when reading and throws and error if
     * it fails.
     *
     * @param n Number of children to skip.
     *
     * @throws EIOError If there are not n files that can be skipped.
     */
    public void unsafeSkipChildren(final int n) throws EIOError {
        try {
            skipChildren(n);
        } catch (final EIOException eioe) {
            throw new EIOError("Failed to skip children!", eioe);
        }
    }

    /**
     * Reads data into the given array.
     *
     * @param destination Destination array of data.
     * @param offset Where to start writing in destination array.
     * @param length Number of bytes to read.
     * @return Number of bytes written.
     *
     * @throws EIOException If there is no data, or if there is not
     * enough data, to read.
     */
    // PMD_ANNOTATION @SuppressWarnings("PMD.CyclomaticComplexity")
    public int read(final byte[] destination,
                    final int offset,
                    final int length)
        throws EIOException {

        if (!isLeaf()) {
            throw new EIOException("Attempting to read data from non-child!");
        }

        // This problem occurs if the byte tree is maliciously
        // constructed to have too few bytes. This should not give a
        // fatal error.
        if (length > remaining) {
            close();
            throw new EIOException("Requesting too many bytes!");
        }

        // These problems can occur due to programming errors.
        if (offset < 0 || length < 0) {
            throw new EIOError("Negative offset or length!");
        }
        if (offset + length > destination.length) {
            throw new EIOError("Bytes do not fit in destination array!");
        }

        try {
            readInner(destination, offset, length);
        } catch (final IndexOutOfBoundsException ioobe) {
            // This is impossible for a proper byte tree.
            throw new EIOException("This should be impossible!", ioobe);
        }
        remaining -= length;

        if (remaining == 0) {

            // If there is nothing more to read from this instance,
            // then we move up the tree until we find something that
            // has not been processed fully, or end up at the root.
            ByteTreeReader btr = parent;
            while (btr != null && btr.remaining == 0) {

                // We close children that should never be visited
                // again to the garbage collect.
                final ByteTreeReader tmpbtr = btr;
                btr = btr.parent;
                tmpbtr.close();
            }
            if (btr != null) {

                // If there is more to process, then we signal this.
                btr.activeChild = false;
            }
        }
        return length;
    }

    /**
     * Returns the number of children/bytes remaining to be read.
     *
     * @return Number of children/bytes remaining to be read.
     */
    public int getRemaining() {
        return remaining;
    }

    /**
     * Reads data into the given array.
     *
     * @param destination Destination array of data.
     * @return Number of written bytes.
     *
     * @throws EIOException If there is no data, or if there is not
     * enough data, to read.
     */
    public int read(final byte[] destination) throws EIOException {
        return read(destination, 0, destination.length);
    }

    /**
     * Reads and returns a given number of bytes.
     *
     * @param length Number of bytes to read.
     * @return Read bytes.
     *
     * @throws EIOException If there is no data, or if there is not
     * enough data, to read.
     */
    public byte[] read(final int length) throws EIOException {
        final byte[] tmp = new byte[length];
        read(tmp, 0, length);
        return tmp;
    }

    /**
     * Reads and returns all remaining bytes.
     *
     * @return Read bytes.
     *
     * @throws EIOException If there is no data to read.
     */
    public byte[] read() throws EIOException {
        final byte[] tmp = new byte[remaining];
        read(tmp, 0, remaining);
        return tmp;
    }

    /**
     * Reads and returns the next byte tree.
     *
     * @param btr Source of byte trees.
     * @return Read byte tree.
     *
     * @throws EIOException If there is no data to read.
     */
    private static ByteTree readByteTree(final ByteTreeReader btr)
        throws EIOException {

        if (btr.isLeaf()) {

            return new ByteTree(btr.read());

        } else {

            final ByteTree[] res = new ByteTree[btr.getRemaining()];
            for (int i = 0; i < res.length; i++) {
                res[i] = readByteTree(btr.getNextChild());
            }
            return new ByteTree(res);
        }
    }

    /**
     * Reads and returns the next byte tree.
     *
     * @return Read byte tree.
     *
     * @throws EIOException If there is no data to read.
     */
    public ByteTree readByteTree() throws EIOException {
        return readByteTree(this);
    }

    /**
     * Read four bytes and return them as an int.
     *
     * @return Integer read.
     *
     * @throws EIOException If there is no integer to read, i.e., if
     * there are not 4 bytes to read.
     */
    public int readInt() throws EIOException {
        return ExtIO.readInt(read(4), 0);
    }

    /**
     * Read byte[] return it as an int[].
     *
     * @param size Number of integers to read.
     * @return Array of integers
     *
     * @throws EIOException If there are not enough bytes to read.
     */
    public int[] readInts(final int size) throws EIOException {
        final int[] res = new int[size];
        ExtIO.readInts(res, 0, read(4 * size), 0, size);
        return res;
    }

    /**
     * Reads a boolean value from this reader.
     *
     * @return Read boolean value.
     *
     * @throws EIOException If there is no boolean value to be read.
     */
    public boolean readBoolean() throws EIOException {
        final byte[] res = read(1);
        if (res[0] == 0) {
            return false;
        } else if (res[0] == 1) {
            return true;
        }
        throw new EIOException("Not a boolean value!");
    }

    /**
     * Reads a <code>boolean[]</code> value from this reader.
     *
     * @param size Number of booleans to read.
     * @return Array of boolean.
     *
     * @throws EIOException If there are not size boolean values to be
     * read.
     */
    public boolean[] readBooleans(final int size) throws EIOException {
        final boolean[] res = new boolean[size];
        final byte[] tmp = read(size);
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] == 1) {
                res[i] = true;
            } else if (tmp[i] == 0) {
                res[i] = false;
            } else {
                throw new EIOException("Not an array of booleans!");
            }
        }
        return res;
    }

    /**
     * Reads at most the given number of bytes and interprets them as
     * a UTF-8 encoded string.
     *
     * @param size Number of characters read.
     * @return String representing the read data.
     *
     * @throws EIOException If there is not a string of the given size
     * to be read.
     */
    public String readString(final int size) throws EIOException {
        try {

            return new String(read(size), "UTF-8");

        } catch (final UnsupportedEncodingException uee) {

            // This should never happen, since UTF-8 is a valid
            // encoding.
            throw new EIOError("This is a bug!", uee);
        }
    }

    /**
     * Reads all remaining bytes and interprets them as a UTF-8
     * encoded string.
     *
     * @return String representing the read data.
     *
     * @throws EIOException If there is no string to be read.
     */
    public String readString() throws EIOException {
        return readString(remaining);
    }
}
