/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
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
import ij.process.ColorProcessor;
import net.sf.ij_plugins.multiband.VectorProcessor;


/**
 * Converts image pixels from XYZ color space to RGB color space.
 *
 * @author Jarek Sacha
 */
public class ConvertXYZStackToRGBPlugin implements PlugIn {

    private static final String PLUGIN_NAME = "Convert CIE XYZ to sRGB";
    private static final String ABOUT_COMMAND = "about";
    private static final String ABOUT_MESSAGE =
            "Converts image pixels from XYZ color space to RGB color space.\n" +
                    "The XYZ image is asumed to a stack of floating point images (32 bit).\n" +
                    "Uses formulas provided at: http://www.brucelindbloom.com";


    @Override
    public void run(final String arg) {
        if (ABOUT_COMMAND.equalsIgnoreCase(arg)) {
            IJ.showMessage("About " + PLUGIN_NAME, ABOUT_MESSAGE);
            return;
        }

        final ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }


        if (imp.getType() != ImagePlus.GRAY32 || imp.getStackSize() != 3) {
            IJ.showMessage(PLUGIN_NAME,
                    "Conversion supported for GRAY32 stacks with three slices.");
            Macro.abort();
            return;
        }

//        IJ.showStatus(PLUGIN_NAME);
        final VectorProcessor vp = new VectorProcessor(imp.getStack());
        final ColorProcessor cp = ColorSpaceConversion.xyzToColorProcessor(vp);
        final ImagePlus dest = new ImagePlus(imp.getTitle() + " - RGB", cp);
        dest.show();
    }
}