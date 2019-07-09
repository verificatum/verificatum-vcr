
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

package com.verificatum.ui.info;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import com.verificatum.crypto.CryptoError;
import com.verificatum.crypto.Hashfunction;
import com.verificatum.crypto.HashfunctionHeuristic;
import com.verificatum.crypto.PRG;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.ExtIO;
import com.verificatum.eio.Hex;
import com.verificatum.eio.TempFile;
import com.verificatum.protocol.Protocol;
import com.verificatum.protocol.ProtocolBB;
import com.verificatum.protocol.ProtocolBBGen;
import com.verificatum.protocol.ProtocolFormatException;
import com.verificatum.protocol.com.BullBoardBasicGen;
import com.verificatum.ui.gen.GenException;
import com.verificatum.ui.gen.GenUtil;
import com.verificatum.ui.gen.GeneratorTool;
import com.verificatum.ui.opt.Opt;
import com.verificatum.ui.opt.OptException;
import com.verificatum.ui.opt.OptUtil;
import com.verificatum.vcr.VCR;

/**
 * Command line tool for generating info files.
 *
 * @author Douglas Wikstrom
 */
public final class InfoTool {

    /**
     * Name of info stub file used if the user does not provide a
     * file name.
     */
    public static final String STUB_INFO_FILENAME = "stub.xml";

    /**
     * Name of private info file used if the user does not provide a
     * file name.
     */
    public static final String PRIVINFO_FILENAME = "privInfo.xml";

    /**
     * Name of local protocol info file used if the user does not
     * provide a file name.
     */
    public static final String LOCAL_PROTINFO_FILENAME = "localProtInfo.xml";

    /**
     * Name of protocol info file used if the user does not provide a
     * file name.
     */
    public static final String PROTINFO_FILENAME = "protInfo.xml";

    /**
     * Avoid accidental instantiation.
     */
    private InfoTool() { }

    /**
     * Extracts a comma separated string containing the names of all
     * options.
     *
     * @param defaultInfo Info instance containing some default
     * values.
     * @param required Decides if required or optional option values
     * are generated.
     * @return String representation of extracted options.
     */
    public static String extractOptions(final Info defaultInfo,
                                        final boolean required) {
        final StringBuilder sb = new StringBuilder();
        for (final InfoField inf : defaultInfo.infoFields) {
            final String name = inf.getName();

            if (!name.equals(RootInfo.VERSION)
                && defaultInfo.hasValue(inf.getName()) != required) {
                sb.append(",-");
                sb.append(name);
            }
        }
        String s = sb.toString();
        if (s.length() > 0 && s.charAt(0) == ',') {
            s = s.substring(1);
        }
        return s;
    }

    /**
     * Generates an option field in the given option instance for each
     * value in the given info.
     *
     * @param opt Destination of options.
     * @param info Source of options.
     */
    public static void addOptions(final Opt opt, final Info info) {
        for (final InfoField inf : info.infoFields) {

            if (!inf.getName().equals(RootInfo.VERSION)) {

                final String optionName = "-" + inf.getName();
                if (!opt.hasOption(optionName)) {
                    opt.addOption(optionName, "value", inf.getDescription());
                }
            }
        }
    }

    /**
     * Adds the exception and plain error flags.
     *
     * @param base Basic string to which flags are added.
     * @return Adds the exception and plain error flags.
     */
    public static String eandcerrOptions(final String base) {
        if ("".equals(base)) {
            return "-e,-cerr";
        } else {
            return base + ",-e,-cerr";
        }
    }

    /**
     * Utility function to add standard options -e, -cerr, -h,
     * -version used in many commands in a uniform way.
     *
     * @param opt Instance to add options to.
     */
    public static void addECerrHVersion(final Opt opt) {
        opt.addOption("-e", "", "Print exception trace upon error.");
        opt.addOption("-cerr", "",
                      "Print error messages as clean strings without any "
                      + "error prefix or newlines.");
        opt.addOption("-h", "", "Display usage information");
        opt.addOption("-version", "", "Print the package version.");
    }

