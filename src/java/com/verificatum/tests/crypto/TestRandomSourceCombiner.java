
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
