
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

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.verificatum.ui.Util;
import com.verificatum.util.Lazy;

/**
 * Abstract base class for representing configuration data for
 * protocols and parties in a protocol.
 *
 * @author Douglas Wikstrom
 */
public class Info {

    /**
     * Used to pretty print an instance as XML code.
     */
    protected static final String INDENT_STRING = "   ";

    /**
     * Used to start XML tags.
     */
    protected static final String BEGIN = "";

    /**
     * Used to end XML tags.
     */
    protected static final String END = "/";

    /**
     * Abbreviation for "end of file".
     */
    protected static final String EOF = "end of file";

    /**
     * Stores the <code>InfoField</code> instances associated with
     * this instance in the order they are added.
     */
    protected List<InfoField> infoFields;

    /**
     * Stores the <code>InfoField</code> instances associated with
     * this instance.
     */
    protected Map<String, InfoField> infoFieldsHashMap;

    /**
     * Stores the values stored in the <code>InfoField</code>
     * instances of this instance.
     */
    protected Map<String, List<Object>> values;

    /**
     * Creates an empty instance with no <code>InfoField</code>s and
     * no values.
     */
    protected Info() {
        infoFields = new ArrayList<InfoField>();
        infoFieldsHashMap = new HashMap<String, InfoField>();
        values = new HashMap<String, List<Object>>();
    }

    /**
     * Adds an <code>InfoField</code> to this instance.
     *
     * @param infoField <code>InfoField</code> instance added to this
     * <code>Info</code>.
     */
    public void addInfoField(final InfoField infoField) {

        if (infoFieldsHashMap.containsKey(infoField.getName())) {
            throw new InfoError("Attempting to add the same field twice!");
        }

        infoFields.add(infoField);
        infoFieldsHashMap.put(infoField.getName(), infoField);
        values.put(infoField.getName(), new ArrayList<Object>());
    }

    /**
     * Adds several <code>InfoField</code> to this instance.
     *
     * @param infoFields <code>InfoField</code> instances added to
     * this instance.
     */
    public void addInfoFields(final InfoField... infoFields) {
        for (final InfoField infoField : infoFields) {
            addInfoField(infoField);
        }
    }

    /**
     * Writes the schema entries of the <code>InfoField</code>
     * instances of this instance.
     *
     * @param sb Where the schema entries are written.
     */
    public void schemaOfInfoFields(final StringBuffer sb) {
        for (final InfoField infoField : infoFields) {
            sb.append(infoField.schemaElementString());
            sb.append("\n\n");
        }
    }

    /**
     * Called by {@link InfoParser} when an element is found. This
     * should normally be overridden by subclasses.
     *
     * @param tagName Name of element.
     * @return Returns this instance.
     */
    protected Info startElement(final String tagName) {
        return this;
    }

    /**
     * Adds a value under a given tag.
     *
     * @param tagName Name of element.
     * @param content String representing value.
     *
     * @throws InfoException If the content can no be added under the
     * given tag.
     */
    public void addValue(final String tagName, final Object content)
        throws InfoException {
        values.get(tagName).add(content);
    }

    /**
     * Delete value stored in this info.
     *
     * @param tagName Name of value to delete.
     */
    public void deleteValue(final String tagName) {
        infoFieldsHashMap.remove(tagName);
    }

    /**
     * Adds a value under a given tag.
     *
     * @param tagName Name of element.
     * @param content String representing value.
     *
     * @throws InfoException If the content can no be added under the
     * given tag.
     */
    public void addValue(final String tagName, final String content)
        throws InfoException {
        final InfoField infoField = infoFieldsHashMap.get(tagName);
        Object obj;
        try {
            obj = infoField.parse(content);
        } catch (final InfoException ie) {
            throw new InfoException("Value out of domain! " + ie.getMessage(),
                                    ie);
        }
        values.get(tagName).add(obj);
    }

    /**
     * Adds a value under a given tag.
     *
     * @param tagName Name of element.
     * @param intValue Integer value.
     *
     * @throws InfoException If the content can no be added under the
     * given tag.
     */
    public void addValue(final String tagName, final int intValue)
        throws InfoException {
        addValue(tagName, Integer.toString(intValue));
    }

    /**
     * Copy values under a given tag name from another info instance.
     * If an object to be copied is an instance of {@link Lazy}, then
     * the output of {@link Lazy#gen()} is used instead. This allows
     * lazy evaluation of default parameters that are costly to
     * generate.
     *
     * @param tagName Name of tag.
     * @param info Source of values.
     */
    public void copyValues(final String tagName, final Info info) {
        final List<Object> al = values.get(tagName);
        for (final Object obj : info.values.get(tagName)) {
            if (obj instanceof Lazy) {
                al.add(((Lazy) obj).gen());
            } else {
                al.add(obj);
            }
        }
    }

    /**
     * Transfers values to this instance from the given info instance.
     *
     * @param defaultInfo Source of default values.
     */
    public void transferValues(final Info defaultInfo) {

        for (final InfoField inf : infoFields) {
            final String name = inf.getName();
            copyValues(name, defaultInfo);
        }
    }

    /**
     * Writes the XML code of the fields stored in this instance.
     *
     * @param sb Where the XML code is written.
     * @param indent Indent depth of this invokation.
     */
    protected void xmlOfInfoFields(final StringBuffer sb, final int indent) {
        for (final InfoField infoField : infoFields) {

            final String description = infoField.getDescription();
            if (!"".equals(description)) {
                sb.append('\n');
                formatComment(sb, indent, description, 0);
            }
            sb.append('\n');

            for (final Object obj : values.get(infoField.getName())) {
                formatContent(sb, indent, infoField.getName(), obj.toString(),
                              1);
            }
        }
    }

