
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

/**
 * Abstract class for a bilinear map from the product of a
 * {@link PRing} and {@link PGroup} to a {@link PGroup}.
 *
 * @author Douglas Wikstrom
 */
public abstract class BiPRingPGroup {

    /**
     * Returns the ring part of the domain of this map.
     *
     * @return Ring part of the domain of this map.
     */
    public abstract PRing getPRingDomain();

    /**
     * Returns the group part of the domain of this map.
     *
     * @return Group part of the domain of this map.
     */
    public abstract PGroup getPGroupDomain();

    /**
     * Returns the range of this map.
     *
     * @return Range of this map.
     */
    public abstract PGroup getRange();

    /**
     * Evaluates the map at the given point.
     *
     * @param ringElement Ring element input.
     * @param groupElement Group element input.
     * @return Value of the map at the given point.
     */
    public abstract PGroupElement map(PRingElement ringElement,
                                      PGroupElement groupElement);

    /**
     * Returns the homomorphism resulting from restricting this
     * bilinear map.
     *
     * @param groupElement Restriction of this bilinear map.
     * @return Resulting homomorphism.
     */
    public HomPRingPGroup restrict(final PGroupElement groupElement) {
        return new HomPRingPGroupRest(this, groupElement);
    }

    /**
     * Returns the homomorphism resulting from restricting this
     * bilinear map.
     *
     * @param ringElement Restriction of this bilinear map.
     * @return Resulting homomorphism.
     */
    public HomPGroupPGroup restrict(final PRingElement ringElement) {
        return new HomPGroupPGroupRest(this, ringElement);
    }
}
