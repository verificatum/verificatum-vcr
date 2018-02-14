
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

package com.verificatum.ui.tui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import com.verificatum.eio.ExtIO;
import com.verificatum.ui.Log;
import com.verificatum.ui.UI;
import com.verificatum.ui.UIError;


/**
 * Implements a simple textual interface for cryptographic protocols.
 *
 * @author Douglas Wikstrom
 */
public final class TextualUI implements UI {

    /**
     * Description string used to refer to the parties of the executed
     * protocol.
     */
    private String descriptionString;

    /**
     * Console used by this user interface.
     */
    private final com.verificatum.ui.Console jc;

    /**
     * Wrapper of the input stream in the console.
     */
    private BufferedReader bf;

    /**
     * Logging context of this user interface.
     */
    private final Log log;

    /**
     * Creates a textual interface using the given console and logging
     * stream.
     *
     * @param jc Console used by this interface.
     */
    public TextualUI(final com.verificatum.ui.Console jc) {
        this.jc = jc;

        try {

            final InputStreamReader isr =
                new InputStreamReader(jc.in, ExtIO.CHARACTER_ENCODING);
            bf = new BufferedReader(isr);

        } catch (final UnsupportedEncodingException uee) {
            throw new Error("Bad encoding!", uee);
        }

        this.log = new Log();

        this.descriptionString = "Party";
    }

    /**
     * Sets the generic description string of a party.
     *
     * @param descriptionString Description string of a party.
     */
    public void setDescriptionString(final String descriptionString) {
        this.descriptionString = descriptionString;
    }

    // The methods below are documented in UI.java

    @Override
    public String getDescrString(final int i) {
        return String.format("%s%02d", descriptionString, i);
    }

    @Override
    public String getDescrString() {
        return descriptionString;
    }

    @Override
    public String stringQuery(final String msgString) {

        jc.out.print(msgString);
        try {
            return bf.readLine();
        } catch (final IOException ioe) {
            throw new UIError("Input/Output error!", ioe);
        }
    }

    @Override
    public boolean dialogQuery(final String msgString) {
        for (;;) {
            final String ans = stringQuery(msgString + " (yes/no) ");
            if (ans.toLowerCase(Locale.US).equals("yes")) {
                return true;
            } else if (ans.toLowerCase(Locale.US).equals("no")) {
                return false;
            }
        }
    }

    @Override
    public int intQuery(final String msgString) {
        for (;;) {
            final String answerString = stringQuery(msgString);
            try {
                return Integer.parseInt(answerString);
            } catch (final NumberFormatException nfe) {
                jc.out.println("The string: " + answerString
                               + " is not an integer!");
            }
        }
    }

    @Override
    public int alternativeQuery(final String[] alternatives,
                                final String descString) {
        final StringBuffer sb = new StringBuffer();

        for (;;) {
            sb.append("\nAlternatives:\n");
            for (int i = 0; i < alternatives.length; i++) {
                sb.append(i);
                sb.append(") ");
                sb.append(alternatives[i]);
                sb.append('\n');
            }
            sb.append('\n').append("Choose entry: ");

            final int theAnswer = intQuery(descString + "\n" + sb.toString());
            if (0 <= theAnswer && theAnswer < alternatives.length) {
                return theAnswer;
            } else {
                jc.out.println("The integer " + theAnswer
                               + " is not a valid alternative!");
            }
        }
    }

    @Override
    public Log getLog() {
        return log;
    }

    @Override
    public void print(final String str) {
        jc.out.print(str);
    }
}
