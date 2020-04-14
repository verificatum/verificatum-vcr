
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.verificatum.crypto.CryptoException;
import com.verificatum.crypto.PRG;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.ExtIO;
import com.verificatum.eio.Hex;
import com.verificatum.eio.Marshalizer;
import com.verificatum.eio.TempFile;
import com.verificatum.ui.Util;
import com.verificatum.ui.opt.Opt;
import com.verificatum.ui.opt.OptException;
import com.verificatum.ui.opt.OptUtil;


/**
 * Uniform command line interface to all classes in the packages
 * <code>com.verificatum.arithm</code> and <code>com.verificatum.crypto</code>
 * that implements the interface {@link Generator} and follows the
 * convention of naming the generator
 * <code>&lt;classname&gt;Gen</code>, e.g., the generator of
 * {@link com.verificatum.crypto.PRGElGamal} is
 * {@link com.verificatum.crypto.PRGElGamalGen}.
 *
 * @author Douglas Wikstrom
 */
public final class GeneratorTool {

    /**
     * Maximal number of bytes in seed file. This is only used as a
     * defensive upper bound.
     */
    public static final int MAX_SEED_SIZE = 10000;

    /**
     * Name of class name parameters.
     */
    public static final String CLASSNAME = "classname";

    /**
     * Avoid accidental instantiation.
     */
    private GeneratorTool() { }

    /**
     * List of packages where the tool searches for qualified names.
     */
    static final List<String> PACKAGES = new ArrayList<String>();

    /**
     * Class names that identify the packages where the tool searches
     * for qualified names.
     */
    static final List<String> CLS_NAMES = new ArrayList<String>();

    // We list one class from each package to be included.
    static {
        CLS_NAMES.add("com.verificatum.crypto.PRG");
        CLS_NAMES.add("com.verificatum.arithm.LargeInteger");
    }

    /**
     * Creates a default option instance containing the options -h and
     * -v for help and verbose outputs respectively.
     *
     * @param commandName Command name used when printing usage
     * information, i.e., when a java invokation is wrapped
     * in a shell script, this would typically be the name
     * of the shell script.
     * @param ways Number of usage forms.
     * @return Default option instance.
     */
    public static Opt defaultOpt(final String commandName, final int ways) {

        final String defaultErrorString = "Invalid usage form, please use \""
            + commandName + " -h\" for usage information!";

        final Opt opt = new Opt(commandName, defaultErrorString);
        opt.addOption("-h", "", "Print usage information.");
        opt.addOption("-v", "", "Verbose human readable description.");

        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h###");

        for (int i = 1; i <= ways; i++) {
            opt.addUsageForm();
            opt.appendToUsageForm(i, "#-v##");
        }

        return opt;
    }

    /**
     * Performs default processing of the command line arguments.
     *
     * @param opt Option instance to use for parsing.
     * @param args Command line arguments.
     * @return Returns usage information if the <code>-h</code> option
     * is used and otherwise it returns <code>null</code>.
     * @throws GenException If the default processing fails.
     */
    public static String defaultProcess(final Opt opt, final String[] args)
        throws GenException {
        try {
            opt.parse(args);
        } catch (final OptException oe) {
            throw new GenException(oe.getMessage(), oe);
        }
        if (opt.getBooleanValue("-h")) {
            return opt.usage();
        }
        return null;
    }

    /**
     * Formats the description of the class with the given name.
     *
     * @param f Formatter used.
     * @param fullName Full name of a class implementing
     * {@link Generator}.
     */
    protected static void formatGeneratorDescription(final Formatter f,
                                                     final String fullName) {
        try {
            final Class<?> klass = Class.forName(fullName);
            final String description = ((Generator) klass.newInstance())
                .briefDescription();

            final String broken = Util.breakLines(description, 75);
            final String[] lines = Util.split(broken, "\n");
            f.format("* %s%n  %s",
                     fullName.substring(0, fullName.length() - 3), lines[0]);
            for (int i = 1; i < lines.length; i++) {
                f.format("%n  %s", lines[i]);
            }
        } catch (final ClassNotFoundException cnfe) {
            throw new GenError("Class can not be found!", cnfe);
        } catch (final InstantiationException ie) {
            throw new GenError("No default constructor!", ie);
        } catch (final IllegalAccessException iae) {
            throw new GenError("Can not access class!", iae);
        }
    }

