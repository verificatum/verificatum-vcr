
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

package com.verificatum.arithm;

import java.util.Arrays;

import com.verificatum.annotation.CoberturaIgnore;
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.HashfunctionHeuristic;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.ExtIO;


/**
 * Implements a group element belonging to a {@link ModPGroup}
 * instance.
 *
 * @author Douglas Wikstrom
 */
public final class ModPGroupElement extends BPGroupElement {

    /**
     * Value of this instance.
     */
    LargeInteger value;

    /**
     * Creates an element extracted from the input. The input integer
     * must be canonically reduced.
     *
     * @param pGroup Group to which the instance belongs.
     * @param value An integer from which the element is constructed.
     */
    public ModPGroupElement(final PGroup pGroup, final LargeInteger value) {
        super(pGroup);
        this.value = value;
    }

    /**
     * Creates a <code>PGroupElement</code> instance from its byte
     * tree representation.
     *
     * <p>
     *
     * WARNING! If the safe flag is set to false and the input is
     * incorrectly formatted, then the output is undefined and no
     * exception is thrown.
     *
     * @param pGroup Group to which the instance belongs.
     * @param btr A representation of an instance.
     * @param safe Determines if the input is verified or not.
     *
     * @throws ArithmFormatException If the input does not represent
     * an element in the given group.
     */
    protected ModPGroupElement(final ModPGroup pGroup,
                               final ByteTreeReader btr,
                               final boolean safe)
        throws ArithmFormatException {
        super(pGroup);

        if (btr.getRemaining() != pGroup.modulusByteLength) {
            throw new ArithmFormatException("Incorrect length of data!");
        }
        value = new LargeInteger(pGroup.modulusByteLength, btr);

        if (safe && !pGroup.contains(value)) {
            throw new ArithmFormatException("Not a group element!");
        }
    }

    @Override
    protected void verifyUnsafe() throws ArithmFormatException {
        if (!((ModPGroup) this.pGroup).contains(value)) {
            throw new ArithmFormatException("Not a group element!");
        }
    }

    /**
     * Encode the input bytes as a group element.
     *
     * @param byteArray Byte array containing data to be encoded.
     * @param startIndex Index of start of data to be encoded.
     * @param len Number of bytes to be encoded.
     * @return Encoding of input bytes.
     */
    private LargeInteger roEncoding(final byte[] byteArray,
                                    final int startIndex,
                                    final int len) {

        // Encode length in two bits.
        final Hashfunction hf = new HashfunctionHeuristic("SHA-256");

        PGroupElement el = pGroup.getONE();

        // We repeatedly hash until we hit the message we intend to
        // encode. We could build a table for this...

        LargeInteger value = null;

        while (value == null) {

            el = el.mul(pGroup.getg());
            final byte[] digest = hf.hash(el.toByteTree().toByteArray());

            if ((digest[0] & (byte) 0x03) == len) {

                value = ((ModPGroupElement) el).value;

                for (int i = 1, j = startIndex; i <= len; i++, j++) {
                    if (digest[i] != byteArray[j]) {
                        value = null;
                        break;
                    }
                }
            }
        }

        return value;
    }

    /**
     * Encodes a part of an arbitrary <code>byte[]</code> as an
     * element in the group. The input is truncated if it is longer
     * than {@link PGroup#getEncodeLength()} bytes. The resulting
     * element can be decoded again using
     * {@link PGroupElement#decode(byte[],int)}.
     *
     * @param pGroup Group to which the resulting element belongs.
     * @param byteArray Bytes to be encoded.
     * @param startIndex Starting index.
     * @param length Number of bytes to encode.
     */
    public ModPGroupElement(final ModPGroup pGroup,
                            final byte[] byteArray,
                            final int startIndex,
                            final int length) {
        super(pGroup);

        // Make sure that we never use more than the allowed number of
        // bytes.
        final int len = Math.min(length, pGroup.getEncodeLength());

        if (pGroup.encoding == ModPGroup.RO_ENCODING) {

            value = roEncoding(byteArray, startIndex, len);

        } else {

            // Make room for an array with an int-prefix that says how
            // many bytes are encoded.
            final int noBytesToUse = pGroup.getEncodeLength() + 4;
            final byte[] bytesToUse = new byte[noBytesToUse];

            // Write the number of bytes.
            ExtIO.writeInt(bytesToUse, 0, len);

            // Write the content.
            System.arraycopy(byteArray, startIndex, bytesToUse, 4, len);

            // For unique encoding we put zeros at the end.
            Arrays.fill(bytesToUse, 4 + len, noBytesToUse, (byte) 0);

            // Make sure value is non-zero. This byte is ignored when
            // decoding, since the length is zero.
            if (len == 0) {
                bytesToUse[5] = 1;
            }

            // Turn the resulting byte[] into a LargeInteger. This
            // integer is positive, since len is bounded giving
            // a starting zero-byte.
            value = new LargeInteger(bytesToUse);

            if (pGroup.encoding == ModPGroup.SAFEPRIME_ENCODING) {

                // Multiply by quadratic non-residue if needed.
                if (value.legendre(pGroup.modulus) != 1) {
                    value = value.neg().mod(pGroup.modulus);
                }

            } else if (pGroup.encoding == ModPGroup.SUBGROUP_ENCODING) {

                // Repeatedly add 2^noBytesToUse until we are in the
                // subgroup.
                int i = 0;

                for (; i < pGroup.encodingAttempts; i++) {
                    if (pGroup.contains(value)) {
                        break;
                    } else {
                        value = value.add(pGroup.addNum);
                    }
                }

                if (i == 2 * pGroup.encodingAttempts) {

                    // This should never happen. We expect that the
                    // probability that this happens is roughly
                    // 2^(-256).
                    throw new ArithmError("Encoding failed!");
                }

            } else {

                throw new ArithmError("Unknown encoding!");

            }
        }
    }

