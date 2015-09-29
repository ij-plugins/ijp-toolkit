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
package net.sf.ij_plugins.multiband;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

/**
 * @author Jarek Sacha
 */
public class VectorEdgeDetectorPlugin implements PlugIn {
    private static final String PLUGIN_NAME = "Multi-band edge detection";
    private static final String ABOUT_COMMAND = "about";
    private static final String ABOUT_MESSAGE =
            "Sobel edge detector that supports multi-band and color images.\n" +
                    "Slices in an image stack are interpreted as bands in a multi-band image.\n" +
                    "An RGB image is interpreted as 3 band image. A simple gray image in interpreted as\n" +
                    "as a single band image.";


    /**
     * Values of the <code>arg</code> argument: <ul> <li> 'about' - show short help message. </li>
     * <li> 'VectorSobelEdgeOperator' - invoke {@link VectorSobelEdgeOperator} filter.</li>
     * <li>'VectorGradientEdgeOperator' - invoke {@link VectorGradientEdgeOperator} filter.</li>
     * <li>'VectorDifferenceEdgeOperator' - invoke {@link VectorDifferenceEdgeOperator} filter.</li>
     * </ul>
     */
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

        final FloatProcessor fp;
        final String postFix;
        if ("VectorSobelEdgeOperator".equalsIgnoreCase(arg)) {
            fp = VectorSobelEdgeOperator.run(imp);
            postFix = "Sobel";
        } else if ("VectorDifferenceEdgeOperator".equalsIgnoreCase(arg)) {
            fp = VectorDifferenceEdgeOperator.run(imp);
            postFix = "difference";
        } else if ("VectorGradientEdgeOperator".equalsIgnoreCase(arg)) {
            fp = VectorGradientEdgeOperator.run(imp);
            postFix = "gradient";
        } else {
            throw new IllegalArgumentException("Invalid invocation argument: " + arg);
        }

        fp.resetMinAndMax();
        final String title = imp.getShortTitle() + " - " + postFix + " edges";
        new ImagePlus(title, fp).show();

    }
}
