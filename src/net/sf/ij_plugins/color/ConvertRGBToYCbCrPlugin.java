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

package net.sf.ij_plugins.color;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.util.progress.IJProgressBarAdapter;


/**
 * Converts image pixels from RGB color space to YCbCr color space.
 *
 * @author Jarek Sacha
 */
public class ConvertRGBToYCbCrPlugin implements PlugInFilter {

    final private static String PLUGIN_NAME = "Convert RGB to YCbCr";
    final private static String ABOUT_COMMAND = "about";
    final private static String ABOUT_MESSAGE =
            "Converts image pixels from RGB color space to YCbCr color space.\n" +
                    "Uses formulas provided at:\n" +
                    "http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30\n" +
                    "See also:\n" +
                    "http://en.wikipedia.org/wiki/YCbCr";

    private String imageTitle = "";


    @Override
    public int setup(final String arg, final ImagePlus imp) {
        if (ConvertRGBToYCbCrPlugin.ABOUT_COMMAND.equalsIgnoreCase(arg)) {
            IJ.showMessage("About " + ConvertRGBToYCbCrPlugin.PLUGIN_NAME, ConvertRGBToYCbCrPlugin.ABOUT_MESSAGE);
            return DONE;
        }

        if (imp != null) {
            imageTitle = imp.getShortTitle();
        }
        return DOES_RGB | DOES_STACKS | NO_CHANGES;
    }


    @Override
    public void run(final ImageProcessor ip) {
        IJ.showStatus(ConvertRGBToYCbCrPlugin.PLUGIN_NAME);

        final ColorProcessor cp = (ColorProcessor) ip;
        final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
        final ByteProcessor[] bps = ColorSpaceConversion.rgbToYCbCr(cp, progressBarAdapter);
        final ImageStack stack = new ImageStack(ip.getWidth(), ip.getHeight());
        stack.addSlice("Y", bps[0]);
        stack.addSlice("Cb", bps[1]);
        stack.addSlice("Cr", bps[2]);
        final ImagePlus imp = new ImagePlus(imageTitle + " - YCbCr", stack);
        imp.show();
    }
}
