
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

package com.verificatum.ui.opt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.verificatum.ui.Util;


/**
 * Simple command line parser. It allows the definition both of
 * options that take parameters and options that do not take
 * parameters. It also allows definition of named parameters. The
 * possible ways to invoke the command under consideration are divided
 * into families. Each family is represented by a "usage form"
 * implemented by {@link UsageForm}. A usage form defines a set of
 * required options, a set of optional options, a set of required
 * parameters, and a set of optional parameters. It can also handle
 * variable number of parameters.
 *
 * <p>
 *
 * Options are assumed to start with a hyphen, e.g., -t, or
 * -my_option.
 *
 * <p>
 *
 * After parsing command line arguments the first matching usage form
 * is used to interpret the inputs. Then the inputs can be accessed
 * using the names of options or parameters respectively.
 *
 * <p>
 *
 * Usage forms can be built up by appending new options and parameters
 * to an existing usage form. This allows subclasses to add new
 * options and parameters to those assumed to exist by a superclass.
 *
 * <p>
 *
 * Finally, a usage description of the command under consideration can
 * be generated.
 *
 * @author Douglas Wikstrom
 */
public final class Opt {

    /**
     * Type of an option.
     */
    public enum Type {

        /**
         * <code>String</code> option.
         */
        STRING,

        /**
         * <code>int</code> option.
         */
        INT
    };

    /**
     * Line width of descriptions.
     */
    public static final int LINE_WIDTH = 78;

    /**
     * Line width subdescriptions.
     */
    public static final int SUBLINE_WIDTH = 59;

    /**
     * Name of command of which we parse the command line. In a
     * typical application the execution of this class is wrapped in a
     * shell script. The name of the shell script is used to generate
     * proper usage information.
     */
    String commandName;

    /**
     * All possible options of the command.
     */
    Map<String, Option> options;

    /**
     * All possible parameters of the command.
     */
    Map<String, Parameter> parameters;

    /**
     * All possible usage forms of the command.
     */
    private final List<UsageForm> usageForms;

    /**
     * Actual options extracted from a command line.
     */
    Map<String, List<String>> givenOptions;

    /**
     * Actual parameters extracted from a command line.
     */
    List<String> givenParameters;

    /**
     * First usage form that matches the actual options and parameters
     * extracted from the command line.
     */
    private UsageForm uf;

    /**
     * Description of this command.
     */
    String description;

    /**
     * Brief comment on usage forms.
     */
    private String usageComment;

    /**
     * Default error string printed when called without options.
     */
    private final String defaultErrorString;

    /**
     * Stores hidden ad-hoc handled debug flags.
     */
    private final Map<String, String> debugMap;

    /**
     * Creates a command line parser for a given command name.
     *
     * @param commandName Name of the command.
     * @param defaultErrorString Default error string output when
     * command line parameters are wrong.
     */
    public Opt(final String commandName, final String defaultErrorString) {
        this.commandName = commandName;
        this.defaultErrorString = defaultErrorString;
        this.description = "";
        options = new TreeMap<String, Option>();
        parameters = new TreeMap<String, Parameter>();
        givenOptions = new TreeMap<String, List<String>>();
        givenParameters = new ArrayList<String>();
        usageForms = new ArrayList<UsageForm>();
        usageComment = "";
        debugMap = new TreeMap<String, String>();
    }

    /**
     * Expands the description.
     *
     * @param description Information to be added to the description.
     */
    public void appendDescription(final String description) {
        this.description += description;
    }

    /**
     * Replaces the description.
     *
     * @param description New description.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Sets a brief comment for the usage forms.
     *
     * @param usageComment Comment.
     */
    public void setUsageComment(final String usageComment) {
        this.usageComment = usageComment;
    }