    /**
     * Creates the option instance of this tool.
     *
     * @param commandName Command name used when printing usage
     * information, i.e., when a java invokation is wrapped
     * in a shell script, this would typically be the name
     * of the shell script.
     * @return Option instance of this tool.
     */
    protected static Opt opt(final String commandName) {
        final String defaultErrorString = "Invalid usage form, please use \""
            + commandName + " -h\" for usage information!";
        final Opt opt = new Opt(commandName, defaultErrorString);

        opt.addParameter(CLASSNAME, "Name of class that allows generation.");
        opt.addParameter("parameters",
                         "Parameters of generator of class named <classname>.");
        opt.addParameter("shellcmd", "Shell command turned into template.");

        opt.addOption("-h", "", "Print usage information.");
        opt.addOption("-e", "", "Print exception trace upon error.");
        opt.addOption("-cerr", "",
                      "Print error messages as clean strings without any "
                      + "error prefix or newlines.");
        opt.addOption("-version", "", "Print package version.");
        opt.addOption("-list", "",
                      "List subclasses of class <classname> with "
                      + "descriptions.");
        opt.addOption("-pkgs", "names",
                      "Packages searched. Given as a colon-separated list "
                      + "of full class names; one class/interface contained in "
                      + "each package to be searched.");
        opt.addOption("-gen", "", "Invoke generator of class <classname>.");
        opt.addOption("-tem", "", "Make a template for the given parameters, "
                      + "i.e., a shell command (only for debugging).");
        opt.addOption("-rndinit", "",
                      "Initialize the random source used by this command.");
        opt.addOption("-seed", "file",
                      "File containing truly random bits (master seed).");

        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h###");

        opt.addUsageForm();
        opt.appendToUsageForm(1, "-list#-pkgs##classname");

        opt.addUsageForm();
        opt.appendToUsageForm(2, "-gen#-e,-cerr,-pkgs#classname#parameters");

        opt.addUsageForm();
        opt.appendToUsageForm(3, "-tem##shellcmd#");

        opt.addUsageForm();
        opt.appendToUsageForm(4, "-rndinit#-seed#classname#parameters");

        opt.addUsageForm();
        opt.appendToUsageForm(5, "-version###");

        final String s =
            "This command provides a uniform interface to all objects that "
            + "can be generated and used in initialization files of "
            + "protocols or as inputs to other calls to this tool, e.g., "
            + "cryptographic keys, collision-free hash functions, etc."
            + "\n\n"
            + "The two most important options are: \"-list\" which lists, for "
            + "a given class, all its sub-classes/interfaces, and \"-gen\" "
            + "which invokes the generator of the given class. For such a "
            + "class the option \"-h\" should give a usage description. For "
            + "example, the following describes the possible ways of "
            + "generating subgroups of multiplicative groups."
            + "\n\n" + "   "
            + commandName
            + " -gen ModPGroup -h"
            + "\n\n"
            + "Some classes require an instance of another class as input. "
            + "Using shell-quoting it is possible to write any such invokation "
            + "as a single shell command. In Bash you can quote with \"$(\" "
            + "and \")\" and generate a instance of Pedersen's collision-free "
            + "hash function as follows."
            + "\n\n"
            + "   "
            + commandName
            + " -gen HashfunctionPedersen -width 2 \\\n"
            + "           \"$("
            + commandName
            + " -gen ModPGroup -fixed 2048)\""
            + "\n\n"
            + "The \"-rndinit\" option can only be used once. It initializes "
            + "the source of randomness used by the Verificatum command "
            + "line tools in all future "
            + "invokations. If this option has not been used at all, then the "
            + "calls that needs a random source complains, but all other calls "
            + "complete without errors. The random source can either be a "
            + "wrapper of a device, e.g., /dev/urandom or a hardware "
            + "randomness generator mounted as a device, or it can be a "
            + "software PRG. There are pros and cons of each. A notable "
            + "advantage of the latter is that it is platform and JVM "
            + "independent, whereas, e.g., /dev/urandom, is not."
            + "\n\n"
            + "We intentionally force the user to make an explicit choice "
            + "and we never rely on the builtin java.security.SecureRandom for "
            + "security critical operations, since this is both platform and "
            + "JVM dependent and poorly documented."
            + "\n\n"
            + "That said, the following, which uses the "
            + "/dev/urandom device as a source of bits, is usually a "
            + "reasonable default, but please make sure that this is the case "
            + "on your platform before you use it."
            + "\n\n"
            + "   "
            + commandName
            + " -rndinit RandomDevice /dev/urandom"
            + "\n\n"
            + "Similarly, the random source can be initialized as SHA-256 "
            + "in \"counter mode\" as follows, given a seed file."
            + "\n\n"
            + "   "
            + "vog -rndinit -seed seedfile PRGHeuristic"
            + "\n\n"
            + "Note that the seed file should contain hexadecimal encoded "
            + "random bytes. The seed file itself will be deleted when the "
            + "random source is initialized to avoid accidental reuse."
            + "\n\n"
            + "On many Un*xes such a seed file can be generated as follows "
            + "by reading from a device /dev/mydevice, but rolling a die is "
            + "perfectly practical, since it is only done once."
            + "\n\n"
            + "   "
            + "head -c 2048 < /dev/mydevice | hexdump -e '\"%x\"' > seedfile"
            + "\n\n"
            + "Some usage examples:"
            + "\n"
            + "   "
            + commandName
            + " -list PRG                        "
            + "# Sub-classes/interfaces of PRG.\n"
            + "   "
            + commandName
            + " -gen PRGHeuristic                "
            + "# SHA-256 with counter.\n"
            + "   "
            + commandName
            + " -gen ModPGroup -fixed 1024       # Squares modulo safe prime.";

        opt.appendDescription(s);
        return opt;
    }

