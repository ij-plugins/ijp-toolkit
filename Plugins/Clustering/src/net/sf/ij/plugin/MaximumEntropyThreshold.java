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
package net.sf.ij.plugin;


import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.sf.ij.thresholding.HistogramThreshold;

/**
 * Automatic thresholding technique based on the entopy of the histogram. See:
 * P.K. Sahoo, S. Soltani, K.C. Wong and, Y.C. Chen "A Survey of Thresholding
 * Techniques", Computer Vision, Graphics, and Image Processing, Vol. 41,
 * pp.233-260, 1988.
 *
 * @author Jarek Sacha
 */
public final class MaximumEntropyThreshold implements PlugInFilter {

    // TODO: Add to CVS and make this plugin available for 2D, 3D, and stacks

    /*
     *
     */
    public final int setup(final java.lang.String s, final ImagePlus imagePlus) {
        return PlugInFilter.DOES_8G | PlugInFilter.DOES_STACKS;
    }

    /*
     *
     */
    public final void run(final ImageProcessor imageProcessor) {
        final int[] hist = imageProcessor.getHistogram();
        final int threshold = HistogramThreshold.maximumEntropy(hist);
        imageProcessor.threshold(threshold);
    }

}
