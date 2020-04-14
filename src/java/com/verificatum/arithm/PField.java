
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

import java.util.Map;
import java.util.TreeMap;

import com.verificatum.annotation.CoberturaIgnore;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeReader;


/**
 * Implements an immutable field of prime order, i.e., arithmetic
 * modulo a prime. The elements of the field are implemented by the
 * class {@link PFieldElement}.
 *
 * @author Douglas Wikstrom
 */
public final class PField extends PRing {

    /**
     * Error message used when fields to do not match.
     */
    public static final String MISMATCHING_FIELDS = "Mismatching fields!";

    /**
     * Makes sure that every field with the same order uses the same
     * instance of {@link LargeInteger} to represent it. This speeds
     * up later initializations and checking for equal fields, since
     * it can be done by comparing references.
     */
    private static final Map<LargeInteger, LargeInteger> ORDERS;

    static {
        ORDERS = new TreeMap<LargeInteger, LargeInteger>();
    }

    /**
     * Order of the field.
     */
    LargeInteger order;

    /**
     * Fixed number of bytes needed to store the order.
     */
    int orderByteLength;

    /**
     * Fixed number of bytes needed to injectively map elements to
     * byte[].
     */
    int byteLength;

    /**
     * Zero element of the field.
     */
    public final PFieldElement ZERO;

    /**
     * Unit element of the field.
     */
    public final PFieldElement ONE;

    /**
     * Creates an empty uninitialized instance. It is the
     * responsibility of the programmer to initialize this field
     * before usage.
     */
    PField() {
        ZERO = new PFieldElement(this, LargeInteger.ZERO);
        ONE = new PFieldElement(this, LargeInteger.ONE);
    }

    /**
     * Initializes the order of this instance without checking that
     * the order is a positive prime.
     *
     * @param order Order of this field.
     */
    void unsafeInit(final LargeInteger order) {
        this.order = order;
        this.orderByteLength = order.toByteArray().length;
        this.byteLength = ONE.toByteArray().length;
    }

    /**
     * Creates a field with the given order.
     *
     * @param order Order of the field.
     */
    public PField(final LargeInteger order) {
        this();
        unsafeInit(order);
    }

    /**
     * Initializes the order of this instance.
     *
     * @param order Order of this field.
     * @param rs Random source used to probabilistically check
     * primality.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code></sup>.
     *
     * @throws ArithmFormatException If the order is not a positive
     * prime.
     */
    protected void init(final LargeInteger order,
                        final RandomSource rs,
                        final int certainty)
        throws ArithmFormatException {

        LargeInteger trueOrder = order;

        if (ORDERS.containsKey(trueOrder)) {

            // This ensures that all fields with the same order
            // represent their order using the same instance of
            // LargeInteger.
            trueOrder = ORDERS.get(trueOrder);

        } else {

            if (trueOrder.compareTo(LargeInteger.ZERO) <= 0) {
                throw new ArithmFormatException("Non-positive order!");
            }

            if (!trueOrder.isProbablePrime(rs, certainty)) {
                throw new ArithmFormatException("Non-prime order!");
            }
            ORDERS.put(trueOrder, trueOrder);
        }
        unsafeInit(trueOrder);
    }

    /**
     * Creates a field with the given order.
     *
     * @param order Order of the field.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with
     * probability at most 2<sup>- <code>certainty</code></sup>.
     *
     * @throws ArithmFormatException If the modulus is not a positive
     * prime number.
     */
    public PField(final LargeInteger order,
                  final RandomSource rs,
                  final int certainty)
        throws ArithmFormatException {
        this();
        init(order, rs, certainty);
    }

    /**
     * Returns the order of the field.
     *
     * @return Order of this field.
     */
    @CoberturaIgnore
    public LargeInteger getOrder() {
        return order;
    }

    /**
     * Returns an array containing the canonical integer
     * representative of each field element in the input. This assumes
     * that all elements in the input belong to the same field.
     *
     * @param elements Array of field elements.
     * @return Array containing the canonical integer representative
     * of each field element in the input.
     */
    public LargeInteger[] toLargeIntegers(final PRingElement[] elements) {
        if (elements.length == 0) {

            return new LargeInteger[0];

        } else if (equals(elements[0].getPRing())) {

            final LargeInteger[] res = new LargeInteger[elements.length];

            for (int i = 0; i < elements.length; i++) {
                res[i] = ((PFieldElement) elements[i]).toLargeInteger();
            }
            return res;

        } else {
            throw new ArithmError("Elements of foreign field!");
        }
    }

