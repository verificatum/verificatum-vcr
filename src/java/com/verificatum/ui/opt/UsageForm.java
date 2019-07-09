
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

package com.verificatum.ui.opt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Stores a representation of a family of possible ways to invoke a
 * command. This is a helper class for {@link Opt}.
 *
 * @author Douglas Wikstrom
 */
class UsageForm {

    /**
     * Associated Opt instance.
     */
    Opt opt;

    /**
     * Required options for this usage form.
     */
    Set<String> requiredOptions;

    /**
     * Required option names in a given order.
     */
    List<String> requiredOptionNames;

    /**
     * Optional options for this usage form.
     */
    Set<String> optionalOptions;

    /**
     * Required parameters for this usage form.
     */
    List<String> requiredParams;

    /**
     * Optional parameters for this usage form.
     */
    List<String> optionalParams;

    /**
     * Name of multiparameter if this is an admitted usage form.
     */
    String multiParam = null;

    /**
     * Number of required parameters.
     */
    int multiParamIndex;

    /**
     * Creates an empty usage form associated with the given
     * <code>Opt</code> instance.
     *
     * @param opt Instance with which this instance is associated.
     */
    UsageForm(final Opt opt) {
        this.opt = opt;
        requiredOptions = new TreeSet<String>();
        requiredOptionNames = new ArrayList<String>();
        optionalOptions = new TreeSet<String>();
        requiredParams = new ArrayList<String>();
        optionalParams = new ArrayList<String>();
    }

    /**
     * Returns true or false depending on if this usage form contains
     * the option or not.
     *
     * @param name Name of option.
     * @return Boolean indicating of the option exists or not.
     */
    boolean hasOption(final String name) {
        return requiredOptions.contains(name) || optionalOptions.contains(name);
    }

    /**
     * Returns true or false depending on if this usage form contains
     * the parameter or not.
     *
     * @param name Name of parameter.
     * @return Boolean indicating if the usage form has a parameter.
     */
    boolean hasParameter(final String name) {
        return requiredParams.contains(name)
            || optionalParams.contains(name);
    }

    /**
     * Returns the index of the parameter with the given name, where
     * the parameters are numbered starting from the required
     * parameters and continuing with the optional parameters.
     *
     * @param name Name of a parameter.
     * @return Index of the given parameter, or -1 if no parameter
     *         with the given name exists.
     */
    int parameterIndexFromName(final String name) {
        int index = requiredParams.indexOf(name);
        if (index >= 0) {
            return index;
        } else {
            index = optionalParams.indexOf(name);
            if (index >= 0) {
                return requiredParams.size() + index;
            }
        }
        return -1;
    }

    /**
     * Appends the given options and parameters to this
     * <code>UsageForm</code>. The options and parameters must not
     * duplicate existing options.
     *
     * @param ro Additional required options.
     * @param oo Additional optional options.
     * @param rp Additional required parameters.
     * @param op Additional optional parameters.
     */
    void append(final String[] ro,
                final String[] oo,
                final String[] rp,
                final String[] op) {

        final Set<String> ops = opt.options.keySet();
        carefulAddAll(requiredOptions, ro, ops);
        for (final String rop : ro) {
            if (!requiredOptionNames.contains(rop)) {
                requiredOptionNames.add(rop);
            }
        }
        carefulAddAll(optionalOptions, oo, ops);

        final Collection<String> c = new HashSet<String>(requiredOptions);
        c.addAll(optionalOptions);
        if (c.size() < requiredOptions.size() + optionalOptions.size()) {
            throw new OptError("An option is both required and optional!");
        }

        final Set<String> pars = opt.parameters.keySet();

        carefulAddAll(requiredParams, rp, pars);
        carefulAddAll(optionalParams, op, pars);

        if (multiParam != null && !optionalParams.isEmpty()) {
            throw new OptError("Can not have both multiparameter "
                               + "and optional parameters!");
        }
    }

    /**
     * Strips the given options and parameters from this
     * <code>UsageForm</code>.
     *
     * @param ro Removed required options.
     * @param oo Removed optional options.
     * @param rp Removed required parameters.
     * @param op Removed optional parameters.
     */
    void remove(final String[] ro,
                final String[] oo,
                final String[] rp,
                final String[] op) {
        carefulRemoveAll(requiredOptions, ro);
        carefulRemoveAll(optionalOptions, oo);
        carefulRemoveAll(requiredParams, rp);
        carefulRemoveAll(optionalParams, op);
    }

    /**
     * Adds all strings in the given array to the collection of
     * strings.
     *
     * @param ts Set to which the strings are added.
     * @param sa Strings to be added.
     * @param possibleNames Set of all valid names.
     */
    void carefulAddAll(final Collection<String> ts,
                       final String[] sa,
                       final Set<String> possibleNames) {
        for (String s : sa) {
            if (s.charAt(0) == '+') {
                s = s.substring(1);
                if (multiParam != null) {
                    throw new OptError("Can not add another multi-parameter!");
                }
                multiParam = s;
                multiParamIndex = ts.size();
            }
            if (!possibleNames.contains(s)) {
                throw new OptError("Can not add " + s + " to usage form."
                                   + " It does not exist as option or "
                                   + "parameter!");
            }

            // Ignore doubles.
            if (!"".equals(s) && !ts.contains(s)) {
                ts.add(s);
            }
        }
    }

