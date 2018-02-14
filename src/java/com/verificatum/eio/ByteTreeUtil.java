
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

package com.verificatum.eio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import com.verificatum.arithm.LargeIntegerArray;

/**
 * Manipulates a byte tree in different ways.
 *
 * @author Douglas Wikstrom
 */
public final class ByteTreeUtil {

    /**
     * Maximal number of byte tree readers open at any time. This
     * bounds the number of file descriptors used.
     */
    public static final int MAX_NO_READERS = 10;

    /**
     * Private constructor to avoid accidental instantiation.
     */
    private ByteTreeUtil() {
    }

    /**
     * Merge byte trees whose children are already sorted. At most
     * {@link MAX_NO_READERS} byte trees can be merged.
     *
     * @param parts Byte trees storing already sorted byte trees.
     * @param comparator Procedure used to order byte trees.
     * @return Byte tree such that its children are the children of
     * the input byte trees, but in sorted order.
     */
    private static ByteTreeF mergeInner(final List<ByteTreeF> parts,
                                        final ByteTreeComparator comparator) {

        // Create induced comparator for queues of byte trees from
        // comparator of byte trees.
        final ByteTreeQueueComparator queueComparator =
            new ByteTreeQueueComparator(comparator);

        // Set up a priority queue for the parts viewed as sorted
        // queues, sorted with respect to the first byte tree in of
        // each queue.

        final PriorityQueue<ByteTreeQueue> queues =
            new PriorityQueue<ByteTreeQueue>(parts.size(), queueComparator);

        ByteTreeWriterF btw = null;
        try {

            // Set up priority queue of queues and determine total
            // length.
            int totalLength = 0;
            for (final ByteTreeF part : parts) {

                final ByteTreeReader btr = part.getByteTreeReader();
                totalLength = totalLength + btr.getRemaining();

                queues.add(new ByteTreeQueue(btr));
            }

            // Prepare for the result.
            final File file = TempFile.getFile();
            btw = new ByteTreeWriterF(totalLength, file);

            // Write the top element of the top queue to the output.
            while (queues.size() > 0) {

                // Fetch the first queue according to the comparator.
                final ByteTreeQueue btq = queues.peek();

                // Read the first byte tree in the chosen queue, which
                // is assumed to be sorted.
                final ByteTree byteTree = btq.popByteTree();

                // Write byte tree to result.
                btw.unsafeWrite(byteTree);

                // We remove it *after* performing IO to make sure
                // that it is closed properly in "finally" in the
                // event of a failure.
                queues.poll();

                // If there are more byte trees in the queue, then we
                // remove and put it back in the priority queue and
                // otherwise we have one less queue to worry about.
                if (btq.getRemaining() > 0) {
                    queues.add(btq);
                } else {
                    btq.close();
                }
            }

            return new ByteTreeF(file);

        } catch (final IOException ioe) {
            throw new EIOError("Fatal error during merge!", ioe);
        } catch (final EIOException eioe) {
            throw new EIOError("Fatal error during merge!", eioe);
        } finally {

            for (final ByteTreeQueue queue : queues) {
                ExtIO.strictClose(queue);
            }
            ExtIO.strictClose(btw);
        }
    }

    /**
     * Slices a list into sublists of size at most {@link
     * MAX_NO_READERS}.
     *
     * @param list List of byte trees on file.
     * @return List of lists of size at most {@link MAX_NO_READERS}.
     */
    private static List<List<ByteTreeF>> slice(final List<ByteTreeF> list) {

        final List<List<ByteTreeF>> slices = new ArrayList<List<ByteTreeF>>();

        int j = 0;
        List<ByteTreeF> slice = new ArrayList<ByteTreeF>();

        for (final ByteTreeF part : list) {

            slice.add(part);
            j = (j + 1) % MAX_NO_READERS;

            if (j == 0) {
                slices.add(slice);
                slice = new ArrayList<ByteTreeF>();
            }
        }

        if (j != 0) {
            slices.add(slice);
        }
        return slices;
    }

    /**
     * Merge already sorted byte trees stored as children of input
     * byte trees.
     *
     * @param parts Byte trees storing already sorted byte trees.
     * @param comparator Procedure to compare byte trees.
     * @return Byte tree such that its children are the children of
     * the input byte trees in sorted order.
     */
    public static ByteTreeF merge(final List<ByteTreeF> parts,
                                  final ByteTreeComparator comparator) {

        List<ByteTreeF> current = parts;

        while (current.size() > 1) {

            final List<List<ByteTreeF>> slices = slice(current);
            current = new ArrayList<ByteTreeF>();

            for (final List<ByteTreeF> slice : slices) {

                current.add(mergeInner(slice, comparator));

                for (final ByteTreeF part : slice) {
                    part.free();
                }
            }
        }

        return current.get(0);
    }

    /**
     * Sorts the children of a byte tree.
     *
     * @param byteTree Source byte tree.
     * @param comparator Comparison function.
     * @return Byte tree where the children are sorted according to
     * the comparator.
     */
    public static ByteTreeF sort(final ByteTreeF byteTree,
                                 final ByteTreeComparator comparator) {

        // Maximal number of byte trees stored in memory at any given
        // time. This clearly makes implicit assumptions on the size
        // of each byte tree.
        final int maxBatchSize = LargeIntegerArray.getBatchSize();

        // Source byte tree reader.
        final ByteTreeReader btr = byteTree.getByteTreeReader();

        // List of sorted subsets of the children of the input.
        final List<ByteTreeF> parts = new ArrayList<ByteTreeF>();

        try {
            do {

                // Read a batch.
                final int batchSize =
                    Math.min(maxBatchSize, btr.getRemaining());
                final ByteTree[] byteTrees = new ByteTree[batchSize];
                for (int i = 0; i < batchSize; i++) {
                    byteTrees[i] = btr.getNextChild().readByteTree();
                }

                // Sort batch.
                Arrays.sort(byteTrees, comparator);

                // Create byte tree for batch.
                final ByteTree batchByteTree = new ByteTree(byteTrees);

                // Write batch to file.
                final File file = TempFile.getFile();
                batchByteTree.unsafeWriteTo(file);

                // Add byte tree on file to list of sorted parts.
                parts.add(new ByteTreeF(file));

            } while (btr.getRemaining() > 0);

        } catch (final EIOException eioe) {
            throw new EIOError("Fatal error when reading!", eioe);
        } finally {
            btr.close();
        }

        return merge(parts, comparator);
    }

