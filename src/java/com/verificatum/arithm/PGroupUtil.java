
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

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for working with group elements and arrays of group
 * elements.
 *
 * @author Douglas Wikstrom
 */
public final class PGroupUtil {

    /**
     * Avoid accidental instantiation.
     */
    private PGroupUtil() { }

    /**
     * Releases any resources allocated by the the group element
     * arrays, e.g., a file based implementation may delete the
     * underlying file. It is the responsibility of the programmer to
     * only call this method if this instance is not used again.
     *
     * @param arrays Arrays to be freed.
     */
    public void free(final List<PGroupElementArray> arrays) {
        for (final PGroupElementArray array : arrays) {
            array.free();
        }
    }

    /**
     * Verify that a source index is valid.
     *
     * @param source Source index of list element.
     * @param upperBound Upper bound for source index.
     * @throws ArithmFormatException If the source index is out of
     * bounds.
     */
    private static void checkSourceIndex(final int source, final int upperBound)
        throws ArithmFormatException {
        if (source < 0) {

            final String e = "Negative source index! (" + source + ")";
            throw new ArithmFormatException(e);

        } else if (source >= upperBound) {

            final String f = "Too large source index (%d), maximal is %d!";
            final String e = String.format(f, source, upperBound);
            throw new ArithmFormatException(e);
        }
    }

    /**
     * Verify that an index is valid.
     *
     * @param index Index of list element.
     * @param upperBound Upper bound of index.
     * @throws ArithmFormatException If the index is out of bounds.
     */
    private static void checkIndex(final int index, final int upperBound)
        throws ArithmFormatException {
        if (index < 0) {

            final String e = "Negative index! (" + index + ")";
            throw new ArithmFormatException(e);

        } else if (index > upperBound - 1) {

            final String f = "Too large index (%d), maximal is %d!";
            final String e = String.format(f, index, upperBound);
            throw new ArithmFormatException(e);
        }
    }

    /**
     * Verify that an index is zero.
     *
     * @param index Index for list of group elements.
     * @throws ArithmFormatException If the index is not zero.
     */
    private static void checkZero(final int index)
        throws ArithmFormatException {
        if (index != 0) {
            final String e =
                String.format("Attempting to access subelement at index %d "
                              + "in atomic group!", index);
            throw new ArithmFormatException(e);
        }
    }

    /**
     * Verify that all lists are of the same size.
     *
     * @param <T> Type of element in list.
     * @param factors List of list of basic type.
     * @throws ArithmFormatException If not all lists are of the same
     * size.
     */
    private static <T> void checkSizes(final List<List<T>> factors)
        throws ArithmFormatException {
        final int size = factors.get(0).size();
        for (final List<T> f : factors) {
            if (f.size() != size) {
                throw new ArithmFormatException("Lists have different "
                                                + "sizes");
            }
        }
    }

    /**
     * Returns the subelement identified by the source index and
     * subelement index.
     *
     * @param atomicPGroup Atomic group.
     * @param elements List of source elements.
     * @param source Index identifying an element in the input.
     * @param index Index identifying a subelement.
     * @return Identified subelement.
     * @throws ArithmFormatException If the index is invalid.
     */
    public static PGroupElement getElement(final PGroup atomicPGroup,
                                           final List<PGroupElement> elements,
                                           final int source,
                                           final int index)
        throws ArithmFormatException {

        checkSourceIndex(source, elements.size());

        final PGroupElement element = elements.get(source);
        final PGroup pGroup = element.getPGroup();

        if (pGroup.equals(atomicPGroup)) {

            checkZero(index);
            return element;

        } else {

            final PPGroupElement pElement = (PPGroupElement) element;
            final PPGroup pPGroup = (PPGroup) pGroup;

            checkIndex(index, pPGroup.getWidth());

            final PGroupElement component = pElement.project(index);

            if (!atomicPGroup.equals(component.getPGroup())) {
                throw new ArithmFormatException("Atomic group does not match "
                                                + "the atomic group in the "
                                                + "input!");
            }
            return component;
        }
    }

    /**
     * Returns the array of subelements identified by the source index
     * and subelement index.
     *
     * @param atomicPGroup Atomic group.
     * @param arrays List of source arrays.
     * @param source Index identifying an array of elements in the input.
     * @param index Index identifying an array of subelements within
     * the identified arrays.
     * @return Identified subarray.
     * @throws ArithmFormatException If the index is invalid.
     */
    public static PGroupElementArray
        getElementArray(final PGroup atomicPGroup,
                        final List<PGroupElementArray> arrays,
                        final int source,
                        final int index)
        throws ArithmFormatException {

        checkSourceIndex(source, arrays.size());

        final PGroupElementArray array = arrays.get(source);
        final PGroup pGroup = array.getPGroup();

        if (pGroup.equals(atomicPGroup)) {

            checkZero(index);
            return array;

        } else {

            final PPGroupElementArray pArray = (PPGroupElementArray) array;
            final PPGroup pPGroup = (PPGroup) pGroup;

            checkIndex(index, pPGroup.getWidth());

            return pArray.project(index);
        }
    }

    /**
     * Returns a list of group elements derived from the input group
     * elements. Each output group element is defined as the
     * projection to the <code>index</code>th component of the
     * respective input group element.
     *
     * @param atomicPGroup Atomic group.
     * @param inputElements List of input group elements.
     * @param index Index of component of input group elements.
     * @return List of elements of subgroups identified by the given
     * index.
     * @throws ArithmFormatException If the index is not valid.
     */
    public static List<PGroupElement>
        project(final PGroup atomicPGroup,
                final List<PGroupElement> inputElements,
                final int index)
        throws ArithmFormatException {

        final List<PGroupElement> res = new ArrayList<PGroupElement>();

        for (final PGroupElement element : inputElements) {

            final PGroup pGroup = element.getPGroup();

            if (pGroup.equals(atomicPGroup)) {

                checkZero(index);
                res.add(element);

            } else {

                checkIndex(index, ((PPGroup) pGroup).getWidth());
                res.add(((PPGroupElement) element).project(index));

            }
        }
        return res;
    }

