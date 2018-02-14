
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

import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.gen.GenException;
import com.verificatum.ui.gen.Generator;
import com.verificatum.ui.gen.GeneratorTool;
import com.verificatum.ui.opt.Opt;

/**
 * Generates a human oriented string representation of a
 * <code>ModPGroup</code> suitable for initialization files. Using
 * {@link com.verificatum.ui.gen.GeneratorTool} this functionality can be
 * invoked from the command line.
 *
 * @author Douglas Wikstrom
 */
public final class ModPGroupGen implements Generator {

    /**
     * Default certainty used to check primality of safe prime moduli.
     */
    static final int CERTAINTY = 50;

    /**
     * Generates an option instance containing suitable options and a
     * general description.
     *
     * @return Option instance representing valid inputs to this
     * instance.
     */
    protected Opt opt() {
        final Opt opt =
            GeneratorTool.defaultOpt(ModPGroup.class.getSimpleName(), 3);

        opt.setUsageComment("(where " + ModPGroup.class.getSimpleName() + " = "
                            + ModPGroup.class.getName() + ")");

        opt.addParameter("modulus", "Modulus in hexadecimal twos complement "
                         + "representation.");
        opt.addParameter("order",
                         "Order of group in hexadecimal twos complement "
                         + "representation.");
        opt.addParameter("generator", "Generator of the group in hexadecimal.");
        opt.addParameter("bitLen", "Bit-length of modulus.");
        opt.addParameter("obitLen", "Bit-length of order.");
        opt.addOption("-cert", "value", "Certainty in primality testing, "
                      + "i.e., a value of t gives a probability of an "
                      + "incorrect group of at most 2^(-t).");
        opt.addOption("-fixed", "",
                      "Fixed safe-prime modulus of given size from a table "
                      + "in the class " + SafePrimeTable.class.getName()
                      + ".");
        opt.addOption("-explic", "", "Explicit modulus.");
        opt.addOption("-rand", "", "Random modulus of given size.");

        opt.addOption("-roenc", "", "Use random encoding of messages. At most "
                      + ModPGroup.RO_MAX_NO_BYTES
                      + " bytes can be encoded in each group element using "
                      + "this scheme.");

        final String s =
            "Generates a subgroup of prime order q of the multiplicative "
            + "group modulo a prime p=kq+1. The simplest way to generate a "
            + "suitable group is to use, e.g.:"
            + "\n\n"
            + "   vog -gen ModPGroup -fixed 2048"
            + "\n\n"
            + "This gives a 2048-bit safe prime modulus (k = 2) derived from "
            + "the bitlength 2048 using SHA-256. It is also possible to "
            + "generate a group with explicit parameters. If a non-safe "
            + "prime (k > 2) is used, you need to specify the group order as "
            + "well. A random group can be generated as well. Specify the "
            + "<obitLen> parameter to generate a non-safe prime modulus. "
            + "The bit-length of the order must be at least "
            + (SafePrimeTable.MIN_BIT_LENGTH - 1)
            + "."
            + "\n\n"
            + "Three different encodings of messages are used:"
            + "\n\n"
            + "1) For safe prime moduli, a message m is encoded as m or -m "
            + "depending on which is a quadratic residue. The message is "
            + "recovered from a group element u by taking the smallest "
            + "integer of u and p-u."
            + "\n\n"
            + "2) For non-safe prime moduli where the order of the subgroup "
            + "is almost as big as the modulus, the message is padded until "
            + "the result is in the subgroup."
            + "\n\n"
            + "3) For non-safe prime moduli where the order of the subgroup "
            + "is much smaller than the modulus, a SHA-256 based \"random\" "
            + "mapping from the set of group elements to the set of messages "
            + "is used. Only very short messages can be encoded in this way, "
            + "but it is useful in "
            + "electronic voting.";

        opt.appendToUsageForm(1,
                              "-explic#-cert,-roenc#modulus,generator#order");
        opt.appendToUsageForm(2, "-fixed##bitLen#");
        opt.appendToUsageForm(3, "-rand#-cert,-roenc#bitLen#obitLen");

        opt.appendDescription(s);

        return opt;
    }

    /**
     * Derive an instance from a set of explicit parameters.
     *
     * @param randomSource Source of randomness.
     * @param certainty Determines the probability of accepting
     * invalid parameters.
     * @param opt Containiner of values parsed from command line.
     * @return Subgroup of multiplicative group specified by the input
     * parameters.
     * @throws GenException If the parameters contained in the option
     * container do not represent a valid subgroup of the
     * multiplicative group.
     */
    private ModPGroup explicModPGroup(final RandomSource randomSource,
                                      final int certainty,
                                      final Opt opt)
        throws GenException {

        if (randomSource == null) {
            throw new GenException("The random source is null!");
        }

        // Read modulus and generator.
        final String hexModulus = opt.getStringValue("modulus");
        final LargeInteger modulus = new LargeInteger(hexModulus, 16);

        // Read order if present and define it as (modulus-1)/2
        // otherwise.
        LargeInteger order = null;
        if (opt.valueIsGiven("order")) {

            final String hexOrder = opt.getStringValue("order");
            order = new LargeInteger(hexOrder, 16);

        } else {

            try {
                order = modulus.sub(LargeInteger.ONE).divide(LargeInteger.TWO);
            } catch (final ArithmException ae) {
                throw new ArithmError("This is a bug!", ae);
            }
        }

        // We default to safe prime encoding even if the order is
        // given.
        final LargeInteger modulusCandidate =
            order.mul(LargeInteger.TWO).add(LargeInteger.ONE);

        int encoding;
        if (modulusCandidate.equals(modulus)) {
            encoding = ModPGroup.SAFEPRIME_ENCODING;
        } else {
            encoding = ModPGroup.RO_ENCODING;
        }

        // We may override the encoding.
        if (opt.getBooleanValue("-roenc")) {
            encoding = ModPGroup.RO_ENCODING;
        }

        if (order.bitLength() < SafePrimeTable.MIN_BIT_LENGTH - 1) {
            final String s =
                "Bit-length of order must be at least "
                + (SafePrimeTable.MIN_BIT_LENGTH - 1) + "!";
            throw new GenException(s);
        }

        final String hexGenerator = opt.getStringValue("generator");
        final LargeInteger generator = new LargeInteger(hexGenerator, 16);

        // Attempt to construct group. This verifies the parameters.
        try {
            return new ModPGroup(modulus,
                                 order,
                                 generator,
                                 encoding,
                                 randomSource,
                                 certainty);
        } catch (final ArithmFormatException afe) {
            throw new GenException(afe.getMessage(), afe);
        }
    }

