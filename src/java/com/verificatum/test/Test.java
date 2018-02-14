
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

package com.verificatum.test;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

import com.verificatum.crypto.RandomDevice;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.ExtIO;
import com.verificatum.eio.Hex;
import com.verificatum.eio.TempFile;
import com.verificatum.ui.gen.GenUtil;
import com.verificatum.ui.opt.Opt;
import com.verificatum.ui.opt.OptException;
import com.verificatum.ui.opt.OptUtil;

// FB_ANNOTATION import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


/**
 * Simple class for unit testing. Each set of tests is represented by
 * a class and each individual test is represented by a public static
 * method with <code>boolean</code> return type. Each test should
 * simply return true or false depending on the test passes or not. If
 * a test throws an exception, then a stack trace of the throwable is
 * printed.
 *
 * @author Douglas Wikstrom
 */
// PMD_ANNOTATION @SuppressWarnings("PMD.AvoidCatchingGenericException")
public final class Test {

    /**
     * Ruler between tests.
     */
    public static final String RULER =
        "----------------------------------------"
        + "------------------------------";

    /**
     * Maximal running time of each test.
     */
    public static final int DEFAULT_TEST_TIME = 1000;

    /**
     * Stream where the result of the test is written.
     */
    static PrintStream ps;

    /**
     * Constructor needed to avoid that this class is instantiated.
     */
    private Test() {
    }


    /**
     * Initialize working directory.
     *
     * @param randomSource Source of randomness.
     * @param opt Parsed command line.
     */
    private static void initWorkingDirectory(final RandomSource randomSource,
                                             final Opt opt) {

        // The default is probably safe, but in any case it can be
        // overridden from a wrapper of this program.

        File wd = new File("/tmp/com.verificatum/");
        if (opt.valueIsGiven("-wd")) {
            wd = new File(wd, opt.getStringValue("-wd"));
        } else {
            final byte[] subdirbytes = randomSource.getBytes(10);
            wd = new File(wd, Hex.toHexString(subdirbytes));
        }

        try {
            ExtIO.mkdirs(wd);
        } catch (final EIOException eioe) {
            throw new TestError("Unable to create working directory!", eioe);
        }
        TempFile.init(wd);
    }

    /**
     * Returns current time in milliseconds.
     *
     * @return Current time in milliseconds.
     */
    public static long now() {
        return System.currentTimeMillis();
    }

    /**
     * Returns false until the given number of milliseconds has
     * passed.
     *
     * @param t Number of milliseconds that must pass to make the
     * output true.
     * @param ms Starting time.
     * @return False until the given number of milliseconds has
     *         passed.
     */
    public static boolean done(final long t, final long ms) {
        return System.currentTimeMillis() > t + ms;
    }

    /**
     * Generates a basic command line parser instance.
     *
     * @param commandName Command name.
     * @return Command line parser.
     */
    public static Opt opt(final String commandName) {

        final String defaultErrorString = "Invalid usage form, please use \""
            + commandName + " -h\" for usage information!";

        final Opt opt = new Opt(commandName, defaultErrorString);

        final String testString =
            "Test classes, which may either instance test methods or static "
            + "test methods. To execute any instance test methods, the class "
            + "must have a constructor with signature "
            + "\"public <classname>(TestParameters)\", and each test must "
            + "have the signature \"public boolean <funcname>()\". "
            + "If a test succeeds, then the function must return true. If it "
            + "fails, then it must throw an Exception or Error.";
        opt.addParameter("test", testString);

        opt.addOption("-e", "", "Print stack trace for exceptions.");
        opt.addOption("-cerr", "",
                      "Print error messages as clean strings without any "
                      + "error prefix or newlines.");

        opt.addOption("-h", "", "Display usage information");
        opt.addOption("-exec", "", "Execute tests.");
        opt.addOption("-version", "", "Print the package version.");


        opt.addOption("-wd", "string",
                      "Working directory used for file based arrays. This "
                      + "defaults to a uniquely named subdirectory of "
                      + "/tmp/com.verificatum.");

        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h###");

        opt.addUsageForm();
        opt.appendToUsageForm(1, "-exec#-e,-cerr,-wd#+test#");

        opt.addUsageForm();
        opt.appendToUsageForm(2, "-version###");

        final String s =
            "Executes the tests implemented by the test classes given "
            + "as parameters.";

        opt.appendDescription(s);

        return opt;
    }

