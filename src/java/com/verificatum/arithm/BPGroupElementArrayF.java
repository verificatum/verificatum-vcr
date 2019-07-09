
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
import java.io.FileNotFoundException;
import java.io.IOException;

import com.verificatum.eio.ByteTreeBasic;
import com.verificatum.eio.ByteTreeF;
import com.verificatum.eio.ByteTreeReader;
import com.verificatum.eio.ByteTreeWriterF;
import com.verificatum.eio.EIOException;
import com.verificatum.eio.TempFile;


/**
 * On-file implementation of an array of {@link BPGroupElement} of a
 * {@link BPGroup}.
 *
 * @author Douglas Wikstrom
 */
public class BPGroupElementArrayF extends PGroupElementArray {

    /**
     * Number of elements in this instance.
     */
    protected int size;

    /**
     * File storing the data of this instance.
     */
    protected File file;

    /**
     * Creates an empty instance. It is the responsibility of the
     * programmer to fill this instance with data.
     *
     * @param pGroup Group to which the elements of this array
     * belongs.
     * @param size Number of elements in array.
     */
    protected BPGroupElementArrayF(final PGroup pGroup, final int size) {
        super(pGroup);
        this.size = size;
        this.file = TempFile.getFile();
    }

    /**
     * Creates an instance from the given file. It is the
     * responsibility of the programmer to ensure that the contents of
     * the file is valid.
     *
     * @param pGroup Group to which the elements of this array
     * belongs.
     * @param size Number of elements in array.
     * @param file Representation of array.
     */
    protected BPGroupElementArrayF(final PGroup pGroup,
                                   final int size,
                                   final File file) {
        super(pGroup);
        this.size = size;
        this.file = file;
    }

    /**
     * Returns the batch size used when processing arrays.
     *
     * @return Batch size used when processing arrays.
     */
    protected static int batchSize() {
        return LargeIntegerArray.batchSize;
    }

    /**
     * Returns a writer that allows writing {@link #size} byte trees.
     *
     * @return Writer for the contents of this instance.
     */
    protected ByteTreeWriterF getWriter() {
        try {
            return new ByteTreeWriterF(size, file);
        } catch (final FileNotFoundException fnfe) {
            throw new ArithmError("Unable to create writer!", fnfe);
        } catch (final IOException ioe) {
            throw new ArithmError("Unable to create writer!", ioe);
        }
    }

