
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

package com.verificatum.ui;

import java.text.BreakIterator;
import java.util.ArrayList;

/**
 * Implements simple general utility functions.
 *
 * @author Douglas Wikstrom
 */
public final class Util {

    /**
     * Avoid accidental instantiation.
     */
    private Util() { }

    /**
     * Returns the class name of the input instance.
     *
     * @param obj Instance of which the class name is requested.
     * @param verbose Decides if a qualified name is returned or not.
     * @return Name of class of input object.
     */
    public static String className(final Object obj, final boolean verbose) {
        if (verbose) {
            return obj.getClass().getName();
        } else {
            return obj.getClass().getSimpleName();
        }
    }

    /**
     * Returns the unqualified class name of the input instance.
     *
     * @param obj Instance of which the class name is requested.
     * @return Name of class of input object.
     */
    public static String className(final Object obj) {
        return obj.getClass().getSimpleName();
    }

    /**
     * Breaks the lines of the input string for the given width.
     *
     * @param source String to break.
     * @param width Width of the broken string.
     * @return Broken string.
     */
    public static String breakLines(final String source, final int width) {
        final String[] lines = Util.split(source, "\n\n");
        final StringBuilder sb = new StringBuilder();
        for (final String line : lines) {
            sb.append(breakLine(line, width)).append("\n\n");
        }
        if (sb.length() >= 2) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    /**
     * Breaks the input string for the given width.
     *
     * @param source String to break.
     * @param width Width of the broken string.
     * @return Broken string.
     */
    public static String breakLine(final String source, final int width) {

        final StringBuilder sb = new StringBuilder();

        final BreakIterator boundary = BreakIterator.getLineInstance();
        boundary.setText(source);

        int start = boundary.first();
        int end = boundary.next();
        int lineLength = 0;

        String prevWord = "";
        while (end != BreakIterator.DONE) {

            final String word = source.substring(start, end);
            lineLength = lineLength + word.length();
            if (lineLength >= width) {
                if (!(end < source.length() && prevWord.endsWith("\n"))) {
                    sb.append('\n');
                }
                lineLength = word.length();
            } else if (prevWord.endsWith("\n")) {
                lineLength = word.length();
            }
            sb.append(word);
            start = end;
            end = boundary.next();
            prevWord = word;
        }
        return sb.toString();
    }

    /**
     * Performs a Python-like split of the input text based on the
     * given split-string.
     *
     * @param text Text to be split.
     * @param pattern Splitting pattern.
     * @return Split text.
     */
    public static String[] split(final String text, final String pattern) {

        final ArrayList<String> al = new ArrayList<String>();
        int startIndex = 0;
        while (startIndex < text.length()) {

            final int index = text.indexOf(pattern, startIndex);

            if (index == -1) {
                al.add(text.substring(startIndex));
                startIndex = text.length();
            } else {
                al.add(text.substring(startIndex, index));
                startIndex = index + pattern.length();
            }
        }
        if (text.endsWith(pattern)) {
            al.add("");
        }

        final int size = al.size();

        return al.toArray(new String[size]);
    }
}