    /**
     * Adds an option with the given name, value-name, and
     * description. The value-name is used to illustrate an option
     * parameter in the usage description.
     *
     * @param name Name of option, e.g., -f, or -t.
     * @param valueName Name used to illustrate the value of the
     * option. This must be the empty string if the option
     * does not take any parameter, and it must not be the
     * empty string if the option takes a parameter.
     * @param description Description of this option.
     */
    public void addOption(final String name,
                          final String valueName,
                          final String description) {
        options.put(name, new Option(description, valueName));
    }

    /**
     * Returns <code>true</code> or <code>false</code> depending on if
     * there is an option with the given name.
     *
     * @param name Name of candidate option.
     * @return <code>true</code> or <code>false</code> depending on if
     *         there is an option with the given name.
     */
    public boolean hasOption(final String name) {
        return options.containsKey(name);
    }

    /**
     * Adds an option with the given name, value-name, and
     * description. The value-name is used to illustrate an option
     * parameter in the usage description.
     *
     * @param name Name of option, e.g., -f, or -t.
     * @param valueName Name used to illustrate the value of the
     * option. This must be the empty string if the option
     * does not take any parameter, and it must not be the
     * empty string if the option takes a parameter.
     * @param description Description of this option.
     * @param optionType Type of option added.
     */
    public void addOption(final String name,
                          final String valueName,
                          final String description,
                          final Opt.Type optionType) {
        options.put(name, new Option(description, valueName, optionType));
    }

    /**
     * Adds a named parameter with the given name and description.
     *
     * @param name Name used to illustrate the value of the parameter
     * in the usage description. This should be the empty
     * string if the option does not take any parameter.
     * @param description Description of this parameter.
     */
    public void addParameter(final String name, final String description) {
        parameters.put(name, new Parameter(description));
    }

    /**
     * Adds an additional usage form to this instance.
     */
    public void addUsageForm() {
        usageForms.add(new UsageForm(this));
    }

    /**
     * Removes the given usage form from this instance.
     *
     * @param index Remove the usage form of the given index.
     */
    public void removeUsageForm(final int index) {
        usageForms.remove(index);
    }

    /**
     * Adds the given number of additional usage forms to this
     * instance.
     *
     * @param n Number of usage forms to add.
     */
    public void addUsageForms(final int n) {
        for (int i = 0; i < n; i++) {
            addUsageForm();
        }
    }

    /**
     * Extends the <code>index</code>th usage form with additional
     * options and parameters.
     *
     * @param index Index of a usage form.
     * @param oap Options and parameters.
     */
    public void appendToUsageForm(final int index, final String[][] oap) {
        usageForms.get(index).append(oap[0], oap[1], oap[2], oap[3]);
    }

    /**
     * Strips a string from leading or trailing commas.
     *
     * @param s Original string.
     * @return Cleansed string.
     */
    String cleanString(final String s) {

        int i = 0;
        while (i < s.length() && ',' == s.charAt(i)) {
            i++;
        }

        int j = s.length() - 1;
        while (j > i && ',' == s.charAt(j)) {
            j--;
        }
        return s.substring(i, j + 1);
    }

    /**
     * Parses a string representation of options and parameters into
     * an array of array of options.
     *
     * @param oap Options and parameters string. The options and
     * parameters string should be given on the following form:
     * <required options>#<optional options>#<required
     * parameters>#<optional parameters>, where each group is a
     * comma-separated list of options and parameters.
     * @return Array of array of options in the order required
     * options, optional options, required parameters, and optional
     * parameters.
     */
    public String[][] parseOptionsAndParameters(final String oap) {
        final String[] a = Util.split(oap, "#");
        if (a.length != 4) {
            throw new OptError("Incorrect number of option and "
                               + "parameter groups!");
        }
        final String[][] result = new String[4][];
        for (int i = 0; i < result.length; i++) {
            result[i] = Util.split(cleanString(a[i]), ",");
        }
        return result;
    }

