
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

package com.verificatum.arithm;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeContainer;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizable;
import com.verificatum.util.ArrayWorker;
import com.verificatum.util.Functions;


/**
 * Abstract class representing a group for cryptographic use, where
 * each element has the same prime order. The group is not necessarily
 * cyclic. (To check if a group is cyclic one can check if its
 * associated {@link PRing} is a {@link PField}.) Elements in the
 * group are represented by the abstract class {@link PGroupElement}
 * and the "exponents" of this group is represented by {@link PRing}.
 * Arrays of group elements are handled by {@link PGroupElementArray}.
 *
 * @author Douglas Wikstrom
 */
public abstract class PGroup implements Marshalizable, PRingAssociated {

    /**
     * Error for mixed group operations.
     */
    public static final String MISMATCHING_GROUPS =
        "Parameters from different groups!";

    /**
     * Error for mixed ring and group operations.
     */
    public static final String MISMATCHING_GROUP_RING =
        "Ring parameters do not match group!";

    /**
     * Error for different lengths of arrays.
     */
    public static final String DIFFERENT_LENGTHS =
        "Different lengths of arrays!";

    /**
     * Ring associated with this instance.
     */
    protected PRing pRing;

    /**
     * Breakpoint at which exponentiation of
     * <code>PGroupElement[]</code> is threaded.
     */
    protected int expThreadThreshold = 100;

    /**
     * Breakpoint at which multiplication (and other operations with
     * similar cost) of <code>PGroupElement[]</code> is threaded.
     */
    protected int mulThreadThreshold = 1000;

    /**
     * Creates a group. It is the responsibility of the programmer to
     * initialize this instance, e.g., by calling {@link #init(PRing)}
     */
    protected PGroup() {
        super();
    }

    /**
     * Creates a group with the given associated ring.
     *
     * @param pRing Ring associated with this instance.
     */
    protected PGroup(final PRing pRing) {
        init(pRing);
    }

    /**
     * Initializes this instance with the given ring.
     *
     * @param pRing Ring associated with this instance.
     */
    protected void init(final PRing pRing) {
        this.pRing = pRing;
    }

    /**
     * Returns the order of every non-unit element in this group.
     *
     * @return Order of every non-unit element in this group.
     */
    public final LargeInteger getElementOrder() {
        return pRing.getPField().getOrder();
    }

    /**
     * Returns the prime order group used to construct this group.
     *
     * @return Prime order group used to construct this group.
     */
    public PGroup getPrimeOrderPGroup() {
        return this;
    }

    /**
     * Returns the breakpoint at which exponentiation of
     * <code>PGroupElement[]</code> is threaded.
     *
     * @return Current threshold.
     */
    public int getExpThreadThreshold() {
        synchronized (this) {
            return expThreadThreshold;
        }
    }

    /**
     * Sets the breakpoint at which exponentiation of
     * <code>PGroupElement[]</code> is threaded.
     *
     * @param expThreadThreshold New threshold.
     */
    public void setExpThreadThreshold(final int expThreadThreshold) {
        synchronized (this) {
            this.expThreadThreshold = expThreadThreshold;
        }
    }

    /**
     * Returns the breakpoint at which multiplication (and other
     * operations with similar cost) of <code>PGroupElement[]</code>
     * is threaded.
     *
     * @return Current threshold.
     */
    public int getMulThreadThreshold() {
        synchronized (this) {
            return mulThreadThreshold;
        }
    }

    /**
     * Sets the breakpoint at which multiplication (and other
     * operations with similar cost) of <code>PGroupElement[]</code>
     * is threaded.
     *
     * @param mulThreadThreshold New threshold.
     */
    public void setMulThreadThreshold(final int mulThreadThreshold) {
        synchronized (this) {
            this.mulThreadThreshold = mulThreadThreshold;
        }
    }

    /**
     * Outputs a human readable description of the group. This should
     * only be used for debugging.
     *
     * @return Human readable description of the group.
     */
    @Override
    public abstract String toString();

    /**
     * Returns the standard generator of the group, where "generator"
     * means a generator under the action of the associated ring,
     * (which is not necessarily a field).
     *
     * @return Standard generator.
     */
    public abstract PGroupElement getg();

    /**
     * Returns the unit in the group.
     *
     * @return Unit in the group.
     */
    public abstract PGroupElement getONE();

