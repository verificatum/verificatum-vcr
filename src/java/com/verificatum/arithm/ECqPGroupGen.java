
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

package com.verificatum.arithm;

import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.gen.GenException;
import com.verificatum.ui.gen.Generator;
import com.verificatum.ui.gen.GeneratorTool;
import com.verificatum.ui.opt.Opt;

/**
 * Generates a human oriented string representation of a
 * <code>ECqPGroup</code> suitable for initialization files. Using
 * {@link com.verificatum.ui.gen.GeneratorTool} this functionality can be
 * invoked from the command line.
 *
 * @author Douglas Wikstrom
 */
public final class ECqPGroupGen implements Generator {

    /**
     * Generates an option instance containing suitable options and a
     * general description.
     *
     * @return Option instance representing valid inputs to this
     *         instance.
     */
    protected Opt opt() {
        final Opt opt =
            GeneratorTool.defaultOpt(ECqPGroup.class.getSimpleName(), 1);

        opt.setUsageComment("(where " + ECqPGroup.class.getSimpleName() + " = "
                            + ECqPGroup.class.getName() + ")");

        final StringBuilder sb = new StringBuilder();
        sb.append("\n\n");

        final String[] curveNames = ECqPGroupParams.getCurveNames();
        sb.append(curveNames[0]);
        for (int i = 1; i < curveNames.length; i++) {
            sb.append(' ');
            sb.append(curveNames[i]);
        }

        opt.addOption("-name", "value", "Name of standardized elliptic curve "
                      + "group. The following names can be used."
                      + sb.toString() + "\n\n");

        final String s = "Generates one of the standard elliptic curve groups.";

        opt.appendToUsageForm(1, "-name###");

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

        final String name = opt.getStringValue("-name");

        PGroup pGroup = null;
        try {

            pGroup = ECqPGroupParams.getECqPGroup(name);

        } catch (final ArithmFormatException afe) {
            throw new GenException("Unknown curve name: \" + name + \"!", afe);
        }

        return Marshalizer.marshalToHexHuman(pGroup, opt.getBooleanValue("-v"));
    }

    @Override
    public String briefDescription() {
        return "Elliptic curve group.";
    }
}
