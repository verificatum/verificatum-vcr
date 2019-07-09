
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.verificatum.crypto.Hashdigest;


/**
 * This class is the base class of an intermediate byte oriented
 * format used for communication and storing data to file. Let us call
 * this format byte tree. In contrast to a raw byte array, a byte tree
 * has ordered tree structure. This simplifies parsing any one of the
 * numerous formats needed in other classes and allows performing
 * basic verification of an input. We use our own format and not the
 * {@link java.io.Serializable} class to avoid the overhead imposed by
 * this interface and to simplify writing a compatible implementation
 * in some other language.
 *
 * <p>
 *
 * A byte tree is defined as follows. A byte tree either holds data or
 * other byte trees. In the former case the data is simply stored as a
 * <code>byte[]</code>. In the latter case the instance holds an array
 * of byte trees, that may themselves either hold data or be byte
 * trees. A byte tree can be turned into an array of bytes using depth
 * first traversal. If a byte tree stores data, it is converted to a
 * byte array consisting of: a single byte (one) signalling that this
 * is a data-storing byte tree (a leaf), four bytes giving the number
 * of bytes of data (an int), and the actual data bytes. If a byte
 * tree stores other byte trees, then it is converted into a byte
 * array containing: a single byte (zero) saying that this is a
 * tree-storing byte tree (an inner node), four bytes storing the
 * number of byte trees (an int), and then recursively the byte arrays
 * of the child byte trees. Conversely, data on file can be recovered
 * from an array of bytes. A byte tree can of course be read/written
 * to/from a file without first explicitly converting it into/from an
 * array of bytes. This is needed to allow very large byte trees.
 *
 * <p>
 *
 * The implementation consists of the following classes.
 *
 * <ul>
 *
 * <li>ByteTreeBasic (this class): Provides basic writing
 * functionality and hashing functionality; partially in terms of
 * abstract methods.
 *
 * <li> {@link ByteTree}: Provides an in-memory implementation of a
 * handler of the format. This should be used to pack/unpack
 * reasonable amounts of data that fits comfortably in memory.
 *
 * <li> {@link ByteTreeF}: Provides a file based byte tree.
 *
 * <li> {@link ByteTreeContainer}: In protocols, the parties may need
 * to send structured data where some subtrees are fairly small and
 * thus most easily handled using <code>ByteTree</code> and others are
 * huge and handled by <code>ByteTreeLazy</code>. For the receiver
 * this is not a problem. He can read such data using
 * <code>ByteTreeF</code>. <code>ByteTreeContainer</code> provides the
 * needed functionality of the sender. Several
 * <code>ByteTreeBasic</code> instances can be combined. This
 * corresponds to structured data where some subtrees are stored on
 * file and some are in memory.
 *
 * <li> {@link ByteTreeReader}: Abstract class representing a reader of
 * byte trees
 *
 * <li> {@link ByteTreeReaderF}: Reader of {@link ByteTreeF}.
 *
 * <li> {@link ByteTreeReaderC}: Reader of {@link ByteTreeContainer}.
 *
 * <li> {@link ByteTreeReaderBT}: Reader of {@link ByteTree}.
 *
 * <li> {@link ByteTreeWriterF}: Writer of {@link ByteTree} instance.
 * This should only be used <em>inside</em> classes, e.g., in
 * {@link com.verificatum.arithm.LargeIntegerArrayF}.
 *
 * </ul>
 *
 * @author Douglas Wikstrom
 */
public abstract class ByteTreeBasic {

    /**
     * Tag used to label the instance as a node.
     */
    public static final byte NODE = 0;

    /**
     * Tag used to label the instance as a leaf.
     */
    public static final byte LEAF = 1;

    /**
     * Indentation used for each block when pretty printing.
     */
    public static final int INDENT = 2;

    /**
     * Returns a reader of this instance.
     *
     * @return Reader of this instance.
     */
    public abstract ByteTreeReader getByteTreeReader();

    /**
     * Update the given hash digest with the content of this byte
     * tree.
     *
     * @param digest Digest to be updated.
     */
    public abstract void update(Hashdigest digest);

    /**
     * Writes this instance to the output stream given as input.
     *
     * @param dos Destination output stream.
     *
     * @throws EIOException If this instance can not be written to the
     *  given output stream.
     */
    public abstract void writeTo(DataOutputStream dos) throws EIOException;

    /**
     * Outputs the total number of bytes needed to flatten this
     * instance, i.e., to represent this instance as a
     * <code>byte[]</code>.
     *
     * @return Total number of bytes needed.
     */
    public abstract long totalByteSize();

    /**
     * Writes a <code>byte[]</code> representation of this instance to
     * <code>result</code> starting at the index <code>i</code>.
     *
     * @param result Destination array.
     * @param offset Index where to start writing.
     * @return Number of bytes written.
     */
    public abstract int toByteArray(byte[] result, int offset);

    // Implemented in terms of the above.

