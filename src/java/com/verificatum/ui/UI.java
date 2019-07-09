
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

/**
 * Simple abstract user interface for protocols.
 *
 * @author Douglas Wikstrom
 */
public interface UI {

    /**
     * Returns the description string used to refer to the parties of
     * the executed protocol, e.g., "Party", "P", or "M".
     *
     * @return Description string.
     */
    String getDescrString();

    /**
     * Returns the description string used to refer to the
     * <code>i</code>th party in the protocol. This simply
     * concatenates the input integer onto the description string
     * output by {@link #getDescrString()}.
     *
     * @param i Index of party.
     * @return Description string.
     */
    String getDescrString(int i);

    /**
     * Presents the question string <code>msgString</code> to the user
     * who can reply by a <code>String</code> that is then returned.
     *
     * @param msgString Question string presented to the user.
     * @return Answer from user.
     */
    String stringQuery(String msgString);

    /**
     * Presents the question string <code>msgString</code> to the user
     * who can reply by <code>yes</code> or <code>no</code> that is
     * then returned in the form of a <code>boolean</code>.
     *
     * @param msgString Question string presented to the user.
     * @return <code>boolean</code> representing the answer.
     */
    boolean dialogQuery(String msgString);

    /**
     * Presents the question string <code>msgString</code> to the user
     * who can reply with an integer that is then returned.
     *
     * @param msgString Question string showed to the user.
     * @return Answer of user.
     */
    int intQuery(String msgString);

    /**
     * Presents several alternatives to the user who can reply by
     * choosing one of the alternatives. The index of the chosen
     * alternative is returned.
     *
     * @param alternatives Alternatives presented to the user.
     * @param descString General description.
     * @return Choice of user.
     */
    int alternativeQuery(String[] alternatives, String descString);

    /**
     * Returns the root logging context of the user interface.
     *
     * @return Logging context of this user interface.
     */
    Log getLog();

    /**
     * Present the given information to the user.
     *
     * @param str Message to display.
     */
    void print(String str);
}
