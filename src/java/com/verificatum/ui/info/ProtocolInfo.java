
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import javax.xml.XMLConstants;

/**
 * Represents an execution of a given protocol with a given set of
 * parties. It stores global fields such as session id, name, and
 * description. It also stores several <code>PartyInfo</code>
 * instances corresponding to the parties executing the protocol.
 *
 * @author Douglas Wikstrom
 */
public final class ProtocolInfo extends RootInfo {

    /**
     * Name of protocol tag.
     */
    public static final String PROTOCOL = "protocol";

    /**
     * List of party infos.
     */
    private final List<PartyInfo> partyInfos;

    /**
     * Factory for creating new info fields.
     */
    private final PartyInfoFactory factory;

    /**
     * Create instance with the default factory for creating parties
     * and with the given additional info fields. This is useful for
     * subclasses.
     *
     * @param infoFields Additional info fields.
     */
    public ProtocolInfo(final InfoField... infoFields) {
        this(new PartyInfoFactory(), infoFields);
    }

    /**
     * Create instance with a given factory for creating parties and
     * with the given additional info fields.
     *
     * @param factory Factory for creating parties.
     * @param infoFields Additional info fields.
     */
    public ProtocolInfo(final PartyInfoFactory factory,
                        final InfoField... infoFields) {
        addInfoFields(infoFields);
        this.partyInfos = new ArrayList<PartyInfo>();
        this.factory = factory;
    }

    /**
     * Parses values from the file with the given filename and returns
     * itself, i.e., it fills itself with data from the file.
     *
     * @param infoFilename Name of info file.
     * @return This instance.
     * @throws InfoException If parsing fails.
     */
    @Override
    public ProtocolInfo parse(final String infoFilename)
        throws InfoException {
        return (ProtocolInfo) super.parse(infoFilename);
    }

    /**
     * Copies the party infos to this instance from the given protocol
     * info. Note that this may change the indices of existing party
     * infos.
     *
     * @param pi Source of party infos.
     * @throws InfoException If several party infos to be added have
     *  the same primary info field.
     */
    public void addPartyInfos(final ProtocolInfo pi) throws InfoException {
        for (final PartyInfo partyInfo : pi.partyInfos) {
            partyInfos.add(partyInfo);
        }
    }

    /**
     * Adds a <code>PartyInfo</code> instance to this instance.
     *
     * @param partyInfo Instance to add.
     *
     * @throws InfoException If two PartyInfo instances are
     * encountered with the same primary info field value.
     */
    public void addPartyInfo(final PartyInfo partyInfo) throws InfoException {
        final String pif = partyInfo.getPrimaryInfoField();
        final String pifValue = partyInfo.getStringValue(pif);
        final PartyInfo pai = getPartyInfo(pifValue);

        if (pai == null) {

            partyInfos.add(partyInfo);
            Collections.sort(partyInfos);

        } else if (!pai.equalInfoFields(partyInfo)) {

            final String s = "Different PartyInfo instances with the same"
                + " primary info field value (" + pifValue + ")";
            throw new InfoException(s);
        }
    }

    /**
     * Returns the party info with the given primary info field.
     *
     * @param pif Primary info field value.
     * @return The party info with the given primary info field.
     */
    public PartyInfo getPartyInfo(final String pif) {
        for (final PartyInfo pi : partyInfos) {
            if (pi.getStringValue(pi.getPrimaryInfoField()).equals(pif)) {
                return pi;
            }
        }
        return null;
    }