    /**
     * Extends the <code>index</code>th usage form with additional
     * options and parameters.
     *
     * @param index Index of a usage form.
     * @param oap Additional options and parameters. The options and
     * parameters string should be given on the following form:
     * <required options>#<optional options>#<required
     * parameters>#<optional parameters>, where each group is a
     * comma-separated list of options and parameters.
     */
    public void appendToUsageForm(final int index,
                                  final String oap) {
        appendToUsageForm(index, parseOptionsAndParameters(oap));
    }

    /**
     * Removes the <code>index</code>th usage form with additional
     * options and parameters. This can be used to restrict the user
     * interface of an existing command.
     *
     * @param index Index of a usage form.
     * @param oap Options and parameters.
     */
    public void removeFromUsageForm(final int index, final String[][] oap) {
        usageForms.get(index).remove(oap[0], oap[1], oap[2], oap[3]);
    }

    /**
     * Removes the <code>index</code>th usage form with additional
     * options and parameters. This can be used to restrict the user
     * interface of an existing command.
     *
     * @param index Index of a usage form.
     * @param oap Options and parameters.
     */
    public void removeFromUsageForm(final int index, final String oap) {
        removeFromUsageForm(index, parseOptionsAndParameters(oap));
    }

    /**
     * Cleans this instance from unused options and parameters.
     */
    public void cleanOptionsAndParameters() {
        for (final String name : options.keySet()) {
            boolean remove = true;
            for (final UsageForm usageForm : usageForms) {
                if (!usageForm.hasOption(name)) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                options.remove(name);
            }
        }
        for (final String name : parameters.keySet()) {
            boolean remove = true;
            for (final UsageForm usageForm : usageForms) {
                if (!usageForm.hasParameter(name)) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                parameters.remove(name);
            }
        }
    }

    /**
     * Returns a usage string for the command for which this instance
     * is used.
     *
     * @return Usage description.
     */
    public String usage() {
        final StringBuilder sb = new StringBuilder();

        sb.append("Usage: \n");
        for (final UsageForm uf : usageForms) {
            sb.append(uf.toString(LINE_WIDTH));
        }

        if ("".equals(usageComment)) {
            sb.append(usageComment).append('\n');
        }

        sb.append("\nDescription:\n\n");
        sb.append(Util.breakLines(description, LINE_WIDTH));
        sb.append('\n');

        if (parameters.keySet().size() > 0) {
            sb.append("\nParameters:\n");

            for (final Map.Entry<String, Parameter> entry
                     : parameters.entrySet()) {
                entry.getValue().write(sb, entry.getKey());
                sb.append('\n');
            }
        }
        if (options.keySet().size() > 0) {
            sb.append("\nOptions:\n");
            for (final Map.Entry<String, Option> entry
                     : options.entrySet()) {
                entry.getValue().write(sb, entry.getKey());
                sb.append('\n');
            }
        }

        return sb.toString();
    }

    /**
     * Store an option value.
     *
     * @param name Name of option.
     * @param value Value to store.
     */
    public void storeOptionValue(final String name, final String value) {
        if (!givenOptions.containsKey(name)) {
            givenOptions.put(name, new ArrayList<String>());
        }
        givenOptions.get(name).add(0, value);
    }

    /**
     * Magic processing of the arguments to remove any debug flag and
     * set the debug string.
     *
     * @param args Command line parameters.
     * @return Command line parameters except that a debug flag is
     * removed.
     */
    public String[] processDebugFlags(final String[] args) {

        final List<String> newList = new ArrayList<String>();

        for (int i = 0; i < args.length; i++) {

            if (args[i].startsWith("-debug")) {

                if (i < args.length - 1 && '-' != args[i + 1].charAt(0)) {
                    debugMap.put(args[i], args[i + 1]);
                    i++;
                } else {
                    final String f = "Debug flag without value! (%s)";
                    throw new OptError(String.format(f, args[i]));
                }

            } else {
                newList.add(args[i]);
            }
        }

        return newList.toArray(new String[newList.size()]);
    }

