
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

package com.verificatum.eio;

import java.util.Arrays;

import com.verificatum.crypto.PRGHeuristic;
import com.verificatum.crypto.RandomSource;
import com.verificatum.test.TestParameters;
import com.verificatum.util.Timer;


/**
 * Tests some of the functionality of {@link ByteTree} and associated
 * classes.
 *
 * @author Douglas Wikstrom
 */
// PMD_ANNOTATION @SuppressWarnings("PMD.SignatureDeclareThrowsException")
public final class TestByteTree {

    /**
     * Lock object.
     */
    private static Object lock = new Object();

    /**
     * Source of randomness.
     */
    static RandomSource rs;

    /**
     * Constructor needed to avoid that this class is instantiated.
     */
    private TestByteTree() {
    }

    /**
     * Instantiate a random source.
     *
     * @param tp Test parameters configuration of the servers.
     * @throws Exception when failing test.
     */
    protected static void setup(final TestParameters tp)
        throws Exception {

        synchronized (lock) {
            rs = new PRGHeuristic(ExtIO.getBytes(tp.prgseed));
        }
    }

    /**
     * Verify that a boolean can be convert from and to byte tree.
     *
     * @param tp Test parameters configuration of the servers.
     * @throws Exception when failing test.
     */
    public static void convertBoolean(final TestParameters tp)
        throws Exception {

        setup(tp);

        boolean b;
        ByteTree bt;

        bt = ByteTree.booleanToByteTree(true);
        b = ByteTree.byteTreeToBoolean(bt);
        assert b
            : "Failed to convert true value!";

        bt = ByteTree.booleanToByteTree(false);
        b = ByteTree.byteTreeToBoolean(bt);
        assert !b
            : "Failed to convert false value!";
    }

    /**
     * Verify that an int can be convert from and to byte tree.
     *
     * @param tp Test parameters configuration of the servers.
     * @throws Exception when failing test.
     */
    public static void convertInt(final TestParameters tp) throws Exception {

        setup(tp);

        final Timer timer = new Timer(tp.milliSeconds);

        final byte[] intAsByte = new byte[4];
        final byte[] intAsByte2 = new byte[4];

        while (!timer.timeIsUp()) {

            rs.getBytes(intAsByte);

            final int k = ExtIO.readInt(intAsByte, 0);

            ExtIO.writeInt(intAsByte2, 0, k);

            final int l = ExtIO.readInt(intAsByte2, 0);

            final ByteTree bt = ByteTree.intToByteTree(l);

            final int m = ByteTree.byteTreeToInt(bt);

            assert k == m
                : "Failed to convert int!";
        }
    }

    /**
     * Verify that a short can be convert from and to byte tree.
     *
     * @param tp Test parameters configuration of the servers.
     * @throws Exception when failing test.
     */
    public static void convertShort(final TestParameters tp) throws Exception {

        setup(tp);

        final Timer timer = new Timer(tp.milliSeconds);

        final byte[] shortAsByte = new byte[4];
        final byte[] shortAsByte2 = new byte[4];

        while (!timer.timeIsUp()) {

            rs.getBytes(shortAsByte);

            final short k = ExtIO.readShort(shortAsByte, 0);

            ExtIO.writeShort(shortAsByte2, 0, k);

            final short l = ExtIO.readShort(shortAsByte2, 0);

            final ByteTree bt = ByteTree.shortToByteTree(l);

            final short m = ByteTree.byteTreeToShort(bt);

            assert k == m
                : "Failed to convert short!";
        }
    }

    /**
     * Verify that a string can be convert from and to byte tree.
     *
     * @param tp Test parameters configuration of the servers.
     * @throws Exception when failing test.
     */
    public static void convertString(final TestParameters tp) throws Exception {

        setup(tp);

        final Timer timer = new Timer(tp.milliSeconds);

        final int size = 1;

        while (!timer.timeIsUp()) {

            final byte[] stringBytes = new byte[size];

            rs.getBytes(stringBytes);

            String s1 = new String(stringBytes, "UTF-8");

            // This roundabout is needed to make sure that we start
            // from a valid string.
            System.arraycopy(s1.getBytes("UTF-8"),
                             0,
                             stringBytes,
                             0,
                             size);
            s1 = new String(stringBytes, "UTF-8");

            final ByteTree bt = ByteTree.stringToByteTree(s1);
            final String s2 = ByteTree.byteTreeToString(bt);

            assert s1.equals(s2)
                : "Failed to convert string!";
        }
    }

    /**
     * Generates a random double between zero and one.
     *
     * @return Random double value.
     */
    protected static double randomProbability() {

        final byte[] bytes = new byte[4];
        rs.getBytes(bytes);
        final int ri = Math.abs(ExtIO.readInt(bytes, 0));

        final double res = ((double) ri) / Integer.MAX_VALUE;
        if (res == 0) {
            return res + 1;
        } else {
            return res;
        }
    }

    /**
     * Generates a byte tree.
     *
     * @param numberOfNodes Number of nodes in generated subtree.
     * @return Generated byte tree.
     */
    protected static ByteTree generateByteTree(final int numberOfNodes) {

        final int contentSize = 5;

        if (numberOfNodes < 3) {

            final byte[] content = new byte[contentSize];
            rs.getBytes(content);

            return new ByteTree(content);

        } else {

            final double rp = randomProbability();

            int leftNumberOfNodes = (int) (rp * numberOfNodes);
            if (leftNumberOfNodes == 0) {
                leftNumberOfNodes = 1;
            }
            if (leftNumberOfNodes == numberOfNodes) {
                leftNumberOfNodes--;
            }

            final int rightNumberOfNodes =
                numberOfNodes - leftNumberOfNodes - 1;

            final ByteTree[] node = new ByteTree[2];

            node[0] = generateByteTree(leftNumberOfNodes);
            node[1] = generateByteTree(rightNumberOfNodes);

            return new ByteTree(node);
        }
    }

    /**
     * Verify that a byte tree can be written and recovered.
     *
     * @param tp Test parameters configuration of the servers.
     * @throws Exception when failing test.
     */
    public static void writeByteTree(final TestParameters tp) throws Exception {

        setup(tp);

        ByteTree bt1;
        byte[] byteArray1;
        ByteTree bt2;
        byte[] byteArray2;

        bt1 = new ByteTree();
        byteArray1 = bt1.toByteArray();
        bt2 = new ByteTree(byteArray1, null);
        byteArray2 = bt2.toByteArray();

        assert Arrays.equals(byteArray1, byteArray2)
            : "Failed to convert single byte tree!";

        final Timer timer = new Timer(tp.milliSeconds);

        int size = 1;

        while (!timer.timeIsUp()) {

            bt1 = generateByteTree(size);
            byteArray1 = bt1.toByteArray();
            bt2 = new ByteTree(byteArray1, null);
            byteArray2 = bt2.toByteArray();

            assert Arrays.equals(byteArray1, byteArray2)
                : "Failed to convert random binary byte tree!";

            size++;
        }
    }
}