    /**
     * Returns an array containing the canonical integer
     * representative of each field element in the input. This assumes
     * that all elements in the input belongs to the same field.
     *
     * @param elements Array of field elements.
     * @return Array containing the canonical integer representative
     * of each field element in the input.
     */
    public LargeIntegerArray
        toLargeIntegerArray(final PRingElementArray elements) {
        if (equals(elements.getPRing())) {
            return ((PFieldElementArray) elements).values;
        } else {
            throw new ArithmError("Elements of foreign field!");
        }
    }

    /**
     * Returns an element corresponding to the equivalence class of
     * the input integer.
     *
     * @return Field element corresponding to the input
     * representative.
     * @param li Representative of a field element.
     */
    @CoberturaIgnore
    public PFieldElement toElement(final LargeInteger li) {
        return new PFieldElement(this, li.mod(order));
    }

    /**
     * Returns an element corresponding to the equivalence class of
     * the input integer.
     *
     * @param i Non-negative integer representative of a field
     * element.
     * @return Field element corresponding to the input.
     */
    @CoberturaIgnore
    public PFieldElement toElement(final int i) {
        return toElement(new LargeInteger(i));
    }

    /**
     * Returns an array of the field elements corresponding to the
     * integers in the input.
     *
     * @param integers Integer representatives of field elements.
     * @return Field elements corresponding to the input integers.
     */
    @CoberturaIgnore
    public PFieldElementArray toElementArray(final LargeIntegerArray integers) {
        final LargeIntegerArray reduced = integers.mod(order);
        return new PFieldElementArray(this, reduced);
    }

    /**
     * Returns an array of the field elements corresponding to the
     * integers in the input. WARNING! This method does not reduce the
     * integers in the input to canonical representatives and simply
     * absorbs the input array. Do not call
     * {@link LargeIntegerArray#free()} directly on the input array.
     *
     * @param integers Integer representatives of field elements.
     * @return Field elements corresponding to the input integers.
     */
    @CoberturaIgnore
    public PFieldElementArray
        unsafeToElementArray(final LargeIntegerArray integers) {
        return new PFieldElementArray(this, integers);
    }

    // Documented in PRing.java

    @CoberturaIgnore
    @Override
    public PField getPField() {
        return this;
    }

    @CoberturaIgnore
    @Override
    public PFieldElement getZERO() {
        return ZERO;
    }

    @CoberturaIgnore
    @Override
    public PFieldElement getONE() {
        return ONE;
    }

    @CoberturaIgnore
    @Override
    public int getByteLength() {
        return byteLength;
    }

    @CoberturaIgnore
    @Override
    public String toString() {
        return order.toString(16);
    }

    @CoberturaIgnore
    @Override
    public int getEncodeLength() {
        return (order.bitLength() - 1) / 8;
    }

    @Override
    public PFieldElement toElement(final ByteTreeReader btr)
        throws ArithmFormatException {
        return new PFieldElement(this, btr);
    }

    @Override
    public PFieldElement toElement(final byte[] bytes) {
        return new PFieldElement(this,
                                 LargeInteger.toPositive(bytes).mod(order));
    }

    @Override
    public PFieldElement toElement(final byte[] bytes,
                                   final int offset,
                                   final int length) {
        return new PFieldElement(this,
                                 LargeInteger.toPositive(bytes,
                                                         offset,
                                                         length).mod(order));
    }

    @Override
    public PFieldElement randomElement(final RandomSource rs,
                                       final int statDist) {
        return new PFieldElement(this, new LargeInteger(order, statDist, rs));
    }

    @Override
    public PFieldElementArray randomElementArray(final int size,
                                                 final RandomSource rs,
                                                 final int statDist) {
        return new PFieldElementArray(this, size, rs, statDist);
    }

    @Override
    public PFieldElementArray
        toElementArray(final PRingElement[] elements) {
        return new PFieldElementArray(this, elements);
    }

    @Override
    public PFieldElementArray toElementArray(final int size,
                                             final PRingElement element) {
        final LargeIntegerArray values =
            LargeIntegerArray.fill(size, ((PFieldElement) element).value);
        return new PFieldElementArray(this, values);
    }

    @Override
    public PFieldElementArray toElementArray(final int size,
                                             final ByteTreeReader btr)
        throws ArithmFormatException {
        return new PFieldElementArray(this, size, btr);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PField)) {
            return false;
        }
        final PField pField = (PField) obj;
        return pField.order.equals(order);
    }

    @CoberturaIgnore
    @Override
    public boolean contains(final PRingElement el) {
        return equals(el.getPRing());
    }

    // Documented in ByteTreeConvertible.java.

    @CoberturaIgnore
    @Override
    public ByteTree toByteTree() {
        return new ByteTree(order.toByteArray());
    }
}
