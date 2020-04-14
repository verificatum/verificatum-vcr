
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

package com.verificatum.tests.arithm;

import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.PGroupElement;
import com.verificatum.arithm.PGroupElementArray;
import com.verificatum.arithm.PGroupElementIterator;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link PGroupElementIterator}.
 *
 * @author Douglas Wikstrom
 */
public class TestPGroupElementIterator extends TestClass {

    /**
     * Batch used.
     */
    public static final int TEST_BATCH_SIZE = 5;

    /**
     * Group used for testing.
     */
    protected PGroup pGroup;

    /**
     * Constructs test.
     *
     * @param pGroup Group used for testing.
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    protected TestPGroupElementIterator(final PGroup pGroup,
                                        final TestParameters tp)
        throws ArithmFormatException {
        super(tp);
        this.pGroup = pGroup;
    }

    /**
     * Iterate.
     */
    protected void iterate() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        // General case.
        while (!timer.timeIsUp()) {

            final PGroupElementArray x =
                pGroup.randomElementArray(size, rs, 10);

            final PGroupElementIterator iterator = x.getIterator();

            final PGroupElement[] elements = x.elements();
            int i = 0;
            while (iterator.hasNext()) {
                final PGroupElement a = iterator.next();
                assert a.equals(elements[i])
                    : "Failed to get the right element!";
                i++;
            }
            assert iterator.next() == null
                : "Failed to indicate end by returning null!";
            iterator.close();
            assert i == x.size() : "Failed to traverse some elements!";

            size++;
        }
    }

    /**
     * Iterate.
     */
    public void iterateIM() {
        TestLargeIntegerArray.memoryBased();
        iterate();
        TestLargeIntegerArray.resetBased();
    }

    /**
     * Iterate.
     */
    public void iterateF() {
        TestLargeIntegerArray.fileBased(TEST_BATCH_SIZE);
        iterate();
        TestLargeIntegerArray.resetBased();
    }
}