    /**
     * Returns the value of a debug flag.
     *
     * @param flag Debug flag.
     * @return String value of a debug flag.
     */
    public String getDebugStringValue(final String flag) {
        if (debugMap.containsKey(flag)) {
            return debugMap.get(flag);
        } else {
            return null;
        }
    }

    /**
     * Determines if the given debug flag has been given.
     *
     * @param flag Debug flag.
     * @return True or false depending on if a given debug flag has
     * been given.
     */
    public boolean getDebugBooleanValue(final String flag) {
        final String value = getDebugStringValue(flag);

        if (value == null || "f".equals(value)) {
            return false;
        } else if ("t".equals(value)) {
            return true;
        } else {
            final String e =
                "Attempting to read a boolean value from a non-boolean string!";
            throw new OptError(e);
        }
    }

    /**
     * Parse the given command line parameters and store the result.
     *
     * @param args Command line parameters.
     * @throws OptException If the parsing fails, in which case the
     *  message string in the exception explains why it
     *  failed.
     */
    public void parse(final String[] args) throws OptException {
        parse(args, 0);
    }

    /**
     * Parse the first command line parameters and store the result.
     * Parse until <code>noParameters</code> have been found.
     *
     * @param args Command line parameters.
     * @param maxNoParameters Number of parameters that decides when
     * to return, i.e., we parse until
     * <code>maxNoParameters</code> are encountered and
     * ignore the remainder of the command line arguments.
     * @return Parsed command line entries.
     * @throws OptException If the parsing fails, in which case the
     *  message string in the exception explains why it
     *  failed.
     */
    public int parse(final String[] args, final int maxNoParameters)
        throws OptException {

        final String[] newargs = processDebugFlags(args);

        String currentOption = null;

        int noArgsConsidered = 0;

        int noParameters = 0;

        for (final String arg : newargs) {

            if (currentOption != null) { // NOPMD
                if (arg.charAt(0) == '-') {
                    throw new OptException("Expected a parameter for option "
                                           + currentOption + "!");
                } else {
                    storeOptionValue(currentOption, arg);
                    currentOption = null;
                }
            } else if (options.containsKey(arg)) {
                final Option option = options.get(arg);
                if ("".equals(option.valueName)) {
                    storeOptionValue(arg, null);
                } else {
                    currentOption = arg;
                }
            } else if (arg.charAt(0) == '-') {
                throw new OptException("Unknown option: " + arg + "!");
            } else {
                givenParameters.add(arg);
                noParameters++;
                if (maxNoParameters != 0 && noParameters >= maxNoParameters) {
                    break;
                }
            }
            noArgsConsidered++;
        }

        if (currentOption != null) {
            throw new OptException("Expected parameter for option "
                                   + currentOption + "!");
        }

        validate();

        return noArgsConsidered;
    }

    /**
     * Verifies that there exists a usage form that matches the
     * options and parameters extracted from the command line, and if
     * this is the case store the matching usage form internally.
     *
     * @throws OptException If no matching usage form exists.
     */
    protected void validate() throws OptException {
        if (uf == null) {
            for (final UsageForm ufs : usageForms) {
                if (ufs.matches()) {
                    this.uf = ufs;
                    return;
                }
            }
            throw new OptException(defaultErrorString);
        }

        for (final Map.Entry<String, Option> entry
                 : options.entrySet()) {
            if (entry.getValue().optionType == Opt.Type.INT) {
                final String name = entry.getKey();
                try {
                    Integer.parseInt(givenOptions.get(name).get(0));
                } catch (final NumberFormatException nfe) {
                    throw new OptException("Option " + name
                                           + " takes an integer parameter!",
                                           nfe);
                }
            }
        }
        final List<String> tmp = new ArrayList<String>(uf.requiredParams);
        tmp.addAll(uf.optionalParams);

        for (int i = 0; i < givenParameters.size(); i++) {
            final String name = tmp.get(i);
            final Parameter p = parameters.get(name);
            if (p.paramType == Opt.Type.INT) {
                try {
                    Integer.parseInt(givenParameters.get(i));
                } catch (final NumberFormatException nfe) {
                    throw new OptException("Parameter " + name
                                           + " must be an integer!",
                                           nfe);
                }
            }
        }
    }