    /**
     * Returns the integer representating this group element.
     *
     * @return Integer representing this group element.
     */
    @CoberturaIgnore
    public LargeInteger toLargeInteger() {
        return value;
    }

    // Documented in PGroupElement.java.

    @Override
    public ByteTreeBasic toByteTree() {
        final byte[] temp = value.toByteArray();
        final byte[] result = new byte[((ModPGroup) pGroup).modulusByteLength];

        // We know that temp.length <= modulusByteLength
        Arrays.fill(result,
                    0,
                    ((ModPGroup) pGroup).modulusByteLength - temp.length,
                    (byte) 0);

        System.arraycopy(temp,
                         0,
                         result,
                         ((ModPGroup) pGroup).modulusByteLength - temp.length,
                         temp.length);

        return new ByteTree(result);
    }

    @CoberturaIgnore
    @Override
    public String toString() {
        return value.toString(16);
    }

    @Override
    public int decode(final byte[] array, final int startIndex) {

        byte[] raw = null;

        if (((ModPGroup) pGroup).encoding == ModPGroup.RO_ENCODING) {

            // Length is embedded in two bits.
            final Hashfunction hf = new HashfunctionHeuristic("SHA-256");
            raw = hf.hash(toByteTree().toByteArray());

            int length = raw[0] & 0x03;
            length = Math.min(length, pGroup.getEncodeLength());

            System.arraycopy(raw, 1, array, startIndex, length);

            return length;

        } else {

            if (((ModPGroup) pGroup).encoding == ModPGroup.SAFEPRIME_ENCODING) {

                final ModPGroup modPGroup = (ModPGroup) pGroup;

                final LargeInteger negValue =
                    value.neg().mod(modPGroup.modulus);

                if (negValue.compareTo(value) < 0) {

                    raw = negValue.toByteArray();

                } else {

                    raw = value.toByteArray();

                }

            } else if (((ModPGroup) pGroup).encoding
                       == ModPGroup.SUBGROUP_ENCODING) {

                raw = value.toByteArray();

            } else {

                throw new ArithmError("Malformed group!");
            }

            // Make sure we have sufficiently many bytes.
            if (raw.length < pGroup.getEncodeLength() + 4) {
                final byte[] tmp = new byte[pGroup.getEncodeLength() + 4];
                System.arraycopy(raw,
                                 0,
                                 tmp,
                                 tmp.length - raw.length,
                                 raw.length);
                raw = tmp;
            }

            // We jump over potential encoding bits.
            final int offset = raw.length - pGroup.getEncodeLength() - 4;

            // If the length is illegal, then we view it as zero.
            final int len = ExtIO.readInt(raw, offset);

            if (len < 0 || pGroup.getEncodeLength() < len) {
                return 0;
            }

            // We know that there are always len bytes to copy.
            System.arraycopy(raw, offset + 4, array, startIndex, len);

            return len;
        }
    }

    @Override
    public PGroupElement mul(final PGroupElement el) {
        if (pGroup.equals(el.pGroup)) {
            final LargeInteger li =
                value.mul(((ModPGroupElement) el).value).
                mod(((ModPGroup) pGroup).modulus);
            return new ModPGroupElement(pGroup, li);
        } else {
            throw new ArithmError("Distinct groups!");
        }
    }

    @Override
    public PGroupElement inv() throws ArithmError {
        try {

            final LargeInteger li = value.modInv(((ModPGroup) pGroup).modulus);
            return new ModPGroupElement(pGroup, li);

        // UNCOVERABLE (Every element in the group is invertible.)
        } catch (final ArithmException ae) {
            throw new ArithmError("Inversion failed!", ae);
        }
    }

    @Override
    public PGroupElement exp(final LargeInteger exponent) {
        final LargeInteger res =
            value.modPow(exponent, ((ModPGroup) pGroup).modulus);
        return new ModPGroupElement(pGroup, res);
    }

    @Override
    public PGroupElementArray exp(final PRingElementArray exponents) {
        if (pGroup.pRing.equals(exponents.pRing)) {
            final LargeIntegerArray integers =
                ((PFieldElementArray) exponents).values;

            final LargeIntegerArray res =
                integers.modPowVariant(value, ((ModPGroup) pGroup).modulus);

            return new ModPGroupElementArray(pGroup, res);
        } else {
            throw new ArithmError("Mismatching elements!");
        }
    }

    @Override
    public int compareTo(final PGroupElement el) {
        if (pGroup.equals(el.pGroup)) {
            return value.compareTo(((ModPGroupElement) el).value);
        } else {
            throw new ArithmError("Distinct groups!");
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModPGroupElement)) {
            return false;
        }
        final ModPGroupElement el = (ModPGroupElement) obj;
        return pGroup.equals(el.pGroup) && value.compareTo(el.value) == 0;
    }
}