    /**
     * Returns the fixed prime of the given bit length.
     *
     * @param bitLen Bit length of fixed instance.
     * @return Subgroup of multiplicative group specified by the bit
     * length parameters.
     * @throws ArithmFormatException If there is no tabulated safe
     * prime with the given number of bits.
     * @throws GenException If there is no tabulated safe prime with
     * the given number of bits.
     */
    private ModPGroup fixedModPGroup(final int bitLen)
        throws ArithmFormatException, GenException {

        // Construct safe prime group based on modulus from table.
        if (bitLen < SafePrimeTable.MIN_BIT_LENGTH) {
            final String s =
                "Minimal bitlength of fixed modulus is "
                + SafePrimeTable.MIN_BIT_LENGTH + "!";
            throw new GenException(s);
        }
        if (bitLen >= SafePrimeTable.MAX_BIT_LENGTH) {
            final String s =
                "Maximal bitlength of fixed modulus is "
                + (SafePrimeTable.MAX_BIT_LENGTH - 1) + "!";
            throw new GenException(s);
        }
        return new ModPGroup(bitLen);
    }

    /**
     * Derive an instance from a set of explicit parameters.
     *
     * @param bitLen Bit length of modulus of generated instance.
     * @param randomSource Source of randomness.
     * @param certainty Determines the probability of accepting
     * invalid parameters.
     * @param opt Containiner of values parsed from command line.
     * @return Random subgroup of a multiplicative group of the given
     * bit length.
     * @throws ArithmFormatException If no safe prime can be
     * generateed with the given mumber of bits.
     * @throws GenException If no safe prime can be generateed with
     * the given mumber of bits.
     */
    private ModPGroup randomModPGroup(final int bitLen,
                                      final RandomSource randomSource,
                                      final int certainty,
                                      final Opt opt)
        throws ArithmFormatException, GenException {

        if (randomSource == null) {
            throw new GenException("The random source is null!");
        }

        if (opt.valueIsGiven("obitLen")) {

            final int obitLen = opt.getIntValue("obitLen");

            if (obitLen < SafePrimeTable.MIN_BIT_LENGTH - 1) {
                final String s =
                    "Bit-length of order must be at least "
                    + (SafePrimeTable.MIN_BIT_LENGTH - 1) + "!";
                throw new GenException(s);
            }

            if (bitLen <= obitLen) {
                final String s =
                    "Bit-length of modulus must be at least one greater "
                    + "than the bit-length of the order!";
                throw new GenException(s);
            }

            int encoding;
            if (opt.getBooleanValue("-roenc")) {
                encoding = ModPGroup.RO_ENCODING;
            } else {
                encoding = ModPGroup.SUBGROUP_ENCODING;
            }

            return new ModPGroup(bitLen,
                                 obitLen,
                                 encoding,
                                 randomSource,
                                 certainty);

        } else {

            // Construct random safe prime group derived from random
            // source.
            return new ModPGroup(bitLen, randomSource, certainty);
        }
    }

    // Documented in com.verificatum.crypto.Generator.java.

    @Override
    public String gen(final RandomSource randomSource, final String[] args)
        throws GenException {
        final Opt opt = opt();

        final String res = GeneratorTool.defaultProcess(opt, args);
        if (res != null) {
            return res;
        }

        ModPGroup pGroup = null;

        int certainty = CERTAINTY;
        if (opt.valueIsGiven("-cert")) {
            certainty = opt.getIntValue("-cert");
        }

        // Explicit modulus, generator, and perhaps order is given by
        // the user.
        if (opt.getBooleanValue("-explic")) {

            pGroup = explicModPGroup(randomSource, certainty, opt);

        } else {

            try {
                final int bitLen = opt.getIntValue("bitLen");

                if (opt.getBooleanValue("-fixed")) {

                    pGroup = fixedModPGroup(bitLen);

                // opt.getBooleanValue("-rand")
                } else {

                    pGroup =
                        randomModPGroup(bitLen, randomSource, certainty, opt);

                }
            } catch (final ArithmFormatException afe) {
                throw new GenException(afe.getMessage(), afe);
            }
        }

        return Marshalizer.marshalToHexHuman(pGroup, opt.getBooleanValue("-v"));
    }

    @Override
    public String briefDescription() {
        return "Prime order subgroup of the multiplicative group modulo "
            + "a safe prime.";
    }
}
