
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
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.PField;
import com.verificatum.arithm.PPRing;
import com.verificatum.arithm.SafePrimeTable;
import com.verificatum.eio.ByteTree;
import com.verificatum.test.TestParameters;


/**
 * Tests {@link PField}.
 *
 * @author Douglas Wikstrom
 */
public final class TestPField extends TestPRing {

    /**
     * Tested field.
     */
    final PField pField;

    /**
     * Other field used for failure testing.
     */
    final PField pField2;

    /**
     * Constructs test.
     *
     * @param tp Test parameters.
     * @throws ArithmFormatException If construction of the test
     * failed.
     */
    public TestPField(final TestParameters tp)
        throws ArithmFormatException {
        super(new PField(SafePrimeTable.safePrime(512)),
              new PField(SafePrimeTable.safePrime(740)),
              new PPRing(new PField(SafePrimeTable.safePrime(740)),
                         new PField(SafePrimeTable.safePrime(740))),
              tp);
        this.pField = (PField) pRing;
        this.pField2 = (PField) pRing2;
    }

    /**
     * Constructors.
     *
     * @throws ArithmFormatException If a test fails.
     */
    public void constructors()
        throws ArithmFormatException {

        // Exercise properties file.
        SafePrimeTable.safePrime(512);

        // Fail on zero modulus.
        boolean invalid = false;
        try {
            new PField(LargeInteger.ZERO, rs, 10);
        } catch (final ArithmFormatException afe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on zero order";

        // Fail on non-prime order.
        invalid = false;
        try {
            new PField(LargeInteger.ONE.shiftLeft(20), rs, 10);
        } catch (final ArithmFormatException afe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on non-prime order!";

        // Check that different constructors give the same field.
        final PField altPField = new PField(pField.getOrder());
        assert altPField.equals(pField) : "Constructors give different fields!";
    }

    /**
     * Byte tree.
     */
    public void byteTree() {

        // Fail on wrong byte length.
        final ByteTree bbt = new ByteTree(new byte[2]);
        boolean invalid = false;
        try {
            pField.toElement(bbt.getByteTreeReader());
        } catch (final ArithmFormatException afe) {
            invalid = true;
        }
        assert invalid : "Failed to fail on wrong byte length!";

        // Fail on element represented by integer outside interval.
        ByteTree bt = new LargeInteger(-1).toByteTree(pField.getByteLength());
        try {
            pField.toElement(bt.getByteTreeReader());
        } catch (final ArithmFormatException afe) {
            bt = null;
        }
        assert bt == null : "Failed to catch negative representative!";

        // Fail on element represented by integer outside interval.
        bt = pField.getOrder().toByteTree(pField.getByteLength());
        try {
            pField.toElement(bt.getByteTreeReader());
        } catch (ArithmFormatException afe) {
            bt = null;
        }
        assert bt == null : "Failed to catch too large representative!";
    }

    /**
     * Equals.
     */
    public void equality() {

        final PField pFieldAlt = new PField(pField.getOrder());
        assert pFieldAlt.equals(pField) : "Constructors give different fields!";

        assert !pField.equals(new Object()) : "Field equals object!";

        final LargeInteger prime = pField.getOrder().nextPrime(rs, 50);
        final PField pFieldOrd = new PField(prime);
        assert !pField.equals(pFieldOrd)
            : "Fields with different orders are equal!";
    }
}
