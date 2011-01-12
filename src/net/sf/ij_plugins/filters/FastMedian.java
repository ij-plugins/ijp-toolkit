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
package net.sf.ij_plugins.filters;

import ij.IJ;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.color.ColorProcessorUtils;


/**
 * Helper for calling fast median filter algorithms.
 * It will call an implementation that best matches input Image {@code ip}.
 * <p/>
 * Sample usa of a 7x7 median filter:
 * <pre>
 *   ColorProcessor src = ...
 *   ColorProcessor dest = (ColorProcessor) FastMedian.process(src, 7);
 * </pre>
 *
 * @author Jarek Sacha
 * @since Oct 23, 2008 10:01:46 PM
 */
public final class FastMedian {

    /**
     * Apply median filter to input image {@code ip}.
     * For color images ({@link ColorProcessor}) it will process each color band (R,G,B) independently.
     *
     * @param ip         input image.
     * @param filterSize filter size (window size is filterSize x filterSize)
     * @return median filtered input image. The type is the same sa input image,
     *         so for ColorProcessor it will be ColorProcessor.
     */
    public static ImageProcessor process(final ImageProcessor ip, final int filterSize) {

        final ImageProcessor dest;
        if (ip instanceof ByteProcessor) {
            dest = process((ByteProcessor) ip, filterSize);
        } else if (ip instanceof ColorProcessor) {
            final ByteProcessor[] srcBps = ColorProcessorUtils.splitRGB((ColorProcessor) ip);
            for (int i = 0; i < srcBps.length; i++) {
                srcBps[i] = process(srcBps[i], filterSize);
            }
            dest = ColorProcessorUtils.mergeRGB(srcBps);
        } else {
            final FloatProcessor src = (FloatProcessor) (ip instanceof FloatProcessor
                    ? ip
                    : ip.convertToFloat());

            final RunningFilter filter
                    = new RunningFilter(new RunningMedianOperator(), filterSize, filterSize);

            // Set progress bar
            if (IJ.getInstance() != null) {
                filter.setProgressBar(IJ.getInstance().getProgressBar());
            }

            // Perform filtering
            dest = filter.run(src);
        }

        return dest;
    }


    private static ByteProcessor process(final ByteProcessor src, final int filterSize) {
        final FastMedianUInt8 filter = new FastMedianUInt8();

        // Set progress bar
        if (IJ.getInstance() != null) {
            filter.setProgressBar(IJ.getInstance().getProgressBar());
        }
        return filter.run(src, filterSize, filterSize);
    }
}