    /**
     * Generates an option instance from the given info instances. If
     * default values are given, then the options are optional and
     * otherwise they are required.
     *
     * @param commandName Name of command to be used when generating
     * usage information.
     * @param pi Empty protocol info with the needed fields.
     * @param dpi Protocol info containing some default values.
     * @param pri Empty private info with the needed fields.
     * @param dpri Private info containing some default values.
     * @param pai Empty party info with the needed fields.
     * @param dpai Party info containing some default values.
     * @return Instance representing the given command line.
     */
    protected static Opt opt(final String commandName,
                             final ProtocolInfo pi,
                             final ProtocolInfo dpi,
                             final PrivateInfo pri,
                             final PrivateInfo dpri,
                             final PartyInfo pai,
                             final PartyInfo dpai) {

        final String defaultErrorString = "Invalid usage form, please use \""
            + commandName + " -h\" for usage information!";

        final Opt opt = new Opt(commandName, defaultErrorString);

        addECerrHVersion(opt);

        opt.addParameter("protInfoIn",
                         "Protocol info file containing joint parameters "
                         + "and possibly some party info entries.");
        opt.addParameter("privInfo", "Private info output file.");
        opt.addParameter("protInfoOut", "Protocol info output file.");

        opt.addParameter("file", "Info file.");

        opt.addOption("-prot", "",
                      "Generate protocol info stub file containing only "
                      + "joint parameters.");
        opt.addOption("-party", "",
                      "Generate private and protocol info files based on "
                      + "the given protocol info stub file.");
        opt.addOption("-seed", "value",
                      "Seed file for pseudo-random generator of this party.");
        opt.addOption("-digest", "",
                      "Compute hexadecimal encoded digest of file.");
        opt.addOption("-merge", "",
                      "Merge several protocol info files with identical "
                      + "joint parameters into a single protocol info file.");
        opt.addOption("-hash", "value",
                      "Name of an algorithm from the SHA-2 family, i.e., "
                      + "SHA-256, SHA-384, or SHA-512, used to compute a "
                      + "digest of an info file. (Default is SHA-256.)");
        opt.addOption("-schema", "type",
                      "Output the XML schema definition of private or "
                      + "protocol files. Legal values are \"private\" and "
                      + "\"protocol\".");

        addOptions(opt, pi);
        addOptions(opt, pri);
        addOptions(opt, pai);

        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h#" + "-" + ProtocolBB.BULLBOARD + "##");

        opt.addUsageForm();
        opt.appendToUsageForm(1, "-prot,"
                              + extractOptions(dpi, true)
                              + "#"
                              + eandcerrOptions(extractOptions(dpi, false))
                              + "#protInfoOut#");

        opt.addUsageForm();
        opt.appendToUsageForm(2,
                              "-party," + extractOptions(dpri, true) + ","
                              + extractOptions(dpai, true) + "#-seed,"
                              + extractOptions(dpri, false) + ","
                              + eandcerrOptions(extractOptions(dpai, false))
                              + "#protInfoIn,privInfo,protInfoOut#");

        opt.addUsageForm();
        opt.appendToUsageForm(3, "-merge#-e,-cerr#+protInfoIn,protInfoOut#");

        opt.addUsageForm();
        opt.appendToUsageForm(4, "-digest#-hash#file#");

        opt.addUsageForm();
        opt.appendToUsageForm(5, "-schema###");

        opt.addUsageForm();
        opt.appendToUsageForm(6, "-version###");

        final String descr =
            "This command is used to generate the configuration file of a "
            + "protocol in three simple steps:"
            + "\n\n"
            + "   (1) Each party generates a stub protocol info file with "
            + "the\n"
            + "       global parameters.\n"
            + "   (2) Each party generates private and protocol info files.\n"
            + "   (3) Each party merges all protocol info files into a single\n"
            + "       protocol info file."
            + "\n\n"
            + "The options \"-prot\", \"-party\", and \"-merge\" must appear "
            + "as the first "
            + "option. Use \"-prot\" to generate the global stub file "
            + "containing only the global parameters that all parties agree "
            + "on. For example:" + "\n\n" + "   "
            + commandName
            + " -prot -sid \"SID\" "
            + "-name \"Execution\" -nopart 3 -thres 2 stub.xml"
            + "\n\n"
            + "Use \"-party\" to generate: (i) a private info file containing "
            + "your private parameters, e.g., your secret signing key, and (ii)"
            + " a new protocol info file based on the input protocol info "
            + "stub file, where all your public information has been added. "
            + "For example:"
            + "\n\n"
            + "   "
            +  commandName
            + " -party -name \"Santa Claus\" "
            + "stub.xml privInfo1.xml protInfo1.xml"
            + "\n\n"
            + "(If you use a PRG as the \"-rand\" (random source), then use "
            + "the \"-seed\" option as well and specify a seed file (or "
            + "device) containing a seed of suitable length.)"
            + "\n\n"
            + "Use \"-merge\" to generate a single protocol info file from "
            + "several protocol info files with identical joint parameters. "
            + "Assuming that the ith party names its info files as above:"
            + "\n\n"
            + "   "
            +  commandName
            + " -merge protInfo1.xml protInfo2.xml protInfo3.xml protInfo.xml"
            + "\n\n"
            + "All optional values have reasonable defaults, i.e., you can "
            + "actually use the above commands provided that /dev/urandom "
            + "contains good randomness. Please generate dummy files to "
            + "investigate exactly what these defaults are. It is unwise to "
            + "touch the defaults unless you know exactly what you are doing."
            + "\n\n"
            + "The stub filename can be dropped when the \"-prot\" option is "
            + "used, in which case it defaults to \"stub.xml\". Similarly, the "
            + "filenames can be dropped when using the \"-party\" option, in "
            + "which case they default to \"stub.xml\", \"privInfo.xml\", and "
            + "\"localProtInfo.xml\". The name of the output joint protocol "
            + "info file can also be dropped when using the \"-merge\" option, "
            + "in which case it defaults to \"protInfo.xml\""
            + "\n\n"
            + "WARNING!"
            + "\n\n"
            + "All basic inputs are verified to be of the right form and "
            + "within reasonable ranges already at a syntactical XML level, "
            + "but due to the flexibility of the protocols certain "
            + "combinations of inputs can make the protocol:"
            + "\n\n"
            + "(1) Insecure in an application due to too small parameters. "
            + "The default parameters are conservative, but the user is "
            + "responsible for checking that these, or any custom parameters "
            + "provides the expected level of security."
            + "\n\n"
            + "(2) Computationally infeasible to execute due to too large "
            + "parameters. In this case the software will stall without output."
            + "\n\n"
            + "Furthermore, some inputs such as paths, URLs, and plugin "
            + "classes can not be fully validated at a syntactical XML "
            + "level, i.e., only upper bounds on input lengths and "
            + "alphabets used can be verified."
            + "\n\n"
            + "They are of course validated at the application level, but "
            + "since these objects can be instantiations of custom classes "
            + "implemented by third parties the responsibility for this "
            + "validation may fall outside the scope of this software. All "
            + "such inputs are clearly marked with \"WARNING!\" in the "
            + "descriptions below.";

        opt.appendDescription(descr);

        return opt;
    }

