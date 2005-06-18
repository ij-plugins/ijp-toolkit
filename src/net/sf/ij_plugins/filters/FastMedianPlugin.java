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
package net.sf.ij_plugins.filters;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * @author Jarek Sacha
 * @version $ Revision: $
 */

public class FastMedianPlugin implements PlugInFilter {
    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G | DOES_16 | DOES_32 | NO_CHANGES;
    }

    public void run(final ImageProcessor ip) {

        final GenericDialog genericDialog = new GenericDialog("Fast median parameters");
        genericDialog.addNumericField("Filter size", 3, 0, 3, "pixels");
        genericDialog.showDialog();

        if (genericDialog.wasCanceled()) {
            return;
        }

        int filterSize = (int) Math.round(genericDialog.getNextNumber());

        if (ip instanceof ByteProcessor) {
            final ByteProcessor src = (ByteProcessor) ip;


            final FastMedianUInt8 filter = new FastMedianUInt8();

            // Set progress bar
            if (IJ.getInstance() != null) {
                filter.setProgressBar(IJ.getInstance().getProgressBar());
            }

            // Perform filtering
            long start = System.currentTimeMillis();
            ByteProcessor dest = filter.run(src, filterSize, filterSize);
            long end = System.currentTimeMillis();

            // Show results
            new ImagePlus("Median", dest).show();

            if (IJ.debugMode) {
                IJ.write("Median filtering completed in " + (end - start) + "ms.");
            }
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
            long start = System.currentTimeMillis();
            FloatProcessor dest = filter.run(src);
            long end = System.currentTimeMillis();

            // Show results
            new ImagePlus("Median", dest).show();

            if (IJ.debugMode) {
                IJ.write("Median filtering completed in " + (end - start) + "ms.");
            }
        }
    }
}