    /**
     * Verifies that a random source is available and throws an
     * exception otherwise.
     *
     * @param randomSource Random source that is verified.
     * @throws GenException If the random source is null.
     */
    public static void verify(final RandomSource randomSource)
        throws GenException {
        final String s =
            "This call requires that a random source is available! "
            + "Please use the \"-h\" option for instructions how "
            + "to do this.";
        if (randomSource == null) {
            throw new GenException(s);
        }
    }

    /**
     * Searches for the class given only an unqualified name.
     *
     * @param packages Packages to search for the given class.
     * @param name Name of class.
     * @return Class corresponding to the input name.
     * @throws GenException If no class with the given name is found.
     */
    protected static Class<?>
        getClass(final List<String> packages, final String name)
        throws GenException {
        if (name.contains(".")) {
            for (final String packageName : packages) {
                if (name.startsWith(packageName)) {
                    try {
                        return Class.forName(name);
                    } catch (final ClassNotFoundException cnfe) {
                        break;
                    }
                }
            }
        } else {
            for (final String packageName : packages) {
                try {
                    String prefix = "";
                    if (!"".equals(packageName)) {
                        prefix = packageName + ".";
                    }
                    return Class.forName(prefix + name);
                } catch (final ClassNotFoundException cnfe) {
                    continue;
                }
            }
        }
        throw new GenException("Error! Can not find class implementing "
                               + "the interface "
                               + "com.verificatum.ui.gen.Generator "
                               + "which is named " + name + "!");
    }

