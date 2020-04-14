
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

import com.verificatum.eio.ByteTreeBasic;

/**
 * Restriction of a bilinear map to a homomorphism.
 *
 * @author Douglas Wikstrom
 */
public final class HomPRingPGroupRest implements HomPRingPGroup {

    /**
     * Underlying bilinear map.
     */
    final BiPRingPGroup bi;

    /**
     * Restriction inducing a homomorphism.
     */
    final PGroupElement restriction;

    /**
     * Creates the homomorphism from the given restriction.
     *
     * @param bi Underlying bilinear map.
     * @param restriction Restriction of the bilinear map.
     */
    public HomPRingPGroupRest(final BiPRingPGroup bi,
                              final PGroupElement restriction) {
        this.bi = bi;
        this.restriction = restriction;
    }

    // Documented in HomPRingPGroup.java

    @Override
    public PRing getDomain() {
        return bi.getPRingDomain();
    }

    @Override
    public PGroup getRange() {
        return bi.getRange();
    }

    @Override
    public PGroupElement map(final PRingElement element) {
        return bi.map(element, restriction);
    }

    @Override
    public ByteTreeBasic toByteTree() {
        return restriction.toByteTree();
    }
}
