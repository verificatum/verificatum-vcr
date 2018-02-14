
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.verificatum.crypto.Hashdigest;


/*
 * Note that the functions reading from/writing to streams are not
 * based on the ones dealing reading from/writing to byte[]. This is
 * to allow objects that may be too large to allow encoding as a
 * byte[].
 */

/**
 * This class is part of an implementation of a byte oriented
 * intermediate data format. Documentation is provided in
 * {@link ByteTreeBasic}.
 *
 * @author Douglas Wikstrom
 */
public final class ByteTree extends ByteTreeBasic {

    /**
     * Error message used when no data is found from the source of
     * input.
     */
    public static final String NO_DATA = "No data found!";

    /**
     * Error message used when an array can not be permuted.
     */
    public static final String UNABLE_TO_PERMUTE_ARRAY =
        "Unable to permute array!";

    /**
     * If this instance is a leaf, then value holds its contents and
     * otherwise it is null.
     */
    byte[] value;

    /**
     * If this instance is an inner node, then it holds an array of
     * its children and otherwise it is null.
     */
    ByteTree[] children;

    /**
     * Recover an instance from its hexadecimal string representation.
     *
     * @param hexString Representation of an instance.
     *
     * @throws EIOException If it is not possible to read a
     *  <code>ByteTree</code> from the input.
     */
    public ByteTree(final String hexString) throws EIOException {
        this(Hex.toByteArray(hexString), null);
    }

    /**
     * Create a <code>ByteTree</code> with a single zero length
     * <code>byte[]</code> as value, to be used as a default value.
     */
    public ByteTree() {
        value = new byte[0];
        children = null;
    }

    /**
     * Create a leaf containing the given data.
     *
     * @param value Contents of the leaf.
     */
    public ByteTree(final byte[] value) {
        this.value = Arrays.copyOf(value, value.length);
        this.children = null;
    }

    /**
     * Create an inner node containing the inputs as children. The
     * input array is not copied.
     *
     * @param children Children of this node.
     */
    public ByteTree(final ByteTree... children) {
        this.value = null;
        this.children = children;
    }

    @Override
    public ByteTreeReader getByteTreeReader() {
        return new ByteTreeReaderBT(null, this);
    }

    @Override
    public long totalByteSize() {

        // One byte for the label and 4 bytes for either the number of
        // children or the number of bytes of data.
        long byteSize = 5;

        // If value is not null, then this is a leaf and we simply add
        // the length of the value byte[].
        if (value == null) {
            for (final ByteTree child : children) {
                byteSize += child.totalByteSize();
            }
        } else {
            byteSize += value.length;

            // Otherwise this is an inner vertex, and we add the total
            // byte sizes of all its children.
        }
        return byteSize;
    }

    // ##################################################################
    // ######### Writing/reading this instance to a byte[].
    // #############
    // ##################################################################

    @Override
    public int toByteArray(final byte[] result, final int offset) {

        int currentOffset = offset;

        if (value == null) { // We are an inner vertex.

            // Write label
            result[currentOffset] = NODE;
            currentOffset++;

            // Write number of children
            ExtIO.writeInt(result, currentOffset, children.length);
            currentOffset += 4;

            // Write the children
            for (final ByteTree child : children) {
                currentOffset += child.toByteArray(result, currentOffset);
            }

        } else { // We are a leaf.

            // Write label
            result[currentOffset] = LEAF;
            currentOffset++;

            // Write size
            ExtIO.writeInt(result, currentOffset, value.length);
            currentOffset += 4;

            // Write contents
            System.arraycopy(value, 0, result, currentOffset, value.length);
            currentOffset += value.length;

        }

        // Return the number of bytes written.
        return currentOffset - offset;
    }

    /**
     * Reads this instance from the <code>byte[]</code> given as input
     * starting at the index contained in the second input. In
     * external calls the second parameter is expected to be
     * <code>null</code> meaning that the method reads from the
     * beginning of the input <code>byte[]</code>.
     *
     * @param data Holds representation of an instance.
     * @param currentIC Container of the index of where to start
     * reading.
     *
     * @throws EIOException If it is not possible to read a
     *  <code>ByteTree</code> from the input.
     */
    public ByteTree(final byte[] data, final IntContainer currentIC)
        throws EIOException {

        IntContainer ic = currentIC;

        if (ic == null) {
            ic = new IntContainer(0);
        }
        if (data.length - ic.i < 5) {
            throw new EIOException("Not a representation of ByteTree!");
        }
        ic.i++;
        if (data[ic.i - 1] == LEAF) { // We are reading a leaf.

            // Read length of data stored in the leaf.
            final int length = ExtIO.readInt(data, ic.i);
            ic.i += 4;

            // Copy data
            if (data.length - ic.i < length) {
                throw new EIOException("Missing data!");
            }
            value = Arrays.copyOfRange(data, ic.i, ic.i + length);
            ic.i += length;

            children = null;

        } else if (data[ic.i - 1] == NODE) { // We are reading an inner node.

            value = null;

            // Read number of children.
            final int length = ExtIO.readInt(data, ic.i);
            ic.i += 4;

            // Read each child.
            children = new ByteTree[length];
            for (int j = 0; j < children.length; j++) {
                children[j] = new ByteTree(data, ic);
            }
        } else {
            throw new EIOException("Neither leaf nor node! ("
                                   + data[ic.i - 1] + ")");
        }
    }

