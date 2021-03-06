/*
 *  IJ-Plugins
 *  Copyright (C) 2002-2021 Jarek Sacha
 *  Author's email: jpsacha at gmail dot com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Latest release available at https://github.com/ij-plugins/ijp-toolkit/
 */

package ij_plugins.toolkit.thresholding;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij_plugins.toolkit.ui.progress.IJProgressBarAdapter;
import ij_plugins.toolkit.util.IJPUtils;


/**
 * Automatic thresholding technique based on the maximum entropy of the histogram. See: P.K. Sahoo,
 * S. Soltani, K.C. Wong and, Y.C. Chen "A Survey of Thresholding Techniques", Computer Vision,
 * Graphics, and Image Processing, Vol. 41, pp.233-260, 1988.
 * <br>
 * The original bi-level threshold is extended here to multiple-levels.
 *
 * @author Jarek Sacha
 */
public final class MaximumEntropyMultiThresholdPlugin implements PlugInFilter {

    private static final String TITLE = "Maximum Entropy Multi-Threshold";
    private static final String DESCRIPTION = "<html>" +
            "Automatic multiple thresholding technique, generalization of a single maximum <br>" +
            "entropy thresholding of Kapur, Sahoo, and Wond: <br>" +
            "J.N. Kapur, P.K. Sahoo and A.K.C. Wong, \"A New Method for Gray-Level Picture <br>" +
            "Thresholding Using the Entropy of the Histogram\", <em>CVGIP</em>, (29), pp.273-285, 1985." +
            "</html>";
    private static final String HELP_URL = "https://github.com/ij-plugins/ijp-toolkit/wiki/Maximum-Entropy-Threshold";

    // TODO: Add to CVS and make this plugin available for 2D, 3D, and stacks


    @Override
    public int setup(final String s, final ImagePlus imagePlus) {

        if ("about".equalsIgnoreCase(s)) {
            IJ.showMessage(TITLE, DESCRIPTION);
            return PlugInFilter.DONE;
        }

        return PlugInFilter.DOES_8G | PlugInFilter.DOES_STACKS;
    }


    @Override
    public void run(final ImageProcessor imageProcessor) {

        final GenericDialog dialog = new GenericDialog(TITLE);
        dialog.addPanel(IJPUtils.createInfoPanel(TITLE, DESCRIPTION));
        dialog.addNumericField("Number of thresholds:", 2, 0);
        dialog.addHelp(HELP_URL);

        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }

        final int nbThresholds = (int) Math.round(dialog.getNextNumber());
        if (nbThresholds < 1) {
            IJ.error(TITLE, "Number of thresholds cannot be less than 1 [" + nbThresholds + "].");
            return;
        }
        if (nbThresholds > 255) {
            IJ.error(TITLE, "Number of thresholds cannot be more than 255 [" + nbThresholds + "].");
            return;
        }

        final int[] hist = imageProcessor.getHistogram();
        final MaximumEntropyMultiThreshold maximumEntropyMultiThreshold = new MaximumEntropyMultiThreshold();
        maximumEntropyMultiThreshold.addProgressListener(new IJProgressBarAdapter());
        final int[] thresholds = maximumEntropyMultiThreshold.maximumEntropy(hist, nbThresholds);
        StringBuilder logMsg = new StringBuilder("Maximum Entropy Thresholds: ");
        for (final int threshold : thresholds) {
            logMsg.append(" ").append(threshold);
        }
        IJ.log(logMsg.toString());
        encode((ByteProcessor) imageProcessor, thresholds);
    }


    private void encode(final ByteProcessor ip, final int[] thresholds) {
        final int[] values = new int[thresholds.length + 1];
        final double inc = 255.0 / thresholds.length;
        for (int i = 0; i < values.length; i++) {
            values[i] = (int) Math.round(i * inc);
        }
        StringBuilder logMsg = new StringBuilder("Levels in thresholded image: ");
        for (final int v : values) {
            logMsg.append(" ").append(v);
        }
        IJ.log(logMsg.toString());

        final byte[] pixels = (byte[]) ip.getPixels();
        for (int i = 0; i < pixels.length; i++) {
            final int srcPixel = pixels[i] & 0xff;
            int destPixel = -1;
            if (srcPixel <= thresholds[0]) {
                destPixel = values[0];
            } else {
                for (int t = 1; t < thresholds.length; t++) {
                    if (srcPixel > thresholds[t - 1] && srcPixel <= thresholds[t]) {
                        destPixel = values[t];
                    }
                }
            }
            if (destPixel < 0) {
                destPixel = values[thresholds.length];
            }
            pixels[i] = (byte) (destPixel & 0xff);
        }
    }


}