    /**
     * Generate a simple option instance to be used to generate usage
     * information.
     *
     * @param commandName Name of command to be used when generating
     * usage information.
     * @return Instance representing the given command line.
     */
    protected static Opt simpleOpt(final String commandName) {

        final Opt opt = new Opt(commandName, "hejhopp");

        opt.addParameter("commandName", "Name of shell script wrapper.");
        opt.addParameter("rsFile",
                         "File containing a representation of a random "
                         + "source.");
        opt.addParameter("seedFile",
                         "File containing a seed for the random source, "
                         + "if it is PRG (otherwise this input is ignored).");
        opt.addParameter("classname",
                         "Name of protocol class for which to generate "
                         + "info files.");

        opt.addUsageForm();
        opt.appendToUsageForm(0, "##commandName,rsFile,seedFile,classname#");

        final String s =
            "Generate info files for a given class which implements the "
            + "ui.info.InfoGenerator interface. If you give the classname as a "
            + "parameter you get class specific usage information. You should "
            + "normally only invoke this command in this way if you are a "
            + "programmer. A wrapper script should be provided to users."
            + "\n\n"
            + "The option -Djava.security.egd=file:/dev/./urandom is needed "
            + "due to a bug in JDK 6. Please have a look at the documentation "
            + "of com.verificatum.crypto.PRGHeuristic for more information."
            + "\n\n"
            + "If you are a normal user, then this is almost certainly a bug. "
            + "Please report it!";
        opt.appendDescription(s);

        return opt;
    }