    /**
     * Returns a list of group elements derived from the input group
     * elements. Each output group element is defined as the
     * projection to the <code>index</code>th component of the
     * respective input group element.
     *
     * @param inputElements List of input group elements.
     * @param index Index of component of input group elements.
     * @return List of elements of subgroups identified by the given
     * index.
     * @throws ArithmFormatException If the requested projected is
     * invalid.
     */
    public static List<PGroupElement>
        unsafeProject(final List<PGroupElement> inputElements, final int index)
        throws ArithmFormatException {

        final List<PGroupElement> res = new ArrayList<PGroupElement>();

        for (final PGroupElement element : inputElements) {

            res.add(((PPGroupElement) element).project(index));
        }
        return res;
    }

    /**
     * Returns a list of group element arrays derived from the input
     * group element arrays. Each output group element array is
     * defined as the <code>index</code>th component of the respective
     * input group element array.
     *
     * @param atomicPGroup Atomic group.
     * @param inputArrays List of input group element arrays.
     * @param index Index of component of input group element array.
     * @return List of elements of subgroups identified by the given
     * index.
     * @throws ArithmFormatException If the index is not valid.
     */
    public static List<PGroupElementArray>
        projects(final PGroup atomicPGroup,
                 final List<PGroupElementArray> inputArrays,
                 final int index)
        throws ArithmFormatException {

        final List<PGroupElementArray> res =
            new ArrayList<PGroupElementArray>();

        for (final PGroupElementArray array : inputArrays) {

            final PGroup pGroup = array.getPGroup();

            if (pGroup.equals(atomicPGroup)) {

                checkZero(index);
                res.add(array);

            } else {

                checkIndex(index, ((PPGroup) pGroup).getWidth());
                res.add(((PPGroupElementArray) array).project(index));

            }
        }
        return res;
    }

    /**
     * Project each group element array in the input to the given
     * index.
     *
     * @param inputArrays Arrays to be projected.
     * @param index Position to project to.
     * @return Projected group element arrays.
     * @throws ArithmFormatException If the requested projected is
     * invalid.
     */
    public static List<PGroupElementArray>
        unsafeProjects(final List<PGroupElementArray> inputArrays,
                       final int index)
        throws ArithmFormatException {

        final List<PGroupElementArray> res =
            new ArrayList<PGroupElementArray>();

        for (final PGroupElementArray array : inputArrays) {

            res.add(((PPGroupElementArray) array).project(index));
        }
        return res;
    }

    /**
     * Computes a list of group elements where each group element is
     * formed as a product group element of each list of group
     * elements in the input list.
     *
     * @param factors List of lists of group elements to be belong to
     * product groups.
     * @return List of product group elements.
     * @throws ArithmFormatException If the index is invalid.
     */
    public static List<PGroupElement>
        product(final List<List<PGroupElement>> factors)
        throws ArithmFormatException {

        checkSizes(factors);

        if (factors.size() == 1) {

            return factors.get(0);

        } else {

            final List<PGroupElement> res = new ArrayList<PGroupElement>();

            for (int j = 0; j < factors.get(0).size(); j++) {

                final PGroupElement[] posFactors =
                    new PGroupElement[factors.size()];
                final PGroup[] posPGroups = new PGroup[factors.size()];

                for (int i = 0; i < factors.size(); i++) {
                    posFactors[i] = factors.get(i).get(j);
                    posPGroups[i] = posFactors[i].getPGroup();
                }
                final PPGroup posPPGroup = new PPGroup(posPGroups);
                res.add(posPPGroup.product(posFactors));
            }

            return res;
        }
    }

    /**
     * Computes a list of arrays of group elements where each array of
     * group elements is formed as a product of arrays of group
     * element of each list of arrays of group elements in the input
     * list.
     *
     * @param factors List of lists of group elements to be belong to
     * product groups.
     * @return List of product group elements.
     * @throws ArithmFormatException If the index is invalid.
     */
    public static List<PGroupElementArray>
        products(final List<List<PGroupElementArray>> factors)
        throws ArithmFormatException {

        checkSizes(factors);

        if (factors.size() == 1) {

            return factors.get(0);

        } else {

            final List<PGroupElementArray> res =
                new ArrayList<PGroupElementArray>();

            for (int j = 0; j < factors.get(0).size(); j++) {

                final PGroup[] posPGroups = new PGroup[factors.size()];
                final PGroupElementArray[] posFactors =
                    new PGroupElementArray[factors.size()];

                for (int i = 0; i < factors.size(); i++) {
                    posFactors[i] = factors.get(i).get(j);
                    posPGroups[i] = posFactors[i].getPGroup();
                }
                final PPGroup posPPGroup = new PPGroup(posPGroups);
                res.add(posPPGroup.product(posFactors));
            }

            return res;
        }
    }

    /**
     * Returns the groups containing the given elements.
     *
     * @param elements Group elements for the containing groups are
     * returned.
     * @return groups containing the given elements.
     */
    public static PGroup[] getPGroups(final PGroupElement[] elements) {
        final PGroup[] res = new PGroup[elements.length];
        for (int i = 0; i < elements.length; i++) {
            res[i] = elements[i].getPGroup();
        }
        return res;
    }
}