    /**
     * Returns an initialized random source as defined by parameters.
     *
     * @param rsFile File containing a string that can be input to
     * {@link Marshalizer#unmarshalHex_RandomSource(String)}
     * .
     * @param seedFile If the random source is a {@link PRG}, then it
     * must contain a sufficiently long seed.
     * @param tmpSeedFile Temporary seed file used to implement atomic
     * write of a new seed.
     * @return Source of random bits.
     * @throws GenException If it is not possible to create a random
     *  source from the data on the given files.
     */
    public static RandomSource standardRandomSource(final File rsFile,
                                                    final File seedFile,
                                                    final File tmpSeedFile)
        throws GenException {
        try {
            final String rsString = ExtIO.readString(rsFile);
            final RandomSource randomSource = Marshalizer
                .unmarshalHex_RandomSource(rsString);

            // If the random source is a PRG, then there must
            // exist an associated seed file of sufficient length.
            if (randomSource instanceof PRG) {
                try {
                    ((PRG) randomSource).setSeedReplaceStored(seedFile,
                                                              tmpSeedFile);
                } catch (final CryptoException ce) {
                    final String e = "Unable to read/write PRG seed file! "
                        + "(" + seedFile + ")."
                        + " " + ce.getMessage();
                    throw new GenException(e, ce);
                }
            }
            return randomSource;
        } catch (final IOException ioe) {
            throw new GenException("Unable to read random source file!", ioe);
        } catch (final EIOException eioe) {
            final String e =
                "Unable to create random source! " + "Make sure that "
                + rsFile + " is valid!";
            throw new GenException(e, eioe);
        }
    }

    /**
     * Constructs a list of the resources where the input packages are
     * found.
     *
     * @param packages Packages to be located.
     * @return List of resources.
     * @throws GenException If a resource can not be found.
     */
    public static List<String>
        getResources(final List<String> packages)
        throws GenException {
        final List<String> resources = new ArrayList<String>();

        for (int i = 0; i < packages.size(); i++) {

            final String path =
                "/" + CLS_NAMES.get(i).replace(".", "/") + ".class";

            final URL packageURL = GeneratorTool.class.getResource(path);

            if (packageURL == null) {

                throw new GenException("Can not find class! (" + path + ")");

            } else {

                final File dir = new File(packageURL.getFile());

                String dirString = dir.getParent();

                if (dirString.startsWith("jar:")) {
                    dirString = dirString.substring(4, dirString.length());
                }
                if (dirString.startsWith("file:")) {
                    dirString = dirString.substring(5, dirString.length());
                }

                final int index = dirString.indexOf(".jar!");
                if (index > 0) {
                    dirString = dirString.substring(0, index + 4);
                } else {
                    if (dirString.endsWith("/")) {
                        dirString = dirString.substring(0,
                                                        dirString.length() - 1);
                    }
                }
                resources.add(dirString);
            }
        }

        return resources;
    }

    /**
     * Tests if the named class is a subclass of the given super class
     * and also that the corresponding named generator class is a
     * subclass of {@link Generator}.
     *
     * @param superKlass Superclass.
     * @param genClassName Candidate generator class name.
     * @param className Class to be tested.
     * @return Boolean indicating if the test succeeded or not.
     */
    public static boolean isSubclassAndGenerator(final Class<?> superKlass,
                                                 final String genClassName,
                                                 final String className) {
        try {

            final Class<?> genClass = Class.forName(genClassName);
            final Class<?> klass = Class.forName(className);

            return Generator.class.isAssignableFrom(genClass)
                && superKlass.isAssignableFrom(klass);

        } catch (final ClassNotFoundException cnfex) {
            return false;
        }
    }