    /**
     * Transfers values to the given info instance either from the
     * option instance as given by the user or from the default info
     * instance when no user given values are available.
     *
     * @param info Destination of values.
     * @param opt Source of values given by user.
     * @param defaultInfo Source of default values.
     *
     * @throws InfoException If the given values can not be transfered
     * with successful validation.
     */
    public static void transferValues(final Info info,
                                      final Opt opt,
                                      final Info defaultInfo)
        throws InfoException {
        for (final InfoField inf : info.infoFields) {
            final String name = inf.getName();
            final String optionName = "-" + name;
            if (opt.valueIsGiven(optionName)) {
                try {
                    info.addValue(name, opt.getStringValue(optionName));
                } catch (final InfoException ie) {
                    throw new InfoException("Failed to parse value of "
                                            + "-" + name + " option! "
                                            + ie.getMessage(), ie);
                }
            } else {
                info.copyValues(name, defaultInfo);
            }
        }
    }

    /**
     * Check that the command line parameters contain the needed
     * parameters for the wrapper.
     *
     * @param args Command line parameters.
     */
    private static void sanityCheckOfHiddenParameters(final String[] args) {
        if (args.length < 4) {
            final String cn = "java -Djava.security.egd="
                + "file:/dev/./urandom com.verificatum.ui.info.InfoTool";
            System.out.println(simpleOpt(cn).usage());
            System.exit(0);
        }
    }

    /**
     * Read standard random source.
     *
     * @param randomSourceFile File containing description of random source.
     * @param seedFile File containing the seed for the random source
     * if it is a PRG.
     * @param tmpSeedFile Temporary seed file.
     * @return Standard random source.
     * @throws InfoException If the random source can not be recovered.
     */
    private static RandomSource
        standardRandomSource(final File randomSourceFile,
                             final File seedFile,
                             final File tmpSeedFile)
        throws InfoException {

        try {
            return GeneratorTool.standardRandomSource(randomSourceFile,
                                                      seedFile,
                                                      tmpSeedFile);
        } catch (final GenException ge) {
            throw new InfoException(ge.getMessage(), ge);
        }
    }