    /**
     * Parses command line.
     *
     * @param commandName Name of wrapper of this tool.
     * @param newargs Command-line arguments.
     * @return Parsed command line arguments.
     * @throws TestException If command line arguments can
     * not be parsed.
     */
    private static Opt parseCommandLine(final String commandName,
                                        final String[] newargs)
        throws TestException {
        final Opt opt = opt(commandName);
        try {
            opt.parse(newargs);
        } catch (final OptException oe) {
            throw new TestException(oe.getMessage(), oe);
        }
        return opt;
    }

    /**
     * This method simply runs all the tests, i.e., all public static
     * methods of each class which is listed on the command line.
     *
     * @param args Names of classes containing tests.
     */
    public static void main(final String[] args) {

        if (args.length < 1) {
            System.err.println("Missing command name!");
            System.exit(0);
        }

        // We must treat the flags -e and -cerr in an ad hoc way to
        // make sure that they work even when parsing the command line
        // fails.
        final boolean cerrFlag = GenUtil.specialFlag("-cerr", args);
        final boolean eFlag = GenUtil.specialFlag("-e", args);

        try {

            // Parse command line arguments.
            final Opt opt =
                parseCommandLine(args[0],
                                 Arrays.copyOfRange(args, 1, args.length));

            OptUtil.processHelpAndVersion(opt);

            if (opt.getBooleanValue("-exec")) {

                initWorkingDirectory(new RandomDevice(), opt);

                ps = System.out;

                final TestParameters tp =
                    new TestParameters("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                                       + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                                       DEFAULT_TEST_TIME,
                                       ps);

                final String[] clazzNames = opt.getMultiParameters();

                int succ = 0;

                ps.println("\n " + RULER + "\n");
                ps.println(" EXECUTING TEST SEQUENCE\n");

                final String s =
                    " You can execute a test separately using the \"vtest\" "
                    + "command. Use \n \""
                    + args[0] + " -h\" to learn how this command can be "
                    + "invoked, e.g., to\n implement and run your own tests.\n";
                ps.println(s);

                for (final String arg : clazzNames) {
                    try {
                        final int result = runTests(Class.forName(arg), tp);
                        if (result < 0) {
                            ps.println("Aborting test sequence...");
                            System.exit(1);
                        } else {
                            succ += result;
                        }
                    } catch (final ClassNotFoundException cnfe) {
                        cnfe.printStackTrace(System.err);
                        System.exit(1);
                    }
                }
                ps.println();
                ps.println(" EXECUTED " + succ + " TESTS SUCCESSFULLY.\n");

                System.exit(0);
            }
        } catch (final TestException te) {

            GenUtil.processErrors(te, cerrFlag, eFlag);

        } finally {

            TempFile.free();
        }
    }

    /**
     * Prints error information.
     *
     * @param clazz Name of tested class.
     * @param t Exception or error that is thrown.
     */
    public static void errorStop(final Class<?> clazz, final Throwable t) {
        ps.println("ERROR!\n\nAborting tests in " + clazz.getName()
                   + ".\n");

        if (t.getCause() != null) {
            t.getCause().printStackTrace(System.err);
        }
    }

    /**
     * Indicates if the method is a test method.
     *
     * @param method Method to be tested.
     * @param isStatic Determines if we are looking for a static of
     * instance method.
     * @return True or false depending on if the method is a test
     * method or not.
     */
    private static boolean isTestMethod(final Method method,
                                        final boolean isStatic) {
        final int mod = method.getModifiers();
        final Class<?>[] paramTypes = method.getParameterTypes();

        if (!method.getReturnType().toString().equals("void")
            || !Modifier.isPublic(mod)) {
            return false;
        }
        if (Modifier.isStatic(mod) != isStatic) {
            return false;
        }
        if (Modifier.isStatic(mod)) {
            return paramTypes.length == 1
                && paramTypes[0].equals(TestParameters.class);
        } else {
            return paramTypes.length == 0;
        }
    }