    /**
     * Extracts the generator names corresponding to subclasses of the
     * superclass from the given jar.
     *
     * @param generators Destination of extracted generator names.
     * @param superKlass Superclass.
     * @param jarFileName Name of jar file.
     * @param packageName Name of package to be searched.
     */
    public static void getGeneratorsFromJar(final List<String> generators,
                                            final Class<?> superKlass,
                                            final String jarFileName,
                                            final String packageName) {

        FileInputStream is = null;
        JarInputStream jis = null;

        try {
            final File jarFile = new File(jarFileName);
            is = new FileInputStream(jarFile);
            jis = new JarInputStream(is);

            final String slashedPackageName = packageName.replace(".", "/");

            JarEntry je = jis.getNextJarEntry();

            while (je != null) {
                final String entryName = je.getName();
                if (entryName.startsWith(slashedPackageName)
                    && entryName.endsWith("Gen.class")) {

                    final String tmp =
                        entryName.substring(0, entryName.length() - 6);
                    final String genClassName = tmp.replace("/", ".");

                    final String className =
                        genClassName.substring(0, genClassName.length() - 3);

                    if (isSubclassAndGenerator(superKlass,
                                               genClassName,
                                               className)) {
                        generators.add(genClassName);
                    }
                }

                je = jis.getNextJarEntry();
            }

        } catch (final IOException ioe) {
            throw new GenError("Failed to read from jar!", ioe);
        } finally {
            ExtIO.strictClose(is);
            ExtIO.strictClose(jis);
        }
    }

    /**
     * Extracts the generator names corresponding to subclasses of the
     * superclass from the given directory.
     *
     * @param generators Destination of extracted generator names.
     * @param superKlass Superclass.
     * @param dirPath Path to directory.
     * @param packageName Name of package to be searched.
     *
     * @throws GenException If classes can not be listed.
     */
    public static void
        getGeneratorsFromDirectory(final List<String> generators,
                                   final Class<?> superKlass,
                                   final String dirPath,
                                   final String packageName)
        throws GenException {
        final File dir = new File(dirPath);

        final String[] fileNames = dir.list();
        if (fileNames == null) {
            throw new GenException("Unable to list classes!");
        }

        String prefix = "";
        if (!"".equals(packageName)) {
            prefix = packageName + ".";
        }

        for (int i = 0; i < fileNames.length; i++) {

            if (fileNames[i].endsWith("Gen.class")) {

                final String genClassName = prefix
                    + fileNames[i].substring(0, fileNames[i].length() - 6);
                final String className =
                    genClassName.substring(0,
                                           genClassName.length() - 3);

                if (isSubclassAndGenerator(superKlass,
                                           genClassName,
                                           className)) {

                    generators.add(genClassName);
                }
            }
        }
    }

    /**
     * Returns an array of the names of all classes which implements
     * or subclasses the given class and also implements the
     * {@link Generator} interface. Only the listed packages are
     * searched.
     *
     * @param superKlass Class of which subclasses are sought.
     * @param packages Packages to search.
     * @return List of names of generators.
     * @throws GenException If a resource can not be found.
     */
    public static List<String> getGenerators(final Class<?> superKlass,
                                             final List<String> packages)
        throws GenException {

        final List<String> resources = getResources(packages);

        final List<String> generators = new ArrayList<String>();

        for (int i = 0; i < packages.size(); i++) {

            final String resource = resources.get(i);
            final String packageName = packages.get(i);

            final int index = resource.indexOf(".jar");

            // Resource is a jar-file.
            if (index > -1) {

                final String jarFileName = resource.substring(0, index + 4);
                getGeneratorsFromJar(generators, superKlass, jarFileName,
                                     packageName);

                // Resource is a directory.
            } else {

                getGeneratorsFromDirectory(generators, superKlass, resource,
                                           packageName);

            }

        }

        return generators;
    }

