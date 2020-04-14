
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
import com.verificatum.arithm.LargeInteger;
import com.verificatum.arithm.LargeIntegerArray;
import com.verificatum.arithm.PField;
import com.verificatum.arithm.PFieldElement;
import com.verificatum.arithm.PFieldElementArray;
import com.verificatum.arithm.PPRing;
import com.verificatum.arithm.SafePrimeTable;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests {@link PFieldElement}.
 *
 * @author Douglas Wikstrom
 */
public final class TestPFieldElement extends TestPRingElement {

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
    public TestPFieldElement(final TestParameters tp)
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
     * Byte tree.
     */
    public void byteArray() {

        final PFieldElement x = pField.randomElement(rs, 20);
        final LargeInteger a = x.toLargeInteger();
        final byte[] b = a.toByteArray();
        final PFieldElement y = pField.toElement(b);

        assert x.equals(y) : "Failed to create element!";

        final LargeInteger d = LargeInteger.ONE.shiftLeft((b.length - 1) * 8);
        final LargeInteger li2 = a.mod(d);

        final PFieldElement z = pField.toElement(b, 1, b.length - 1);
        final PFieldElement w = pField.toElement(li2);

        assert z.equals(w) : "Failed to create element!";
    }

    /**
     * To integer.
     */
    public void toInteger() {

        final Timer timer = new Timer(testTime);

        while (!timer.timeIsUp()) {

            final PFieldElement x = pField.randomElement(rs, 50);
            final LargeInteger a = ((PFieldElement) x).toLargeInteger();

            final PFieldElement y = pField.toElement(a);
            final LargeInteger b = ((PFieldElement) y).toLargeInteger();

            assert x.equals(y) && a.equals(b)
                : "Mapping integer to and from field element failed!";
        }
    }

    /**
     * To integers.
     */
    public void integers() {

        final Timer timer = new Timer(testTime);

        boolean invalid = false;
        int size = 1;
        while (!timer.timeIsUp()) {

            // Arrays
            final PFieldElementArray x =
                pField.randomElementArray(size, rs, 50);
            final LargeIntegerArray a = pField.toLargeIntegerArray(x);
            final PFieldElementArray y = pField.toElementArray(a);

            assert x.equals(y) : "Conversion of arrays failed!";

            // Verify failure
            invalid = false;
            try {
                pField2.toLargeIntegerArray(x);
            } catch (final ArithmError ae) {
                invalid = true;
            }
            assert invalid : "Failed to fail!";


            // Primitive arrays
            final PFieldElement[] xs = x.elements();

            assert pField.toLargeIntegers(new PFieldElement[0]).length == 0
                : "Failed to handle zero-length array!";

            // Verify failure.
            invalid = false;
            try {
                pField2.toLargeIntegers(xs);
            } catch (final ArithmError ae) {
                invalid = true;
            }
            assert invalid : "Failed to fail!";

            assert x.equals(y)
                : "Mapping integer arrays to and from element arrays failed!";

            x.free();
            y.free();
            a.free();

            size++;
        }
    }
}
