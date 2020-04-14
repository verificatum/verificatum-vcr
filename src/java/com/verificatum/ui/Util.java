
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
