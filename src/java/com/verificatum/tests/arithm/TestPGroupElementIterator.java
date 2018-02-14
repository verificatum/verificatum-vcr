
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
