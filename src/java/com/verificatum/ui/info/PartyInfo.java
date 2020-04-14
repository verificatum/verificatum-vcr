
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
