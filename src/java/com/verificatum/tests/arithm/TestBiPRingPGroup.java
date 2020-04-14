
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

import com.verificatum.arithm.ArithmError;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.BiExp;
import com.verificatum.arithm.BiPRingPGroup;
import com.verificatum.arithm.HomPRingPGroup;
import com.verificatum.arithm.HomPGroupPGroup;
import com.verificatum.arithm.PHomPRingPGroup;
import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.PGroupElement;
import com.verificatum.arithm.PRing;
import com.verificatum.arithm.PRingElement;
import com.verificatum.arithm.PPRing;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link BiExp}.
 *
 * @author Douglas Wikstrom
 */
public abstract class TestBiPRingPGroup extends TestClass {

    /**
     * Group used for testing.
     */
    protected PGroup pGroup;

    /**
     * Secondary group used for failure testing.
     */
    protected PGroup pGroup2;

    /**
     * Bilinear map.
     */
    protected BiPRingPGroup bi;

    /**
     * Initializes the group for testing.
     *
     * @param tp Test parameters.
     * @param pGroup Primary group used for testing.
     * @param pGroup2 Secondary group used for testing.
     * @param bi Bilinear map.
     * @throws ArithmFormatException
     */
    protected TestBiPRingPGroup(final TestParameters tp,
                                final PGroup pGroup,
                                final PGroup pGroup2,
                                final BiPRingPGroup bi) {
        super(tp);
        this.pGroup = pGroup;
        this.pGroup2 = pGroup2;
        this.bi = bi;
    }

    /**
     * Computes the map in a naive way.
     *
     * @param e Ring element parameter.
     * @param b Group element parameter.
     * @return Result of map computed naively.
     */
    protected abstract PGroupElement naiveMap(final PRingElement e,
                                              final PGroupElement b);

    /**
     * Map.
     */
    public void map() {

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PRingElement e =
                bi.getPRingDomain().randomElement(rs, 10);
            final PGroupElement b =
                bi.getPGroupDomain().randomElement(rs, 10);

            final PGroupElement x = bi.map(e, b);

            assert x.equals(naiveMap(e, b)) : "Failed to evaluate!";

            assert x.getPGroup().equals(bi.getRange()) : "Outside range!";
        }

        final PRingElement e = pGroup.getPRing().randomElement(rs, 10);
        final PRingElement e2 = pGroup2.getPRing().randomElement(rs, 10);

        final PGroupElement b = pGroup.randomElement(rs, 10);
        final PGroupElement b2 = pGroup2.randomElement(rs, 10);

        boolean invalid = false;
        try {
            bi.map(e2, b);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on invalid ring input!";

        invalid = false;
        try {
            bi.map(e, b2);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on invalid group input!";
    }

    /**
     * Restriction to homomorphism from ring.
     */
    public void restrictToPGroupDomain() {

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PRingElement e = bi.getPRingDomain().randomElement(rs, 10);
            final HomPGroupPGroup hom = bi.restrict(e);

            final PGroupElement x = hom.getDomain().randomElement(rs, 10);
            final PGroupElement y = hom.getDomain().randomElement(rs, 10);

            final PGroupElement a = hom.map(x);
            final PGroupElement b = hom.map(y);
            final PGroupElement c = hom.map(x.mul(y));

            assert c.equals(a.mul(b)) : "Restriction is not linear!";

            assert a.getPGroup().equals(hom.getRange())
                : "Outputs are in the wrong range!";

            // Exercise byte tree.
            hom.toByteTree();
        }
    }

    /**
     * Restriction to homomorphism from group.
     */
    public void restrictToPRingDomain() {

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PGroupElement e = bi.getPGroupDomain().randomElement(rs, 10);
            final HomPRingPGroup hom = bi.restrict(e);

            final PRingElement x = hom.getDomain().randomElement(rs, 10);
            final PRingElement y = hom.getDomain().randomElement(rs, 10);

            final PGroupElement a = hom.map(x);
            final PGroupElement b = hom.map(y);
            final PGroupElement c = hom.map(x.add(y));

            assert c.equals(a.mul(b)) : "Restriction is not linear!";

            assert a.getPGroup().equals(hom.getRange())
                : "Outputs are in the wrong range!";

            // Exercise byte tree.
            hom.toByteTree();
        }
    }

    /**
     * Restriction of product homomorphism from ring.
     */
    public void productRestrictToPRingDomain() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            final HomPRingPGroup[] homs = new HomPRingPGroup[size];
            for (int i = 0; i < homs.length; i++) {
                final PGroupElement e =
                    bi.getPGroupDomain().randomElement(rs, 10);
                homs[i] = bi.restrict(e);
            }
            final PHomPRingPGroup hom = new PHomPRingPGroup(homs);

            final PRingElement x = hom.getDomain().randomElement(rs, 10);
            final PRingElement y = hom.getDomain().randomElement(rs, 10);

            final PGroupElement a = hom.map(x);
            final PGroupElement b = hom.map(y);
            final PGroupElement c = hom.map(x.add(y));

            assert c.equals(a.mul(b)) : "Restriction is not linear!";

            assert a.getPGroup().equals(hom.getRange())
                : "Outputs are in the wrong range!";

            // Fail on input from wrong ring.
            boolean invalid = false;
            final PRing pRing2 = pGroup2.getPRing();
            final PPRing pPRing = new PPRing(pRing2, 2);
            try {
                hom.map(pPRing.randomElement(rs, 10));
            } catch (final ArithmError ae) {
                invalid = true;
            }
            assert invalid : "Failed to fail on invalid ring input!";

            // Exercise byte tree.
            hom.toByteTree();

            final HomPRingPGroup[] homs2 = hom.getFactors();
            final HomPRingPGroup hom2 = new PHomPRingPGroup(homs2);
            final PGroupElement a2 = hom2.map(x);

            assert a2.equals(a) : "Factors do not represent the product!";

            size++;
        }
    }
}
