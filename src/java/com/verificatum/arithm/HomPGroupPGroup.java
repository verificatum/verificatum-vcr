
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

import com.verificatum.eio.ByteTreeConvertible;

/**
 * Interface for a homomorphism from a {@link PGroup} to a
 * {@link PGroup}.
 *
 * @author Douglas Wikstrom
 */
public interface HomPGroupPGroup extends ByteTreeConvertible {

    /**
     * Returns the domain of this homomorphism.
     *
     * @return Domain of this homomorphism.
     */
    PGroup getDomain();

    /**
     * Returns the range of this homomorphism.
     *
     * @return Range of this homomorphism.
     */
    PGroup getRange();

    /**
     * Evaluates the homomorphism at the given point.
     *
     * @param element Point of evaluation.
     * @return Value of the homomorphism at the given point.
     */
    PGroupElement map(PGroupElement element);
}