    /**
     * Returns the fixed number of bytes used to injectively map an
     * instance to a <code>byte[]</code>.
     *
     * @return Fixed byte length.
     */
    public abstract int getByteLength();

    /**
     * Returns the maximal number of bytes that can be encoded in an
     * element of the group.
     *
     * @return Maximal number of bytes that can be encoded in a group
     * element.
     */
    public abstract int getEncodeLength();

    /**
     * Creates a {@link PGroupElement} instance from the given
     * representation.
     *
     * @param btr Representation of an instance.
     * @return Group element represented by the input.
     *
     * @throws ArithmFormatException If the input does not represent
     * an element of this group.
     */
    public abstract PGroupElement toElement(ByteTreeReader btr)
        throws ArithmFormatException;

    /**
     * Creates a {@link PGroupElement} instance from the given
     * representation. The representation is expected to be
     * correct. If this is not the case, the output is undefined and
     * an error could be thrown (don't rely on such an error).
     *
     * @param btr A representation of an instance.
     * @return Group element represented by the input.
     *
     * @throws ArithmError If the input does not represent a group
     * element of this group.
     */
    public abstract PGroupElement unsafeToElement(final ByteTreeReader btr)
        throws ArithmError;

    /**
     * Encodes a part of an arbitrary <code>byte[]</code> as an
     * element in the group. The input is truncated if it is longer
     * than {@link #getEncodeLength()} bytes. The resulting bytes can
     * be recovered using {@link PGroupElement#decode(byte[],int)}.
     *
     * @param bytes Bytes to be encoded.
     * @param startIndex Starting index.
     * @param length Length of bytes to be encoded.
     * @return Group element encoding the input.
     */
    public abstract PGroupElement encode(byte[] bytes, int startIndex,
                                         int length);

    /**
     * Returns a randomly chosen element.
     *
     * <p>
     *
     * <b>WARNING! The element must be derived from the random source
     * in such a way that the discrete logarithm is hard to compute in
     * the standard basis. In particular that output CAN NOT be
     * computed by raising the standard generator to random
     * exponent.</b>
     *
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @return Random element of this group.
     */
    public abstract PGroupElement randomElement(RandomSource rs, int statDist);

    /**
     * Creates an array containing the given elements.
     *
     * @param elements Elements to be contained in the array.
     * @return Array containing the input group elements.
     */
    public abstract PGroupElementArray toElementArray(PGroupElement[] elements);

    /**
     * Returns the concatenation of the inputs.
     *
     * @param arrays Arrays to be concatenated.
     * @return Concatenation of input arrays.
     */
    public abstract PGroupElementArray
        toElementArray(PGroupElementArray... arrays);

    /**
     * Recovers an array of group elements from the given
     * representation.
     *
     * @param size Expected number of elements in array. If this is
     * set to zero, then an array of any size is accepted.
     * @param btr Representation of array.
     * @return Array of group elements.
     *
     * @throws ArithmFormatException If the input does not represent
     * an array of group elements of the given size.
     */
    public abstract PGroupElementArray toElementArray(int size,
                                                      ByteTreeReader btr)
        throws ArithmFormatException;

    /**
     * Creates an instance of the given size filled with copies of the
     * given element.
     *
     * @param size Number of elements in resulting array.
     * @param element Element to use.
     * @return Array of copied elements.
     */
    public abstract PGroupElementArray toElementArray(int size,
                                                      PGroupElement element);

    /**
     * Generates a random array of group elements.
     *
     * <p>
     *
     * <b>WARNING! The elements must be derived from the random source
     * in such a way that the discrete logarithm is hard to compute in
     * the standard basis. In particular that output elements CAN NOT
     * be computed by raising the standard generator to random
     * exponents.</b>
     *
     * @param size Number of elements to generate.
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @return Array of random group elements.
     */
    public abstract PGroupElementArray randomElementArray(int size,
                                                          RandomSource rs,
                                                          int statDist);

    /**
     * Returns true if and only if the input group equals this group.
     *
     * @param obj Instance to compare with.
     * @return True if and only if the input group equals this group.
     */
    @Override
    public abstract boolean equals(Object obj);

    // ############ Implemented in terms of the above. ###########

