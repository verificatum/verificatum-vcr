
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

package com.verificatum.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * This is a minimal and incomplete implementation of the <a
 * href="http://www.json.org">JSON</a> format. It can only handle maps
 * of strings, but this suffices for our needs.
 *
 * @author Douglas Wikstrom
 */
public final class SimpleJSON {

    /**
     * Avoid accidental instantiation.
     */
    private SimpleJSON() { }

    /**
     * Converts a table of strings into its JSON representation.
     *
     * @param map JSON representation of input.
     * @return String representation of the input map.
     */
    public static String toJSON(final Map<String, String> map) {

        final StringBuilder sb = new StringBuilder();
        sb.append('{');

        final Iterator<Entry<String, String>> iterator =
            map.entrySet().iterator();

        while (iterator.hasNext()) {

            final Entry<String, String> entry = iterator.next();

            sb.append('"');
            sb.append(entry.getKey());
            sb.append("\":\"");
            sb.append(entry.getValue());
            sb.append('"');
            sb.append(',');
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append('}');

        return sb.toString();
    }

    /**
     * Parses a quoted string.
     *
     * @param head Source of tokens.
     * @return String represented by the input.
     * @throws SimpleJSONException If the content at the current
     *  position is not a string.
     */
    protected static String readString(final Head head)
        throws SimpleJSONException {

        if (head.getc() != '"') {
            throw new SimpleJSONException("Expected a string!");
        }
        head.inc();

        final StringBuilder sb = new StringBuilder();
        while (!head.end() && head.isAlphaNum()) {
            sb.append(head.getc());
            head.inc();
        }

        if (head.end() || head.getc() != '"') {
            throw new SimpleJSONException("Missing ending quote!");
        }
        head.inc();

        return sb.toString();
    }

    /**
     * Parses a key-and-value pair and stores the result in the given
     * map.
     *
     * @param head Source of tokens.
     * @param map Destination of key-and-value pair.
     * @throws SimpleJSONException If the content at the current
     *  position is not a key-value pair.
     */
    protected static void readPair(final Head head,
                                   final Map<String, String> map)
        throws SimpleJSONException {

        final String key = readString(head);

        if (head.end() || head.getc() != ':') {
            throw new SimpleJSONException("Missing ':'!");
        }

        head.inc();

        final String value = readString(head);
        map.put(key, value);
    }

    /**
     * Reads a map from the given head.
     *
     * @param head Source of data.
     * @return Map as a Java {@link TreeMap}.
     * @throws SimpleJSONException If the input is malformed.
     */
    public static Map<String, String> readMap(final Head head)
        throws SimpleJSONException {

        head.skipWhitespace();

        if (head.end() || head.getc() != '{') {
            throw new SimpleJSONException("Missing starting '{'!");
        }
        head.inc();
        head.skipWhitespace();

        final TreeMap<String, String> map = new TreeMap<String, String>();

        readPair(head, map);

        head.skipWhitespace();

        while (head.getc() == ',') {

            head.inc();
            head.skipWhitespace();
            readPair(head, map);
            head.skipWhitespace();

        }

        if (head.end() || head.getc() != '}') {
            throw new SimpleJSONException("Missing ending '}'!");
        }

        head.inc();
        head.skipWhitespace();

        return map;
    }

    /**
     * Reads a map from the given string.
     *
     * @param mapString Source of data.
     * @return Map as a Java {@link TreeMap}.
     * @throws SimpleJSONException If the input is malformed.
     */
    public static Map<String, String> readMap(final String mapString)
        throws SimpleJSONException {
        final Head head = new Head(mapString);
        return readMap(head);
    }

    /**
     * Reads several maps from the given string.
     *
     * @param mapString Source of data.
     * @return Array of maps as a Java {@link List}.
     * @throws SimpleJSONException If the input is malformed.
     */
    public static List<Map<String, String>>
        readMaps(final String mapString)
        throws SimpleJSONException {
        final Head head = new Head(mapString);

        head.skipWhitespace();

        if (head.end() || head.getc() != '[') {
            throw new SimpleJSONException("Missing starting '['!");
        }
        head.inc();
        head.skipWhitespace();

        final List<Map<String, String>> maps =
            new ArrayList<Map<String, String>>();

        maps.add(readMap(head));

        head.skipWhitespace();

        while (!head.end() && head.getc() == ',') {

            head.inc();
            head.skipWhitespace();
            maps.add(readMap(head));
            head.skipWhitespace();
        }

        if (head.end() || head.getc() != ']') {
            throw new SimpleJSONException("Missing ending ']'!");
        }

        head.inc();
        head.skipWhitespace();

        if (!head.end()) {
            throw new SimpleJSONException("Junk at end of input!");
        }

        return maps;
    }
}

/**
 * Simple tokenizer moving over a string.
 *
 * @author Douglas Wikstrom
 */
class Head {

    /**
     * Current position of head.
     */
    protected int i;

    /**
     * Data source.
     */
    protected String src;

    /**
     * Creates an instance that reads from the given source.
     *
     * @param src Source of characters.
     */
    Head(final String src) {
        this.src = src;
        this.i = 0;
    }

    /**
     * Returns the current character.
     *
     * @return Current character.
     */
    public char getc() {
        return src.charAt(i);
    }

    /**
     * Returns true if and only if the character is a letter or a
     * digit.
     *
     * @return True if and only if the character is a letter or a
     *         digit.
     */
    public boolean isAlphaNum() {
        final char c = src.charAt(i);
        return 48 <= c && c < 58 // 0-9
            || 65 <= c && c < 91 // A-Z
            || 97 <= c && c < 123; // a-z
    }

    /**
     * Moves the head passed any whitespace.
     */
    public void skipWhitespace() {
        while (!end() && Character.isWhitespace(src.charAt(i))) {
            i++;
        }
    }

    /**
     * Moves the head one step forward.
     *
     * @throws SimpleJSONException If the end of input was reached
     * inexpectedly.
     */
    public void inc() throws SimpleJSONException {
        if (end()) {
            throw new SimpleJSONException("Unexpected end of input!");
        }
        i++;
    }

    /**
     * Returns true if and only if the head is passed the end of the
     * input.
     *
     * @return true if and only if the head is passed the end of the
     *         input.
     */
    public boolean end() {
        return i == src.length();
    }
}