    /**
     * Returns a writer that allows writing {@link #size} group
     * elements as batches.
     *
     * @return Writer for the contents of this instance.
     */
    protected BPGroupElementBatchWriter getBatchWriter() {
        return new BPGroupElementBatchWriter(getWriter());
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
     * Returns a reader that allows reading batches of group elements.
     *
     * @return Reader for the contents of this instance.
     */
    protected BPGroupElementBatchReader getBatchReader() {
        return new BPGroupElementBatchReader(pGroup, getReader());
    }

    /**
     * Constructs an array of elements of the given group.
     *
     * @param pGroup Group to which the elements of this array belong.
     * @param elements Elements of this array.
     */
    protected BPGroupElementArrayF(final PGroup pGroup,
                                   final PGroupElement[] elements) {
        this(pGroup, elements.length);

        final ByteTreeWriterF btw = getWriter();
        btw.unsafeWrite(elements);
        btw.close();
    }

    /**
     * Returns the total number of integers in the given arrays.
     *
     * @param arrays Input arrays.
     * @return Total number of integers.
     */
    protected static int totalSize(final PGroupElementArray... arrays) {
        int size = 0;
        for (int i = 0; i < arrays.length; i++) {
            size += arrays[i].size();
        }
        return size;
    }

    /**
     * Constructs the concatenation of the given inputs.
     *
     * @param pGroup Group to which the elements of this array belong.
     * @param arrays Source arrays.
     */
    protected BPGroupElementArrayF(final PGroup pGroup,
                                   final PGroupElementArray... arrays) {
        this(pGroup, totalSize(arrays));

        final ByteTreeWriterF btw = getWriter();

        for (int i = 0; i < arrays.length; i++) {

            final ByteTreeReader btr =
                ((BPGroupElementArrayF) arrays[i]).getReader();

            while (btr.getRemaining() > 0) {

                final PGroupElement[] elements = readBatch(pGroup, btr);
                btw.unsafeWrite(elements);

            }
            btr.close();
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
    protected static int zeroMapped(final int expectedSize,
                                    final int actualSize) {
        if (expectedSize == 0) {
            return actualSize;
        } else {
            return expectedSize;
        }
    }

    /**
     * Constructs an array of elements from the given representation.
     *
     * @param pGroup Group to which the elements of this array belong.
     * @param size Expected number of elements in array.
     * @param btr Representation of an instance.
     * @param safe Indicates if inputs should be verified or not.
     *
     * @throws ArithmFormatException If the input does not represent
     *  an instance.
     */
    protected BPGroupElementArrayF(final PGroup pGroup,
                                   final int size,
                                   final ByteTreeReader btr,
                                   final boolean safe)
        throws ArithmFormatException {

        // It is important to not mix up size and this.size, since the
        // former may be zero when the latter is not.
        this(pGroup, zeroMapped(size, btr.getRemaining()));

        if (size != 0 && btr.getRemaining() != size) {
            throw new ArithmFormatException("Unexpected number of integers!");
        }

        final ByteTreeWriterF btw = getWriter();

        try {
            while (btr.getRemaining() > 0) {
                final PGroupElement[] elements = readBatch(pGroup, btr);
                if (safe) {
                    ((BPGroup) this.pGroup).verifyUnsafe(elements);
                }
                btw.unsafeWrite(elements);
            }
        } finally {
            if (btw != null) {
                btw.close();
            }
        }
    }

    /**
     * Constructs an array of elements from the given representation.
     *
     * @param pGroup Group to which the elements of this array belong.
     * @param size Expected number of elements in array.
     * @param el Element to use.
     */
    protected BPGroupElementArrayF(final PGroup pGroup,
                                   final int size,
                                   final PGroupElement el) {
        this(pGroup, size);

        final ByteTreeWriterF btw = getWriter();

        for (int i = 0; i < size; i++) {
            btw.unsafeWrite(el);
        }
        btw.close();
    }

    /**
     * Reads the given number of elements from the reader.
     *
     * @param pGroup Group to which the elements of this array belong.
     * @param len Number of elements to read.
     * @param btr Representation of the elements.
     * @return Array of elements.
     */
    protected static PGroupElement[] readBatch(final PGroup pGroup,
                                               final int len,
                                               final ByteTreeReader btr) {
        try {

            final PGroupElement[] res = new PGroupElement[len];
            for (int i = 0; i < len; i++) {
                res[i] = pGroup.unsafeToElement(btr.getNextChild());
            }
            return res;

        } catch (final EIOException eioe) {
            throw new ArithmError("Unable to read data!", eioe);
        }
    }

    /**
     * Reads a batch of group elements. The size of the batch is the
     * minimum of {@link #batchSize()} and the remaining number of
     * elements in the reader.
     *
     * @param pGroup Group to which the elements of this array belong.
     * @param btr Source of elements.
     * @return Array of elements.
     */
    protected static PGroupElement[] readBatch(final PGroup pGroup,
                                               final ByteTreeReader btr) {
        final int len = Math.min(batchSize(), btr.getRemaining());
        return readBatch(pGroup, len, btr);
    }

    // Documented in PGroupElementArray.java
    @Override
    public ByteTreeBasic toByteTree() {
        return new ByteTreeF(file);
    }

    @Override
    public PGroupElement[] elements() {
        final ByteTreeReader btr = getReader();
        final PGroupElement[] res = readBatch(pGroup, size, btr);
        btr.close();
        return res;
    }

    @Override
    public PGroupElementIterator getIterator() {
        return new BPGroupElementIteratorF(pGroup, file);
    }

    @Override
    public PGroupElement get(final int index) {
        try {
            final ByteTreeReader btr = getReader();
            btr.skipChildren(index);
            final PGroupElement res =
                pGroup.unsafeToElement(btr.getNextChild());
            btr.close();
            return res;

        } catch (final EIOException eioe) {
            throw new ArithmError("Unable to read from array!", eioe);
        }
    }

    @Override
    public PGroupElementArray mul(final PGroupElementArray factorsArray) {
        if (size != factorsArray.size()) {
            throw new ArithmError(PGroup.DIFFERENT_LENGTHS);
        }

        final BPGroupElementArrayF res = new BPGroupElementArrayF(pGroup, size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr1 = getReader();
        final ByteTreeReader btr2 =
            ((BPGroupElementArrayF) factorsArray).getReader();

        while (btr1.getRemaining() > 0) {
            final PGroupElement[] elements1 = readBatch(pGroup, btr1);
            final PGroupElement[] elements2 = readBatch(pGroup, btr2);
            btw.unsafeWrite(pGroup.mul(elements1, elements2));
        }
        btr2.close();
        btr1.close();
        btw.close();

        return res;
    }

    @Override
    public PGroupElementArray inv() {

        final BPGroupElementArrayF res = new BPGroupElementArrayF(pGroup, size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr = getReader();

        while (btr.getRemaining() > 0) {

            final PGroupElement[] elements = readBatch(pGroup, btr);
            btw.unsafeWrite(pGroup.inv(elements));
        }
        btr.close();
        btw.close();

        return res;
    }

    @Override
    public PGroupElementArray exp(final PRingElementArray exponentsArray) {
        if (!pGroup.pRing.equals(exponentsArray.pRing)) {
            throw new ArithmError(PGroup.MISMATCHING_GROUP_RING);
        }
        if (size != exponentsArray.size()) {
            throw new ArithmError(PGroup.DIFFERENT_LENGTHS);
        }

        final LargeIntegerArrayF integersArray =
            (LargeIntegerArrayF) ((PFieldElementArray) exponentsArray)
            .toLargeIntegerArray();

        final BPGroupElementArrayF res = new BPGroupElementArrayF(pGroup, size);
        final BPGroupElementBatchWriter bw = res.getBatchWriter();

        final BPGroupElementBatchReader br1 = getBatchReader();
        final LargeIntegerBatchReader br2 = integersArray.getBatchReader();

        PGroupElement[] elements = br1.readNext();
        LargeInteger[] integers = br2.readNext();
        while (elements != null && integers != null) {
            bw.writeNext(((BPGroup) pGroup).exp(elements, integers));
            elements = br1.readNext();
            integers = br2.readNext();
        }
        br2.close();
        br1.close();
        bw.close();

        return res;
    }

    @Override
    public PGroupElementArray exp(final PRingElement exponent) {
        if (!pGroup.pRing.equals(exponent.pRing)) {
            throw new ArithmError(PGroup.MISMATCHING_GROUP_RING);
        }

        final BPGroupElementArrayF res = new BPGroupElementArrayF(pGroup, size);
        final BPGroupElementBatchWriter bw = res.getBatchWriter();
        final BPGroupElementBatchReader br = getBatchReader();

        PGroupElement[] elements = br.readNext();
        while (elements != null) {
            bw.writeNext(pGroup.exp(elements, exponent));
            elements = br.readNext();
        }
        br.close();
        bw.close();

        return res;
    }

    @Override
    public PGroupElement prod() {

        final ByteTreeReader btr = getReader();

        PGroupElement res = pGroup.getONE();
        while (btr.getRemaining() > 0) {

            final PGroupElement[] elements = readBatch(pGroup, btr);
            res = res.mul(pGroup.prod(elements));
        }
        btr.close();

        return res;
    }

    @Override
    public PGroupElement expProd(final PRingElementArray exponentsArray) {
        if (!pGroup.pRing.equals(exponentsArray.pRing)) {
            throw new ArithmError(PGroup.MISMATCHING_GROUP_RING);
        }
        if (size != exponentsArray.size()) {
            throw new ArithmError(PGroup.DIFFERENT_LENGTHS);
        }

        final LargeIntegerArrayF integersArray =
            (LargeIntegerArrayF) ((PFieldElementArray) exponentsArray)
            .toLargeIntegerArray();

        final BPGroupElementBatchReader br1 = getBatchReader();
        final LargeIntegerBatchReader br2 = integersArray.getBatchReader();

        PGroupElement res = pGroup.getONE();

        PGroupElement[] elements = br1.readNext();
        LargeInteger[] integers = br2.readNext();
        while (elements != null && integers != null) {
            int bitLength = 0;
            for (int i = 0; i < integers.length; i++) {
                bitLength = Math.max(bitLength, integers[i].bitLength());
            }

            res = res.mul(pGroup.expProd(elements, integers, bitLength));
            elements = br1.readNext();
            integers = br2.readNext();
        }
        br2.close();
        br1.close();

        return res;
    }

    @Override
    public int compareTo(final PGroupElementArray array) {

        if (size != array.size()) {
            throw new ArithmError(PGroup.DIFFERENT_LENGTHS);
        }

        if (array instanceof BPGroupElementArrayF) {

            final BPGroupElementArrayF bparray = (BPGroupElementArrayF) array;

            if (bparray.pGroup.equals(pGroup)) {

                final ByteTreeReader btr1 = getReader();
                final ByteTreeReader btr2 = bparray.getReader();

                int res = 0;

                while (res == 0 && btr1.getRemaining() > 0) {

                    final PGroupElement[] left = readBatch(pGroup, btr1);
                    final PGroupElement[] right = readBatch(pGroup, btr2);

                    res = pGroup.compareTo(left, right);
                }
                btr2.close();
                btr1.close();

                return res;
            }
        }
        throw new ArithmError("Illegal comparison!");
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BPGroupElementArrayF)) {
            return false;
        }
        final BPGroupElementArrayF array = (BPGroupElementArrayF) obj;
        return pGroup.equals(array.pGroup) && compareTo(array) == 0;
    }

    @Override
    public boolean[] equalsAll(final PGroupElementArray otherArray) {

        if (otherArray.size() != size) {
            throw new ArithmError("Mismatching sizes!");
        }

        final boolean[] res = new boolean[size];

        final PGroupElementIterator ei1 = getIterator();
        final PGroupElementIterator ei2 = otherArray.getIterator();

        int i = 0;
        for (;;) {
            final PGroupElement left = ei1.next();
            if (left == null) {
                break;
            }
            final PGroupElement right = ei2.next();

            res[i++] = left.equals(right);
        }

        return res;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public PGroupElementArray permute(final Permutation permutation) {

        if (!(permutation instanceof PermutationF)) {
            final String e =
                "Non file-mapped permutation used with file-mapped array!";
            throw new ArithmError(e);
        }
        final PermutationF permutationF = (PermutationF) permutation;

        final ByteTreeF result =
            permutationF.applyPermutation(new ByteTreeF(file));

        return new BPGroupElementArrayF(pGroup, size(), result.file);
    }

    @Override
    public PGroupElementArray shiftPush(final PGroupElement el) {

        final BPGroupElementArrayF res = new BPGroupElementArrayF(pGroup, size);
        final ByteTreeWriterF btw = res.getWriter();
        final ByteTreeReader btr = getReader();

        btw.unsafeWrite(el);

        while (btr.getRemaining() > 1) {

            final int len = Math.min(batchSize(), btr.getRemaining() - 1);
            final PGroupElement[] tmp = readBatch(pGroup, len, btr);
            btw.unsafeWrite(tmp);
        }
        btr.close();
        btw.close();

        return res;
    }

    @Override
    public PGroupElementArray copyOfRange(final int startIndex,
                                          final int endIndex) {

        final ByteTreeReader btr = getReader();

        try {
            btr.skipChildren(startIndex);
        } catch (final EIOException eioe) {
            throw new ArithmError("Unable to skip content!", eioe);
        }

        int resSize = endIndex - startIndex;

        final BPGroupElementArrayF res =
            new BPGroupElementArrayF(pGroup, resSize);
        final ByteTreeWriterF btw = res.getWriter();

        while (resSize > 0) {

            final int len = Math.min(batchSize(), resSize);
            final PGroupElement[] elements = readBatch(pGroup, len, btr);
            btw.unsafeWrite(elements);
            resSize -= len;

        }
        btr.close();
        btw.close();

        return res;
    }

    @Override
    public PGroupElementArray extract(final boolean[] valid) {

        if (size != valid.length) {
            throw new ArithmError(PGroup.DIFFERENT_LENGTHS);
        }

        int total = 0;
        for (int i = 0; i < valid.length; i++) {
            if (valid[i]) {
                total++;
            }
        }

        final BPGroupElementArrayF res =
            new BPGroupElementArrayF(pGroup, total);
        final ByteTreeWriterF btw = res.getWriter();

        final PGroupElementIterator ei = getIterator();

        for (int i = 0; i < valid.length; i++) {
            final PGroupElement element = ei.next();
            if (valid[i]) {
                btw.unsafeWrite(element);
            }
        }
        btw.close();

        return res;
    }

    @Override
    public void free() {
        TempFile.delete(file);
    }
}
