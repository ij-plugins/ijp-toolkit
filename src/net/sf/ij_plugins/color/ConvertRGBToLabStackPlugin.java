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
package net.sf.ij_plugins.color;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.multiband.VectorProcessor;

/**
 * Converts image pixels from RGB color space to CIE L*a*b* color space.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class ConvertRGBToLabStackPlugin implements PlugInFilter {
    final private static String PLUGIN_NAME = "Convert RGB to CIE L*a*b*";
    final private static String ABOUT_COMMAND = "about";
    final private static String ABOUT_MESSAGE =
            "Converts image pixels from RGB color space to CIE L*a*b* color space.\n" +
                    "The CIE L*a*b* is represented as a stack of floating point images.\n" +
                    "Assumes observer = 2°, illuminant = D65, and uses formulas provided at:\n" +
                    "http://www.easyrgb.com/math.php";

    private String imageTitle = "";

    public int setup(String arg, ImagePlus imp) {
        if (ABOUT_COMMAND.equalsIgnoreCase(arg)) {
            IJ.showMessage("About " + PLUGIN_NAME, ABOUT_MESSAGE);
            return DONE;
        }

        imageTitle = imp.getShortTitle();
        return DOES_RGB | DOES_STACKS | NO_CHANGES;
    }

    public void run(final ImageProcessor ip) {
        IJ.showStatus(PLUGIN_NAME);

        final ColorProcessor cp = (ColorProcessor) ip;
        final VectorProcessor vp = ColorSpaceConvertion.rgbToLabVectorProcessor(cp);
        final ImagePlus imp = vp.toFloatStack();
        imp.setTitle(imageTitle + " - Lab");
        imp.show();
    }
}