    /**
     * Returns a string with the given number of spaces.
     *
     * @param indent Number of spaces.
     * @return Indentation string.
     */
    private static String indentString(final int indent) {

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent * INDENT; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Writes formatted string illustrating the byte tree indented the
     * given number of blocks to the string builder.
     *
     * @param indent Number of indent blocks on each line.
     * @param dos Destination source.
     * @param btr Source for the byte tree.
     * @throws EIOException If the input is not a byte tree.
     */
    private static void prettyWriteTo(final int indent,
                                      final DataOutputStream dos,
                                      final ByteTreeReader btr)
        throws EIOException {

        try {

            final String is = indentString(indent);

            if (btr.isLeaf()) {

                dos.writeBytes(is);
                final byte[] data = btr.read();
                final String hexData = Hex.toHexString(data);
                dos.writeChar('"');
                dos.writeBytes(hexData);
                dos.writeChar('"');

            } else {

                dos.writeBytes(is);
                dos.writeBytes("[\n");

                while (btr.getRemaining() > 0) {

                    prettyWriteTo(indent + 1, dos, btr.getNextChild());

                    if (btr.getRemaining() > 0) {
                        dos.writeChar(',');
                    }

                    dos.writeChar('\n');
                }
                dos.writeBytes(is);
                dos.writeChar(']');
            }
        } catch (final IOException ioe) {
            throw new EIOException(ioe.getMessage(), ioe);
        }
    }

    /**
     * Writes a formatted string representation of this byte tree. The
     * format is a recursive natural JSON array.
     *
     * @param dos Destination source.
     * @throws EIOException If the input is not a byte tree.
     */
    public void prettyWriteTo(final DataOutputStream dos)
        throws EIOException {
        final ByteTreeReader btr = getByteTreeReader();
        prettyWriteTo(0, dos, btr);
        btr.close();
    }

    /**
     * Writes this instance as a JSON formatted string to the file
     * given as input.
     *
     * @param file Destination output file.
     *
     * @throws EIOException If this instance can not be written to the
     *  given file.
     */
    public void prettyWriteTo(final File file) throws EIOException {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(file));
            prettyWriteTo(dos);
        } catch (final IOException ioe) {
            throw new EIOException("Can not write byte tree to file! ("
                                   + file.toString() + ")", ioe);
        } finally {
            ExtIO.strictClose(dos);
        }
    }

    /**
     * Writes this instance to the output stream given as input.
     *
     * @param dos Destination output stream.
     *
     * @throws EIOError If this instance can not be written to the
     *  given output stream.
     */
    public void unsafeWriteTo(final DataOutputStream dos)
        throws EIOError {
        try {
            writeTo(dos);
        } catch (final EIOException eioe) {
            throw new EIOError("Internal error!", eioe);
        }
    }

    /**
     * Writes this instance to the file given as input.
     *
     * @param file Destination output file.
     *
     * @throws EIOException If this instance can not be written to the
     *  given file.
     */
    public void writeTo(final File file) throws EIOException {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(file));
            writeTo(dos);
        } catch (final IOException ioe) {
            throw new EIOException("Can not write byte tree to file! ("
                                   + file.toString() + ")", ioe);
        } finally {
            ExtIO.strictClose(dos);
        }
    }

    /**
     * Writes this instance to the file given as input.
     *
     * @param file Destination output file.
     *
     * @throws EIOError If this instance can not be written to the
     *  given file.
     */
    public void unsafeWriteTo(final File file) throws EIOError {
        try {
            writeTo(file);
        } catch (final EIOException eioe) {
            throw new EIOError("Internal error!", eioe);
        }
    }

    /**
     * Outputs a <code>byte[]</code> representation of this instance.
     * WARNING! A <code>byte[]</code> can have at most
     * <i>2<sup>31</sup>-1</i> elements. Thus, if
     * <code>ByteTree</code>s that do not fit into a
     * <code>byte[]</code> must be handled, then use
     * {@link #writeTo(DataOutputStream)} instead.
     *
     * @return Representation of this instance.
     * @throws EIOError If this instance is too large to convert to a
     *  <code>byte[]</code>.
     */
    public byte[] toByteArray() throws EIOError {
        final long total = totalByteSize();

        if (total > Integer.MAX_VALUE) {
            throw new EIOError("Too big to convert to byte[]!");
        }

        final byte[] result = new byte[(int) total];
        toByteArray(result, 0);
        return result;
    }

    /**
     * Outputs a hexadecimal encoding of a <code>byte[]</code>
     * representation of this instance.  WARNING! A
     * <code>byte[]</code> can have at most <i>2<sup>31</sup>-1</i>
     * elements. Thus, if <code>ByteTree</code>s that do not fit into
     * a <code>byte[]</code> must be handled, then use {@link
     * #writeTo(DataOutputStream)} instead.
     *
     * @return Representation of this instance.
     * @throws EIOError If this instance is too large to convert to a
     *  <code>byte[]</code>.
     */
    public String toHexString() throws EIOError {
        return Hex.toHexString(toByteArray());
    }
}
