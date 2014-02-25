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
package net.sf.ij_plugins.quilting;

import java.util.LinkedList;

/**
 * This class has some helper methods for texture synthesis algorithms.
 */
public class SynthAide {
    /**
     * This copies a rectangular region of pixels from one view to another. The region copied will
     * start at (firstx,firsty) and extend to include (firstx+width-1, firsty+height-1). Coordinates
     * are view coordinates.
     */
    public static void copy(final View from, final View to, final int firstx, final int firsty,
                            final int width, final int height) {
        // TODO: copy by blittering
        final int lastx = firstx + width - 1;
        final int lasty = firsty + height - 1;
        final int[] sample = new int[3];
        for (int y = firsty; y <= lasty; y++) {
            for (int x = firstx; x <= lastx; x++) {
                to.putSample(x, y, from.getSample(x, y, sample));
            }
        }
    }


    /**
     * This returns a square 2D normalized Gaussian filter of the given size.
     */
    public static double[][] gaussian(int length) {

        if (length % 2 == 0) {
            length++;
        }

        // this stddev puts makes a good spread for a given size
        final double stddev = length / 4.9;

        // make a 1d gaussian kernel
        final double oned[] = new double[length];
        for (int i = 0; i < length; i++) {
            final int x = i - length / 2;
            final double exponent = x * x / (-2 * stddev * stddev);
            oned[i] = Math.exp(exponent);
        }

        // make the 2d version based on the 1d
        final double twod[][] = new double[length][length];
        double sum = 0.0;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                twod[i][j] = oned[i] * oned[j];
                sum += twod[i][j];
            }
        }

        // normalize
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                twod[i][j] /= sum;
            }
        }

        return twod;
    }

    /**
     * This searches the given array for all non-negative values less than or equal to a given
     * threshold and returns the list of array indicies of matches. Negative values are assumed to
     * be invalid and thus are ignored.
     *
     * @return This returns a list of net.sf.ij_plugins.quilting.TwoDLoc objects.
     */
    public static LinkedList<TwoDLoc> lessThanEqual(final double[][] vals, final double threshold) {

        final LinkedList<TwoDLoc> list = new LinkedList<>();
        for (int r = 0; r < vals.length; r++) {
            for (int c = 0; c < vals[r].length; c++) {
                if (vals[r][c] >= 0 && vals[r][c] <= threshold) {
                    list.addFirst(new TwoDLoc(r, c));
                }
            }
        }
        return list;
    }

    /**
     * This blends the pixel values at (x,y) from the two patches and puts the result in toPatch.
     *
     * @param frompart This gives the ration of the fromPatch value to use (0 &lt;= frompart &lt;= 1). The
     *                 rest of the value comes from toPatch.
     */
    public static void blend(final Patch fromPatch, final Patch toPatch, final int x, final int y,
                             final double frompart) {

        final int[] tovals = toPatch.getSample(x, y, null);
        final int[] fromvals = fromPatch.getSample(x, y, null);
        final int[] newvals = new int[3];
        for (int i = 0; i < 3; i++) {
            final double sum = tovals[i] * (1 - frompart) + fromvals[i] * frompart;
            newvals[i] = (int) Math.round(sum);
        }
        toPatch.putSample(x, y, newvals);
    }


    /**
     * This computes the sum (across channels) of squared differences between the pixel values at
     * the given coordinate in the given views.
     */
    public static int ssd(final View view1, final View view2, final int x, final int y) {

        final int vals[] = view1.getSample(x, y, null);
        final int vals2[] = view2.getSample(x, y, null);

        int diff = vals[0] - vals2[0];
        int sum = diff * diff;
        diff = vals[1] - vals2[1];
        sum += diff * diff;
        diff = vals[2] - vals2[2];
        sum += diff * diff;

        return sum;
    }
}