    /**
     * Called by {@link InfoParser} when an element is ended. This
     * should normally be overrridden by subclasses.
     *
     * @param tagName Name of element.
     * @param content String representing value.
     * @return Returns this instance.
     *
     * @throws InfoException If the content can no be added under the
     * given tag.
     */
    public Info endElement(final String content, final String tagName)
        throws InfoException {
        addValue(tagName, content);
        return this;
    }

    /**
     * Returns an iterator of all values stored under a given tag.
     *
     * @param tagName Name of tag.
     * @return Iterator for values stored under the tag.
     */
    public ListIterator<Object> getValues(final String tagName) {
        return values.get(tagName).listIterator();
    }

    /**
     * Checks if any value has been stored under the given tag name.
     *
     * @param tagName Name of tag.
     * @return <code>true</code> if this instance stores a value under
     *         the given tag name and <code>false</code> otherwise.
     */
    public boolean hasValue(final String tagName) {
        final List<Object> someValues = values.get(tagName);
        return someValues != null && !someValues.isEmpty();
    }

    /**
     * Returns the first value stored under a given tag name.
     *
     * @param tagName Name of tag.
     * @return First value stored under the given tag name.
     */
    public Object getValue(final String tagName) {
        return values.get(tagName).get(0);
    }

    /**
     * Returns the first value stored under a given tag name.
     *
     * @param tagName Name of tag.
     * @return First value stored under the given tag name.
     */
    public int getIntValue(final String tagName) {
        return ((Integer) getValue(tagName)).intValue();
    }

    /**
     * Returns the first value stored under a given tag name as a
     * File.
     *
     * @param tagName Name of tag.
     * @return First value stored under the given tag name as a File.
     */
    public File getFileValue(final String tagName) {
        return new File(getStringValue(tagName));
    }

    /**
     * Returns the first value stored under a given tag name.
     *
     * @param tagName Name of tag.
     * @return First value stored under the given tag name.
     */
    public String getStringValue(final String tagName) {
        return (String) getValue(tagName);
    }

    /**
     * Returns the Internet address including the port represented by
     * a value under the given tag.
     *
     * @param tagName Name of tag.
     * @return First Internet address stored under the given tag.
     */
    public InetSocketAddress getInetSocketAddress(final String tagName) {

        // Validated instances string can not fail to be converted
        // into InetSocketAddress.
        final String value = getStringValue(tagName);
        final String[] s = value.split(":");
        if (s.length != 2) {
            throw new InfoError("Invalid address! (" + value + ")");
        }
        try {
            final int port = Integer.parseInt(s[1]);
            return new InetSocketAddress(s[0], port);
        } catch (final NumberFormatException nfe) {
            throw new InfoError("Invalid port number! (" + s[1] + ")", nfe);
        }
    }

    /**
     * Returns a string with the number of indent steps given as
     * input. This is used for formatting output.
     *
     * @param indent Number of indent steps.
     * @return Indent string.
     */
    protected String indentString(final int indent) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indent; i++) {
            sb.append(INDENT_STRING);
        }
        return sb.toString();
    }

    /**
     * Returns a string with the number of new lines given as input.
     * This is used for formatting output.
     *
     * @param nl Number of new lines.
     * @return String containing new lines.
     */
    protected String newLines(final int nl) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nl; i++) {
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Formats a schema tag.
     *
     * @param sb Where the formatted tag is written.
     * @param indent How much the formatted tag should be indented.
     * @param tag Name of tag.
     * @param tagType Schema type of tag.
     * @param nl Number of new lines after the tag.
     */
    protected void formatTag(final StringBuffer sb,
                             final int indent,
                             final String tag,
                             final String tagType,
                             final int nl) {
        sb.append(indentString(indent));
        sb.append('<');
        sb.append(tagType);
        sb.append(tag);
        sb.append('>');
        sb.append(newLines(nl));
    }

    /**
     * Formats an element.
     *
     * @param sb Where the formatted tag is written.
     * @param indent How much the formatted tag should be indented.
     * @param tag Name of tag.
     * @param content Encoded value.
     * @param nl Number of new lines after the element.
     */
    protected void formatContent(final StringBuffer sb,
                                 final int indent,
                                 final String tag,
                                 final String content,
                                 final int nl) {
        formatTag(sb, indent, tag, BEGIN, 0);
        sb.append(content);
        formatTag(sb, 0, tag, END, nl);
    }

    /**
     * Formats a comment.
     *
     * @param sb Where the formatted tag is written.
     * @param indent How much the formatted tag should be indented.
     * @param description Comment string.
     * @param nl Number of new lines at end of comment.
     */
    protected void formatComment(final StringBuffer sb, final int indent,
                                 final String description, final int nl) {
        final String is = indentString(indent);
        sb.append(is);
        sb.append("<!-- ");

        // XXX below is a hack to avoid line breaks in "-->".
        String brokenDescription =
            Util.breakLine(description + " XXX", 65 - indent + 5);
        brokenDescription =
            brokenDescription.substring(0, brokenDescription.length() - 3)
            + "-->";

        sb.append(brokenDescription.replaceAll("\n", "\n" + is + "     "));
        sb.append(newLines(nl));
    }

    /**
     * Verifies that the input contains identical info fields and that
     * it has identical info values to the ones in this instance.
     *
     * @param info Instance compared with this instance.
     * @return <code>true</code> or <code>false</code> depending on if
     *         this instance is equal to the input or not.
     */
    public boolean equalInfoFields(final Info info) {
        if (infoFields.size() != info.infoFields.size()) {
            return false;
        }
        for (final InfoField infoField : infoFields) {
            final Object name = values.get(infoField.name);
            final Object otherName = info.values.get(infoField.name);
            if (!name.equals(otherName)) {
                return false;
            }
        }
        return true;
    }
}