    /**
     * Creates a new byte tree from two input byte trees by forming a
     * new child from each pair of children at the same index from the
     * two input byte trees.
     *
     * @param leftByteTree Left byte tree.
     * @param rightByteTree Right byte tree.
     * @return Combined byte tree.
     */
    public static ByteTreeF zip(final ByteTreeF leftByteTree,
                                final ByteTreeF rightByteTree) {

        ByteTreeReader leftReader = null;
        ByteTreeReader rightReader = null;
        ByteTreeWriterF resultWriter = null;

        try {

            // Open both byte trees for reading.
            leftReader = leftByteTree.getByteTreeReader();
            final int leftRemaining = leftReader.getRemaining();

            rightReader = rightByteTree.getByteTreeReader();
            final int rightRemaining = rightReader.getRemaining();

            // Check that their lengths are equal.
            if (leftRemaining != rightRemaining) {

                final String f = "Mismatching lengths! (%s != %s)";
                final String m =
                    String.format(f, leftRemaining, rightRemaining);
                throw new EIOError(m);
            }

            // Create a byte tree for the result.
            final File resultFile = TempFile.getFile();
            resultWriter = new ByteTreeWriterF(leftRemaining, resultFile);

            // Write the combined byte trees to the resulting byte tree.
            for (int i = 0; i < leftRemaining; i++) {

                final ByteTree leftChild =
                    leftReader.getNextChild().readByteTree();
                final ByteTree rightChild =
                    rightReader.getNextChild().readByteTree();

                final ByteTree resultByteTree =
                    new ByteTree(leftChild, rightChild);

                resultWriter.unsafeWrite(resultByteTree);
            }

            return new ByteTreeF(resultFile);

        } catch (final EIOException eioe) {
            throw new EIOError("Attempting to merge invalid inputs!", eioe);
        } catch (final IOException ioe) {
            throw new EIOError("Attempting to merge invalid inputs!", ioe);
        } finally {

            ExtIO.strictClose(leftReader);
            ExtIO.strictClose(rightReader);
            ExtIO.strictClose(resultWriter);
        }
    }

    /**
     * Takes a byte tree as input, extracts the child of a given index
     * from each child of the original input byte tree to form a new
     * byte tree. This assumes that the input is properly formed.
     *
     * @param byteTree Source byte tree.
     * @param index Index of each component to extract.
     * @return Byte tree containing extracted byte trees.
     */
    public static ByteTreeF project(final ByteTreeF byteTree, final int index) {

        ByteTreeReader btr = null;
        ByteTreeWriterF btw = null;

        try {

            // Prepare source and determine number of inputs.
            btr = byteTree.getByteTreeReader();
            final int remaining = btr.getRemaining();

            // Prepare resulting byte tree.
            final File file = TempFile.getFile();

            btw = new ByteTreeWriterF(remaining, file);

            for (int i = 0; i < remaining; i++) {

                // Read next byte tree and verify that it has at least
                // index children.
                final ByteTreeReader child = btr.getNextChild();
                if (child.isLeaf()) {
                    throw new EIOError("Child is a leaf!");
                }
                if (!(0 <= index || index < child.getRemaining())) {
                    throw new EIOError("Invalid index!");
                }
                child.skipChildren(index);

                final ByteTree chosenChild =
                    child.getNextChild().readByteTree();

                child.skipChildren(child.getRemaining());

                btw.unsafeWrite(chosenChild);
            }

            return new ByteTreeF(file);

        } catch (final EIOException eioe) {
            throw new EIOError("Attempting to project invalid inputs!", eioe);
        } catch (final IOException ioe) {
            throw new EIOError("Attempting to project invalid inputs!", ioe);
        } finally {
            ExtIO.strictClose(btr);
            ExtIO.strictClose(btw);
        }
    }

    /**
     * Takes two byte trees as input, zips them into a single byte
     * tree, sorts with repespect to the elements of the first byte
     * tree, and projects to the second (now re-ordered) byte tree.
     *
     * @param sortByteTree Byte trees with respect sorting takes place.
     * @param valueByteTree Byte trees containing values to be
     * re-ordered.
     * @param comparator Comparator used to sort.
     * @return Byte tree containing extracted byte trees.
     */
    public static ByteTreeF
        zipSortProject(final ByteTreeF sortByteTree,
                       final ByteTreeF valueByteTree,
                       final ByteTreeComparator comparator) {

        // Zip the random integers with the consecutive integers.
        final ByteTreeF zipped =
            ByteTreeUtil.zip(sortByteTree, valueByteTree);

        // Sort zipped integers with respect to the random integers.
        final ByteTreeF sorted = ByteTreeUtil.sort(zipped, comparator);
        zipped.free();

        // Project to the re-ordered consecutive integers.
        final ByteTreeF projected = ByteTreeUtil.project(sorted, 1);

        sorted.free();

        return projected;
    }
}
