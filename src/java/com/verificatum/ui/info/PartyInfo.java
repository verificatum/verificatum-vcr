
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

import java.util.List;

import com.verificatum.protocol.ProtocolGen;

/**
 * Represents the public information about a party in a protocol. It
 * stores its name, description, and how to reach it. Additional
 * fields may (and should) of course be added if needed in a
 * particular protocol.
 *
 * @author Douglas Wikstrom
 */
public final class PartyInfo extends Info implements Comparable<PartyInfo> {

    /**
     * Name of the party tag.
     */
    public static final String PARTY = "party";

    /**
     * Creates an instance with the given additional fields.
     *
     * @param infoFields Additional fields of this instance.
     */
    public PartyInfo(final InfoField... infoFields) {
        addInfoFields(infoFields);
    }

    /**
     * Creates an instance with the given additional fields.
     *
     * @param infoFields Additional fields of this instance.
     */
    public PartyInfo(final List<InfoField> infoFields) {
        for (final InfoField infoField : infoFields) {
            addInfoFields(infoField);
        }
    }

    /**
     * Appends the schema of this instance to the input.
     *
     * @param sb Destination of schema.
     */
    protected void generateSchema(final StringBuffer sb) {
        String s =
            "<xs:element name=\"" + PARTY + "\"\n"
            + "            minOccurs=\"0\"\n"
            + "            maxOccurs=\"" + ProtocolGen.MAX_NOPARTIES + "\">\n"
            + "<xs:complexType>\n" + "<xs:sequence>\n\n";
        sb.append(s);

        schemaOfInfoFields(sb);

        s = "</xs:sequence>\n" + "</xs:complexType>\n" + "</xs:element>\n\n";
        sb.append(s);
    }

    /**
     * Writes an XML representation of this instance to the input
     * <code>StringBuffer</code> using the given indent level.
     *
     * @param sb Destination of XML representation.
     * @param indent Indentation level.
     */
    public void toXML(final StringBuffer sb, final int indent) {
        formatTag(sb, indent, PARTY, BEGIN, indent);
        xmlOfInfoFields(sb, indent + 1);
        sb.append('\n');
        formatTag(sb, indent, PARTY, END, indent);
    }

    // These methods are documented in the super class.

    @Override
    public Info endElement(final String content, final String tagName)
        throws InfoException {
        if (tagName.equals(PARTY)) {
            return null;
        } else {
            return super.endElement(content, tagName);
        }
    }

    /**
     * Returns the first info field or the empty string if none
     * exists.
     *
     * @return Primary info field.
     */
    public String getPrimaryInfoField() {
        if (infoFields.size() > 0) {
            return infoFields.get(0).name;
        } else {
            return "";
        }
    }

    /**
     * Returns the first second field or the empty string if none
     * exists.
     *
     * @return Secondary info field.
     */
    public String getSecondaryInfoField() {
        if (infoFields.size() > 1) {
            return infoFields.get(1).name;
        } else {
            return "";
        }
    }

    /**
     * Performs a comparison between this instance and the input.
     *
     * @param pi Instance with which this instance is compared.
     * @return -1, 0, or 1 depending on if this instance is less,
     *         equal, or larger than the input respectively.
     */
    @Override
    public int compareTo(final PartyInfo pi) {

        final String secString = this.getStringValue(getSecondaryInfoField());
        final String otherSecString =
            pi.getStringValue(getSecondaryInfoField());

        final int c = secString.compareTo(otherSecString);

        if (c == 0) {

            return this.getStringValue(getPrimaryInfoField()).
                compareTo(pi.getStringValue(getPrimaryInfoField()));

        } else {

            return c;
        }
    }

    @Override
    public int hashCode() {
        return infoFields.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PartyInfo)) {
            return false;
        }
        final PartyInfo pi = (PartyInfo) obj;

        return compareTo(pi) == 0;
    }
}
