
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

package com.verificatum.arithm;

import java.math.BigInteger;

/**
 * Implements primality tests and safe-primality tests such that the
 * caller provides the randomness. Admittedly, this is an exercise in
 * paranoia.
 *
 * @author Douglas Wikstrom
 */
public class MillerRabin {

    /**
     * Decides if we are checking primality or safe primality.
     */
    protected boolean primality;

    /**
     * Current integer that is tested.
     */
    protected BigInteger n;

    /**
     * Stores result of trial divisions.
     */
    protected boolean trialStatus;

    /**
     * Integer constant one.
     */
    public static final BigInteger ONE = BigInteger.ONE;

    /**
     * Integer constant two.
     */
    public static final BigInteger TWO = ONE.add(ONE);

    /**
     * Integer constant four.
     */
    public static final BigInteger FOUR = TWO.add(TWO);

    /**
     * Initializes the Miller-Rabin test for the given integers.
     * Please use the method {@link #trial()} and read the comment.
     *
     * @param n Integer to test.
     * @param primality Decides if we are checking primality or safe
     * primality.
     * @param search Decides if we are searching for an integer or
     * testing.
     */
    public MillerRabin(final BigInteger n, final boolean primality,
                       final boolean search) {

        this.n = n;
        this.primality = primality;

        if (search) {

            nextCandidate();

        } else {

            if (primality) {
                trialStatus = MillerRabinTrial.trial(n);
            } else {
                trialStatus = MillerRabinTrial.safeTrial(n);
            }
        }
    }

    /**
     * Returns the result of the trial divisions.
     * {@link #once(BigInteger)} or {@link #done()} must not be called
     * if this function returns false. Note that if this instance is
     * created for searching, this will always return
     * <code>true</code>, since the constructor in that case moves to
     * the first candidate integer that passes trial divisions.
     *
     * @return Returns <code>true</code> or <code>false</code>
     *         depending on if the integer is found not to be a
     *         candidate after trial divisions.
     */
    public final boolean trial() {
        return trialStatus;
    }

    /**
     * Increases the integer to the next candidate prime, or safe
     * prime, depending on how this instance was created a candidate
     * prime passes all trial divisions.
     */
    public final void nextCandidate() {

        // Next prime.
        if (primality) {

            // Add one if n is even and two otherwise.
            if (n.testBit(0)) {
                n = n.add(TWO);
            } else {
                n = n.add(ONE);
            }

            // Add two as long as trial divisions show that n is
            // composite.
            while (!MillerRabinTrial.trial(n)) {
                n = n.add(TWO);
            }

            // Next safe prime.
        } else {

            boolean increased = false;

            // Add one if n is even.
            if (!n.testBit(0)) {
                n = n.add(ONE);
                increased = true;
            }

            // Add two if (n-1)/2 is even.
            if (!n.testBit(1)) {
                n = n.add(TWO);
                increased = true;
            }

            // If both n and (n-1)/2 were already odd, then we add
            // four.
            if (!increased) {
                n = n.add(FOUR);
            }

            // Then we add four until we find a candidate that passes
            // the trial divisions.
            for (;;) {
                trialStatus = MillerRabinTrial.safeTrial(n);
                if (trialStatus) {
                    return;
                } else {
                    n = n.add(FOUR);
                }
            }
        }
    }

    /**
     * Returns the current candidate.
     *
     * @return Current candidate.
     */
    public final BigInteger getCurrentCandidate() {
        return n;
    }

    /**
     * Perform one Miller-Rabin test using the given base and modulus.
     *
     * @param base Base used in testing.
     * @param n Modulus.
     * @return <code>false</code> if the integer is not prime and
     *         <code>true</code> otherwise.
     */
    protected static final boolean once(final BigInteger base,
                                        final BigInteger n) {

        if (n.compareTo(BigInteger.valueOf(4)) < 0) {
            return n.compareTo(BigInteger.valueOf(1)) > 0;
        }

        final BigInteger nMinusOne = n.subtract(ONE);
        final int k = nMinusOne.getLowestSetBit();
        final BigInteger q = nMinusOne.shiftRight(k);

        BigInteger y = base.modPow(q, n);

        if (y.equals(ONE) || y.equals(n.subtract(ONE))) {
            return true;
        }

        for (int i = 1; i < k; i++) {

            y = y.modPow(TWO, n);

            if (y.equals(ONE)) {
                return false;
            }
            if (y.equals(n.subtract(ONE))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Perform one Miller-Rabin test using the given base.
     *
     * @param base Base used in testing.
     * @return <code>false</code> if the integer is not prime and
     *         <code>true</code> otherwise.
     */
    public final boolean once(final BigInteger base) {
        return once(base, n);
    }

    /**
     * Perform one Miller-Rabin test using the given base.
     *
     * @param base Base used in testing.
     * @param index Determines if Miller-Rabin is executed on the
     * tested integer <i>n</i> or <i>(n-1)/2</i>.
     * @return <code>false</code> if the integer is not prime and
     *         <code>true</code> otherwise.
     */
    public final boolean once(final BigInteger base, final int index) {

        if (index == 0) {

            return once(base, n);

        } else {

            return once(base, n.subtract(ONE).shiftRight(1));

        }
    }

    /**
     * Releases resources allocated for testing. This must be called
     * after testing is completed, but it must not be called if
     * {@link #trial()} returns 0.
     */
    public void done() {
    }
}
