
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

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Simple hierarchical logging class. This allows adding output
 * streams.
 *
 * @author Douglas Wikstrom
 */
public final class Log {

    /**
     * List of streams to which we should log events.
     */
    private final List<PrintStream> pout;

    /**
     * Name of this log.
     */
    private final String name;

    /**
     * Indent string.
     */
    private final String depthString;

    /**
     * Format for outputting dates.
     */
    private final SimpleDateFormat sdf =
        new SimpleDateFormat("yyMMdd HH:mm:ss", Locale.US);

    /**
     * Creates an empty log.
     */
    public Log() {
        this.name = "";
        this.depthString = "";
        this.pout = new ArrayList<PrintStream>();
    }

    /**
     * Creates a new log.
     *
     * @param name Name of log.
     * @param depthString Indent string.
     * @param pout List of output streams.
     */
    protected Log(final String name, final String depthString,
                  final List<PrintStream> pout) {
        this.pout = pout;
        this.name = name;
        this.depthString = depthString;
    }

    /**
     * Add an additional output stream.
     *
     * @param ps Additional output stream.
     */
    public void addLogStream(final OutputStream ps) {
        synchronized (pout) {
            pout.add(new PrintStream(ps));
        }
    }

    /**
     * Creates a child log with a given name.
     *
     * @param postfix Postfix added to name of child log.
     * @return Child log.
     */
    public Log newChildLog(final String postfix) {
        final String childName = this.name + "." + postfix;
        return new Log(childName, this.depthString + "| ", this.pout);
    }

    /**
     * Creates an anonymous child log.
     *
     * @return Child log.
     */
    public Log newChildLog() {
        return newChildLog("#");
    }

    /**
     * Prints an info entry to the logs.
     *
     * @param message Message for user.
     */
    public void info(final String message) {

        final StringBuffer sb = new StringBuffer();

        final Date date = new Date();
        final FieldPosition fp =
            new FieldPosition(DateFormat.Field.DAY_OF_MONTH);
        sdf.format(date, sb, fp);

        sb.append(' ');
        sb.append(depthString);
        sb.append(message);

        plainInfo(sb.toString());
    }

    /**
     * Prints a message to the logs as it is given.
     *
     * @param message Message for user.
     */
    public void plainInfo(final String message) {

        synchronized (pout) {

            for (final PrintStream ps : pout) {
                ps.println(message);
                ps.flush();
            }
        }
    }

    /**
     * Registers a throwable (exceptions and errors) in the log.
     *
     * @param throwable Event to log.
     */
    public void register(final Throwable throwable) {

        synchronized (pout) {
            for (final PrintStream ps : pout) {
                throwable.printStackTrace(ps);
                ps.flush();
            }
        }
    }
}
