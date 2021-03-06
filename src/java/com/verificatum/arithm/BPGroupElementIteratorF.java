
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

import java.io.File;

import com.verificatum.eio.ByteTreeF;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;


/**
 * Iterator over a {@link BPGroupElementArrayF}.
 *
 * @author Douglas Wikstrom
 */
public class BPGroupElementIteratorF implements PGroupElementIterator {

    /**
     * Group to which the elements of the array belongs.
     */
    protected PGroup pGroup;

    /**
     * Underlying source of elements.
     */
    protected ByteTreeReader btr;

    /**
     * Creates an iterator reading from the given array.
     *
     * @param pGroup Group to which the elements of this array
     * belongs.
     * @param file Underlying file
     */
    public BPGroupElementIteratorF(final PGroup pGroup, final File file) {
        this.pGroup = pGroup;
        this.btr = new ByteTreeF(file).getByteTreeReader();
    }

    // Documented in PGroupElementIterator.java

    @Override
    public PGroupElement next() {
        if (btr.getRemaining() > 0) {
            try {
                return pGroup.toElement(btr.getNextChild());
            } catch (final EIOException eioe) {
                throw new ArithmError("Unable to read element!", eioe);
            } catch (final ArithmFormatException afe) {
                throw new ArithmError("Unable to read element!", afe);
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean hasNext() {
        return btr.getRemaining() > 0;
    }

    @Override
    public void close() {
        btr.close();
    }
}