    /**
     * Returns the index of the <code>PartyInfo</code> with the given
     * primary info field. WARNING! Do not use this method until all
     * <code>PartyInfo</code> instances have been added. Doing so may
     * give incorrect results.
     *
     * @param pif Primary info field.
     * @return Index of a party.
     * @throws InfoException If the name is unknown.
     */
    public int getIndex(final String pif) throws InfoException {
        int index = 1;

        for (final ListIterator<PartyInfo> li = partyInfos.listIterator();
             li.hasNext();) {
            final PartyInfo partyInfo = li.next();

            final String fieldName = partyInfo.getPrimaryInfoField();

            if (partyInfo.getValue(fieldName).equals(pif)) {
                return index;
            } else {
                index++;
            }
        }
        throw new InfoException("Can not find entry in protocol info "
                                + "file for the primary info field \""
                                + pif + "\"!");
    }

    /**
     * Returns the number of <code>PartyInfo</code> instances in this
     * instance.
     *
     * @return Number of parties stored in this instance.
     */
    public int getNumberOfParties() {
        return partyInfos.size();
    }

    /**
     * Returns the <code>PartyInfo</code> with the given index.
     *
     * @param i Index of requested party.
     * @return The <code>i</code>th <code>PartyInfo</code> contained
     *         in this instance.
     */
    public PartyInfo get(final int i) {
        return partyInfos.get(i - 1);
    }

    /**
     * Returns the factory of this instance.
     *
     * @return Factory used in this instance.
     */
    public PartyInfoFactory getFactory() {
        return factory;
    }

    // These methods are documented in the super class.

    @Override
    public Info startElement(final String tagName) {
        if (tagName.equals(PartyInfo.PARTY)) {
            final PartyInfo pi = factory.newInstance();
            partyInfos.add(pi);
            return pi;
        } else {
            return super.startElement(tagName);
        }
    }

    @Override
    public Info endElement(final String content, final String tagName)
        throws InfoException {
        if (tagName.equals(PROTOCOL)) {
            return null;
        } else {
            return super.endElement(content, tagName);
        }
    }

    @Override
    public String generateSchema() {
        final StringBuffer sb = new StringBuffer();

        // Generate the beginning of the schema
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
            + "<xs:schema xmlns:xs=\""
            + XMLConstants.W3C_XML_SCHEMA_NS_URI
            + "\">";
        sb.append(s).append('\n');

        // Include custom XS types.
        sb.append('\n').append(XSTypes.IP).append("\n\n");

        // Generate protocol element
        s = "<xs:element name=\"" + PROTOCOL + "\">\n" + "<xs:complexType>\n"
            + "<xs:sequence>";
        sb.append(s).append("\n\n");

        // Generate all our fields
        schemaOfInfoFields(sb);
        sb.append('\n');

        // Generate the party element
        (factory.newInstance()).generateSchema(sb);

        // Generate the end of the schema
        s = "</xs:sequence>\n" + "</xs:complexType>\n" + "</xs:element>\n\n"
            + "</xs:schema>\n";
        sb.append(s);

        return sb.toString();
    }

    @Override
    public String toXML() {
        final StringBuffer sb = new StringBuffer();
        final String genDescription =
            "ATTENTION! WE STRONGLY ADVICE AGAINST EDITING THIS FILE!"
            + "\n\n"
            + "This is a protocol information file. It contains all the "
            + "parameters of a protocol session as agreed by all parties."
            + "\n\n"
            + "Each party must hold an identical copy of this file. "
            + "WE RECOMMEND YOU TO NOT EDIT THIS FILE UNLESS YOU KNOW "
            + "EXACTLY WHAT YOU ARE DOING."
            + "\n\n"
            + "Many XML features are disabled and throw errors, so parsing "
            + "is more restrictive than the schema implies.";

        sb.append('\n');
        formatComment(sb, 0, genDescription, 2);

        formatTag(sb, 0, PROTOCOL, BEGIN, 1);
        xmlOfInfoFields(sb, 1);
        sb.append('\n');
        for (final ListIterator<PartyInfo> li = partyInfos.listIterator();
             li.hasNext();) {

            li.next().toXML(sb, 1);
            sb.append('\n');
        }
        formatTag(sb, 0, PROTOCOL, END, 2);
        return sb.toString();
    }
}
