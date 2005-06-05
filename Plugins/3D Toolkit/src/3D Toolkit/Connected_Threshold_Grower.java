/***
 * Image/J Plugins
 * Copyright (C) 2002 Jarek Sacha
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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import net.sf.ij.im3d.Point3DInt;
import net.sf.ij.im3d.grow.ConnectedThresholdFilterBase;
import net.sf.ij.im3d.grow.ConnectedThresholdFilterUInt16;
import net.sf.ij.im3d.grow.ConnectedThresholdFilterUInt8;

/**
 * Performs connected region growing. User is asked to provide seedPoint point coordinates and
 * min/max limits of the threshold. Result is displayed as a binary image. Works with 2D and 3D
 * images (stacks).
 *
 * @author Jarek Sacha
 * @version $Revision: 1.8 $
 * @since July 14, 2002
 */

public class Connected_Threshold_Grower implements PlugIn {
    private static Point3DInt seedPoint = new Point3DInt();
    private static int valueMin;
    private static int valueMax;

    /**
     * Main processing method for the Connected_Threshold_Grower plugin
     *
     * @param arg Optional argument required by ij.plugin.PlugIn interface (not used).
     */
    public void run(String arg) {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        GenericDialog gd = new GenericDialog("Grow options");
        gd.addMessage("Seed point coordinates");
        gd.addNumericField("x", seedPoint.x, 0);
        gd.addNumericField("y", seedPoint.y, 0);
        gd.addNumericField("z", seedPoint.z, 0);
        gd.addMessage("Threshold limits");
        gd.addNumericField("min", valueMin, 0);
        gd.addNumericField("max", valueMax, 0);

        gd.showDialog();

        if (gd.wasCanceled()) {
            return;
        }

        seedPoint.x = (int) gd.getNextNumber();
        seedPoint.y = (int) gd.getNextNumber();
        seedPoint.z = (int) gd.getNextNumber();
        valueMin = (int) gd.getNextNumber();
        valueMax = (int) gd.getNextNumber();

        ConnectedThresholdFilterBase ctf;
        if (imp.getType() == ImagePlus.GRAY8) {
            ctf = new ConnectedThresholdFilterUInt8();
        } else if (imp.getType() == ImagePlus.GRAY16) {
            ctf = new ConnectedThresholdFilterUInt16();
        } else {
            IJ.showMessage("Incorrect image type, only 8 bit and 16 bit gray level images are supported.");
            return;
        }
        ctf.setValueMin(valueMin);
        ctf.setValueMax(valueMax);
        ImageStack out = ctf.run(imp.getStack(), seedPoint);

        new ImagePlus("Region", out).show();
    }
}
