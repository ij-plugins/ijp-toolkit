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
import ij.Macro;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import net.sf.ij_plugins.util.progress.IJProgressBarAdapter;


/**
 * Converts image pixels from CIE L*a*b* color space to RGB color space.
 *
 * @author Jarek Sacha
 */
public class ConvertYCbCrToRGBPlugin implements PlugIn {

    final private static String PLUGIN_NAME = "Convert YCbCr to RGB";
    final private static String ABOUT_COMMAND = "about";
    final private static String ABOUT_MESSAGE =
            "Converts image pixels from YCbCr color space to RGB color space.\n" +
                    "Uses formulas provided at:\n" +
                    "http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30\n" +
                    "See also:\n" +
                    "http://en.wikipedia.org/wiki/YCbCr";


    @Override
    public void run(final String arg) {
        if (ConvertYCbCrToRGBPlugin.ABOUT_COMMAND.equalsIgnoreCase(arg)) {
            IJ.showMessage("About " + PLUGIN_NAME, ABOUT_MESSAGE);
            return;
        }

        final ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        if (imp.getType() != ImagePlus.GRAY8 || imp.getStackSize() != 3) {
            IJ.showMessage(PLUGIN_NAME, "Conversion supported for GRAY8 stacks with three slices.");
            Macro.abort();
            return;
        }

        final int currentSlice = imp.getCurrentSlice();
        final ByteProcessor[] src = new ByteProcessor[3];
        for (int i = 0; i < src.length; i++) {
            imp.setSlice(i + 1);
            src[i] = (ByteProcessor) imp.getProcessor().duplicate();
        }
        imp.setSlice(currentSlice);

        IJ.showStatus(PLUGIN_NAME);
        final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
        final ColorProcessor dest = ColorSpaceConversion.ycbcrToRGB(src, progressBarAdapter);
        new ImagePlus(imp.getTitle() + " - RGB", dest).show();
    }
}
