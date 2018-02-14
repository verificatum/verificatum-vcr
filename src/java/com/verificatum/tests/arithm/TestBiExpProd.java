
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
import com.verificatum.arithm.BiExp;
import com.verificatum.arithm.BiExpProd;
import com.verificatum.arithm.ModPGroup;
import com.verificatum.arithm.PGroupElement;
import com.verificatum.arithm.PPGroupElement;
import com.verificatum.arithm.PRingElement;
import com.verificatum.arithm.PPRingElement;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link BiExp}.
 *
 * @author Douglas Wikstrom
 */
public class TestBiExpProd extends TestBiPRingPGroup {

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestBiExpProd(final TestParameters tp)
        throws ArithmFormatException {
        super(tp, new ModPGroup(512), new ModPGroup(640),
              new BiExpProd(new ModPGroup(512), 2));
    }

    @Override
    protected PGroupElement naiveMap(final PRingElement e,
                                     final PGroupElement b) {
        final PRingElement[] es = ((PPRingElement) e).getFactors();
        final PGroupElement[] bs = ((PPGroupElement) b).getFactors();

        PGroupElement res = bs[0].getPGroup().getONE();
        for (int i = 0; i < bs.length; i++) {
            res = res.mul(bs[i].exp(es[i]));
        }
        return res;
    }
}