    /**
     * Execute a given generator using the given random source and
     * command line arguments.
     *
     * @param randomSource Source of randomness.
     * @param commandName Command name used when printing usage
     * information, i.e., when a java invokation is wrapped
     * in a shell script, this would typically be the name
     * of the shell script.
     * @param args Command line arguments.
     * @return Output of generator.
     * @throws GenException If the generation fails, in which case the
     *  error message describes the cause.
     */
    public static String gen(final RandomSource randomSource,
                             final String commandName,
                             final String[] args)
        throws GenException {

        try {
            final Opt opt = opt(commandName);
            final int parsedArgs = opt.parse(args, 1);

            return gen(randomSource, opt, parsedArgs, args);

        } catch (final OptException oe) {
            throw new GenException(oe.getMessage(), oe);
        }
    }

    /**
     * Initializes the set of packages to be searched.
     *
     * @param opt Options given by the user.
     */
    public static void initPackages(final Opt opt) {
        if (opt.valueIsGiven("-pkgs")) {

            final String optcnsString = opt.getStringValue("-pkgs");

            final String[] optcns = Util.split(optcnsString, ":");

            for (int i = 0; i < optcns.length; i++) {
                CLS_NAMES.add(optcns[i]);
            }
        }

        for (final String className : CLS_NAMES) {
            final int index = className.lastIndexOf(".");
            if (index == -1) {
                PACKAGES.add("");
            } else {
                PACKAGES.add(className.substring(0, index));
            }
        }
    }

    /**
     * Process a request to list subclasses of an interface or class.
     *
     * @param args Command line parameters.
     * @param opt Parsed command line parameters.
     * @return List of subclasses of input interface or class as a
     * string.
     * @throws GenException If the input command line parameters do
     * not represent a class or interface..
     */
    private static String list(final String[] args, final Opt opt)
        throws GenException {

        // This is a hack to improve the feedback to the user.
        if (args.length != 2) {
            throw new GenException("You must specify at exactly one class "
                                   + "name when listing implementors!");
        }

        Class<?> klass = Object.class;
        if (opt.valueIsGiven(CLASSNAME)) {
            klass = getClass(PACKAGES, opt.getStringValue(CLASSNAME));
        }

        final StringBuilder sb = new StringBuilder();
        final Formatter f = new Formatter(sb);
        final List<String> fullNames = getGenerators(klass, PACKAGES);
        Collections.sort(fullNames);

        f.format("%nClasses/interfaces that inherit/implement "
                 + klass.getName() + ":%n");
        for (final String fullName : fullNames) {
            f.format("%n");
            formatGeneratorDescription(f, fullName);
        }
        f.format("%n");

        return sb.toString();
    }

    /**
     * Execute a given generator using the given random source and
     * command line arguments.
     *
     * @param randomSource Source of randomness.
     * @param opt Options given by the user in parsed form.
     * @param parsedArgs Number of command line arguments that have
     * been parsed already.
     * @param args Command line arguments.
     * @return Output of generator.
     * @throws GenException If the generation fails, in which case the
     *  error message describes the cause.
     */
    public static String gen(final RandomSource randomSource,
                             final Opt opt,
                             final int parsedArgs,
                             final String[] args)
        throws GenException {

        if (opt.getBooleanValue("-rndinit")) {

            throw new GenException("Attempting to reinitialize random source!");

        }

        final String res = OptUtil.processHelpAndVersionString(opt);

        if (res != null) { // NOPMD

            return res;

        } else if (opt.getBooleanValue("-list")) {

            return list(args, opt);

        } else if (opt.getBooleanValue("-gen")) {

            final String className = opt.getStringValue(CLASSNAME) + "Gen";
            final Class<?> klass = getClass(PACKAGES, className);
            try {

                final Generator generator = (Generator) klass.newInstance();

                final String[] cmdArgs =
                    Arrays.copyOfRange(args,
                                       Math.min(parsedArgs + 1, args.length),
                                       args.length);

                return generator.gen(randomSource, cmdArgs);

            } catch (final InstantiationException ie) {
                throw new GenException(ie.getMessage(), ie);
            } catch (final IllegalAccessException iae) {
                throw new GenException(iae.getMessage(), iae);
            }

        } else if (opt.getBooleanValue("-tem")) {

            final GeneratorTemplate gt =
                new GeneratorTemplate(GeneratorTemplate.CMD,
                                      opt.getStringValue("shellcmd"));

            // Before constructing the template we try to use
            // it to see that the template works.
            try {
                gt.execute();
            } catch (final GenException ge) {
                final String s = "The command to be templated is malformed. "
                    + "Please try this command on its own before "
                    + "templating it!";
                throw new GenException(s, ge);
            }

            return Marshalizer.marshalToHexHuman(gt, true);

        } else {
            throw new GenError("Invalid option!");
        }
    }

