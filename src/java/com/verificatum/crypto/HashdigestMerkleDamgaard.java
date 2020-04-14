
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

import java.util.Arrays;

/**
 * Digest for the Merkle-Damgaard construction.
 *
 * @author Douglas Wikstrom
 */
public final class HashdigestMerkleDamgaard implements Hashdigest {

    /**
     * Underlying fixed length collision-resistant hash function.
     */
    HashfunctionFixedLength hffl;

    /**
     * Input byte-length of fixed-length hash function.
     */
    int inputByteLength;

    /**
     * Offset used to deal with the case where the output length of
     * the fixed length hash function is not a multiple of 8.
     */
    int inputByteOffset;

    /**
     * Output byte-length of fixed-length hash function.
     */
    int outputByteLength;

    /**
     * Temporary array to store inputs to the fixed length hash
     * function.
     */
    byte[] temp;

    /**
     * Index within the temp array.
     */
    int tempIndex;

    /**
     * Total number of bytes hashed so far.
     */
    long totalLength;

    /**
     * Creates an instance using the given fixed output length
     * hashfunction.
     *
     * @param hffl Fixed length collision-resistant hash function.
     */
    public HashdigestMerkleDamgaard(final HashfunctionFixedLength hffl) {

        this.hffl = hffl;

        inputByteLength = hffl.getInputLength() / 8;
        if (hffl.getInputLength() % 8 == 0) {
            inputByteOffset = 0;
        } else {
            inputByteOffset = 1;
        }

        outputByteLength = (hffl.getOutputLength() + 7) / 8;

        temp = new byte[inputByteOffset + inputByteLength];
        tempIndex = 0;

        totalLength = 0;
    }

    // Documented in Hashdigest.java

    @Override
    public void update(final byte[]... data) {

        int i = 0; // Indexes the input arrays.
        int dataIndex = 0; // Index within the current input array.

        for (;;) {

            // Copy as much as possible from the current input array.
            final int len = Math.min(inputByteLength - tempIndex, // Space
                                     // left
                                     data[i].length - dataIndex); // Data left
            System.arraycopy(data[i], dataIndex, temp, tempIndex, len);

            // Update the total length
            totalLength += len;

            // Update the index in the current input array.
            dataIndex += len;

            // Update the index in the temporary array.
            tempIndex += len;

            // If the temporary block is filled, then we hash it,
            // and copy the result back to the beginning of the
            // temporary block.
            if (tempIndex == temp.length) {
                System.arraycopy(hffl.hash(temp), 0, temp, inputByteOffset,
                                 outputByteLength);
                tempIndex = outputByteLength + inputByteOffset;
            }

            // If there is no data left to process in the current
            // data block, then we:
            if (dataIndex == data[i].length) {

                // Check if there are more input blocks to
                // process, in which case we move on to the next
                // input block.
                if (i < data.length - 1) {
                    dataIndex = 0;
                    i++;

                    // or if there are no more blocks, then return.
                } else {
                    return;
                }
            }
        }
    }

    @Override
    public void update(final byte[] data, final int offset, final int length) {
        update(Arrays.copyOfRange(data, offset, offset + length));
    }

    @Override
    public byte[] digest() {

        // If there is no room to embed the length at the end of the
        // temporary block, then we set the remainder of the temporary
        // block to zero, hash it and copy the result back to the
        // beginning of the temporary block. This makes room for the
        // length in the new final block.
        if (tempIndex >= temp.length - 8) {
            Arrays.fill(temp, tempIndex, temp.length, (byte) 0);
            System.arraycopy(hffl.hash(temp), 0, temp, inputByteOffset,
                             outputByteLength);
            tempIndex = outputByteLength + inputByteOffset;
        }

        // Then we fill the remainder of the temporary block with
        // zeros and embed the length at the end.
        Arrays.fill(temp, tempIndex, temp.length - 8, (byte) 0);

        for (int i = 1; i <= 8; i++) {
            temp[temp.length - i] = (byte) (totalLength & 0xFF);
            totalLength >>= 8;
        }

        return hffl.hash(temp);
    }
}