    /**
     * Returns true or false depending on if it is a protocol
     * invocation.
     *
     * @param args Command line parameters.
     * @return True or false depending on if it is a protocol
     * information.
     */
    private static boolean isPartyInvocation(final String[] args) {

        for (int i = 0; i < args.length; i++) {
            if ("-party".equals(args[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract generator of the bulletin board.
     *
     * @param args Command line parameters.
     * @return Generator of bulletin board.
     * @throws InfoException If the the command line argument do not
     * describe a bulletin board.
     */
    private static BullBoardBasicGen
        extractBullBoardBasicGen(final String[] args)
        throws InfoException {

        if (isPartyInvocation(args)) {

            // If we are taking a stub info file as input, then we
            // must extract the bulletin board definition from this
            // file.
            final int indexOfInputProtFile = args.length - 3;

            ProtocolFormatException tmppfe = null;
            File protocolInfoFile = new File("");
            try {

                if (indexOfInputProtFile > 0) {
                    protocolInfoFile = new File(args[indexOfInputProtFile]);
                }
                return ProtocolBBGen.getBullBoardBasicGen(protocolInfoFile);
            } catch (final ProtocolFormatException pfe) {
                protocolInfoFile = null;
                tmppfe = pfe;
            }
            if (protocolInfoFile == null) {
                try {
                    protocolInfoFile = new File(STUB_INFO_FILENAME);
                    return ProtocolBBGen.getBullBoardBasicGen(protocolInfoFile);

                } catch (final ProtocolFormatException ipfe) {
                    throw new InfoException("Failed to find valid bulletin "
                                            + "board definition!", ipfe);
                }
            } else {
                throw new InfoException("Failed to find valid bulletin "
                                        + "board definition!", tmppfe);
            }

        } else {

            // If we are creating a protocol stub file, then we need
            // to fetch the definition of the bulletin board
            // definition from the command line arguments.
            try {
                return ProtocolBBGen.getBullBoardBasicGen(args);
            } catch (final ProtocolFormatException pfe) {
                throw new InfoException("Unable to get bulletin board!", pfe);
            }
        }
    }

    /**
     * Returns the info generator identified by the input class name.
     *
     * @param className Class name of an info generator.
     * @param bullBoardBasicGen Generator of bulletin board.
     * @return Info generator identified by the input class name.
     * @throws InfoException If the input is not the name of an info
     * generator class.
     */
    private static InfoGenerator
        instantiateInfoGenerator(final String className,
                                 final BullBoardBasicGen bullBoardBasicGen)
        throws InfoException {

        // Find the named class.
        Class<?> klass = null;
        try {
            klass = Class.forName(className);
        } catch (final ClassNotFoundException cnfe) {
            final String s =
                "There exists no class " + className + "!";
            throw new InfoException(s, cnfe);
        }

        // Verify that the class is an InfoGenerator.
        if (!InfoGenerator.class.isAssignableFrom(klass)) {
            final String s = "The class " + klass.getName() + " does not "
                + "implement the "
                + "com.verifiacatum.ui.info.InfoGenerator interface!";
            throw new InfoException(s);
        }

        // Instantiate the class with the given generator for the
        // bulletin board.
        try {
            final Constructor constructor =
                klass.getConstructor(BullBoardBasicGen.class);

            return (InfoGenerator) constructor.newInstance(bullBoardBasicGen);

        } catch (final InvocationTargetException ite) {
            throw new InfoException("Could not invoke constructor!", ite);
        } catch (final InstantiationException ie) {
            throw new InfoException("Missing constructor!", ie);
        } catch (final IllegalAccessException iae) {
            throw new InfoException("Not allowed to use constructor!", iae);
        } catch (final NoSuchMethodException nsme) {
            throw new InfoException("Can not find the constructor!", nsme);
        }
    }

    /**
     * Parses command line parameters.
     *
     * @param commandName Name of command to be used when generating
     * usage information.
     * @param pi Empty protocol info with the needed fields.
     * @param dpi Protocol info containing some default values.
     * @param pri Empty private info with the needed fields.
     * @param dpri Private info containing some default values.
     * @param pai Empty party info with the needed fields.
     * @param dpai Party info containing some default values.
     * @param args Command line parameters.
     * @return Instance representing the given command line.
     * @throws InfoException If the command line can not be parsed
     * correctly.
     */
    private static Opt parseCommandLine(final String commandName,
                                        final ProtocolInfo pi,
                                        final ProtocolInfo dpi,
                                        final PrivateInfo pri,
                                        final PrivateInfo dpri,
                                        final PartyInfo pai,
                                        final PartyInfo dpai,
                                        final String[] args)
        throws InfoException {

        // Generate an option instance.
        Opt opt = opt(commandName, pi, dpi, pri, dpri, pai, dpai);

        OptException tmpoe = null;
        try {

            // We first try to parse the command line exactly as given
            // by the user.
            opt.parse(args);

        } catch (final OptException oe) {

            tmpoe = oe;

        }
        if (tmpoe == null) {

            return opt;

        } else {

            // If parsing fails, then we assume that the user is
            // executing the commands "in the working directory" and
            // make another attempt with the default file names.

            if (args.length > 0) {

                String[] newargs = null;

                if ("-prot".equals(args[0])) {

                    newargs = new String[args.length + 1];
                    System.arraycopy(args, 0, newargs, 0, args.length);
                    newargs[args.length] = STUB_INFO_FILENAME;

                } else if ("-party".equals(args[0])) {

                    newargs = new String[args.length + 3];
                    System.arraycopy(args, 0, newargs, 0, args.length);
                    newargs[args.length] = STUB_INFO_FILENAME;
                    newargs[args.length + 1] = PRIVINFO_FILENAME;
                    newargs[args.length + 2] = LOCAL_PROTINFO_FILENAME;

                } else {

                    throw new InfoException(tmpoe.getMessage(), tmpoe);
                }

                opt = opt(commandName, pi, dpi, pri, dpri, pai, dpai);
                try {
                    opt.parse(newargs);
                    return opt;
                } catch (final OptException oe2) {

                    // We intentionally drop this exception and throw
                    // the original.
                    final String e = tmpoe.getMessage();
                    throw new InfoException(e, tmpoe); // NOPMD
                }

            } else {
                throw new InfoException(tmpoe.getMessage(), tmpoe);
            }
        }
    }

    /**
     * Merges protocol info files into a single protocol info file.
     *
     * @param generator Info generator describing the info file
     * formats.
     * @param protocolInfoFile Destination protocol info file.
     * @param mpars File names of protocol info files to be merged.
     * @throws InfoException If the info files can not be merged.
     */
    private static void mergeProtInfos(final InfoGenerator generator,
                                       final File protocolInfoFile,
                                       final String[] mpars)
        throws InfoException {

        File pif = protocolInfoFile;
        String[] tmpars = mpars;

        final ProtocolInfo pi = generator.newProtocolInfo();
        pi.parse(tmpars[0]);

        if (pi.getIntValue(Protocol.NOPARTIES) == tmpars.length + 1) {

            // This is expected if the output file is implicit, so we
            // plug the implicit file name at the end.

            final String[] tmp = new String[tmpars.length + 1];
            System.arraycopy(tmpars, 0, tmp, 0, tmpars.length);
            tmp[tmpars.length] = pif.toString();
            tmpars = tmp;
            pif = new File(PROTINFO_FILENAME);

        } else if (pi.getIntValue(Protocol.NOPARTIES) != tmpars.length) {

            // Otherwise we have too many or too few, i.e., it can not
            // be fixed.

            throw new InfoException("Wrong number of protocol info files!");
        }

        for (int i = 1; i < tmpars.length; i++) {

            final ProtocolInfo tpi = generator.newProtocolInfo();
            tpi.parse(tmpars[i]);

            if (!pi.equalInfoFields(tpi)) {
                final String s =
                    "All input protocol info files must contain "
                    + "identical common data fields.";
                throw new InfoException(s);
            }
            pi.addPartyInfos(tpi);
        }

        final String piVersion = pi.getStringValue(RootInfo.VERSION);
        if (!generator.compatible(piVersion)) {
            final String s = "Input protocol info files have "
                + "incompatible version! (" + piVersion + " != "
                + VCR.version() + ")";
            throw new InfoException(s);
        }

        // Execute hook for checking consistency of the protocol info.
        generator.validate(pi);

        pi.toXML(pif);
    }

    /**
     * Returns digest of an info file.
     *
     * @param opt Parsed command line parameters.
     * @return Hexadecimal encoding of hash digest of info file
     * specified in the parsed parameters.
     * @throws InfoException If the file to be hashed can not be read.
     */
    private static String digestOfFile(final Opt opt)
        throws InfoException {

        final File file = new File(opt.getStringValue("file"));

        String algorithm = "SHA-256";
        try {

            final byte[] bytes = ExtIO.getBytes(ExtIO.readString(file));

            if (opt.valueIsGiven("-hash")) {
                algorithm = opt.getStringValue("-hash");
            }
            final Hashfunction hf = new HashfunctionHeuristic(algorithm);

            return Hex.toHexString(hf.hash(bytes));

        } catch (final FileNotFoundException fnfe) {
            throw new InfoException("Can not find file! ("
                                    + file.toString() + ")", fnfe);
        } catch (final IOException ioe) {
            throw new InfoException("Can not read file! ("
                                    + file.toString() + ")", ioe);
        } catch (final CryptoError ce) {
            throw new InfoException("Unknown hash algorithm! ("
                                    + algorithm + ")", ce);
        }
    }

    /**
     * If a value is stored under the string
     * <code>primaryString</code> and nothing is stored under
     * <code>secondaryString</code>, then the former value is stored
     * under the second string.
     *
     * @param opt Parsed parameters.
     * @param primaryString Name of value considered.
     * @param secondaryString Name of value that may be copied.
     */
    private static void optionalCopyStore(final Opt opt,
                                          final String primaryString,
                                          final String secondaryString) {

        if (opt.valueIsGiven(primaryString)
            && !opt.valueIsGiven(secondaryString)) {

            opt.storeOptionValue(secondaryString,
                                 opt.getStringValue(primaryString));
        }
    }

    /**
     * Generates a party info and writes the result to file.
     *
     * @param generator Generator of infos.
     * @param opt Parsed command line parameters.
     * @param pi Empty protocol info with the needed fields.
     * @param pri Empty private info with the needed fields.
     * @param dpri Private info containing some default values.
     * @param pai Empty party info with the needed fields.
     * @param dpai Party info containing some default values.
     * @param protocolInfoFile File name to which the party info is
     * written.
     * @throws InfoException If the party info can not be initialized
     * as required by the parsed parameters.
     */
    private static void generatePartyInfo(final InfoGenerator generator,
                                          final Opt opt,
                                          final ProtocolInfo pi,
                                          final PrivateInfo pri,
                                          final PrivateInfo dpri,
                                          final PartyInfo pai,
                                          final PartyInfo dpai,
                                          final File protocolInfoFile)
        throws InfoException {

        pi.parse(opt.getStringValue("protInfoIn"));

        final String piVersion = pi.getStringValue(RootInfo.VERSION);
        if (!generator.compatible(piVersion)) {
            final String s = "Input stub protocol info file has "
                + "incompatible version! (" + piVersion + " != "
                + VCR.version() + ")";
            throw new InfoException(s);
        }

        // We use dynamic defaults for the internal port numbers of
        // the HTTP and hint servers.
        optionalCopyStore(opt, "-http", "-httpl");
        optionalCopyStore(opt, "-hint", "-hintl");

        final File privateInfoFile =
            new File(opt.getStringValue("privInfo"));

        transferValues(pri, opt, dpri);
        pri.toXML(privateInfoFile);

        final RandomSource rand = Protocol.randomSource(pri);
        if (rand instanceof PRG) {
            if (opt.valueIsGiven("-seed")) {

                final File seedFile = new File(opt.getStringValue("-seed"));

                if (!seedFile.exists()) {
                    final String e = "Given seed file does not exist!";
                    throw new InfoException(e);
                }
                if (!seedFile.canRead()) {
                    final String e = "Unable to read from given seed file!";
                    throw new InfoException(e);
                }

                DataInputStream dis = null;
                try {
                    dis = new DataInputStream(new FileInputStream(seedFile));
                    final int noSeedBytes = ((PRG) rand).minNoSeedBytes();
                    final byte[] seedBytes = new byte[noSeedBytes];
                    dis.readFully(seedBytes);

                    final File partySeedFile = Protocol.seedFile(pri);
                    final File parentFile = partySeedFile.getParentFile();
                    if (parentFile != null) {
                        try {
                            ExtIO.mkdirs(parentFile);
                        } catch (final EIOException eioe) {
                            final String e = "Unable to create parent file!";
                            throw new InfoError(e, eioe);
                        }
                    }
                    ExtIO.writeString(partySeedFile,
                                      Hex.toHexString(seedBytes));
                } catch (final IOException ioe) {
                    final String e = "Could not create seed file!";
                    throw new InfoException(e, ioe);
                } finally {
                    ExtIO.strictClose(dis);
                }
            } else {
                throw new InfoException("A seed is required!");
            }
        }

        transferValues(pai, opt, dpai);
        pi.addPartyInfo(pai);
        generator.validateLocal(pi);

        pi.toXML(protocolInfoFile);
    }

    /**
     * Invokes the stand-alone command line tool.
     *
     * @param args Command line arguments.
     */
    public static void main(final String[] args) {

        sanityCheckOfHiddenParameters(args);

        // Name of wrapper script.
        final String commandName = args[0];

        // Files describing source of randomness.
        final File defaultRandomSourceFile = new File(args[1]);
        final File defaultSeedFile = new File(args[2]);
        final File defaultTmpSeedFile = new File(args[2] + "_TMP");

        // Main protocol class for which this tool provides info
        // files.
        final String className = args[3] + "Gen";


        // We must treat the flags -e and -cerr in an ad hoc way to
        // make sure that they work even when parsing the command line
        // fails.
        final boolean cerrFlag = GenUtil.specialFlag("-cerr", args);
        final boolean eFlag = GenUtil.specialFlag("-e", args);

        try {

            // Construct source of randomness.
            final RandomSource randomSource =
                standardRandomSource(defaultRandomSourceFile,
                                     defaultSeedFile,
                                     defaultTmpSeedFile);


            // Get rid of the parameters expected to be provided by a
            // wrapper script.
            final String[] newargs = Arrays.copyOfRange(args, 4, args.length);

            // This is a hack to extract the bulletin board class name
            // before even constructing the info objects for the
            // protocol. This allows using standard parsers as if the
            // info files had a fixed format.
            final BullBoardBasicGen bullBoardBasicGen =
                extractBullBoardBasicGen(newargs);


            // Instantiate the info generator for the given bulletin
            // board.
            final InfoGenerator generator =
                instantiateInfoGenerator(className, bullBoardBasicGen);

            // Generate empty infos.
            final ProtocolInfo pi = generator.newProtocolInfo();
            final PrivateInfo pri = generator.newPrivateInfo();
            final PartyInfo pai = pi.getFactory().newInstance();

            // Generate infos with default values filled in.
            final ProtocolInfo dpi = generator.defaultProtocolInfo();
            final PrivateInfo dpri =
                generator.defaultPrivateInfo(dpi, randomSource);
            final PartyInfo dpai =
                generator.defaultPartyInfo(dpi, dpri, randomSource);

            // Parsed command line data.
            final Opt opt = parseCommandLine(commandName,
                                             pi, dpi, pri, dpri, pai, dpai,
                                             newargs);

            // Name of output protocol info file.
            final File protocolInfoFile =
                new File(opt.getStringValue("protInfoOut", ""));


            OptUtil.processHelpAndVersion(opt);

            if (opt.getBooleanValue("-prot")) {

                transferValues(pi, opt, dpi);
                generator.validateLocal(pi);
                pi.toXML(protocolInfoFile);

            } else if (opt.getBooleanValue("-party")) {

                generatePartyInfo(generator, opt,
                                  pi, pri, dpri, pai, dpai,
                                  protocolInfoFile);

            } else if (opt.getBooleanValue("-merge")) {

                final String[] mpars = opt.getMultiParameters();

                mergeProtInfos(generator, protocolInfoFile, mpars);

            } else if (opt.getBooleanValue("-digest")) {

                System.out.println(digestOfFile(opt));

            } else if (opt.valueIsGiven("-schema")) {
                final String schemaType = opt.getStringValue("-schema");
                if ("protocol".equals(schemaType)) {
                    System.out.println(pi.generateSchema());
                } else if ("private".equals(schemaType)) {
                    System.out.println(pri.generateSchema());
                } else {
                    throw new InfoException("Invalid schema type! ("
                                            + schemaType + ")");
                }
            }

        } catch (final InfoException ie) {

            GenUtil.processErrors(ie, cerrFlag, eFlag);

        } finally {

            TempFile.free();
        }
    }
}