    // ##################################################################
    // ## Writing/reading this instance to a Data(Output/Input)Stream.
    // ##
    // ##################################################################

    /**
     * Writes this instance to the output stream given as input.
     *
     * @param dos Destination output stream.
     * @throws EIOException If this instance can not be written to the
     *  given output stream.
     */
    @Override
    public void writeTo(final DataOutputStream dos) throws EIOException {
        try {
            if (value == null) { // We are an inner node.

                dos.writeByte(NODE);
                dos.writeInt(children.length);

                for (final ByteTree child : children) {
                    child.writeTo(dos);
                }

            } else { // We are a leaf.

                dos.writeByte(LEAF);
                dos.writeInt(value.length);
                dos.write(value, 0, value.length);

            }

        } catch (final IOException ioe) {
            throw new EIOException("Unable to write ByteTree to stream!", ioe);
        }
    }

    /**
     * Reads this instance from the input stream given as input.
     *
     * @param dis Source input stream.
     * @throws EIOException If it is not possible to read a
     *  <code>ByteTree</code> from the input stream.
     */
    public ByteTree(final DataInputStream dis) throws EIOException {
        try {
            if (dis.readByte() == LEAF) { // We are reading a leaf.

                value = new byte[dis.readInt()];
                dis.readFully(value);
                children = null;

            } else { // We are reading an inner node.

                value = null;
                children = new ByteTree[dis.readInt()];

                for (int i = 0; i < children.length; i++) {
                    children[i] = new ByteTree(dis);
                }
            }
        } catch (final EOFException eofe) {
            throw new EIOException("Unexpected end of file!", eofe);
        } catch (final IOException ioe) {
            throw new EIOException("Can not read from stream!", ioe);
        }
    }

    // ##################################################################
    // ############ Reading this instance to File.
    // ######################
    // ##################################################################

