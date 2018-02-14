
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
