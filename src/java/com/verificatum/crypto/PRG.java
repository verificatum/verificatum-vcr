
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

package com.verificatum.crypto;

import java.io.File;
import java.io.IOException;

// We only use this source of randomness to generate a unique
// filename and never for any cryptographic purposes.
import java.security.SecureRandom;
import java.util.Random;

import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.ExtIO;
import com.verificatum.eio.Hex;


/**
 * Abstract pseudo-random generator (PRG). This differs from a
 * {@link RandomSource} that happens to be implemented by a
 * pseudo-random generator in that the seed can be set/reset and the
 * PRG is guaranteed to give the same output each time on a given
 * seed.
 *
 * @author Douglas Wikstrom
 */
public abstract class PRG extends RandomSource {

    /**
     * Number of attempts we make to grab a seed file in competition
     * with potential other threads and processes before we give up.
     */
    public static final int SEED_GRAB_ATTEMPTS = 50;

    /**
     * Number of milliseconds we wait inbetween attempts to grab the
     * seed.
     */
    public static final int SEED_GRAB_SLEEP = 300;

    /**
     * Resets the seed to the given value. The output of the PRG
     * <b>must</b> be determined by the seed, i.e., every time the
     * seed is reset the output of the PRG must be exactly the same.
     *
     * @param seed New seed.
     */
    public abstract void setSeed(byte[] seed);

    /**
     * Returns the minimum number of random seed bytes needed for this
     * PRG to remain secure (under the appropriate complexity
     * assumptions).
     *
     * @return Needed number of seed bytes.
     */
    public abstract int minNoSeedBytes();

    // Replaces documentation in io.ByteTreeConvertible.java, since
    // PRGs convert themselves in an unusual way.

    /**
     * Returns a <code>ByteTree</code> representation of this
     * instance. Typically, the instance derives a new seed from its
     * output and stores it. Thus, one should not use this method to
     * store a particular state of a PRG. To do that, a seed must be
     * stored explicitly.
     *
     * @return Representation of this instance.
     */
    @Override
    public abstract ByteTreeBasic toByteTree();

    /**
     * Reads a seed from file, resets this PRG, and replaces the seed
     * on file by a newly generated seed derived from the PRG itself.
     * This encapsulates a relatively safe way to repeatedly use seed
     * a PRG from the same file.
     *
     * <p>
     *
     * This function is not only thread safe, but also process safe in
     * the sense that multiple PRGs can be seeded from the same seed
     * file provided that renaming a file is an atomic operation in
     * the JVM on the file system. The contents of the file obviously
     * changes inbetween each use in a secure way.
     *
     * <p>
     *
     * WARNING! The seed file must remain secret. Make sure it is not
     * readable by the adversary.
     *
     * @param seedFile File containing the seed.
     * @param tmpSeedFile Temporary file name forming the basis of
     * temporary files used to implement atomic write to the seed
     * file.
     *
     * @throws CryptoException If the seed can not be read and replaced.
     */
    // PMD_ANNOTATION @SuppressWarnings("PMD.CyclomaticComplexity")
    public void setSeedReplaceStored(final File seedFile,
                                     final File tmpSeedFile)
        throws CryptoException {

        // Generate two unique temporary filenames.
        final Random random = new SecureRandom();
        final long a = random.nextLong() % (Long.MAX_VALUE / 2);
        final long b = random.nextLong() % (Long.MAX_VALUE / 2);
        final String postfix =
            String.format("%s%s", Math.abs(a), Math.abs(b));
        final File grabbed = new File(tmpSeedFile.getParent(),
                                     tmpSeedFile.getName() + "_" + postfix + 1);
        final File dumped = new File(tmpSeedFile.getParent(),
                                     tmpSeedFile.getName() + "_" + postfix + 2);

        // The idea is to use something very defensive, namely to move
        // the seed file out of the way to a randomly named file
        // before reading it. This guarantees that if we read from the
        // seed file, then no other benign process can read from it
        // even if we crash.
        //
        // First repeatedly attempt to move the seed file to the
        // uniquely named temporary name.
        int i = 0;
        while (i < SEED_GRAB_ATTEMPTS) {

            try {
                ExtIO.atomicMove(seedFile, grabbed);

            // We do not care how it fails.
            } catch (IOException ioe) {
                ioe = null;
            } catch (EIOException eioe) {
                eioe = null;
            }

            // Let us be very defensive.
            if (grabbed.exists()) {
                break;
            }
            try {
                Thread.sleep(SEED_GRAB_SLEEP);
            } catch (final InterruptedException e) {
            }
            i++;
        }
        if (i == SEED_GRAB_ATTEMPTS) {
            throw new CryptoException("Unable to grab seed file after "
                                      + i + " attempts!");
        }

        // We now have the seed in our own file that no benign party
        // knows the name of.

        // Read and set seed.
        final String seedString;
        try {
            seedString = ExtIO.readString(grabbed);
        } catch (final IOException ioe) {
            throw new CryptoException("Unable to read grabbed seed file! ("
                                      + grabbed.toString() + ")", ioe);
        }
        final byte[] seed = Hex.toByteArray(seedString);
        setSeed(seed);

        // Delete the grabbed file. This guarantees that the seed can
        // not be read by other benign processes.
        if (!grabbed.delete()) {
            throw new CryptoException("Unable to delete grabbed seed file! ("
                                      + grabbed.toString() + ")");
        }

        // Generate and write new seed to a dump file.
        final byte[] newSeed = getBytes(minNoSeedBytes());
        final String newSeedString = Hex.toHexString(newSeed);
        try {
            ExtIO.writeString(dumped, newSeedString);
        } catch (final IOException ioe) {
            throw new CryptoException("Unable to write seed to dumped file! ("
                                      + dumped.toString() + ")", ioe);
        }

        // Rename our dump file to the real seed file name. If this
        // fails, then we can not recover.
        try {
            ExtIO.atomicMove(dumped, seedFile);
        } catch (final IOException ioe) {
            throw new CryptoException("Unable to do final write to seed file! "
                                      + ioe.getMessage(), ioe);
        } catch (final EIOException eioe) {
            throw new CryptoException("Unable to do final write to seed file! "
                                      + eioe.getMessage(), eioe);
        }
    }
}