    /**
     * Extracts the test methods of a class.
     *
     * @param methMap Destination of test methods.
     * @param clazz Class containing test methods.
     * @param isStatic Determines if we are looking for a static of
     * instance method.
     */
    private static void getTestMethodsInner(final Map<String, Method> methMap,
                                            final Class<?> clazz,
                                            final boolean isStatic) {
        for (final Method method : clazz.getDeclaredMethods()) {
            final String name = method.getName();
            if (isTestMethod(method, isStatic) && !methMap.containsKey(name)) {
                methMap.put(name, method);
            }
        }
        final Class<?> superclazz = clazz.getSuperclass();
        if (superclazz.getName().startsWith("com.verificatum")) {
            getTestMethodsInner(methMap, superclazz, isStatic);
        }
    }

    /**
     * Extracts the test methods of a class.
     *
     * @param clazz Class containing test methods.
     * @param isStatic Determines if we are looking for a static of
     * instance method.
     * @return List of test methods sorted by name.
     */
    private static List<Method> getTestMethods(final Class<?> clazz,
                                               final boolean isStatic) {
        final Map<String, Method> methMap =
            new ConcurrentHashMap<String, Method>();
        getTestMethodsInner(methMap, clazz, isStatic);
        final List<Method> methods = new ArrayList<Method>(methMap.values());
        Collections.sort(methods, new MethodComparator());
        return methods;
    }

    /**
     * Instantiates a test class.
     *
     * @param clazz Class containing test methods.
     * @param tp Test parameters.
     * @return Instance of test class.
     */
    private static Object testInstance(final Class<?> clazz,
                                       final TestParameters tp) {
        Object instance = null;
        try {
            final Constructor<?> constructor =
                clazz.getDeclaredConstructor(TestParameters.class);
            instance = constructor.newInstance(tp);
        } catch (final Exception e) { // NOPMD
        }
        return instance;
    }

    /**
     * Execute test method using the given instance, which may be
     * null.
     *
     * @param method Test method.
     * @param instance Instance of test class.
     * @param tp Test parameters.
     * @throws Exception If if the method throws an exception.
     */
    // PMD_ANNOTATION @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private static void runMethod(final Method method,
                                  final Object instance,
                                  final TestParameters tp)
        throws Exception {
        final Object[] paramContainer;
        if (tp == null) {
            paramContainer = new Object[0];
        } else {
            paramContainer = new Object[1];
            paramContainer[0] = tp;
        }
        ps.print(" " + method.getName() + "... ");
        method.invoke(instance, paramContainer);
        ps.println("done.");
    }

    /**
     * Excercises the tests in the given class using the given test
     * parameters.
     *
     * @param clazz Class representing a set of tests.
     * @param tp Global test parameters.
     * @return Number of successful tests if all tests pass, or the
     *         negative of the number of the first failed test.
     */
    public static int runTests(final Class<?> clazz, final TestParameters tp) {

        ps.println("\n " + clazz.getName());
        ps.println(" " + RULER);

        final Object instance = testInstance(clazz, tp);
        int succ = 1;
        try {
            for (final Method method : getTestMethods(clazz, false)) {
                runMethod(method, instance, null);
                succ++;
            }
            for (final Method method : getTestMethods(clazz, true)) {
                runMethod(method, null, tp);
                succ++;
            }
        } catch (final Exception exc) {
            errorStop(clazz, exc);
            return -succ;
        } catch (final Error err) {
            errorStop(clazz, err);
            return -succ;
        }

        return succ - 1;
    }
}

/**
 * Lexical comparison of methods by name.
 */
// FB_ANNOTATION @SuppressFBWarnings(value =
// FB_ANNOTATION                     "SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
class MethodComparator implements Comparator<Method> {

    @Override
    public int compare(final Method left, final Method right) {
        return left.getName().compareTo(right.getName());
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj;
    }
}
