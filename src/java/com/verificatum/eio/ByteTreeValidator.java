
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

import java.util.ArrayList;
import java.util.List;

import com.verificatum.util.Pair;

/**
 * Uses a given byte tree as a template for the structure of later
 * inputs to be validated and parsed. In contrast to other byte tree
 * parsers, here we expect the input to be invalid relatively often
 * and need more detailed error information. The method used here is
 * is faster, since it avoids throwing costly exceptions, but it is
 * less flexible, since the format must be completely fixed in terms
 * of the tree structure and the length of byte arrays stored in
 * leaves.
 *
 * @author Douglas Wikstrom
 */
public class ByteTreeValidator {

    /**
     * Byte length of the template byte tree.
     */
    protected int byteLength;

    /**
     * List of headers.
     */
    protected List<ByteTreeHeader> headers;

    /**
     * Creates a validator that uses the given template to derive the
     * format.
     *
     * @param template Template byte tree.
     */
    public ByteTreeValidator(final ByteTree template) {
        this.byteLength = (int) template.totalByteSize();
        this.headers = ByteTreeHeader.getHeaders(template);
    }

    /**
     * Validates that the input is of the same form as the
     * template. If so it returns a pair of a byte tree and null, and
     * otherwise a pair of null and an error string.
     *
     * @param data Candidate byte array representation of a byte tree.
     * @return Pair of a byte tree and null, or null and an error
     * string as appropriate.
     */
    public Pair<ByteTree, String> validatedByteTree(final byte[] data) {

        // Verify byte length of the input.
        if (data.length != byteLength) {
            final String badByteLength =
                "The byte length of the input is wrong! ("
                + data.length + " != " + byteLength + ")";
            return new Pair<ByteTree, String>(null, badByteLength);
        }

        // Verify that all headers are at the right positions, have
        // the right types, and store the right lengths.
        for (final ByteTreeHeader header : headers) {

            // Verify type of vertex in the tree.
            if (data[header.position] != header.type) {
                final String badType =
                    "The node type at position " + header.position
                    + " is neither a leaf nor a node! ("
                    + data[header.position] + ")";
                return new Pair<ByteTree, String>(null, badType);
            }

            // Verify length
            final int length = ExtIO.readInt(data, header.position);

            if (length != header.length) {
                final String badLength =
                    "Unexpected length at position " + header.position
                    + "! (" + length + ")";
                return new Pair<ByteTree, String>(null, badLength);
            }
        }

        try {
            final ByteTree byteTree = new ByteTree(data, null);
            return new Pair<ByteTree, String>(byteTree, null);
        } catch (final EIOException eioe) {

            // Since we have verified the types and the lengths
            // against a valid byte tree this should not happen.
            throw new EIOError("Fatal error! This is a bug.", eioe);
        }
    }
}

/**
 * Stores the header information of a byte tree.
 *
 * @author Douglas Wikstrom
 */
class ByteTreeHeader {

    /**
     * Position of the header in its representation as a byte array.
     */
    final int position;

    /**
     * Type of vertex, i.e., a leaf or a node.
     */
    final int type;

    /**
     * Number of bytes or children of the vertex.
     */
    final int length;

    /**
     * @param position Position of the header in its representation as
     * a byte array.
     * @param type Type of vertex, i.e., a leaf or a node.
     * @param length Number of bytes or children of the vertex.
     */
    ByteTreeHeader(final int position, final int type, final int length) {
        this.position = position;
        this.type = type;
        this.length = length;
    }

    /**
     * Returns the headers of the given byte tree.
     *
     * @param template Template byte tree.
     * @return Headers of the given byte tree.
     */
    static List<ByteTreeHeader> getHeaders(final ByteTree template) {
        final List<ByteTreeHeader> headers = new ArrayList<ByteTreeHeader>();
        getHeadersInner(template.getByteTreeReader(), headers, 0);
        return headers;
    }

    /**
     * Adds headers.
     *
     * @param btr Representation of byte tree.
     * @param headers List of headers of the byte tree.
     * @param position Current position in the underlying byte array.
     * @return Position after execution of this method.
     */
    private static int getHeadersInner(final ByteTreeReader btr,
                                       final List<ByteTreeHeader> headers,
                                       final int position) {
        int pos = position;
        try {
            if (btr.isLeaf()) {
                final int noBytes = btr.getRemaining();
                btr.read();
                headers.add(new ByteTreeHeader(pos,
                                               ByteTree.LEAF,
                                               noBytes));
                return pos + 1 + 4 + noBytes;
            } else {
                headers.add(new ByteTreeHeader(pos,
                                               ByteTree.NODE,
                                               btr.getRemaining()));
                pos = pos + 1 + 4;

                for (int i = 0; i < btr.getRemaining(); i++) {
                    pos += getHeadersInner(btr.getNextChild(), headers, pos);
                }
                return pos;
            }
        } catch (final EIOException eioe) {

            // This should never happen, since we start with a correct
            // byte tree.
            throw new EIOError("Reading failed! This is a bug.", eioe);
        }
    }
}
