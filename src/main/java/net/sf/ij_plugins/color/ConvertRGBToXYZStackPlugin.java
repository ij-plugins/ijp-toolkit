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
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.multiband.VectorProcessor;


/**
 * Converts image pixels from RGB color space to XYZ color space.
 *
 * @author Jarek Sacha
 */
public class ConvertRGBToXYZStackPlugin implements PlugInFilter {

    private static final String PLUGIN_NAME = "Convert sRGB to CIE XYZ";
    private static final String ABOUT_COMMAND = "about";
    private static final String ABOUT_MESSAGE =
            "Converts image pixels from sRGB color space to XYZ color space.\n" +
                    "The XYZ is represented as a stack of floating point images.\n" +
                    "Uses formulas provided at: http://www.brucelindbloom.com";

    private String imageTitle = "";


    @Override
    public int setup(final String arg, final ImagePlus imp) {
        if (ABOUT_COMMAND.equalsIgnoreCase(arg)) {
            IJ.showMessage("About " + PLUGIN_NAME, ABOUT_MESSAGE);
            return DONE;
        }

        if (imp != null) {
            imageTitle = imp.getShortTitle();
        }
        return DOES_RGB | DOES_STACKS | NO_CHANGES;
    }


    @Override
    public void run(final ImageProcessor ip) {
//        IJ.showStatus(PLUGIN_NAME);

        final ColorProcessor cp = (ColorProcessor) ip;
        final VectorProcessor vp = ColorSpaceConversion.rgbToXYZVectorProcessor(cp);
        final ImagePlus imp = vp.toFloatStack(new String[]{"X", "Y", "Z"});
        imp.setTitle(imageTitle + " - XYZ");
        imp.show();
    }
}