    /**
     * Returns an array of random group elements.
     *
     * @param size Size of resulting array.
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @return Array of random group elements.
     */
    public final PGroupElement[] randomElements(final int size,
                                                final RandomSource rs,
                                                final int statDist) {
        final PGroupElement[] res = new PGroupElement[size];
        for (int i = 0; i < size; i++) {
            res[i] = randomElement(rs, statDist);
        }
        return res;
    }

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    /**
     * Creates an array containing the given elements.
     *
     * @param arrays Array of arrays of elements to be contained in
     * the output array.
     * @return Array containing the input group elements.
     */
    public PGroupElementArray
        toElementArray(final List<PGroupElement[]> arrays) {
        int totalLength = 0;
        for (final PGroupElement[] array : arrays) {
            totalLength += array.length;
        }
        final PGroupElement[] result = new PGroupElement[totalLength];
        int offset = 0;
        for (final PGroupElement[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return toElementArray(result);
    }

    /**
     * Orders the arrays over this group "lexicographically". The
     * ordering is obviously not compatible with the binary group
     * operator in any interesting way, but it is useful to have some
     * ordering to be able to sort elements.
     *
     * @param left Left side array of group element.
     * @param right Right side array of group element.
     * @return -1, 0, or 1 depending on if the left array comes
     * before, is equal to, or comes after the right array.
     */
    public final int compareTo(final PGroupElement[] left,
                               final PGroupElement[] right) {

        for (int i = 0; i < left.length; i++) {
            final int cmp = left[i].compareTo(right[i]);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }

    /**
     * Returns the product of all elements in <code>bases</code> to
     * the respective powers in <code>exponents</code>. This is
     * naively implemented, but with threading.
     *
     * @param bases Bases to be exponentiated.
     * @param exponents Powers to be taken.
     * @return Product of all bases to the powers of the given
     * exponents.
     */
    public final PGroupElement naiveExpProd(final PGroupElement[] bases,
                                            final PRingElement[] exponents) {
        final List<PGroupElement> parts =
            Collections.synchronizedList(new LinkedList<PGroupElement>());

        final ArrayWorker worker = new ArrayWorker(bases.length) {
                @Override
                public boolean divide() {
                    return bases.length > expThreadThreshold;
                }
                @Override
                public void work(final int start, final int end) {
                    PGroupElement part = getONE();

                    for (int i = start; i < end; i++) {
                        part = part.mul(bases[i].exp(exponents[i]));
                    }

                    parts.add(part);
                }
            };
        worker.work();

        PGroupElement res = getONE();
        for (final PGroupElement part : parts) {
            res = res.mul(part);
        }
        return res;
    }

    /**
     * Returns the product of all elements in <code>bases</code> to
     * the respective powers in <code>exponents</code> in an
     * element-wise fashion, i.e., the integer exponents are applied
     * to each "row" of elements gathered from the arrays. This uses
     * simultaneous exponentiation and threading.
     *
     * @param bases Bases to be exponentiated.
     * @param integers Powers to be taken.
     * @param bitLength Maximal bit length.
     * @return Product of all bases to the powers of the given
     * exponents.
     */
    public PGroupElementArray expProd(final PGroupElementArray[] bases,
                                      final LargeInteger[] integers,
                                      final int bitLength) {

        final PField pField = pRing.getPField();

        PGroupElementArray res = toElementArray(bases[0].size(), getONE());

        for (int i = 0; i < integers.length; i++) {

            PGroupElementArray tmp1;
            PGroupElementArray tmp2;

            // We exploit that the integers may have small absolute
            // value, but we need to take care of negative integers.
            if (integers[i].compareTo(LargeInteger.ZERO) > 0) {
                tmp1 = bases[i].exp(pField.toElement(integers[i]));
            } else {
                tmp2 = bases[i].inv();
                tmp1 = tmp2.exp(pField.toElement(integers[i].neg()));
                tmp2.free();
            }

            tmp2 = res;
            res = res.mul(tmp1);

            tmp1.free();
            tmp2.free();
        }

        return res;

        //return bases[0].expProd(bases, integers, bitLength);
    }

    /**
     * Returns the product of all elements in <code>bases</code> to
     * the respective powers in <code>exponents</code>. This uses
     * simultaneous exponentiation and threading.
     *
     * @param bases Bases to be exponentiated.
     * @param integers Powers to be taken.
     * @param bitLength Maximal bit length.
     * @return Product of all bases to the powers of the given
     * exponents.
     */
    public PGroupElement expProd(final PGroupElement[] bases,
                                 final LargeInteger[] integers,
                                 final int bitLength) {
        final int maxWidth = PGroupSimExpTab.optimalWidth(bitLength);

        // We need to collect partial results from multiple threads in
        // a thread-safe way.
        final List<PGroupElement> parts =
            Collections.synchronizedList(new LinkedList<PGroupElement>());

        final ArrayWorker worker = new ArrayWorker(bases.length) {
                @Override
                public boolean divide() {
                    return bases.length > expThreadThreshold;
                }
                @Override
                public void work(final int start, final int end) {

                    PGroupElement part = getONE();

                    int offset = start;

                    // Splits parts recieved from ArrayWorker and run
                    // through these smaller parts.
                    while (offset < end) {

                        final int width = Math.min(maxWidth, end - offset);

                        // Compute table for simultaneous exponentiation.
                        final PGroupSimExpTab tab =
                            new PGroupSimExpTab(bases, offset, width);

                        // Perform simultaneous exponentiation.
                        final PGroupElement batch =
                            tab.expProd(integers, offset, bitLength);

                        part = part.mul(batch);

                        offset += width;
                    }
                    parts.add(part);
                }
            };

        worker.work();

        // Multiply the results of the threads.
        PGroupElement res = getONE();
        for (final PGroupElement part : parts) {
            res = res.mul(part);
        }

        return res;
    }

    /**
     * Returns the product of all elements in <code>bases</code> to
     * the respective powers in <code>exponents</code>. This uses
     * simultaneous exponentiation and threading.
     *
     * @param bases Bases to be exponentiated.
     * @param exponents Powers to be taken.
     * @return Product of all bases to the powers of the given
     * exponents.
     */
    public abstract PGroupElement expProd(final PGroupElement[] bases,
                                          final PRingElement[] exponents);

    /**
     * Computes the element-wise product of the inputs.
     *
     * @param array1 Array of group elements.
     * @param array2 Array of group elements.
     * @return Element-wise product of the inputs.
     */
    public final PGroupElement[] mul(final PGroupElement[] array1,
                                     final PGroupElement[] array2) {
        final PGroupElement[] res = new PGroupElement[array1.length];

        final ArrayWorker worker = new ArrayWorker(res.length) {
                @Override
                public boolean divide() {
                    return res.length > mulThreadThreshold;
                }
                @Override
                public void work(final int start, final int end) {
                    for (int i = start; i < end; i++) {
                        res[i] = array1[i].mul(array2[i]);
                    }
                }
            };
        worker.work();
        return res;
    }

    /**
     * Returns the element-wise inverse of the input array.
     *
     * @param elements Elements to be inverted.
     * @return Array of results.
     */
    public final PGroupElement[] inv(final PGroupElement[] elements) {
        final PGroupElement[] res = new PGroupElement[elements.length];

        final ArrayWorker worker = new ArrayWorker(res.length) {
                @Override
                public boolean divide() {
                    return res.length > mulThreadThreshold;
                }
                @Override
                public void work(final int start, final int end) {
                    for (int i = start; i < end; i++) {
                        res[i] = elements[i].inv();
                    }
                }
            };
        worker.work();
        return res;
    }

    /**
     * Returns the element-wise division of the elements in the two
     * arrays.
     *
     * @param numerators Numerators.
     * @param denominators Denominators.
     * @return Array of results.
     */
    public final PGroupElement[] div(final PGroupElement[] numerators,
                                     final PGroupElement[] denominators) {
        final PGroupElement[] res = new PGroupElement[numerators.length];

        final ArrayWorker worker = new ArrayWorker(res.length) {
                @Override
                public boolean divide() {
                    return res.length > mulThreadThreshold;
                }
                @Override
                public void work(final int start, final int end) {
                    for (int i = start; i < end; i++) {
                        res[i] = numerators[i].div(denominators[i]);
                    }
                }
            };
        worker.work();
        return res;
    }

    /**
     * Returns all elements in <code>bases</code> to the respective
     * powers in <code>exponents</code>.
     *
     * @param bases Bases to be exponentiated.
     * @param exponents Powers to be taken.
     * @return All bases to the powers of the given exponents.
     */
    public PGroupElement[] exp(final PGroupElement[] bases,
                               final PRingElement[] exponents) {
        final PGroupElement[] res = new PGroupElement[bases.length];

        final ArrayWorker worker = new ArrayWorker(res.length) {
                @Override
                public boolean divide() {
                    return res.length > expThreadThreshold;
                }
                @Override
                public void work(final int start, final int end) {

                    for (int i = start; i < end; i++) {
                        res[i] = bases[i].exp(exponents[i]);
                    }
                }
            };
        worker.work();
        return res;
    }

    /**
     * Returns elements in <code>bases</code> to the power of
     * <code>exponent</code>.
     *
     * @param bases Bases to be exponentiated.
     * @param exponent Power to be taken.
     * @return All bases to the power of the given exponent.
     */
    public PGroupElement[] exp(final PGroupElement[] bases,
                               final PRingElement exponent) {
        final PGroupElement[] res = new PGroupElement[bases.length];

        final ArrayWorker worker = new ArrayWorker(res.length) {
                @Override
                public boolean divide() {
                    return res.length > expThreadThreshold;
                }
                @Override
                public void work(final int start, final int end) {
                    for (int i = start; i < end; i++) {
                        res[i] = bases[i].exp(exponent);
                    }
                }
            };
        worker.work();
        return res;
    }

    /**
     * Returns the product of the input elements.
     *
     * @param elements Elements to be multiplied.
     * @return Product of input elements.
     */
    public final PGroupElement prod(final PGroupElement[] elements) {
        PGroupElement res = getONE();

        // We need to collect partial results from multiple threads in
        // a thread-safe way.
        final List<PGroupElement> parts =
            Collections.synchronizedList(new LinkedList<PGroupElement>());

        final ArrayWorker worker = new ArrayWorker(elements.length) {
                @Override
                public boolean divide() {
                    return elements.length > mulThreadThreshold;
                }
                @Override
                public void work(final int start, final int end) {
                    PGroupElement part = getONE();

                    for (int i = start; i < end; i++) {
                        part = part.mul(elements[i]);
                    }

                    parts.add(part);
                }
            };
        worker.work();

        // Multiply the results of the threads.
        for (final PGroupElement part : parts) {
            res = res.mul(part);
        }

        return res;
    }

    /**
     * Tests if the elements in the two inputs are equal.
     *
     * @param a Array of elements.
     * @param b Array of elements.
     * @return <code>true</code> or <code>false</code> depending on if
     * the elements in the two arrays are equal or not.
     */
    public boolean equals(final PGroupElement[] a, final PGroupElement[] b) {
        return Arrays.equals(a, b);
    }

    /**
     * Encodes an arbitrary <code>byte[]</code> as an array of
     * elements in the group. The number of elements is chosen such
     * that all bits can be encoded and if needed the last encoded
     * chunk is padded with zeros before encoding. The resulting array
     * can then be decoded again using
     * {@link #decode(PGroupElement[])}. The empty array is encoded as
     * a single group element.
     *
     * @param bytes Bytes to be encoded.
     * @param rs Source of randomness.
     * @return Array of group elements encoding the input.
     */
    public PGroupElement[] encode(final byte[] bytes, final RandomSource rs) {
        final int encodeLength = getEncodeLength();
        final int noPGroupElements =
            Math.max(1, (bytes.length + encodeLength - 1) / encodeLength);

        final PGroupElement[] res = new PGroupElement[noPGroupElements];

        int i = 0;
        int j = 0;
        for (i = 0; i < noPGroupElements - 1; i++) {
            res[i] = encode(bytes, j, encodeLength);
            j += encodeLength;
        }
        res[i] = encode(bytes, j, bytes.length - j);

        return res;
    }

    /**
     * Recovers a <code>byte[]</code> from its encoding as an array of
     * elements in the group, i.e., the output of
     * {@link #encode(byte[],RandomSource)}.
     *
     * @param elements Elements to be decoded.
     * @return Decoded data.
     */
    public byte[] decode(final PGroupElement[] elements) {
        final byte[] tmp = new byte[elements.length * getEncodeLength()];

        int j = 0;
        for (int i = 0; i < elements.length; i++) {
            j += elements[i].decode(tmp, j);
        }
        return Arrays.copyOfRange(tmp, 0, j);
    }

    /**
     * Returns a byte tree representation of the input.
     *
     * @param array Array to represent.
     * @return Representation of the input array.
     */
    public ByteTreeBasic toByteTree(final PGroupElement[] array) {
        final ByteTreeBasic[] byteTrees = new ByteTreeBasic[array.length];
        for (int i = 0; i < byteTrees.length; i++) {
            byteTrees[i] = array[i].toByteTree();
        }
        return new ByteTreeContainer(byteTrees);
    }

    /**
     * Recovers a <code>PGroupElement[]</code> from the given
     * representation.
     *
     * @param maxSize Maximal number of elements read.
     * @param btr Representation of an array of elements.
     * @return Array of group elements represented by the input.
     *
     * @throws ArithmFormatException If the input does not represent a
     * <code>PGroupElement[]</code>.
     */
    public PGroupElement[] toElements(final int maxSize,
                                      final ByteTreeReader btr)
        throws ArithmFormatException {
        try {
            if (btr.getRemaining() > maxSize) {
                throw new ArithmFormatException("Too many elements!");
            }
            final PGroupElement[] res = new PGroupElement[btr.getRemaining()];
            for (int i = 0; i < res.length; i++) {
                res[i] = toElement(btr.getNextChild());
            }
            return res;
        } catch (final EIOException eioe) {
            throw new ArithmFormatException("Malformed ByteTree!", eioe);
        }
    }

    /**
     * Shifts all elements one step to the right (deleting the last
     * element) and sets the given element as the first element of the
     * array. This is a linear-time operation.
     *
     * @param array Original array of elements.
     * @param el First element in resulting array.
     * @return Resulting array.
     */
    public PGroupElement[] shiftPush(final PGroupElement[] array,
                                     final PGroupElement el) {
        final PGroupElement[] res = new PGroupElement[array.length];
        res[0] = el;
        System.arraycopy(array, 0, res, 1, array.length - 1);
        return res;
    }

    /**
     * Performs an element-wise equality test of this instance and the
     * input and outputs the results of the tests as an array of
     * boolean.
     *
     * @param left Array of group elements.
     * @param right Array of group elements.
     * @return Array of equality testing results.
     */
    public boolean[] equalsAll(final PGroupElement[] left,
                               final PGroupElement[] right) {
        final boolean[] res = new boolean[left.length];
        for (int i = 0; i < left.length; i++) {
            res[i] = left[i].equals(right[i]);
        }
        return res;
    }

    /**
     * Recovers an array of group elements from the given
     * representation. This does not necessarily perform complete
     * verifications.
     *
     * @param size Number of elements in array.
     * @param btr Representation of array.
     * @return Array of group elements.
     *
     * @throws ArithmError If the input does not represent an array of
     * group elements.
     */
    public PGroupElementArray unsafeToElementArray(final int size,
                                                   final ByteTreeReader btr)
        throws ArithmError {
        final String s = "Failed to read element array!";
        try {
            return toElementArray(size, btr);
        } catch (final ArithmFormatException afe) {
            throw new ArithmError(s, afe);
        }
    }

    /**
     * Returns a representation of an array of arrays of group
     * elements.
     *
     * @param pea Representation of array of arrays of group elements.
     * @return Representation of array of arrays of group elements.
     */
    public ByteTreeContainer toByteTree(final PGroupElementArray[] pea) {
        final ByteTreeBasic[] btb = new ByteTreeBasic[pea.length];

        for (int i = 0; i < pea.length; i++) {
            btb[i] = pea[i].toByteTree();
        }
        return new ByteTreeContainer(btb);
    }

    /**
     * Verifies that all instances are associated to the same instance
     * <code>PGroup</code>.
     *
     * @param els Group associated instances to be tested.
     * @return true or false depending on if all elements are
     * compatible or not.
     */
    public static boolean compatible(final PGroupAssociated... els) {
        if (els.length == 0) {
            return true;
        } else {
            final PGroup pGroup = els[0].getPGroup();
            for (int i = 1; i < els.length; i++) {
                if (!els[i].getPGroup().equals(pGroup)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Verifies that all <code>PGroupAssociated</code> inputs are
     * associated with the same <code>PGroup</code> instance, and that
     * the <code>PRingAssociated</code> instance is associated with
     * the <code>PRing</code> instance of this group instance.
     *
     * @param x Ring associated instance to be tested.
     * @param els Group associated instances to be tested.
     * @return true or false depending on if all elements are
     * compatible or not.
     */
    public static boolean compatible(final PRingAssociated x,
                                     final PGroupAssociated... els) {
        return compatible(els)
            && (els.length == 0
                || x.getPRing().equals(els[0].getPGroup().getPRing()));
    }

    // Documented in PRingAssociated.java

    @Override
    public PRing getPRing() {
        return pRing;
    }
}
