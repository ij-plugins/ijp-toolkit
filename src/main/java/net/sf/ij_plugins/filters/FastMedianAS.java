/*
 * Image/J Plugins
 * Copyright (C) 2002-2014 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */
package net.sf.ij_plugins.filters;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import net.sf.ij_plugins.io.IOUtils;

import java.io.IOException;

/**
 * The variables:
 * <ul>
 * <li>ix = M X N input array</li>
 * <li>iout=(M-R+l)X(N-S+l) output array
 * <li>median = the median of the past window
 * <li>fmedian = the median of the first window in the previous row
 * <li>row = points to the current row
 * column = points to the current column
 * newcol = the column number assigned to the rightmost column of a window
 * position = position where the old median partitions the rightmost column
 * count = number of pixels to be moved from one subset to the other so
 *         that Subset 1 has (RS + 1)/2 number of elements
 * pointer = points to the column of the window in which the median
 *           was found
 * colvec = l-dimensional N( R + 2)-key array representation of an
 *          (R + 2) x N 2-dimensional image segment in which sentinel values
 *          have been inserted in the beginning and at the end of each
 *          column
 * border = l-dimensional array of S elements holding the partitioned
 *          position of each column vector of the window. The elements
 *         at the borders belong to Subset 2
 * </ul>
 */
public class FastMedianAS {
    /**
     * number of rows in the image
     */
    public static final int M = 6;
    /**
     * number of columns in the image
     */
    public static final int N = 6;
    /**
     * number of rows in the window
     */
    public static final int R = 5;
    /**
     * number of columns in the window
     */
    public static final int S = 5;

    private static final int RP2 = (R + 2);
    private static final int RMlBY2 = ((R - 1) / 2);
    private static final int RPlBY2 = ((R + 1) / 2);
    private static final int SM1BY2 = ((S - 1) / 2);
    private static final int SP1BY2 = ((S + 1) / 2);
    private static final int RSPlBY2 = ((R * S + 1) / 2);
    private static final int RP2MULS = (RP2 * S);
    private static final int MMRMlBY2 = (M - RMlBY2);
    private static final int NMSMlBY2 = (N - SM1BY2);
    private static final int POSITION = (S * RP2 + RPlBY2);

    private final int[] colvec = new int[N * RP2];
    private final int[] border = new int[S];
    private int median;
    private int count;
    private int pointer;
    private int row;
    private final int[][] iout = new int[M - R + 1][N - S + 1];

    private final int[][] ix = new int[M][N];

    public void run(final ByteProcessor ip) {

        final int[][] pixels = {
                {19, 15, 22, 1, 5, 7},
                {24, 31, 28, 9, 11, 23},
                {21, 24, 24, 21, 23, 14},
                {23, 25, 14, 23, 8, 8},
                {22, 24, 6, 15, 16, 7},
                {23, 25, 5, 13, 17, 3}
        };

        for (int y = 0; y < M; ++y) {
            System.arraycopy(pixels[y], 0, ix[y], 0, N);
        }

        fast_median();

        for (int y = 0; y < 1; ++y) {
            for (int x = 0; x < 1; ++x) {
                ip.putPixel(x, y, iout[y][x]);
            }
        }
    }

