
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

package com.verificatum.arithm;

import java.io.File;
import java.io.IOException;

import com.verificatum.annotation.CoberturaIgnore;
import com.verificatum.crypto.RandomSource;
import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeF;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.ByteTreeWriterF;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.TempFile;
import com.verificatum.util.Pair;
import com.verificatum.util.Functions;


/**
 * Huge array of integers stored on file.
 *
 * @author Douglas Wikstrom
 */
public final class LargeIntegerArrayF extends LargeIntegerArray {

    /**
     * Number of elements in this instance.
     */
    final int size;

    /**
     * File storing the data of this instance.
     */
    final File file;

    /**
     * Expected number of bytes in arrays representing elements. This
     * is zero if there is no expected byte length.
     */
    public int expectedByteLength;

    /**
     * Creates an empty instance. It is the responsibility of the
     * programmer to fill this instance with data.
     *
     * @param size Number of elements in array.
     */
    protected LargeIntegerArrayF(final int size) {
        this.size = size;
        this.file = TempFile.getFile();
    }

    /**
     * Creates an instance of the given size and with data stored on
     * the given file. This assumes that these parameters are correct.
     *
     * @param size Size of permutation stored on file.
     * @param file Representation of permutation on file.
     */
    LargeIntegerArrayF(final int size, final File file) {
        this.size = size;
        this.file = file;
    }

    /**
     * Returns a writer that allows writing {@link #size} byte trees.
     *
     * @return Writer for the contents of this instance.
     */
    protected ByteTreeWriterF getWriter() {
        return ByteTreeWriterF.unsafeByteTreeWriterF(size, file);
    }

    /**
     * Returns a reader.
     *
     * @return Reader for the contents of this instance.
     */
    protected ByteTreeReader getReader() {
        return new ByteTreeF(file).getByteTreeReader();
    }

    /**
     * Returns a threaded batch reader that allows reading the next
     * batch in the background when processing.
     *
     * @return Threaded batch reader.
     */
    protected LargeIntegerBatchReader getBatchReader() {
        return new LargeIntegerBatchReader(getReader());
    }

    /**
     * Constructs an instance with the given integers.
     *
     * @param integers Integers of this instance.
     */
    LargeIntegerArrayF(final LargeInteger[] integers) {
        this(integers.length);

        final ByteTreeWriterF btw = getWriter();
        btw.unsafeWrite(integers);
        btw.close();
    }

    /**
     * Returns the total number of integers in the given arrays.
     *
     * @param arrays Input arrays.
     * @return Total number of integers.
     */
    protected static int totalSize(final LargeIntegerArray... arrays) {
        int size = 0;
        for (int i = 0; i < arrays.length; i++) {
            size += arrays[i].size();
        }
        return size;
    }

    /**
     * Constructs the concatenation of the given inputs.
     *
     * @param arrays Source arrays.
     */
    LargeIntegerArrayF(final LargeIntegerArray... arrays) {
        this(totalSize(arrays));

        final ByteTreeWriterF btw = getWriter();

        for (int i = 0; i < arrays.length; i++) {

            final ByteTreeReader btr =
                ((LargeIntegerArrayF) arrays[i]).getReader();

            while (btr.getRemaining() > 0) {

                final LargeInteger[] integers = readBatch(btr);
                btw.unsafeWrite(integers);

            }
            btr.close();
        }
        btw.close();
    }

    /**
     * Constructs an array of random integers.
     *
     * @param size Number of integers to generate.
     * @param bitLength Number of bits in each integer.
     * @param randomSource Source of random bits used to initialize
     * the array.
     */
    LargeIntegerArrayF(final int size,
                       final int bitLength,
                       final RandomSource randomSource) {
        this(size);

        final ByteTreeWriterF btw = getWriter();
        for (int i = 0; i < size; i++) {
            final LargeInteger li = new LargeInteger(bitLength, randomSource);
            btw.unsafeWrite(li);
        }
        btw.close();
    }

    /**
     * Constructs an array of random integers.
     *
     * @param size Number of elements in this instance.
     * @param value Value used to initialize this instance.
     */
    LargeIntegerArrayF(final int size, final LargeInteger value) {
        this(size);

        final ByteTreeWriterF btw = getWriter();
        final ByteTreeBasic bt = value.toByteTree();
        for (int i = 0; i < size; i++) {
            btw.unsafeWrite(bt);
        }
        btw.close();
    }

