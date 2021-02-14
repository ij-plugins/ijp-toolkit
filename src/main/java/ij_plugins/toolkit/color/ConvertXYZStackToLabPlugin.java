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

package ij_plugins.toolkit.color;

import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.plugin.PlugIn;
import ij_plugins.toolkit.multiband.VectorProcessor;


/**
 * Converts image pixels from XYZ color space to CIE L*a*b* color space.
 *
 * @author Jarek Sacha
 */
public class ConvertXYZStackToLabPlugin implements PlugIn {

    private static final String PLUGIN_NAME = "Convert CIE XYZ to CIE L*a*b*";
    private static final String ABOUT_COMMAND = "about";
    private static final String ABOUT_MESSAGE =
            "Converts image pixels from XYZ color space to  CIE L*a*b* color space.\n" +
                    "The XYZ image is assumed to a stack of floating point images (32 bit).\n" +
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
            IJ.showMessage(PLUGIN_NAME, "Conversion supported for GRAY32 stacks with three slices.");
            Macro.abort();
            return;
        }

//        IJ.showStatus(PLUGIN_NAME);
        final VectorProcessor vp = new VectorProcessor(imp.getStack());
        final VectorProcessor lab = ColorSpaceConversion.xyzToLabVectorProcessor(vp);
        final ImagePlus dest = lab.toFloatStack(new String[]{"L*", "a*", "b*"});
        dest.setTitle(imp.getTitle() + " - L*a*b*");
        dest.show();
    }
}