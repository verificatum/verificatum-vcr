
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

package com.verificatum.protocol;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ExtIO;
import com.verificatum.protocol.com.BullBoardBasicGen;
import com.verificatum.protocol.com.BullBoardBasicHTTPWGen;
import com.verificatum.ui.info.InfoException;
import com.verificatum.ui.info.PartyInfo;
import com.verificatum.ui.info.PrivateInfo;
import com.verificatum.ui.info.ProtocolInfo;
import com.verificatum.ui.info.StringField;


/**
 * Defines additional fields and default values used by
 * {@link com.verificatum.protocol.ProtocolBB}.
 *
 * @author Douglas Wikstrom
 */
public class ProtocolBBGen extends ProtocolGen {

    /**
     * Description of bulletin board field.
     */
    public static final String BULLBOARD_DESCRIPTION =
        "Name of bulletin board implementation used, i.e., a subclass of "
        + "com.verificatum.protocol.com.BullBoardBasic. "
        + "WARNING! This field is not validated syntactically.";

    /**
     * Adds the values needed by the particular instantiation of
     * bulletin board used.
     */
    protected BullBoardBasicGen bbbg;

    /**
     * Creates an instance for a given implementation of a bulletin
     * board.
     *
     * @param bbbg Adds the values needed by the particular
     * instantiation of bulletin board used.
     */
    public ProtocolBBGen(final BullBoardBasicGen bbbg) {
        this.bbbg = bbbg;
    }

    /**
     * Creates an instance for the default bulletin board.
     */
    public ProtocolBBGen() {
        this.bbbg = new BullBoardBasicHTTPWGen();
    }


    @Override
    public void addProtocolInfo(final ProtocolInfo pri) {
        super.addProtocolInfo(pri);
        pri.addInfoFields(new StringField(ProtocolBB.BULLBOARD,
                                          BULLBOARD_DESCRIPTION, 1, 1));
        bbbg.addProtocolInfo(pri);
    }

    @Override
    public void addDefault(final ProtocolInfo pri) {
        super.addDefault(pri);
        try {
            pri.addValue(ProtocolBB.BULLBOARD, ProtocolDefaults.BullBoard());
        } catch (final InfoException ie) {
            throw new ProtocolError("Failed to add default value!", ie);
        }
        bbbg.addDefault(pri);
    }

    @Override
    public void addPrivateInfo(final PrivateInfo pi) {
        super.addPrivateInfo(pi);
        bbbg.addPrivateInfo(pi);
    }

    @Override
    public void addDefault(final PrivateInfo pi,
                           final ProtocolInfo pri,
                           final RandomSource rs) {
        super.addDefault(pi, pri, rs);
        bbbg.addDefault(pi, pri, rs);
    }

    @Override
    public void addDefault(final PartyInfo pai, final ProtocolInfo pri,
                           final PrivateInfo pi, final RandomSource rs) {
        super.addDefault(pai, pri, pi, rs);
        bbbg.addDefault(pai, pri, pi, rs);
    }

    /**
     * Attempts to instantiate the given class.
     *
     * @param className Name of a subclass of {@link
     * com.verificatum.protocol.com.BullBoardBasicGen}.
     * @return Instance of the given subclass.
     *
     * @throws ProtocolFormatException If the input is not the class
     * name of an existing bulletin board or if it can not be
     * instantiated.
     */
    protected static BullBoardBasicGen
        newBullBoardBasicGen(final String className)
        throws ProtocolFormatException {

        final String pfes =
            "Unable to instantiate bulletin board info generator!";

        if ("".equals(className)) {
            return new BullBoardBasicHTTPWGen();
        } else {
            try {
                // Instantiate the bulletin board info generator.
                final Class<?> klass = Class.forName(className + "Gen");
                final Constructor<?> constructor = klass.getConstructor();

                final Object obj = constructor.newInstance();

                if (BullBoardBasicGen.class.isAssignableFrom(klass)) {
                    return (BullBoardBasicGen) obj;
                } else {
                    throw new ProtocolFormatException("Invalid bulletin board "
                                                      + "class name!");
                }
            } catch (final InvocationTargetException ite) {
                throw new ProtocolFormatException(pfes, ite);
            } catch (final IllegalAccessException iae) {
                throw new ProtocolFormatException(pfes, iae);
            } catch (final ClassNotFoundException cnfe) {
                throw new ProtocolFormatException(pfes, cnfe);
            } catch (final NoSuchMethodException nsme) {
                throw new ProtocolFormatException(pfes, nsme);
            } catch (final InstantiationException cnfe) {
                throw new ProtocolFormatException(pfes, cnfe);
            }
        }
    }

    /**
     * Creates bulletin board info generator passed on a command line
     * using a "-bullboard". If no such option is used, then it
     * defaults to {@link
     * com.verificatum.protocol.com.BullBoardBasicHTTPW}.
     *
     * @param args Command line parameters that may contain a "-bull"
     * option defining a subclass of {@link
     * com.verificatum.protocol.com.BullBoardBasic}.
     * @return Bulletin board generator.
     *
     * @throws ProtocolFormatException If the input is not the short
     * name of an existing bulletin board or if it can not be
     * instantiated.
     */
    public static BullBoardBasicGen getBullBoardBasicGen(final String[] args)
        throws ProtocolFormatException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-" + ProtocolBB.BULLBOARD)
                && i + 1 < args.length) {

                final String bullBoardClassName = args[i + 1];
                return newBullBoardBasicGen(bullBoardClassName);
            }
        }
        return new BullBoardBasicHTTPWGen();
    }

    /**
     * Creates bulletin board info generator defined in the protcol
     * info file.
     *
     * @param protocolInfoFile Name of protocol info file containing a
     * configuration field for a bulletin board info generator.
     * @return Bulletin board generator.
     *
     * @throws ProtocolFormatException If the input is not the name of
     * a file containing a properly tagged name/classname of a
     * bulletin board.
     */
    public static BullBoardBasicGen
        getBullBoardBasicGen(final File protocolInfoFile)
        throws ProtocolFormatException {

        String xmlString = null;

        try {
            xmlString = ExtIO.readString(protocolInfoFile);
        } catch (final IOException ioe) {
            throw new ProtocolFormatException("Unable to open protocol "
                                              + "info file!", ioe);
        }

        final String startTag = String.format("<%s>", ProtocolBB.BULLBOARD);
        final String endTag = String.format("</%s>", ProtocolBB.BULLBOARD);

        int startIndex = xmlString.indexOf(startTag);
        final int endIndex = xmlString.indexOf(endTag);

        if (startIndex == -1 || endIndex == -1) {
            throw new ProtocolFormatException("Can not find definition "
                                              + "of bulletin board!");
        }
        startIndex = startIndex + startTag.length();
        if (startIndex >= endIndex) {
            throw new ProtocolFormatException("Begin and end tags in wrong "
                                              + "order!");
        }

        final String bullBoardClassName =
            xmlString.substring(startIndex, endIndex);

        return newBullBoardBasicGen(bullBoardClassName);
    }
}