    /**
     * Constructs an array of consecutive integers.
     *
     * @param begin Inclusive starting integer in sequence.
     * @param end Exclusive ending integer in sequence.
     */
    LargeIntegerArrayF(final int begin, final int end) {
        this(end - begin);

        final ByteTreeWriterF btw = getWriter();
        for (int i = 0; i < size; i++) {
            final LargeInteger li = new LargeInteger(i);
            btw.unsafeWrite(li.toByteTree());
        }
        btw.close();
    }

    /**
     * Returns the actual size or expected size depending on if the
     * latter is zero or not.
     *
     * @param expectedSize Expected size.
     * @param actualSize Actual size.
     * @return Actual size or expected size depending on if the latter
     * is zero or not.
     */
    @CoberturaIgnore
    protected static int zeroMapped(final int expectedSize,
                                    final int actualSize) {
        if (expectedSize == 0) {
            return actualSize;
        } else {
            return expectedSize;
        }
    }

    /**
     * Returns the array of integers represented by the input. This
     * constructor requires that each integer falls into the given
     * interval, but also that the representation of each integer is
     * of equal size to the byte tree representation of the upper
     * bound.
     *
     * @param size Expected number of elements in array. If the
     * expected size (number of elements) is set to zero, then the
     * input can have any size.
     * @param btr Should contain a representation of an array of
     * integers.
     * @param lb Non-negative inclusive lower bound for integers.
     * @param ub Positive exclusive upper bound for integers.
     *
     * @throws ArithmFormatException If the input does not represent a
     *  an array of integers satisfying the given bounds.
     * @throws EIOException If the input contains a byte tree of the
     * wrong format.
     */
    LargeIntegerArrayF(final int size,
                       final ByteTreeReader btr,
                       final LargeInteger lb,
                       final LargeInteger ub)
        throws ArithmFormatException, EIOException {

        // It is important to not mix up size and this.size, since the
        // former may be zero when the latter is not.
        this(zeroMapped(size, btr.getRemaining()));

        if (size != 0 && btr.getRemaining() != size) {
            throw new ArithmFormatException("Unexpected number of integers!");
        }

        final int ebl = ub.toByteArray().length;

        final ByteTreeWriterF btw = getWriter();

        // We need this.size here and not size.
        for (int i = 0; i < this.size; i++) {

            final LargeInteger integer =
                new LargeInteger(ebl, btr.getNextChild(), null);

            if (lb.compareTo(integer) <= 0 && integer.compareTo(ub) < 0) {

                btw.unsafeWrite(integer);

            } else {

                btw.close();
                throw new ArithmFormatException("Integer outside interval!");

            }
        }
        btw.close();
    }

    /**
     * Reads a batch of large integers. The size of the batch is the
     * minimum of {@link #batchSize} and the remaining number of
     * integers in the reader.
     *
     * @param btr Source of integers.
     * @return Array of integers.
     */
    protected static LargeInteger[] readBatch(final ByteTreeReader btr) {
        return readBatch(Math.min(batchSize, btr.getRemaining()), btr);
    }

    /**
     * Reads the given number of integers from the reader.
     *
     * @param len Number integers to read.
     * @param btr Representation of the integers.
     * @return Array of integers.
     */
    protected static LargeInteger[] readBatch(final int len,
                                              final ByteTreeReader btr) {
        try {

            final LargeInteger[] res = new LargeInteger[len];
            for (int i = 0; i < len; i++) {
                res[i] = new LargeInteger(btr.getNextChild());
            }
            return res;
        } catch (final EIOException eioe) {
            throw new ArithmError("Unable to read data!", eioe);
        } catch (final ArithmFormatException afe) {
            throw new ArithmError("Unable to read data!", afe);
        }
    }

    // Documented in LargeIntegerArray.java

    @Override
    public LargeIntegerIterator getIterator() {
        return new LargeIntegerIteratorF(file);
    }

    @Override
    public LargeIntegerArray modInv(final LargeInteger modulus)
        throws ArithmException {

        final LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr = getReader();

        while (btr.getRemaining() > 0) {

            final LargeInteger[] integers = readBatch(btr);
            btw.unsafeWrite(LargeInteger.modInv(integers, modulus));
        }
        btr.close();
        btw.close();

        return res;
    }