    /**
     * Throws an error if this instance is not validated.
     */
    protected void checkValidated() {
        if (uf == null) {
            throw new OptError("Attempting extraction from unvalidated "
                               + "instance!");
        }
    }

    /**
     * Returns <code>true</code> or <code>false</code> depending on if
     * the given option or named parameter was extracted from the
     * command line.
     *
     * @param name Name of option or parameter.
     * @return <code>true</code> or <code>false</code> depending on if
     *         a value named <code>name</code> was given.
     */
    public boolean valueIsGiven(final String name) {
        checkValidated();
        final int ufi = uf.parameterIndexFromName(name);
        return givenOptions.containsKey(name)
            || 0 <= ufi && ufi < givenParameters.size();
    }

    /**
     * Returns the value of the given option or named parameter, if
     * one exists. If more than one value exists, the last value
     * listed on the command line is returned.
     *
     * @param name Name of option or parameter.
     * @return Value of the given option.
     */
    public String getStringValue(final String name) {
        if (!valueIsGiven(name)) {
            throw new OptError("Attempting to access option or parameter ("
                               + name + ") not given!");
        }
        if (givenOptions.containsKey(name)) {
            return givenOptions.get(name).get(0);
        } else {
            int index = uf.parameterIndexFromName(name);
            if (uf.multiParam != null
                && index > uf.multiParamIndex) {
                index += givenParameters.size() - uf.requiredParams.size();
            }
            return givenParameters.get(index);
        }
    }

    /**
     * Returns array of all parameters.
     *
     * @return Array of parameters.
     */
    public String[] getMultiParameters() {
        checkValidated();
        if (uf.multiParam == null) {
            throw new OptError("No multiparameter is given!");
        }

        final int size = givenParameters.size();

        final String[] pa = givenParameters.toArray(new String[size]);
        final int len = givenParameters.size() - uf.requiredParams.size() + 1;
        return Arrays.copyOfRange(pa,
                                  uf.multiParamIndex,
                                  uf.multiParamIndex + len);
    }

