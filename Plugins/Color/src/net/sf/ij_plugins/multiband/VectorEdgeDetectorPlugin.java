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
package net.sf.ij_plugins.multiband;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class VectorEdgeDetectorPlugin implements PlugIn {

    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        FloatProcessor fp;
        if ("VectorSobelEdgeOperator".equalsIgnoreCase(arg)) {
            fp = VectorSobelEdgeOperator.run(imp);
        } else if ("VectorDifferenceEdgeOperator".equalsIgnoreCase(arg)) {
            fp = VectorDifferenceEdgeOperator.run(imp);
        } else if ("VectorGradientEdgeOperator".equalsIgnoreCase(arg)) {
            fp = VectorGradientEdgeOperator.run(imp);
        } else {
            throw new IllegalArgumentException("Invalid invocation argument: " + arg);
        }

        new ImagePlus("Vector edges", fp).show();

    }
}
