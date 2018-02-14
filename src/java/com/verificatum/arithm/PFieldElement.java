
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

import com.verificatum.annotation.CoberturaIgnore;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeReader;


/**
 * Implements an immutable element of a prime order field. The field
 * is implemented by {@link PField}.
 *
 * @author Douglas Wikstrom
 */
public final class PFieldElement extends PRingElement {

    /**
     * Internal canonically reduced representation of this field
     * element.
     */
    LargeInteger value;

    /**
     * Creates an instance.
     *
     * <p>
     *
     * WARNING! This constructor assumes that the integer
     * representative is already canonically reduced.
     *
     * @param pField Field to which the resulting element should
     * belong.
     * @param value Representative of the created field element.
     */
    public PFieldElement(final PField pField, final LargeInteger value) {
        super(pField);
        this.value = value;
    }

    /**
     * Creates an instance from the input representation.
     *
     * @param pField Field to which the resulting element should
     * belong.
     * @param btr A representation of an instance.
     *
     * @throws ArithmFormatException If the input does not represent a
     *  field element.
     */
    protected PFieldElement(final PField pField, final ByteTreeReader btr)
        throws ArithmFormatException {
        super(pField);

        if (btr.getRemaining() != pField.orderByteLength) {
            throw new ArithmFormatException("Incorrect length of data!");
        }
        value = new LargeInteger(pField.orderByteLength, btr);
        if (value.compareTo(LargeInteger.ZERO) < 0
            || pField.order.compareTo(value) <= 0) {
            throw new ArithmFormatException("Non-canonical representative!");
        }
    }

    /**
     * Returns the field to which this instance belongs.
     *
     * @return Field to which this instance belongs.
     */
    @CoberturaIgnore
    public PField getPField() {
        return (PField) pRing;
    }

    /**
     * Returns this instance expressed by its unique non-negative
     * representatitive smaller than the modulus of the field to which
     * it belongs.
     *
     * @return Canonical integer representative of this instance.
     */
    @CoberturaIgnore
    public LargeInteger toLargeInteger() {
        return value;
    }

    // Documented in PRingElement.java

    @CoberturaIgnore
    @Override
    public String toString() {
        return value.toString(16);
    }

    @Override
    public PFieldElement add(final PRingElement el) {
        if (pRing.equals(el.pRing)) {
            return new PFieldElement((PField) pRing,
                                     value.add(((PFieldElement) el).value)
                                     .mod(((PField) pRing).order));
        }
        throw new ArithmError("Mismatching fields!");
    }

    @Override
    public PFieldElement neg() {
        return new PFieldElement((PField) pRing,
                                 value.neg().mod(((PField) pRing).order));
    }

    @Override
    public PFieldElement mul(final PRingElement el) {
        if (pRing.equals(el.pRing)) {
            return new PFieldElement((PField) pRing,
                                     value.mul(((PFieldElement) el).value)
                                     .mod(((PField) pRing).order));
        }
        throw new ArithmError("Mismatching fields!");
    }

    @Override
    public PFieldElement inv() throws ArithmException {
        try {
            return new PFieldElement((PField) pRing,
                                     value.modInv(((PField) pRing).order));
        } catch (final ArithmException ae) {
            throw new ArithmException("Zero element is not invertible!", ae);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PFieldElement) {
            final PFieldElement el = (PFieldElement) obj;
            return pRing.equals(el.pRing) && value.equals(el.value);
        }
        return false;
    }

    // Documented in ByteTreeConvertible.java

    @Override
    public byte[] toByteArray() {
        final byte[] temp = value.toByteArray();
        final byte[] data = new byte[((PField) pRing).orderByteLength];

        final int offset = data.length - temp.length;
        Arrays.fill(data, 0, offset, (byte) 0);
        System.arraycopy(temp, 0, data, offset, temp.length);

        return data;
    }

    @Override
    public ByteTree toByteTree() {
        return new ByteTree(toByteArray());
    }
}