    /**
     * Creates an instance from the representation given on file.
     *
     * @param file File containing representation of byte tree.
     * @throws IOException If there is an IO error.
     * @throws EIOException If some input could not be read.
     */
    public ByteTree(final File file) throws IOException, EIOException {
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
            final ByteTree byteTree = new ByteTree(dis);
            value = byteTree.value;
            children = byteTree.children;
        } finally {
            ExtIO.strictClose(dis);
        }
    }

    // ##################################################################
    // Converting primitive types, Strings, and arrays to and from
    // ByteTrees.
    // ##################################################################

    /**
     * Translates a <code>boolean</code> to its <code>ByteTree</code>
     * representation.
     *
     * @param b Boolean value.
     * @return Representation of the given boolean value.
     */
    public static ByteTree booleanToByteTree(final boolean b) {
        final byte[] byteb = new byte[1];
        if (b) {
            byteb[0] = (byte) 1;
        } else {
            byteb[0] = (byte) 0;
        }
        return new ByteTree(byteb);
    }

    /**
     * Translates a <code>ByteTree</code> to a <code>boolean</code>
     * value if possible and throws an exception otherwise.
     *
     * @param bt Representation of the boolean.
     * @return Boolean value represented by the input.
     * @throws EIOException If the input does not represent a boolean.
     */
    public static boolean byteTreeToBoolean(final ByteTree bt)
        throws EIOException {
        if (bt.value == null) {
            throw new EIOException(NO_DATA);
        }
        if (bt.value.length != 1) {
            throw new EIOException("Wrong length!");
        }
        if (bt.value[0] == 0) {
            return false;
        } else if (bt.value[0] == 1) {
            return true;
        } else {
            throw new EIOException("Illegal byte value!");
        }
    }

    /**
     * Translates a <code>ByteTree</code> to a <code>boolean</code>
     * value if possible and throws an error otherwise. WARNING! This
     * method assumes that the input <code>ByteTree</code> is
     * correctly formed.
     *
     * @param bt Representation of the <code>boolean</code>.
     * @return Boolean value represented by the input.
     *
     * @throws EIOError If the input does not represent an
     *  <code>boolean</code>.
     */
    public static boolean unsafeByteTreeToBoolean(final ByteTree bt)
        throws EIOError {
        try {
            return byteTreeToBoolean(bt);
        } catch (final EIOException bfe) {
            throw new EIOError("Fatal error!", bfe);
        }
    }

    /**
     * Translates an <code>int</code> value to a <code>ByteTree</code>
     * representation.
     *
     * @param n The <code>int</code> value.
     * @return Representation of <code>int</code> value.
     */
    public static ByteTree intToByteTree(final int n) {
        final byte[] intBytes = new byte[4];
        ExtIO.writeInt(intBytes, 0, n);
        return new ByteTree(intBytes);
    }

    /**
     * Translates a <code>ByteTree</code> to an <code>int</code> value
     * if possible and throws an exception otherwise.
     *
     * @param bt Representation of the <code>int</code>.
     * @return The <code>int</code> value represented by the input.
     * @throws EIOException If the input does not represent an
     *  <code>int</code>.
     */
    public static int byteTreeToInt(final ByteTree bt) throws EIOException {
        if (bt.value == null) {
            throw new EIOException(NO_DATA);
        }
        if (bt.value.length != 4) {
            throw new EIOException("Wrong length!");
        }
        return ExtIO.readInt(bt.value, 0);
    }

    /**
     * Translates a <code>ByteTree</code> to an <code>int</code> value
     * if possible and throws an error otherwise. WARNING! This method
     * assumes that the input <code>ByteTree</code> is correctly
     * formed.
     *
     * @param bt Representation of the <code>int</code>.
     * @return The <code>int</code> value represented by the input.
     * @throws EIOError If the input does not represent an
     *  <code>int</code>.
     */
    public static int unsafeByteTreeToInt(final ByteTree bt) throws EIOError {
        try {
            return byteTreeToInt(bt);
        } catch (final EIOException eioe) {
            throw new EIOError("Fatal error!", eioe);
        }
    }

    /**
     * Translates a <code>short</code> value to a
     * <code>ByteTree</code> representation.
     *
     * @param n <code>short</code> value.
     * @return Representation of <code>short</code> value.
     */
    public static ByteTree shortToByteTree(final short n) {
        final byte[] shortBytes = new byte[2];
        ExtIO.writeShort(shortBytes, 0, n);
        return new ByteTree(shortBytes);
    }

    /**
     * Translates a <code>ByteTree</code> to a <code>short</code>
     * value if possible and throws an exception otherwise.
     *
     * @param bt Representation of the <code>short</code>.
     * @return The <code>short</code> value represented by the input.
     * @throws EIOException If the input does not represent a
     *  <code>short</code> .
     */
    public static short byteTreeToShort(final ByteTree bt) throws EIOException {
        if (bt.value == null) {
            throw new EIOException(NO_DATA);
        }
        if (bt.value.length != 2) {
            throw new EIOException("Wrong length!");
        }
        return ExtIO.readShort(bt.value, 0);
    }

    /**
     * Translates a <code>ByteTree</code> to a <code>short</code>
     * value if possible and throws an error otherwise. WARNING! This
     * method assumes that the input <code>ByteTree</code> is
     * correctly formed.
     *
     * @param bt Representation of the <code>short</code>.
     * @return The <code>short</code> value represented by the input.
     * @throws EIOError If the input does not represent a
     *  <code>short</code>.
     */
    public static short unsafeByteTreeToShort(final ByteTree bt)
        throws EIOError {
        try {
            return byteTreeToShort(bt);
        } catch (final EIOException eioe) {
            throw new EIOError("Fatal error!", eioe);
        }
    }

    /**
     * Translates an <code>int[]</code> into a <code>ByteTree</code>.
     *
     * @param array Array to be translated.
     * @return Representation of the input array.
     */
    public static ByteTree intArrayToByteTree(final int[] array) {
        final byte[] byteArray = new byte[4 * array.length];

        for (int j = 0, i = 0; i < array.length; j += 4, i++) {
            ExtIO.writeInt(byteArray, j, array[i]);
        }
        return new ByteTree(byteArray);
    }

    /**
     * Translates a <code>ByteTree</code> to an <code>int[]</code> if
     * possible and throws an exception otherwise.
     *
     * @param bt Representation of the <code>int[]</code>.
     * @return Array represented by the input.
     * @throws EIOException If the input does not represent a
     *  <code>int[]</code> .
     */
    public static int[] byteTreeToIntArray(final ByteTree bt)
        throws EIOException {
        if (bt.value == null) {
            throw new EIOException(NO_DATA);
        }
        if (bt.value.length % 4 != 0) {
            throw new EIOException("Length is not multiple of 4!");
        }
        final byte[] byteArray = bt.value;
        final int[] array = new int[byteArray.length / 4];

        for (int j = 0, i = 0; i < array.length; j += 4, i++) {
            array[i] = ExtIO.readInt(byteArray, j);
        }
        return array;
    }

    /**
     * Translates a <code>boolean[]</code> into a
     * <code>ByteTree</code>.
     *
     * @param array Array to be translated.
     * @return Representation of the input array.
     */
    public static ByteTree booleanArrayToByteTree(final boolean[] array) {
        final byte[] byteArray = new byte[array.length];

        for (int i = 0; i < array.length; i++) {
            if (array[i]) {
                byteArray[i] = (byte) 1;
            } else {
                byteArray[i] = (byte) 0;
            }
        }
        return new ByteTree(byteArray);
    }

    /**
     * Translates a <code>ByteTree</code> to an <code>boolean[]</code>
     * if possible and throws an exception otherwise.
     *
     * @param bt Representation of the <code>int[]</code>.
     * @return Array represented by the input.
     * @throws EIOException If the input does not represent a
     *  <code>boolean[]</code>.
     */
    public static boolean[] byteTreeToBooleanArray(final ByteTree bt)
        throws EIOException {
        if (bt.value == null) {
            throw new EIOException(NO_DATA);
        }
        final byte[] byteArray = bt.value;
        final boolean[] array = new boolean[byteArray.length];

        for (int i = 0; i < array.length; i++) {
            if (byteArray[i] == 1) {
                array[i] = true;
            } else if (byteArray[i] == 0) {
                array[i] = false;
            } else {
                throw new EIOException("Malformed array of booleans!");
            }
        }
        return array;
    }

    /**
     * Translates a <code>String</code> to its <code>ByteTree</code>
     * representation.
     *
     * @param s The <code>String</code> to be translated.
     * @return Representation of the <code>String</code>.
     */
    public static ByteTree stringToByteTree(final String s) {
        try {
            return new ByteTree(s.getBytes("UTF-8"));
        } catch (final UnsupportedEncodingException uee) {
            throw new EIOError("This is a bug in the VM!", uee);
        }
    }

    /**
     * Translates a <code>ByteTree</code> to a <code>String</code>.
     *
     * @param bt Representation of the <code>String</code>.
     * @return <code>String</code> element represented by the input.
     * @throws EIOException If the input does not represent a
     *  <code>String</code>.
     */
    public static String byteTreeToString(final ByteTree bt)
        throws EIOException {
        if (bt.value == null) {
            throw new EIOException(NO_DATA);
        }
        try {
            return new String(bt.value, "UTF-8");
        } catch (final UnsupportedEncodingException uee) {
            throw new EIOError("This is a bug in the VM!", uee);
        }
    }

    /**
     * Translates a <code>String[]</code> into a <code>ByteTree</code>
     * .
     *
     * @param array Array to be translated.
     * @return Representation of the input array.
     */
    public static ByteTree stringArrayToByteTree(final String[] array) {
        final ByteTree[] byteTreeStrings = new ByteTree[array.length];

        for (int i = 0; i < array.length; i++) {
            byteTreeStrings[i] = ByteTree.stringToByteTree(array[i]);
        }
        return new ByteTree(byteTreeStrings);
    }

    /**
     * Translates a <code>ByteTree</code> to a <code>String</code>
     * array if this is possible and throws an exception if this is
     * not possible.
     *
     * @param bt Representation of the <code>String[]</code>.
     * @return Array represented by the input.
     * @throws EIOException If the input does not represent a
     *  <code>String[]</code>.
     */
    public static String[] byteTreeToStringArray(final ByteTree bt)
        throws EIOException {
        if (bt.children == null) {
            throw new EIOException("No children in ByteTree!");
        }
        final String[] array = new String[bt.children.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = ByteTree.byteTreeToString(bt.children[i]);
        }
        return array;
    }

    // Documented in ByteTreeBasic.java

    @Override
    public void update(final Hashdigest digest) {
        final byte[] prefix = new byte[5];
        if (value == null) {

            prefix[0] = ByteTreeBasic.NODE;
            ExtIO.writeInt(prefix, 1, children.length);
            digest.update(prefix);
            for (int i = 0; i < children.length; i++) {
                children[i].update(digest);
            }

        } else {

            prefix[0] = ByteTreeBasic.LEAF;
            ExtIO.writeInt(prefix, 1, value.length);
            digest.update(prefix);
            digest.update(value);
        }
    }
}
