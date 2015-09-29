/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
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


import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.util.progress.IJProgressBarAdapter;

/**
 * Automatic thresholding technique based on the maximum entropy of the histogram. See: P.K. Sahoo,
 * S. Soltani, K.C. Wong and, Y.C. Chen "A Survey of Thresholding Techniques", Computer Vision,
 * Graphics, and Image Processing, Vol. 41, pp.233-260, 1988.
 *
 * @author Jarek Sacha
 */
public final class MaximumEntropyThresholdPlugin implements PlugInFilter {
    private static final String ABOUT_MESSAGE =
            "Automatic thresholding technique based on the maximum entropy of the\n" +
                    "histogram. See:\n" +
                    "J.N. Kapur, P.K. Sahoo and A.K.C. Wong, A New Method for Gray-Level Picture\n" +
                    "\"Thresholding Using the Entropy of the Histogram\"CVGIP, (29), pp.273-285,\n" +
                    "1985.";

    // TODO: Make this plugin available for 2D, 3D, and stacks

    @Override
    public int setup(final java.lang.String s, final ImagePlus imagePlus) {
        if ("about".equalsIgnoreCase(s)) {
            IJ.showMessage("Maximum Entropy Threshold", ABOUT_MESSAGE);
            return PlugInFilter.DONE;
        }

        return PlugInFilter.DOES_8G | PlugInFilter.DOES_16 | PlugInFilter.DOES_STACKS;
    }

    @Override
    public void run(final ImageProcessor imageProcessor) {
        final int[] hist = imageProcessor.getHistogram();
        final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
        final int threshold = HistogramThreshold.maximumEntropy(hist, progressBarAdapter);
        imageProcessor.threshold(threshold);
    }

}