    @Override
    public LargeIntegerArray copyOfRange(final int startIndex,
                                         final int endIndex) {

        final ByteTreeReader btr = getReader();
        btr.unsafeSkipChildren(startIndex);

        int resSize = endIndex - startIndex;
        final LargeIntegerArrayF res = new LargeIntegerArrayF(resSize);
        final ByteTreeWriterF btw = res.getWriter();

        while (resSize > 0) {

            final int len = Math.min(batchSize, resSize);
            final LargeInteger[] integers = readBatch(len, btr);
            btw.unsafeWrite(integers);
            resSize -= integers.length;
        }
        btr.close();
        btw.close();

        return res;
    }

    @Override
    public LargeInteger[] integers() {
        final ByteTreeReader btr = getReader();
        final LargeInteger[] res = readBatch(size, btr);
        btr.close();
        return res;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LargeIntegerArrayF)) {
            return false;
        }
        return compareTo((LargeIntegerArrayF) obj) == 0;
    }

    @Override
    public int hashCode() {
        return Functions.hashCode(this);
    }

    @Override
    public LargeIntegerArray extract(final boolean[] valid) {
        int total = 0;
        for (int i = 0; i < valid.length; i++) {
            if (valid[i]) {
                total++;
            }
        }

        final LargeIntegerArrayF res = new LargeIntegerArrayF(total);
        final ByteTreeWriterF btw = res.getWriter();

        final LargeIntegerIterator lii = getIterator();

        for (int i = 0; i < valid.length; i++) {
            final LargeInteger integer = lii.next();
            if (valid[i]) {
                btw.unsafeWrite(integer);
            }
        }
        lii.close();
        btw.close();

        return res;
    }

    @Override
    public LargeIntegerArray permute(final Permutation permutation) {

        if (!(permutation instanceof PermutationF)) {
            final String e =
                "Non file-mapped permutation used with file-mapped array!";
            throw new ArithmError(e);
        }
        final PermutationF permutationF = (PermutationF) permutation;

        final ByteTreeF resultByteTree =
            permutationF.applyPermutation(new ByteTreeF(file));

        return new LargeIntegerArrayF(size(), resultByteTree.file);
    }

    @Override
    public LargeIntegerArray modAdd(final LargeIntegerArray termsArray,
                                    final LargeInteger modulus) {
        final LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr1 = getReader();
        final ByteTreeReader btr2 =
            ((LargeIntegerArrayF) termsArray).getReader();

        while (btr1.getRemaining() > 0) {

            final LargeInteger[] integers1 = readBatch(btr1);
            final LargeInteger[] integers2 = readBatch(btr2);

            btw.unsafeWrite(LargeInteger.modAdd(integers1, integers2, modulus));
        }
        btr2.close();
        btr1.close();
        btw.close();

        return res;
    }

    @Override
    public LargeIntegerArray modNeg(final LargeInteger modulus) {

        final LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr = getReader();

        while (btr.getRemaining() > 0) {

            final LargeInteger[] integers = readBatch(btr);
            btw.unsafeWrite(LargeInteger.modNeg(integers, modulus));
        }
        btr.close();
        btw.close();

        return res;
    }

    @Override
    public LargeIntegerArray modMul(final LargeIntegerArray factorsArray,
                                    final LargeInteger modulus) {
        final LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr1 = getReader();
        final ByteTreeReader btr2 =
            ((LargeIntegerArrayF) factorsArray).getReader();

        while (btr1.getRemaining() > 0) {

            final LargeInteger[] integers1 = readBatch(btr1);
            final LargeInteger[] integers2 = readBatch(btr2);

            btw.unsafeWrite(LargeInteger.modMul(integers1, integers2, modulus));
        }
        btr2.close();
        btr1.close();
        btw.close();

        return res;
    }

    @Override
    public LargeIntegerArray modMul(final LargeInteger scalar,
                                    final LargeInteger modulus) {

        final LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr = getReader();

        while (btr.getRemaining() > 0) {

            final LargeInteger[] integers = readBatch(btr);
            btw.unsafeWrite(LargeInteger.modMul(integers, scalar, modulus));
        }
        btr.close();
        btw.close();

        return res;
    }

    @Override
    public LargeIntegerArray modPow(final LargeIntegerArray exponentsArray,
                                    final LargeInteger modulus) {
        final LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr1 = getReader();
        final ByteTreeReader btr2 =
            ((LargeIntegerArrayF) exponentsArray).getReader();

        final LargeIntegerBatchReader br1 = new LargeIntegerBatchReader(btr1);
        final LargeIntegerBatchReader br2 = new LargeIntegerBatchReader(btr2);
        final LargeIntegerBatchWriter bw = new LargeIntegerBatchWriter(btw);

        LargeInteger[] integers1 = br1.readNext();
        LargeInteger[] integers2 = br2.readNext();
        while (integers1 != null && integers2 != null) {

            bw.writeNext(LargeInteger.modPow(integers1, integers2, modulus));

            integers1 = br1.readNext();
            integers2 = br2.readNext();
        }
        br2.close();
        br1.close();
        bw.close();

        return res;
    }

    @Override
    public LargeIntegerArray modPow(final LargeInteger exponent,
                                    final LargeInteger modulus) {

        final LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr = getReader();

        final LargeIntegerBatchReader br = new LargeIntegerBatchReader(btr);
        final LargeIntegerBatchWriter bw = new LargeIntegerBatchWriter(btw);

        LargeInteger[] integers = br.readNext();
        while (integers != null) {

            bw.writeNext(LargeInteger.modPow(integers, exponent, modulus));
            integers = br.readNext();
        }
        br.close();
        bw.close();

        return res;
    }

    @Override
    public LargeIntegerArray modPowVariant(final LargeInteger basis,
                                           final LargeInteger modulus) {

        final LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr = getReader();

        final LargeIntegerBatchReader br = new LargeIntegerBatchReader(btr);
        final LargeIntegerBatchWriter bw = new LargeIntegerBatchWriter(btw);

        LargeInteger[] integers = br.readNext();
        while (integers != null) {

            bw.writeNext(basis.modPow(integers, modulus));
            integers = br.readNext();
        }

        bw.close();
        br.close();

        return res;
    }

    @Override
    public LargeInteger modPowProd(final LargeIntegerArray exponentsArray,
                                   final LargeInteger modulus) {
        final ByteTreeReader btr1 = getReader();
        final ByteTreeReader btr2 =
            ((LargeIntegerArrayF) exponentsArray).getReader();

        final LargeIntegerBatchReader br1 = new LargeIntegerBatchReader(btr1);
        final LargeIntegerBatchReader br2 = new LargeIntegerBatchReader(btr2);

        LargeInteger res = LargeInteger.ONE;

        LargeInteger[] integers1 = br1.readNext();
        LargeInteger[] integers2 = br2.readNext();
        while (integers1 != null && integers2 != null) {

            final LargeInteger tmp =
                LargeInteger.modPowProd(integers1, integers2, modulus);
            res = res.mul(tmp).mod(modulus);

            integers1 = br1.readNext();
            integers2 = br2.readNext();
        }
        br2.close();
        br1.close();

        return res;
    }

    @Override
    public LargeInteger modProd(final LargeInteger modulus) {
        final ByteTreeReader btr = getReader();
        final LargeIntegerBatchReader br = new LargeIntegerBatchReader(btr);

        LargeInteger res = LargeInteger.ONE;
        LargeInteger[] integers = br.readNext();
        while (integers != null) {

            final LargeInteger tmp = LargeInteger.modProd(integers, modulus);
            res = res.mul(tmp).mod(modulus);
            integers = br.readNext();
        }
        br.close();

        return res;
    }

    @Override
    public LargeIntegerArray modProds(final LargeInteger modulus) {

        final LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr = getReader();

        LargeInteger agg = LargeInteger.ONE;
        while (btr.getRemaining() > 0) {

            final LargeInteger[] integers = readBatch(btr);
            final LargeInteger[] tmp =
                LargeInteger.modProds(agg, integers, modulus);
            agg = tmp[tmp.length - 1];
            btw.unsafeWrite(tmp);
        }
        btr.close();
        btw.close();

        return res;
    }

    @Override
    public LargeInteger get(final int index) {
        final ByteTreeReader btr = getReader();
        btr.unsafeSkipChildren(index);
        final LargeInteger res =
            LargeInteger.unsafeLargeInteger(btr.unsafeGetNextChild());
        btr.close();
        return res;
    }

    @Override
    public LargeIntegerArray shiftPush(final LargeInteger integer) {

        final LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr = getReader();

        btw.unsafeWrite(integer);

        while (btr.getRemaining() > 1) {

            final int len = Math.min(batchSize, btr.getRemaining() - 1);
            final LargeInteger[] tmp = readBatch(len, btr);
            btw.unsafeWrite(tmp);
        }
        btr.close();
        btw.close();

        return res;
    }

    @Override
    public Pair<LargeIntegerArray, LargeInteger>
        modRecLin(final LargeIntegerArray array, final LargeInteger modulus) {
        final LargeIntegerArrayF res = new LargeIntegerArrayF(size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr1 = getReader();
        final ByteTreeReader btr2 = ((LargeIntegerArrayF) array).getReader();

        LargeInteger agg = LargeInteger.ZERO;
        while (btr1.getRemaining() > 0) {

            final LargeInteger[] integers1 = readBatch(btr1);
            final LargeInteger[] integers2 = readBatch(btr2);

            final LargeInteger[] tmp = new LargeInteger[integers1.length];
            for (int i = 0; i < tmp.length; i++) {
                agg = agg.mul(integers2[i]).add(integers1[i]).mod(modulus);
                tmp[i] = agg;
            }

            btw.unsafeWrite(tmp);
        }
        btr2.close();
        btr1.close();
        btw.close();

        return new Pair<LargeIntegerArray, LargeInteger>(res, agg);
    }

    @Override
    public ByteTreeBasic toByteTree() {
        return new ByteTreeF(file);
    }

    @Override
    public ByteTreeBasic toByteTree(final int expectedByteLength) {

        if (this.expectedByteLength == expectedByteLength) {
            return new ByteTreeF(file);
        }

        if (this.expectedByteLength != 0) {
            throw new ArithmError("Attempting to change expected byte length!");
        }

        this.expectedByteLength = expectedByteLength;

        final File btFile = TempFile.getFile();

        ByteTreeWriterF btw;
        try {
            btw = new ByteTreeWriterF(size, btFile);
        } catch (final IOException ioe) {
            throw new ArithmError("Unable to create temporary file!", ioe);
        }

        final ByteTreeReader btr = getReader();
        while (btr.getRemaining() > 0) {

            final LargeInteger[] tmp = readBatch(btr);

            for (int i = 0; i < tmp.length; i++) {
                btw.unsafeWrite(tmp[i].toByteTree(expectedByteLength));
            }
        }
        btw.close();
        btr.close();

        TempFile.delete(file);

        if (!btFile.renameTo(file)) {
            throw new ArithmError("Unable to rename temporary file!");
        }

        return new ByteTreeF(file);
    }

    @Override
    public boolean quadraticResidues(final LargeInteger prime) {

        final ByteTreeReader btr = getReader();

        final LargeIntegerBatchReader br = new LargeIntegerBatchReader(btr);

        boolean res = true;
        LargeInteger[] integers = br.readNext();
        while (integers != null && res) {

            res = LargeInteger.quadraticResidues(integers, prime);
            integers = br.readNext();
        }
        br.close();

        return res;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void free() {
        TempFile.delete(file);
    }

    /**
     * Threaded writer that allows writing data in the background
     * while performing further computations.
     *
     * @author Douglas Wikstrom
     */
    static class LargeIntegerBatchWriter {

        /**
         * Underlying byte tree writer.
         */
        ByteTreeWriterF btw;

        /**
         * Underlying byte tree writer.
         */
        boolean active;

        /**
         * Creates a threaded writer on top of the given byte tree
         * writer.
         *
         * @param btw Underlying byte tree writer.
         */
        LargeIntegerBatchWriter(final ByteTreeWriterF btw) {
            this.btw = btw;
            this.active = false;
        }

        /**
         * Writes a batch of large integers to file as a back ground
         * process. This method blocks if the previous batch has not
         * yet been written to file.
         *
         * @param batch Integers to write.
         */
        void writeNext(final LargeInteger[] batch) {

            while (active) {
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException ie) {
                }
            }

            active = true;

            final Thread thread = new Thread() {

                    @Override
                    public void run() {
                        btw.unsafeWrite(batch);
                        active = false;
                    }
                };
            thread.start();
        }

        /**
         * Closes the writer. This method blocks until this writer has
         * written all pending data and closed the underlying byte
         * tree writer.
         */
        void close() {
            while (active) {
                try {
                    Thread.sleep(100);
                } catch (final InterruptedException ie) {
                }
            }
            btw.close();
        }
    }
}