    /**
     * Check that the seedfile exists and throw an exception
     * otherwise.
     *
     * @param seedFile Candidate seed file.
     * @throws GenException If the seed file does not exist.
     */
    private static void checkSeedFile(final File seedFile)
        throws GenException {
        if (seedFile.exists()) {
            throw new GenException("Please delete the existing "
                                   + "seed file " + seedFile
                                   + "before trying again!");
        }
    }

    /**
     * Description of random source.
     *
     * @param opt Parsed command line parameters.
     * @param cmdArgs Parameters to the random source.
     * @param randomSource Source of randomness.
     * @return Description of random source.
     * @throws GenException If the input parameters do not represent a
     * random source.
     */
    private static String randomSourceString(final Opt opt,
                                             final String[] cmdArgs,
                                             final RandomSource randomSource)
        throws GenException {
        try {
            final Class<?> klass =
                getClass(PACKAGES, opt.getStringValue(CLASSNAME) + "Gen");

            final Generator generator = (Generator) klass.newInstance();

            return generator.gen(randomSource, cmdArgs);
        } catch (final InstantiationException ie) {
            throw new GenException("Unable to determine name!", ie);
        } catch (final IllegalAccessException iae) {
            throw new GenError("Can not access class!", iae);
        }
    }

    /**
     * Reads seed bytes from file.
     *
     * @param opt Parsed command line parameters.
     * @return Seed bytes read from file.
     * @throws GenException If the seed bytes can not be read from the
     * seed file.
     */
    private static byte[] readSeedBytes(final Opt opt)
        throws GenException {

        final File srcFile = new File(opt.getStringValue("-seed"));

        if (!srcFile.exists()) {
            throw new GenException("Seed file does not exist!");
        }

        try {
            final long seedSize = ExtIO.fileSize(srcFile);
            if (seedSize > MAX_SEED_SIZE) {
                throw new GenException("Too large seed file! ("
                                       + seedSize + ")");
            }
        } catch (final IOException ioe) {
            throw new GenException("Unable to determine size of seed file!",
                                   ioe);
        }

        try {
            final String hexSeed = ExtIO.readString(srcFile);
            return Hex.toByteArray(hexSeed);
        } catch (final IOException ioe) {
            throw new GenException("Unable to read hexadecimal encoded seed!",
                                   ioe);
        }
    }