    /**
     * Removes all strings in the array from the collection and throws
     * an error if there is a string in the array that is not in the
     * collection.
     *
     * @param ts Set from which the strings are removed.
     * @param sa Strings to be removed.
     */
    void carefulRemoveAll(final Collection<String> ts, final String[] sa) {
        for (final String s : sa) {
            if (ts.contains(s)) {
                ts.remove(s);
            } else {
                throw new OptError("Failed to remove non-existent option! ("
                                   + s + ")");
            }
        }
    }

    /**
     * Formats the options and writes the result to the given
     * destination.
     *
     * @param res Destination of formatted output.
     * @param ordered Ordered names of options.
     * @param ts Source of options.
     * @param optional Determines if the options should be formatted
     * as optional or not.
     */
    void writeOptions(final List<String> res,
                      final Collection<String> ordered,
                      final Set<String> ts,
                      final boolean optional) {

        for (final String name : ordered) {

            final StringBuilder sb = new StringBuilder();
            if (optional) {
                sb.append('[');
            }
            sb.append(name);
            final Option option = opt.options.get(name);
            if (!"".equals(option.valueName)) {
                sb.append(" <" + option.valueName + ">");
            }
            if (optional) {
                sb.append(']');
            }
            res.add(sb.toString());
        }
    }

    /**
     * Formats the parameters and writes the result to the given
     * destination.
     *
     * @param res Destination of formatted output.
     * @param ts Source of parameters.
     * @param optional Determines if the parameters should be
     * formatted as optional or not.
     */
    void writeParams(final List<String> res,
                     final Collection<String> ts,
                     final boolean optional) {
        for (final String param : ts) {

            final StringBuilder sb = new StringBuilder();
            if (optional) {
                sb.append('[');
            }
            sb.append("<" + param + ">");
            if (param.equals(multiParam)) {
                sb.append(" ...");
            }
            if (optional) {
                sb.append(']');
            }
            res.add(sb.toString());
        }
    }

    /**
     * Returns a space string of the given length.
     *
     * @param indent Length of indent string.
     * @return String containing the given number of spaces.
     */
    private String indentString(final int indent) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Break lines keeping blocks intact.
     *
     * @param sb Destination of lines.
     * @param blocks Usage information chopped into blocks meant to
     * stick together.
     * @param currentWidth Current width of active line.
     * @param lineWidth Width of lines printed.
     * @param indentString String used to indent lines after the first
     * line.
     */
    private void breakLines(final StringBuilder sb,
                            final List<String> blocks,
                            final int currentWidth,
                            final int lineWidth,
                            final String indentString) {

        int cw;

        if (!blocks.isEmpty() && currentWidth == 0) {
            sb.append(indentString);
            cw = indentString.length();
        } else {
            cw = currentWidth;
        }

        for (final String block : blocks) {

            if (cw + 1 + block.length() > lineWidth) {

                sb.append('\n');
                sb.append(indentString);
                sb.append(' ');
                sb.append(block);

                cw = indentString.length() + block.length();

            } else {

                sb.append(' ');
                sb.append(block);
                cw += 1 + block.length();
            }
        }
        if (!blocks.isEmpty()) {
            sb.append('\n');
        }
    }

    /**
     * Returns a formatted description string of the usage form
     * represented by this instance.
     *
     * @param lineWidth Width of line.
     * @return Representation of this usage form.
     */
    public String toString(final int lineWidth) {

        final ArrayList<String> blocks = new ArrayList<String>();
        writeOptions(blocks, requiredOptionNames, requiredOptions, false);

        final ArrayList<String> blocksOO = new ArrayList<String>();
        writeOptions(blocksOO, optionalOptions, optionalOptions, true);

        final ArrayList<String> blocksRP = new ArrayList<String>();
        writeParams(blocksRP, requiredParams, false);

        final ArrayList<String> blocksOP = new ArrayList<String>();
        writeParams(blocksOP, optionalParams, true);

        final StringBuilder sb = new StringBuilder();

        sb.append(' ');
        sb.append(opt.commandName);

        final int length = sb.length();

        final String indentString = indentString(sb.length());

        breakLines(sb, blocks, sb.length(), lineWidth, indentString);
        breakLines(sb, blocksOO, 0, lineWidth, indentString);
        breakLines(sb, blocksRP, 0, lineWidth, indentString);
        breakLines(sb, blocksOP, 0, lineWidth, indentString);

        if (sb.length() == length) {
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Returns <code>true</code> or <code>false</code> depending on if
     * this instance matches/does not match, the information parsed by
     * the <code>Opt</code> instance associated with this instance.
     *
     * @return <code>true</code> or <code>false</code> depending on if
     *         this usage form matches the values given in the
     *         associated option instance.
     */
    boolean matches() {
        for (final String name : requiredOptions) {
            if (!opt.givenOptions.containsKey(name)) {
                return false;
            }
        }
        for (final String name : opt.givenOptions.keySet()) {
            if (!requiredOptions.contains(name)
                && !optionalOptions.contains(name)) {
                return false;
            }
        }
        if (requiredParams.size() > opt.givenParameters.size()) {
            return false;
        }
        if (multiParam != null) {
            return true;
        }
        return opt.givenParameters.size()
            <= requiredParams.size() + optionalParams.size();
    }
}
