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
package net.sf.ij.thresholding;

import java.util.ArrayList;
import java.util.List;

/**
 * histogram based thresholding.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.2 $
 */
public final class HistogramThreshold {
    final static private double EPSILON = Double.MIN_VALUE;

    private HistogramThreshold() {
    }

    /**
     * Calculate maximum entropy split of a histogram. For more inforamtion see:
     * J.N. Kapur, P.K. Sahoo and A.K.C. Wong,
     * "A New Method for Gray-Level Picture Thresholding Using the Entropy of the Histogram",
     * <i>CVGIP</i>, (29), pp.273-285 , 1985.
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
        for (int t = 1; t < hist.length; ++t) {
            final double j = hB[t] + hW[t];
            if (j > jMax) {
                jMax = j;
                tMax = t;
            }
        }

        return tMax;
    }

    /**
     * Find maximum entropy split of a histogram. It is a generalization of single split presented by
     * J.N. Kapur, P.K. Sahoo and A.K.C. Wong,
     * "A New Method for Gray-Level Picture Thresholding Using the Entropy of the Histogram",
     * <i>CVGIP</i>, (29), pp.273-285 , 1985.
     *
     * @param hist        histogram to be partitioned.
     * @param nbDivisions desired number of thresholds.
     * @return array contraining values of maximum entropy thresholds.
     */
    final static public int[] maximumEntropy(int hist[], int nbDivisions) {

        // FIXME: Optimize implementation for larger values of nbDivisions.

        final int min = 0;
        final int max = hist.length;

        double[] h = normalize(hist);

        // Create candidate intevals
        int [][] intervals = intervals(nbDivisions, 0, hist.length);

        // Find an interval that maximizes the entropy
        double bestE = Double.NEGATIVE_INFINITY;
        int[] bestInterval = null;
        for (int i = 0; i < intervals.length; i++) {
            int[] interval = intervals[i];
            double e = 0;
            int lastT = min;
            for (int j = 0; j < interval.length; j++) {
                int t = interval[j];
                e += intervalEntropy(h, lastT, t);
                lastT = t;
            }
            e += intervalEntropy(h, lastT, max);

            if (bestE < e) {
                bestE = e;
                bestInterval = interval;
            }
        }

        assert bestInterval != null;
        assert bestInterval.length == nbDivisions;

        return bestInterval;
    }

    /**
     * Normalize histogram: divide all elemnts of the histogram by a fixed values so sum of the elements is equal 1.
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

    /**
     * Generate all possible divisions of a range of numbers <code>min</code> to <code>max</code>.
     *
     * @param nbDivisions
     * @param min
     * @param max
     * @return
     */
    public static int[][] intervals(int nbDivisions, int min, int max) {
        if (nbDivisions <= 0) {
            throw new IllegalArgumentException("Argument 'nbdivisions' must be greater than 0.");
        }

        final List intervals = new ArrayList();
        if (nbDivisions == 1) {
            for (int n = min + 1; n < max; ++n) {
                intervals.add(new int[]{n});
            }

        } else {
            for (int n = min + 1; n <= max - nbDivisions + 1; ++n) {
                int[][] subIntervals = intervals(nbDivisions - 1, n, max);

                // combine
                for (int i = 0; i < subIntervals.length; i++) {
                    int[] subInterval = subIntervals[i];
                    int[] interval = new int[subInterval.length + 1];
                    interval[0] = n;
                    for (int j = 0; j < subInterval.length; j++) {
                        interval[j + 1] = subInterval[j];
                    }
                    intervals.add(interval);
                }
            }
        }

        return (int[][]) intervals.toArray(new int[intervals.size()][]);
    }

    /**
     * @param hist  normalized histogram
     * @param begin first index to evaluate (inclusive).
     * @param end   last index to evaluate (exclusive).
     * @return
     */
    final static double intervalEntropy(final double[] hist, int begin, int end) {

        final double hSum = sum(hist, begin, end);
        assert hSum < 1 + EPSILON;
        if (hSum < EPSILON) {
            return 0;
        }

        double e = 0;
        for (int i = begin; i < end; ++i) {
            double h = hist[i];
            if (h > EPSILON) {
                double a = hist[i] / hSum;
                e -= a * Math.log(a);
            }
        }

        return e;
    }

    final static double sum(final double[] hist, int begin, int end) {
        double s = 0;
        for (int i = begin; i < end; ++i) {
            s += hist[i];
        }
        return s;
    }
}
