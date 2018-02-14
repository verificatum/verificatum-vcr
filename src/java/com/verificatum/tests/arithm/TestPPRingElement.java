
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

import java.util.Arrays;

import com.verificatum.arithm.ArithmError;
import com.verificatum.arithm.ArithmException;
import com.verificatum.arithm.ArithmFormatException;
import com.verificatum.arithm.PField;
import com.verificatum.arithm.PRing;
import com.verificatum.arithm.PRingElement;
import com.verificatum.arithm.PPRing;
import com.verificatum.arithm.PPRingElement;
import com.verificatum.arithm.SafePrimeTable;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link PPRingElement}.
 *
 * @author Douglas Wikstrom
 */
// PMD_ANNOTATION @SuppressWarnings("PMD.CyclomaticComplexity")
public class TestPPRingElement extends TestPRingElement {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestPPRingElement(final TestParameters tp)
        throws ArithmFormatException {
        super(TestPPRing.genPRing(512),
              TestPPRing.genPRing(640),
              new PField(SafePrimeTable.safePrime(512)),
              tp);
    }

    /**
     * Verify factoring.
     */
    public void factor() {

        final PPRing pPRingX = new PPRing(pRing, pRing);
        final PPRingElement x = (PPRingElement) pPRingX.randomElement(rs, 10);
        final PRingElement[] xs = x.getFactors();
        final PRingElement y = pPRingX.product(xs);

        assert x.equals(y) : "Failed to recover product elements from factors!";
    }

    /**
     * Equals.
     */
    public void equality() {
        super.equality();

        final PPRing pPRingX = new PPRing(pRing, 2);
        final PPRing pPRingY = new PPRing(pRing, 3);

        final PRingElement x = pPRingX.randomElement(rs, 10);
        final PRingElement y = pPRingY.randomElement(rs, 10);

        assert !x.equals(y)
            : "Inequality failed for elements from groups of diferent widths!";
    }

    /**
     * Arithmetic.
     *
     * @throws ArithmException If a test failed.
     */
    public void arithmetic()
        throws ArithmException {
        super.arithmetic();

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PPRing pPRing = new PPRing(pRing, 3);

            final PRingElement[] xs = new PRingElement[pPRing.getWidth()];
            for (int i = 0; i < xs.length; i++) {
                xs[i] = pRing.randomElement(rs, 20);
            }

            final PRingElement x = pPRing.product(xs);

            final PRingElement a = pPRing.project(0).randomElement(rs, 10);

            PPRingElement y = (PPRingElement) x.mul(a);
            for (int i = 0; i < xs.length; i++) {
                assert y.project(i).equals(xs[i].mul(a))
                    : "Failed to multiply with element from subring!";
            }

            y = (PPRingElement) x.add(a);
            for (int i = 0; i < xs.length; i++) {
                assert y.project(i).equals(xs[i].add(a))
                    : "Failed to multiply with element from subring!";
            }
        }
    }

    /**
     * Product and projection of group elements.
     */
    public void productProject() {

        boolean[] mask;

        final PPRing pPRingA = new PPRing(pRing.getPField(), pRing, pRing);
        final PRing[] pRings = pPRingA.getFactors();

        // Form element from parts.
        final PRingElement[] xs = new PRingElement[pRings.length];
        for (int i = 0; i < pRings.length; i++) {
            xs[i] = pRings[i].randomElement(rs, 10);
        }
        final PPRingElement x = pPRingA.product(xs);

        // Project to components.
        for (int i = 0; i < xs.length; i++) {
            assert x.project(i).equals(xs[i]) : "Simple projection failed!";
        }

        // Project to subset of components.
        for (int i = 1; i < 8; i++) {
            mask = new boolean[3];
            for (int j = 0; j < 3; j++) {
                mask[j] = (i & (0x1 << j)) != 1;
            }
            x.project(mask);
        }

        // Project to component using mask.
        mask = new boolean[3];
        for (int i = 0; i < 3; i++) {
            Arrays.fill(mask, false);
            mask[i] = true;
            assert x.project(mask).equals(xs[i])
                : "Projection to single position failed!";
        }

        // Fail on empty projection.
        boolean invalid = false;
        try {
            mask = new boolean[3];
            Arrays.fill(mask, false);
            x.project(mask);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on empty projection!";

        // Fail projection mask of wrong size.
        invalid = false;
        try {
            mask = new boolean[2];
            x.project(mask);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on projection mask of wrong length!";

        // Form element from parts in two ways.
        final PPRing pPRingD = new PPRing(pRing, 3);
        final PRingElement d = pRing.randomElement(rs, 10);
        final PRingElement e = pPRingD.product(d);
        final PRingElement[] ds = new PRingElement[3];
        Arrays.fill(ds, d);
        final PRingElement ee = pPRingD.product(ds);
        assert ee.equals(e) : "Implicit and explicit products disagree!";

        // Fail on wrong number of elements.
        invalid = false;
        try {
            pPRingD.product(Arrays.copyOfRange(ds, 1, 3));
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on wrong number of elements!";

        // Fail on non-power ring.
        invalid = false;
        try {
            ((PPRing) pRing).product(pRing.randomElement(rs, 10));
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on heterogeneous ring!";

        // Fail on elements from wrong ring.
        Arrays.fill(ds, pRing.getPField().randomElement(rs, 10));
        invalid = false;
        try {
            pPRingD.product(ds);
        } catch (final ArithmError ae) {
            invalid = true;
        }
        assert invalid : "Failed to fail on element from wrong ring!";
    }
}