    /**
     * Returns the value of the given option or named parameter, if
     * one exists. If more than one value exists, the last value
     * listed on the command line is returned.
     *
     * @param name Name of option or parameter.
     * @param defaultValue Value returned if not option was given.
     * @return Value of the given option.
     */
    public String getStringValue(final String name, final String defaultValue) {
        if (valueIsGiven(name)) {
            return getStringValue(name);
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns a list of all values of the given option or named
     * parameter, if one exists. This can be used if the user repeats
     * the same option with different parameters.
     *
     * @param name Name of option or parameter.
     * @return Value of the given option.
     */
    public List<String> getStringValues(final String name) {
        checkValidated();
        if (givenOptions.containsKey(name)) {
            return givenOptions.get(name);
        } else {
            throw new OptError("Attempting to access option or parameter ("
                               + name + ") not given!");
        }
    }

    /**
     * Returns the value of the given option or named parameter, if
     * one exists. If more than one value exists, the last value
     * listed on the command line is returned.
     *
     * @param name Name of option or parameter.
     * @return Value of the given option.
     */
    public int getIntValue(final String name) {
        final String value = getStringValue(name);
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException nfe) {
            throw new OptError("Value \"" + value + "\" is not an integer!",
                               nfe);
        }
    }

    /**
     * Returns the value of the given option or named parameter, if
     * one exists. If more than one value exists, the last value
     * listed on the command line is returned.
     *
     * @param name Name of option or parameter.
     * @param defaultValue Value returned if not option was given.
     * @return Value of the given option.
     */
    public int getIntValue(final String name, final int defaultValue) {
        if (valueIsGiven(name)) {
            return getIntValue(name);
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns <code>true</code> or <code>false</code> depending on if
     * the given option was given on the command line or not. This is
     * used for options that do not take parameters. Note that this
     * can not be called with a named parameter.
     *
     * @param name Name of option or parameter.
     * @return <code>true</code> or <code>false</code> depending on if
     *         the given option was given on the command line.
     */
    public boolean getBooleanValue(final String name) {
        checkValidated();
        return givenOptions.containsKey(name);
    }

    /**
     * Returns the index of the usage form that matches the options
     * and parameters extracted from the command line.
     *
     * @return Index of matching usage form.
     */
    public int getUsageFormIndex() {
        checkValidated();
        return usageForms.indexOf(uf);
    }
}

/**
 * Container class of an option.
 *
 * @author Douglas Wikstrom
 */
class Option {

    /**
     * Description of option.
     */
    String description;

    /**
     * String used to indicate value when printing description.
     */
    String valueName;

    /**
     * Type of option.
     */
    Opt.Type optionType;

    /**
     * Creates an option.
     *
     * @param description Description of option.
     * @param valueName String used to indicate value when printing
     * description.
     */
    Option(final String description, final String valueName) {
        this.description = description;
        this.valueName = valueName;
        this.optionType = Opt.Type.STRING;
    }

    /**
     * Creates an option.
     *
     * @param description Description of option.
     * @param valueName String used to indicate value when printing
     * description.
     * @param optionType Type of option.
     */
    Option(final String description,
           final String valueName,
           final Opt.Type optionType) {
        this.description = description;
        this.valueName = valueName;
        this.optionType = optionType;
    }

    /**
     * Write description of option to the given string builder.
     *
     * @param sb Destination of description.
     * @param name Name of option.
     */
    void write(final StringBuilder sb, final String name) {
        final Formatter f = new Formatter(sb, Locale.US);

        String s;

        if ("".equals(valueName)) {
            s = "";
        } else {
            s = "<" + valueName + ">";
        }

        if ("".equals(description)) {
            f.format("%10s %-8s", name, s);
        } else {
            final String broken = Util.breakLines(description,
                                                  Opt.SUBLINE_WIDTH);
            final String[] lines = Util.split(broken, "\n");
            f.format("%10s %-8s - %s", name, s, lines[0]);
            for (int i = 1; i < lines.length; i++) {
                f.format("%n%10s %-8s   %s", "", "", lines[i]);
            }
        }
    }
}

/**
 * Container class of a parameter.
 *
 * @author Douglas Wikstrom
 */
class Parameter {

    /**
     * Description of parameter.
     */
    String description;

    /**
     * Type of parameter.
     */
    Opt.Type paramType;

    /**
     * Create a parameter with the given description.
     *
     * @param description Description of parameter.
     */
    Parameter(final String description) {
        this.description = description;
    }

    /**
     * Create a parameter with the given description.
     *
     * @param description Description of parameter.
     * @param paramType Type of parameter.
     */
    Parameter(final String description, final Opt.Type paramType) {
        this.description = description;
        this.paramType = paramType;
    }

    /**
     * Writes description of param.
     *
     * @param sb Destination of description.
     * @param name Name of parameter.
     */
    void write(final StringBuilder sb, final String name) {
        final Formatter f = new Formatter(sb, Locale.US);

        final String broken = Util.breakLines(description, 57);
        final String[] lines = Util.split(broken, "\n");
        f.format("%2s %-16s - %s", "", "<" + name + ">", lines[0]);
        for (int i = 1; i < lines.length; i++) {
            f.format("%n%2s %-16s   %s", "", "", lines[i]);
        }
    }
}