    public static void main(final String[] args) {
        try {
//            ij.ImageJ.main(null);
            final ImagePlus imp = IOUtils.openImage("test_images/blobs_noise.tif");
            imp.show();
            final ByteProcessor bp = (ByteProcessor) imp.getProcessor().duplicate();

            final FastMedianAS fastMedian = new FastMedianAS();
            fastMedian.run(bp);
            new ImagePlus("Median", bp).show();

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }


    private void fast_median() {
        int column, position, newcol, fmedian;

        // initialization with terminal values, assignment, and sorting of
        // columns in the first row to be processed.
        sort();
        fmedian = 0;
        for (row = RMlBY2; row < MMRMlBY2; row++) {
            // In the following segment the median of the first window of each
            // row is found.
            count = 0;
            for (column = 0, position = 1; column < S; column++, position += RP2) {
                border[column] = position;
                while (colvec[border[column]] < fmedian) {
                    border[column]++;
                    count++;
                }
            }
            count -= RSPlBY2;
            if (count >= 0) {
                count++;
                find_max();
                border[pointer]++;
            } else {
                find_min();
            }

            iout[row - RMlBY2][0] = fmedian = median;  //FIXME:?
            position = POSITION;
            newcol = 0;
            for (column = SP1BY2; column < NMSMlBY2; column++) {
                // the number of elements to be moved between subsets is found
                count = position - border[newcol] - RP2MULS;
                // the border position in the new column is found such that the key
                // at the border is greater than or equal to the past median and the
                // count is minimum
                if (colvec[position] > median || (colvec[position] == median &&
                        count >= 0)) {
                    // searching upwards for the border position
                    while (colvec[position - 1] >= median) {
                        if (colvec[position - 1] == median && count <= 0) {
                            break;
                        }
                        position--;
                        count--;
                    }
                } else {
                    // searching downwards for border position
                    count++;
                    while (colvec[++position] <= median) {
                        if (colvec[position] == median && count >= 0) {
                            break;
                        }
                        count++;
                    }
                }
                border[newcol] = position;
                if (count < 0 || (count == 0 && pointer == newcol)) {
                    // Border adjustment by moving the elements from Subset 1 to Subset 2.
                    // since the past median is the largest number in Subset 1, the first
                    // one moved to Subset 2 is the past median
                    if (colvec[--border[pointer]] == median) {
                    } else {
                        border[pointer]++;
                        count++;
                    }

                    find_max();
                    border[pointer]++;
                } else {
                    // Border adjustment by moving the elements. from Subset 2 to Subset 1
                    find_min();
                }
                iout[row - RMlBY2][column - SM1BY2] = median;   //FIXME:?
                position = border[newcol++] + RP2;
                if (newcol == S) {
                    newcol = 0;
                }
            }
            if (row < MMRMlBY2 - 1) {
                insert_delete();
            }
        }
    }


    /**
     * In this function, the terminal elements of each column are assigned values. The value at one
     * end is smaller than the smallest pixel value in the image and at the other end the value is
     * larger than the largest pixel value in the image. The pixels in the columns of the windows
     * corresponding to the pixels in the (R + 1)/2th row in the image are stored in the colvec
     * array and sorted.
     */
    private void sort() {
        // initialize the terminal values of each column
        for (int i = 0, i1 = S + 1; i < N * RP2; i += RP2, i1 += RP2) {
            colvec[i] = (-2);
            colvec[i1] = 800;
        }
        // insert and sort the pixels in the colvec array
        for (int i = 0, index = 1; i < N; i++, index += RP2) {
            colvec[index] = ix[0][i];
            for (int i1 = index + 1; i1 < index + S; i1++) {
                final int temp = ix[i1 - index][i];
                int i2 = i1 - 1;
                while (temp < colvec[i2]) {
                    colvec[i2 + 1] = colvec[i2--];
                }
                colvec[++i2] = temp;
            }
        }
    }

    /**
     * In this function, count number of smallest elements is moved from Subset 2 to Subset 1 The
     * variables: median2 = the second smallest key along the border in Subset 2 pointer2 = points
     * to the column of median2
     */
    private void find_min() {
        int median2, pointer2, i3, temp;
        while (count < 0) {
            // find the two smallest elements in Subset 2 along the border
            if (colvec[border[0]] < colvec[border[1]]) {
                pointer = 0;
                pointer2 = 1;
            } else {
                pointer = 1;
                pointer2 = 0;
            }
            median = colvec[border[pointer]];
            median2 = colvec[border[pointer2]];
            for (i3 = 2; i3 < S; i3++) {
                temp = colvec[border[i3]];
                if (temp < median2) {
                    if (temp < median) {
                        pointer2 = pointer;
                        median2 = median;
                        median = temp;
                        pointer = i3;
                    } else {
                        median2 = temp;
                        pointer2 = i3;
                    }
                }
            }
            if (++count == 0) {
                border[pointer]++;
                return;
            }
            // find the second, third, etc., smallest key in the column pointed
            // by pointer until no more elements are left or the count is equal to
            // zero.
            while (colvec[++border[pointer]] <= median2) {
                if (++count == 0) {
                    median = colvec[border[pointer]++];
                    return;
                }
            }

            // the second smallest key along the border is the next smallest
            // in Subset 2
            median = median2;
            pointer = pointer2;
            border[pointer]++;
            count++;
        }
    }


    /**
     * In this function, which is similar to function find-mm, count number of largest elements in
     * Subset 1 are found and moved to Subset 2
     */
    private void find_max() {
        int median2, pointer2, i3, temp;
        while (count > 0) {
            if (colvec[border[0] - 1] > colvec[border[1] - 1]) {
                pointer = 0;
                pointer2 = 1;
            } else {
                pointer = 1;
                pointer2 = 0;
            }
            median = colvec[border[pointer] - 1];
            median2 = colvec[border[pointer2] - 1];
            for (i3 = 2; i3 < S; i3++) {
                temp = colvec[border[i3] - 1];
                if (temp > median2) {
                    if (temp > median) {
                        pointer2 = pointer;
                        median2 = median;
                        median = temp;
                        pointer = i3;
                    } else {
                        median2 = temp;
                        pointer2 = i3;
                    }
                }
            }
            if (--count == 0) {
                border[pointer]--;
                return;
            }

            while (colvec[--border[pointer] - 1] >= median2) {
                if (--count == 0) {
                    median = colvec[--border[pointer]];
                    return;
                }
            }
            median = median2;
            pointer = pointer2;
            border[pointer]--;
            count--;
        }
    }

    /**
     * In this function, the columns of the windows corresponding to the next row to be processed
     * are found by updating the columns corresponding to the present row of the image. The
     * variables: delete =  the key to be deleted in a column delpos = the position occupied by the
     * key delete insert = the key to be inserted inspos = the position where the key insert is to
     * be inserted
     */
    private void insert_delete() {
        int column, delpos, inspos, insert, delete;
        for (column = 0, delpos = RPlBY2; column < N; column++, delpos += RP2) {
            delete = ix[row - RMlBY2][column];
            insert = ix[row + RPlBY2][column];
            if (delete != insert) {
                if (colvec[delpos] > delete) {
                    // searching upwards for deleting position
                    while (colvec[--delpos] != delete) {
                        ;
                    }
                } else {
                    // searching downwards for deleting position
                    while (colvec[delpos] != delete) {
                        delpos++;
                    }
                }
                inspos = delpos;
                if (colvec[++inspos] < insert) {
                    // searching downwards for inserting position
                    do {
                        colvec[inspos - 1] = colvec[inspos];
                    } while (colvec[++inspos] < insert);
                    colvec[--inspos] = insert;
                } else {
                    // searching upwards for inserting position
                    inspos--;
                    while (colvec[--inspos] > insert) {
                        colvec[inspos + 1] = colvec[inspos];
                    }
                    colvec[++inspos] = insert;
                }
            }
        }
    }

}
