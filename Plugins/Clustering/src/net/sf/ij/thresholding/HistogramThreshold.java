/***
 * Image/J Plugins
 * Copyright (C) 2002-2004 Jarek Sacha
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
package net.sf.ij.thresholding;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public final class HistogramThreshold {
    private HistogramThreshold() {
    }

    /**
     * Calculate maximum entropy split of a histogram. For more inforamtion see:
     * P.K. Sahoo, S. Soltani, K.C. Wong and, Y.C. Chen "A Survey of
     * Thresholding Techniques", Computer Vision, Graphics, and Image
     * Processing, Vol. 41, pp.233-260, 1988.
     *
     * @param hist histogram to be thresholded.
     * @return index of the maximum entropy split.
     */
    final public static int maximumEntropy(final int[] hist) {
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
        final double epsilon = java.lang.Double.MIN_VALUE;
        final double[] hB = new double[hist.length];
        final double[] hW = new double[hist.length];
        for (int t = 0; t < hist.length; t++) {
            // Black entropy
            if (pT[t] > epsilon) {
                double hhB = 0;
                for (int i = 0; i <= t; i++) {
                    if (normalizedHist[i] > epsilon) {
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
            if (pTW > epsilon) {
                double hhW = 0;
                for (int i = t + 1; i < hist.length; ++i) {
                    if (normalizedHist[i] > epsilon) {
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
        for (int t = 1; t < hist.length; ++t) {
            final double j = hB[t] + hW[t];
            if (j > jMax) {
                jMax = j;
                tMax = t;
            }
        }

        return tMax;
    }
}
