
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
