
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

import com.verificatum.arithm.BiExp;
import com.verificatum.arithm.ECqPGroupParams;
import com.verificatum.arithm.PHomPRingPGroup;
import com.verificatum.arithm.HomPRingPGroup;
import com.verificatum.arithm.HomPRingPGroupRest;
import com.verificatum.arithm.PGroup;
import com.verificatum.arithm.PGroupElement;
import com.verificatum.arithm.PPGroup;
import com.verificatum.arithm.PRingElement;
import com.verificatum.arithm.PPRingElement;
import com.verificatum.crypto.PRGHeuristic;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ExtIO;
import com.verificatum.test.TestClass;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link PHomPRingPGroup}.
 *
 * @author Douglas Wikstrom
 */
// PMD_ANNOTATION @SuppressWarnings("PMD.SignatureDeclareThrowsException")
public final class TestPHomPRingPGroup extends TestClass {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestPHomPRingPGroup(final TestParameters tp) {
        super(tp);
    }

    /**
     * Verifies that the homomorphism can be evaluated.
     *
     * @throws Exception If a test failed.
     */
    public void map()
        throws Exception {

        final RandomSource rs = new PRGHeuristic(ExtIO.getBytes(tp.prgseed));

        final PGroup pGroup1 = ECqPGroupParams.getECqPGroup("prime256v1");
        final PGroup pGroup2 = new PPGroup(pGroup1, 2);
        final PGroup pGroup12 = new PPGroup(pGroup1, pGroup2);

        final BiExp biExp1 = new BiExp(pGroup1);
        final BiExp biExp12 = new BiExp(pGroup12);

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PGroupElement basis1 = pGroup1.randomElement(rs, 100);
            final PGroupElement basis12 = pGroup12.randomElement(rs, 100);

            final HomPRingPGroup hom1 = new HomPRingPGroupRest(biExp1, basis1);
            final HomPRingPGroup hom12 =
                new HomPRingPGroupRest(biExp12, basis12);
            final PHomPRingPGroup hom = new PHomPRingPGroup(hom1, hom12);

            final PRingElement input = hom.getDomain().randomElement(rs, 100);
            final PGroupElement output = hom.map(input);


            // Compute output naively.

            final PRingElement input1 = ((PPRingElement) input).project(0);
            final PRingElement input12 = ((PPRingElement) input).project(1);

            final PGroupElement naiveOutput1 = basis1.exp(input1);
            final PGroupElement naiveOutput12 = basis12.exp(input12);

            final PGroupElement naiveOutput =
                ((PPGroup) hom.getRange()).product(naiveOutput1, naiveOutput12);

            assert output.equals(naiveOutput)
                : "Homomorphism maps incorrectly!";
        }
    }
}
