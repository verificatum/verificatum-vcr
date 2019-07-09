
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

package com.verificatum.ui.gen;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.verificatum.eio.ByteTree;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.ExtIO;
import com.verificatum.eio.Marshalizable;
import com.verificatum.eio.Marshalizer;
import com.verificatum.ui.Util;


// NATIVE
/**
 * Template class used to store instructions for how to generate an
 * object. WARNING! This is mainly for debugging and demonstrations,
 * and may not be platform independent.
 *
 * @author Douglas Wikstrom
 */
public final class GeneratorTemplate implements Marshalizable {

    /**
     * Size of buffer used to read outputs of commands.
     */
    static final int OUTPUT_BUFFER_SIZE = 1024;

    /**
     * Signifies that this instance holds a shell command.
     */
    public static final int CMD = 0;

    /**
     * Signifies that this instance contains data that should simply
     * be copied when this instance is executed.
     */
    public static final int CPY = 1;

    /**
     * Type of instance, i.e., a shell command or a copy.
     */
    private final int type;

    /**
     * Encapsulated shell command or data to be copied.
     */
    private final String data;

    /**
     * Creates an instance.
     *
     * @param type Type of template.
     * @param data Data to be encapsulated.
     */
    public GeneratorTemplate(final int type, final String data) {
        this.data = data;
        this.type = type;
    }

    /**
     * Execute the given generator template.
     *
     * @param humanHex Description of generator template to be
     * executed.
     * @return Resulting instance.
     *
     * @throws GenException If the template can not be executed.
     */
    public static String execute(final String humanHex) throws GenException {
        try {
            final GeneratorTemplate gt =
                Marshalizer.unmarshalHex_GeneratorTemplate(humanHex);
            return gt.execute();
        } catch (final EIOException eioe) {
            throw new GenException("Unable to execute template!", eioe);
        }
    }

    /**
     * Execute this generator template.
     *
     * @return Resulting instance.
     *
     * @throws GenException If this template can not be executed.
     */
    public String execute() throws GenException {

        if (type == CMD) {
            final Runtime runtime = Runtime.getRuntime();
            Process proc = null;
            InputStream procin = null;
            OutputStream procout = null;
            InputStream procerr = null;
            try {

                proc = runtime.exec(data);

                final StringBuffer sb = new StringBuffer(OUTPUT_BUFFER_SIZE);

                procin = proc.getInputStream();
                procout = proc.getOutputStream();
                procerr = proc.getErrorStream();

                int ch = procin.read();
                while (ch != -1) {
                    sb.append((char) ch);
                    ch = procin.read();
                }
                return sb.toString();

            } catch (final IOException ioe) {
                throw new GenException("Unable to execute template!", ioe);
            } finally {
                ExtIO.strictClose(procin);
                ExtIO.strictClose(procout);
                ExtIO.strictClose(procerr);
            }
        } else {
            return data;
        }
    }

    /**
     * Returns the generator template corresponding to the input.
     *
     * @param btr Representation of a generator template.
     * @return Generator template.
     *
     * @throws EIOException If the input does not represent an
     *  instance.
     */
    public static GeneratorTemplate newInstance(final ByteTreeReader btr)
        throws EIOException {
        final int type = btr.getNextChild().readInt();
        final String data = btr.getNextChild().readString();
        return new GeneratorTemplate(type, data);
    }

    // Documented in Marshalizable.java

    @Override
    public ByteTree toByteTree() {
        return new ByteTree(ByteTree.intToByteTree(type),
                            ByteTree.stringToByteTree(data));
    }

    @Override
    public String humanDescription(final boolean verbose) {
        return Util.className(this, verbose) + "(" + data + ")";
    }
}
