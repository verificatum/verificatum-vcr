
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

package com.verificatum.tests.crypto;

import com.verificatum.crypto.CryptoFormatException;
import com.verificatum.crypto.RandomDevice;
import com.verificatum.crypto.RandomSource;
import com.verificatum.crypto.RandomSourceCombiner;
import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.Marshalizer;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;
import com.verificatum.test.TestClass;


/**
 * Tests {@link RandomSourceCombiner}.
 *
 * @author Douglas Wikstrom
 */
// PMD_ANNOTATION @SuppressWarnings("PMD.SignatureDeclareThrowsException")
public final class TestRandomSourceCombiner extends TestClass {

    /**
     * Combiner used for testing.
     */
    final RandomSource combiner;

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     */
    public TestRandomSourceCombiner(final TestParameters tp) {
        super(tp);

        this.combiner = new RandomSourceCombiner(new RandomDevice(),
                                                 new RandomDevice());
    }

    /**
     * Generate.
     */
    public void constructors() {

        ByteTree bt =
            new ByteTree(new byte[RandomSourceCombiner.MAX_RND_SOURCES + 1]);

        boolean invalid = false;
        try {
            new RandomSourceCombiner(bt.getByteTreeReader());
        } catch (final CryptoFormatException cfe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on too many sources!";


        bt = new ByteTree(new byte[RandomSourceCombiner.MAX_RND_SOURCES]);
        invalid = false;
        try {
            new RandomSourceCombiner(bt.getByteTreeReader());
        } catch (final CryptoFormatException cfe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on bad byte tree!";
    }

    /**
     * Generate.
     */
    public void generate() {

        final Timer timer = new Timer(testTime);

        int size = 1;

        while (!timer.timeIsUp()) {

            combiner.getBytes(size);

            size++;
        }
    }

    /**
     * Verify conversion to byte tree.
     *
     * @throws Exception If a test fails.
     */
    public void marshal() throws Exception {

        final ByteTreeBasic bt = Marshalizer.marshal(combiner);
        final RandomSource combiner2 =
            Marshalizer.unmarshal_RandomSource(bt.getByteTreeReader());

        combiner.getBytes(100);
        combiner2.getBytes(100);
    }

    /**
     * Exercise human description.
     */
    public void excHumanDescription() {
        combiner.humanDescription(true);
    }
}
