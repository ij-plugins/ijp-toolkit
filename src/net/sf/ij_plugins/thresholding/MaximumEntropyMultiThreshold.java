/***
 * Image/J Plugins
 * Copyright (C) 2002-2006 Jarek Sacha
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

import net.sf.ij_plugins.util.IJDebug;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class MaximumEntropyMultiThreshold {

    private static final double EPSILON = Double.MIN_VALUE;
    private double histogram[];
    private Double intervalEntropy[][];


    /**
     * Find maximum entropy split of a histogram. It is a generalization of single split presented
     * by J.N. Kapur, P.K. Sahoo and A.K.C. Wong, "A New Method for Gray-Level Picture Thresholding
     * Using the Entropy of the Histogram", <i>CVGIP</i>, (29), pp.273-285 , 1985.
     *
     * @param hist        histogram to be partitioned.
     * @param nbDivisions desired number of thresholds.
     * @return array contraining values of maximum entropy thresholds.
     */
    public final int[] maximumEntropy(final int hist[], final int nbDivisions) {

        // FIXME: Optimize implementation for larger values of nbDivisions.

        final int min = 0;
        final int max = hist.length;

        intervalEntropy = new Double[257][257];

        histogram = HistogramThreshold.normalize(hist);

        // Create candidate intevals
        IJDebug.log("Create candidate intevals");
        final int [][] intervals = intervals(nbDivisions, 0, histogram.length);

        // Find an interval that maximizes the entropy
        IJDebug.log("Find an interval that maximizes the entropy");
        double bestE = Double.NEGATIVE_INFINITY;
        int[] bestInterval = null;
//        final int progressStep = intervals.length > 100 ? intervals.length / 100 : 1;
        for (int i = 0; i < intervals.length; i++) {
//            if (i % progressStep == 0) {
//                final int percProgress = (int) Math.round(i / (double) intervals.length * 100);
//                IJDebug.log("Percent intervals analyzed " + percProgress);
//            }
            final int[] interval = intervals[i];
            double e = 0;
            int lastT = min;
            for (int j = 0; j < interval.length; j++) {
                final int t = interval[j];
                e += intervalEntropy(lastT, t);
                lastT = t;
            }
            e += intervalEntropy(lastT, max);

            if (bestE < e) {
                bestE = e;
                bestInterval = interval;
            }
        }

        assert bestInterval != null;
        assert bestInterval.length == nbDivisions;

        intervalEntropy = null;
        histogram = null;

        return bestInterval;
    }


    /**
     * Generate all possible divisions of a range of numbers <code>min</code> to <code>max</code>.
     */
    private static int[][] intervals(int nbDivisions, int min, int max) {
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
     * @param begin first index to evaluate (inclusive).
     * @param end   last index to evaluate (exclusive).
     */
    private double intervalEntropy(int begin, int end) {

        Double ie = intervalEntropy[begin][end];
        if (ie == null) {
            final double hSum = sum(histogram, begin, end);
            if (hSum < EPSILON) {
                return 0;
            }

            double e = 0;
            for (int i = begin; i < end; ++i) {
                double h = histogram[i];
                if (h > EPSILON) {
                    double a = histogram[i] / hSum;
                    e -= a * Math.log(a);
                }
            }
            ie = new Double(e);
            intervalEntropy[begin][end] = ie;
        }


        return ie.doubleValue();
    }

    static final double sum(final double[] hist, int begin, int end) {
        double s = 0;
        for (int i = begin; i < end; ++i) {
            s += hist[i];
        }
        return s;
    }

}
