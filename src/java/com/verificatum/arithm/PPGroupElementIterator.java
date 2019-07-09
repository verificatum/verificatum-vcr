
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

/**
 * Iterator over a {@link PPGroupElementArray}.
 *
 * @author Douglas Wikstrom
 */
public final class PPGroupElementIterator implements PGroupElementIterator {

    /**
     * Underlying group.
     */
    PPGroup pPGroup;

    /**
     * Underlying iterators.
     */
    PGroupElementIterator[] iterators;

    /**
     * Creates an instance over a {@link PPGroupElementArray}.
     *
     * @param pPGroup Underlying group.
     * @param iterators Underlying iterators.
     */
    public PPGroupElementIterator(final PPGroup pPGroup,
                                  final PGroupElementIterator[] iterators) {
        this.pPGroup = pPGroup;
        this.iterators = Arrays.copyOf(iterators, iterators.length);
    }

    // Documented in PGroupElementIterator.java

    @Override
    public PGroupElement next() {
        final PGroupElement[] res = new PGroupElement[iterators.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = iterators[i].next();
        }
        if (res[0] == null) {
            return null;
        } else {
            return pPGroup.product(res);
        }
    }

    @Override
    public boolean hasNext() {
        boolean res = true;
        for (int i = 0; i < iterators.length; i++) {
            if (!iterators[i].hasNext()) {
                res = false;
            }
        }
        return res;
    }

    @Override
    public void close() {
        for (int i = 0; i < iterators.length; i++) {
            iterators[i].close();
        }
    }
}
