
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

package com.verificatum.crypto;

import com.verificatum.eio.EIOException;
import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.gen.GenException;
import com.verificatum.ui.gen.Generator;
import com.verificatum.ui.gen.GeneratorTool;
import com.verificatum.ui.opt.Opt;

/**
 * Generates a human oriented string representation of a
 * <code>RandomSourceCombiner</code> suitable for initialization
 * files.
 *
 * @author Douglas Wikstrom
 */
public final class RandomSourceCombinerGen implements Generator {

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid inputs to this
     *         instance.
     */
    protected Opt opt() {
        final Opt opt =
            GeneratorTool.defaultOpt(RandomSourceCombiner.class.getSimpleName(),
                                     1);

        opt.setUsageComment("(where "
                            + RandomSourceCombiner.class.getSimpleName() + " = "
                            + RandomSourceCombiner.class.getName() + ")");

        opt.addParameter("rs", "Random source, an instance of "
                         + "com.verificatum.crypto.RandomSource.");
        opt.addParameter("rss", "Random sources, instances of "
                         + "com.verificatum.crypto.RandomSource.");

        final String s =
            "Generates a random source combiner that is simply xors "
            + "the outputs of the random sources it encapsulates.";

        opt.appendToUsageForm(1, "##rs,+rss#");

        opt.appendDescription(s);

        return opt;
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

        try {
            final String[] multi = opt.getMultiParameters();

            final RandomSource[] rs = new RandomSource[1 + multi.length];
            String rsString = opt.getStringValue("rs");
            rs[0] = Marshalizer.unmarshalHex_RandomSource(rsString);

            for (int i = 0; i < multi.length; i++) {
                rsString = multi[i];
                rs[1 + i] = Marshalizer.unmarshalHex_RandomSource(rsString);
            }

            final RandomSourceCombiner rsc = new RandomSourceCombiner(rs);

            // Make sure that what we have can be used directly.
            try {
                rsc.getBytes(1);
            } catch (final CryptoError ce) {
                throw new GenException("Unable to read random bytes from "
                                       + "combiner! If one of your random "
                                       + "sources is a PRG, then please use "
                                       + "PRGCombiner instead.", ce);
            }

            return Marshalizer
                .marshalToHexHuman(rsc, opt.getBooleanValue("-v"));
        } catch (final EIOException eioe) {
            throw new GenException("Generation failed!", eioe);
        }
    }

    @Override
    public String briefDescription() {
        return "Random source combiner.";
    }
}
