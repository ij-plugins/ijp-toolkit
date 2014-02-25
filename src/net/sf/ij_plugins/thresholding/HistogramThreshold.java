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
package net.sf.ij_plugins.thresholding;

import net.sf.ij_plugins.util.progress.ProgressEvent;
import net.sf.ij_plugins.util.progress.ProgressListener;

/**
 * Histogram based thresholding.
 *
 * @author Jarek Sacha
 */
public final class HistogramThreshold {

    private static final double EPSILON = Double.MIN_NORMAL;


    private HistogramThreshold() {
    }


    /**
     * Equivalent to calling  maximumEntropy(hist, null).
     *
     * @param hist histogram to be thresholded.
     * @return index of the maximum entropy split.
     * @see #maximumEntropy(int[], net.sf.ij_plugins.util.progress.ProgressListener)
     */
    public static int maximumEntropy(final int[] hist) {
        return maximumEntropy(hist, null);
    }


    /**
     * Calculate maximum entropy split of a histogram. For more information see: J.N. Kapur, P.K.
     * Sahoo and A.K.C. Wong, "A New Method for Gray-Level Picture Thresholding Using the Entropy of
     * the Histogram", <i>CVGIP</i>, (29), pp.273-285 , 1985.
     * <br>
     * Returned value indicates split position <code>t</code>. First interval are values less than
     * <code>t</code>, second interval are values equal or larger than <code>t</code>.
     * <br>
     * If all values in <code>hist</code> are zero, the split, return value, is set to zero.
     *
     * @param hist             histogram to be thresholded.
     * @param progressListener progress listener, can be <code>null</code>.
     * @return index of the maximum entropy split.
     */
    public static int maximumEntropy(final int[] hist, final ProgressListener progressListener) {

        // Trim leading and trailing zeros
        final int leadingOffset = leadingOffset(hist);
        final int trailingOffset = trailingOffset(hist);
        final int trimmedLength = trailingOffset - leadingOffset;

        final int threshold;
        if (trimmedLength > 0) {
            final int[] histTrimmed = new int[trimmedLength];
            System.arraycopy(hist, leadingOffset, histTrimmed, 0, trimmedLength);
            threshold = leadingOffset + maximumEntropy_impl(histTrimmed, progressListener);
        } else {
            threshold = 0;
        }

        return threshold;
    }


    private static int leadingOffset(final int[] hist) {
        for (int i = 0; i < hist.length; i++) {
            if (hist[i] != 0) {
                return i;
            }
        }
        return hist.length;
    }


    private static int trailingOffset(final int[] hist) {
        for (int i = hist.length - 1; i >= 0; i--) {
            if (hist[i] != 0) {
                return i + 1;
            }
        }
        return 0;
    }


    private static int maximumEntropy_impl(final int[] hist, final ProgressListener progressListener) {

        final String progressMessage = "Maximum entropy threshold...";
        final Object progressSource = HistogramThreshold.class;
        if (progressListener != null) {
            progressListener.progressNotification(new ProgressEvent(progressSource, 0.0, progressMessage));
        }

        // Normalize histogram, that is makes the sum of all bins equal to 1.
        final double[] normalizedHist = normalize(hist);

        //
        final double[] pT = new double[hist.length];
        pT[0] = normalizedHist[0];
        for (int i = 1; i < hist.length; i++) {
            pT[i] = pT[i - 1] + normalizedHist[i];
        }

        // Entropy for black and white parts of the histogram
        final double[] hB = new double[hist.length];
        final double[] hW = new double[hist.length];
        // Calculate increment, make sure that different/larger than 0 otherwise '%' operation will fail.
        final int progressStep = Math.max(1, hist.length / 50);
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

            if ((progressListener != null) && (t % progressStep == 0)) {
                progressListener.progressNotification(new ProgressEvent(progressSource, t / (double) hist.length, progressMessage));
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
     * Normalize histogram: divide all elements of the histogram by a fixed values so sum of the
     * elements is equal 1.
     *
     * @param hist histogram to be normalized.
     * @return normalized histogram.
     */
    static double[] normalize(final int[] hist) {
        // Normalize histogram, that is makes the sum of all bins equal to 1.
        double sum = 0;
        for (final int v : hist) {
            sum += v;
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