    /**
     * Initialize default random source.
     *
     * @param opt Parsed command line parameters.
     * @param cmdArgs Parameters to the random source.
     * @param rsFile File to which the description of random source is
     * written.
     * @param tmprsFile Temporary file used to write the description
     * of the random source atomically.
     * @param seedFile File containing seed bytes.
     * @param tmpSeedFile Temporary file used to write the seed
     * atomically.
     * @throws GenException If the default random source can not be
     * initialized and written to file.
     */
    private static void rndInit(final Opt opt,
                                final String[] cmdArgs,
                                final File rsFile,
                                final File tmprsFile,
                                final File seedFile,
                                final File tmpSeedFile)
        throws GenException {

        try {
            checkSeedFile(seedFile);

            // Try to instantiate.
            final String rss = randomSourceString(opt, cmdArgs, null);
            RandomSource randomSource = null;
            try {
                randomSource = Marshalizer.unmarshalHex_RandomSource(rss);
            } catch (final EIOException eioe) {
                final String e =
                    "Failed to generate random source! Did you "
                    + "perhaps pass the \"-h\" option to the class "
                    + "generator? You are probably not using the "
                    + "\"-gen\" option in that case.";
                throw new GenException(e, eioe);
            }

            if (randomSource instanceof PRG) {

                if (opt.valueIsGiven("-seed")) {

                    final byte[] bytes = readSeedBytes(opt);

                    ExtIO.atomicWriteString(tmpSeedFile,
                                            seedFile,
                                            Hex.toHexString(bytes));
                } else {
                    final String e = "Missing seed file! To use a PRG as a "
                        + "random source you must use the \"-seed\" option and "
                        + "provide a seed.";
                    throw new GenException(e);
                }
            }

            ExtIO.atomicWriteString(tmprsFile, rsFile, rss);

        } catch (final IOException ioe) {
            final String e = "Unable to use random source files!";
            throw new GenException(e, ioe);
        }

        System.out.println("Successfully initialized random source!");
    }

    /**
     * Execute the generator tool as a stand-alone application.
     *
     * @param args Command line arguments.
     */
    public static void main(final String[] args) {

        if (args.length == 0) {
            throw new GenError("No command name specified!");
        }
        if (args.length < 4) {
            throw new GenError("Random source files are missing!");
        }
        final String commandName = args[0];

        final String[] additional = args[1].split(":");
        for (int i = 0; i < additional.length; i++) {
            if (!additional[i].equals("")) {
                CLS_NAMES.add(additional[i]);
            }
        }

        // We must treat the flags -e and -cerr in an ad hoc way to
        // make sure that they work even when parsing the command line
        // fails.
        final boolean cerrFlag = GenUtil.specialFlag("-cerr", args);
        final boolean eFlag = GenUtil.specialFlag("-e", args);

        try {

            // Drop the first 4 arguments and parse the rest up to the
            // first parameter.
            final String[] newargs = Arrays.copyOfRange(args, 4, args.length);

            int parsedArgs;

            Opt opt = null;
            String[] cmdArgs;
            try {

                opt = opt(commandName);
                parsedArgs = opt.parse(newargs, 1);

                // If help or version flags are given we act accordingly.
                OptUtil.processHelpAndVersion(opt);

                // We keep the commands to be parsed by the
                // instantiated object.
                cmdArgs =
                    Arrays.copyOfRange(newargs,
                                       Math.min(parsedArgs + 1, newargs.length),
                                       newargs.length);

            } catch (final OptException oe) {
                throw new GenException(oe.getMessage(), oe);
            }

            RandomSource randomSource = null;

            // Initialize the packages to be searched.
            initPackages(opt);

            final File rsFile = new File(args[2]);
            final File tmprsFile = new File(args[2] + "_TMP");

            final File seedFile = new File(args[3]);
            final File tmpSeedFile = new File(args[3] + "_TMP");

            // If a random source file exists, then we try to use it,
            // and report any failures.
            if (rsFile.exists()) {

                randomSource =
                    standardRandomSource(rsFile, seedFile, tmpSeedFile);
                final String resString =
                    gen(randomSource, opt, parsedArgs, newargs);
                System.out.println(resString);

                // Otherwise we either initialize the random source, or we
                // hope that the call can proceed without the random
                // source.
            } else {

                if (opt.valueIsGiven("-rndinit")) {

                    rndInit(opt, cmdArgs,
                            rsFile, tmprsFile, seedFile, tmpSeedFile);
                } else {

                    final String res =
                        gen(randomSource, opt, parsedArgs, newargs);
                    System.out.println(res);
                }
            }

        // PMD does not understand this.
        } catch (final GenException ge) { // NOPMD

            GenUtil.processErrors(ge, cerrFlag, eFlag);

        } finally {

            TempFile.free();
        }
    }
}
