/***
 * Image/J Plugins
 * Copyright (C) 2002-2005 Jarek Sacha
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
package net.sf.ij_plugins.thresholding;

/**
 * histogram based thresholding.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.6 $
 */
public final class HistogramThreshold {
    private static final double EPSILON = Double.MIN_VALUE;

    private HistogramThreshold() {
    }

    /**
     * Calculate maximum entropy split of a histogram. For more inforamtion see: J.N. Kapur, P.K.
     * Sahoo and A.K.C. Wong, "A New Method for Gray-Level Picture Thresholding Using the Entropy of
     * the Histogram", <i>CVGIP</i>, (29), pp.273-285 , 1985.
     * <p/>
     * Returned value indicates split position <code>t</code>. First interval are values less than
     * <code>t</code>, second interval are values equal or larger than <code>t</code>.
     *
     * @param hist histogram to be thresholded.
     * @return index of the maximum entropy split.
     */
    public static int maximumEntropy(final int[] hist) {
        // Normalize histogram, that is makes the sum of all bins equal to 1.
        double sum = 0;
        for (int i = 0; i < hist.length; ++i) {
            sum += hist[i];
        }
        if (sum == 0) {
            // This should not normally happen, but...
            throw new IllegalArgumentException("Empty histogram: sum of all bins is zero.");
        }

        final double[] normalizedHist = new double[hist.length];
        for (int i = 0; i < hist.length; i++) {
            normalizedHist[i] = hist[i] / sum;
        }

        //
        final double[] pT = new double[hist.length];
        pT[0] = normalizedHist[0];
        for (int i = 1; i < hist.length; i++) {
            pT[i] = pT[i - 1] + normalizedHist[i];
        }

        // Entropy for black and white parts of the histogram
        final double[] hB = new double[hist.length];
        final double[] hW = new double[hist.length];
        for (int t = 0; t < hist.length; t++) {
            // Black entropy
            if (pT[t] > EPSILON) {
                double hhB = 0;
                for (int i = 0; i <= t; i++) {
                    if (normalizedHist[i] > EPSILON) {
                        final double h = normalizedHist[i] / pT[t];
                        hhB -= h * Math.log(h);
                    }
                }
                hB[t] = hhB;
            } else {
                hB[t] = 0;
            }

            // White  entropy
            final double pTW = 1 - pT[t];
            if (pTW > EPSILON) {
                double hhW = 0;
                for (int i = t + 1; i < hist.length; ++i) {
                    if (normalizedHist[i] > EPSILON) {
                        final double h = normalizedHist[i] / pTW;
                        hhW -= h * Math.log(h);
                    }
                }
                hW[t] = hhW;
            } else {
                hW[t] = 0;
            }
        }

        // Find histogram index with maximum entropy
        double jMax = hB[0] + hW[0];
        int tMax = 0;
        for (int t = 0; t < hist.length; ++t) {
            final double j = hB[t] + hW[t];
            if (jMax < j) {
                jMax = j;
                tMax = t + 1;
            }
        }

        return tMax;
    }

    /**
     * Normalize histogram: divide all elemnts of the histogram by a fixed values so sum of the
     * elements is equal 1.
     *
     * @param hist histogram to be normalized.
     * @return normalized histogram.
     */
    static double[] normalize(int[] hist) {
        // Normalize histogram, that is makes the sum of all bins equal to 1.
        double sum = 0;
        for (int i = 0; i < hist.length; ++i) {
            sum += hist[i];
        }

        final double[] normalizedHist = new double[hist.length];
        if (sum != 0) {
            for (int i = 0; i < hist.length; i++) {
                normalizedHist[i] = hist[i] / sum;
            }
        }

        return normalizedHist;
    }

}
