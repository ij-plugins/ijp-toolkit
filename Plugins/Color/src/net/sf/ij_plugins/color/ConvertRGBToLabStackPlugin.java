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
import net.sf.ij.multiband.VectorProcessor;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class ConvertRGBToLabStackPlugin implements PlugInFilter {
    final private static String PLUGIN_NAME = "Convert RGB to CIE Lab";
    final private static String ABOUT_COMMAND = "about";
    final private static String ABOUT_MESSAGE =
            "Converts image pixels from RGB color space to CIE Lab color space.\n" +
            "Assumes observer = 2°, illuminant = D65, and uses formulas provided at:\n" +
            "http://www.easyrgb.com/math.php";

    public int setup(String arg, ImagePlus imp) {
        if (ABOUT_COMMAND.equalsIgnoreCase(arg)) {
            IJ.showMessage("About " + PLUGIN_NAME, ABOUT_MESSAGE);
        }
        return DOES_RGB | DOES_STACKS | NO_CHANGES;
    }

    public void run(final ImageProcessor ip) {
        final ColorProcessor cp = (ColorProcessor) ip;
        final VectorProcessor vp = new VectorProcessor(cp);
        final float[][] pixels = vp.getPixels();
        final float[] tmp = new float[3];

        IJ.showStatus("Converting pixels from RGB to CIELab");

        final int progressStep = pixels.length / 10;
        for (int i = 0; i < pixels.length; i++) {
            if (i % progressStep == 0) {
                IJ.showProgress(i, pixels.length);
            }
            final float[] pixel = pixels[i];
            ColorConvertion.convertRGBToXYZ(pixel, tmp);
            ColorConvertion.convertXYZtoLab(tmp, pixel);
        }

        final ImagePlus imp = vp.toFloatStack();
        imp.show();
    }
}
