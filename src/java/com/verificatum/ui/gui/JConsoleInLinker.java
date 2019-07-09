
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

package com.verificatum.ui.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.swing.JTextArea;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;

import com.verificatum.ui.UIError;


/**
 * Links a <code>PipedInputStream</code> to read from a
 * <code>JTextArea</code>, i.e., anything typed into the text area is
 * written to the input stream.
 *
 * <b>WARNING! Read the documentation of {@link JTextualUI} before
 * use.</b>
 *
 * @author Douglas Wikstrom
 */
public final class JConsoleInLinker extends NavigationFilter
    implements KeyListener, SelectionListener {

    /**
     * Writer to the stream.
     */
    private OutputStreamWriter outWriter;

    /**
     * Source of inputs.
     */
    private final JTextArea jTextArea;

    /**
     * Internal buffer.
     */
    private StringBuilder buffer;

    /**
     * Creates an instance based on the given text area and stream.
     *
     * @param jTextArea Source of input.
     * @param pin Where inputs are written.
     */
    public JConsoleInLinker(final FixedJTextArea jTextArea,
                            final PipedInputStream pin) {

        try {
            outWriter = new OutputStreamWriter(new PipedOutputStream(pin));
        } catch (final IOException ioe) {
            throw new UIError("Unable to create output stream!", ioe);
        }
        this.jTextArea = jTextArea;

        buffer = new StringBuilder();
        jTextArea.setEditable(true);
        jTextArea.setNavigationFilter(this);
        jTextArea.addKeyListener(this);
        jTextArea.addSelectionListener(this);
    }

    // The following methods are documented in the super class or in
    // an interface.

    @Override
    public void setDot(final NavigationFilter.FilterBypass fb,
                       final int dot,
                       final Position.Bias bias) {
        final int tmp =
            Math.max(dot, jTextArea.getText().length() - buffer.length());
        super.setDot(fb, tmp, bias);
    }

    @Override
    public void moveDot(final NavigationFilter.FilterBypass fb,
                        final int dot,
                        final Position.Bias bias) {
        final int tmp =
            Math.max(dot, jTextArea.getText().length() - buffer.length());
        super.moveDot(fb, tmp, bias);
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        final int dot = jTextArea.getCaret().getDot();

        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            e.consume();
        }
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (dot <= (jTextArea.getText().length() - buffer.length())) {
                e.consume();
            } else {
                if (buffer.length() > 0) {
                    buffer.deleteCharAt(dot - jTextArea.getText().length()
                                        + buffer.length() - 1);
                }
            }
        }
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        final int dot = jTextArea.getCaret().getDot();

        if (dot <= (jTextArea.getText().length() - buffer.length())
            && e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            e.consume();
        }
    }

    @Override
    public void keyTyped(final KeyEvent e) {

        final char theChar = e.getKeyChar();
        final int dot = jTextArea.getCaret().getDot();

        if (theChar == '\n') {
            final int pos =
                buffer.length() - jTextArea.getText().length() + dot;

            buffer.insert(pos, '\n');
            String bufferString = buffer.toString();
            final int endIndex = bufferString.indexOf('\n');
            bufferString = bufferString.substring(0, endIndex + 1);

            try {
                outWriter.write(bufferString, 0, bufferString.length());
                outWriter.flush();
                buffer = new StringBuilder();
            } catch (final IOException ioe) {
                // Any exception conditions are caught on the reader
                // side.
                return;
            }
        }
    }

    @Override
    public void replaceSelection(final String content) {
        final int jTextLen = jTextArea.getText().length();
        final int bufLen = buffer.length();
        final int start = jTextArea.getSelectionStart() - jTextLen + bufLen;
        final int end = jTextArea.getSelectionEnd() - jTextLen + bufLen;

        buffer.replace(start, end, content);
    }
}
