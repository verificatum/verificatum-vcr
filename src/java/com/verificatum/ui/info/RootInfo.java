
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

package com.verificatum.ui.info;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import com.verificatum.eio.ExtIO;


/**
 * Abstract base class for a root <code>Info</code> instance.
 *
 * @author Douglas Wikstrom
 */
public abstract class RootInfo extends Info {

    /**
     * Name of version tag.
     */
    public static final String VERSION = "version";

    /**
     * Description of version field.
     */
    public static final String VERSION_DESCRIPTION =
        "Version of Verificatum Software for which this info is intended.";

    /**
     * Creates an empty instance with no <code>InfoField</code>s and
     * no values.
     */
    protected RootInfo() {
        super();
        final StringField versionField =
            new StringField(VERSION, VERSION_DESCRIPTION, 1, 1).
            setPattern("[0-9]{0,3}\\.[0-9]{0,3}\\.[0-9]{0,3}");
        addInfoField(versionField);
    }

    /**
     * Parses the file with the given filename according to its own
     * schema and stores the values it encounters.
     *
     * @param infoFile XML file.
     * @return Instance containing values parsed from file.
     * @throws InfoException If parsing fails.
     */
    public RootInfo parse(final File infoFile) throws InfoException {
        (new InfoParser()).parse(this, infoFile);
        return this;
    }

    /**
     * Parses the file with the given filename according to its own
     * schema and stores the values it encounters.
     *
     * @param infoFilename Name of XML file.
     * @return Instance containing values parsed from file.
     * @throws InfoException If parsing fails.
     */
    public RootInfo parse(final String infoFilename) throws InfoException {
        return parse(new File(infoFilename));
    }

    /**
     * Generates its own schema.
     *
     * @return Schema of this instance.
     */
    public abstract String generateSchema();

    /**
     * Outputs an XML representation of the values in this instance.
     *
     * @return XML code representing this instance.
     */
    protected abstract String toXML();

    /**
     * Writes an XML representation of the values in this instance to
     * the given file.
     *
     * @param infoFile Where the XML is written.
     * @throws InfoException If generating the info file fails.
     */
    public void toXML(final File infoFile) throws InfoException {

        BufferedWriter bw = null;
        try {

            if (!ExtIO.delete(infoFile)) {
                throw new InfoError("Unable to delete existing file!");
            }

            bw = ExtIO.getBufferedWriter(infoFile);
            bw.write(toXML());
            bw.flush();

        } catch (final IOException ioe) {
            throw new InfoException("Can not generate info file!", ioe);
        } finally {
            ExtIO.strictClose(bw);
        }
        if (!infoFile.setWritable(false, false)) {
            throw new InfoException("Unable to make info file read only!");
        }
    }
}
