/**
 *  Image/J Plugins Copyright (C) 2002 Jarek Sacha This library is free
 *  software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software
 *  Foundation; either version 2.1 of the License, or (at your option) any later
 *  version. This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 *  License for more details. You should have received a copy of the GNU Lesser
 *  General Public License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA Latest release available at http://sourceforge.net/projects/ij-plugins/
 */
import ij.*;
import ij.gui.*;
import ij.plugin.PlugIn;

import net.sf.ij.im3d.*;
import net.sf.ij.im3d.grow.*;

/**
 *  Performs connected region growing. User is asked to provide seed point
 *  coordinates and min/max limits of the threshold. Result is displayed as a
 *  binary image. Works with 2D and 3D images (stacks).
 *
 * @author     Jarek Sacha
 * @created    July 14, 2002
 * @version    $Revision: 1.3 $
 */

public class Connected_Threshold_Grower implements PlugIn {

    private static Point3D seed = new Point3D();
    private static int valueMin = 0;
    private static int valueMax = 128;


    /**
     *  Main processing method for the VTK_Writer plugin
     *
     * @param  arg  Optional argument required by ij.plugin.PlugIn interface
     *      (not used).
     */
    public void run(String arg) {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        // TODO: use ConnectedThresholdFilter factory to create approptiate filter type.
        ConnectedThresholdFilterBase connectedThresholdFilter = null;
        if (imp.getType() == ImagePlus.GRAY8) {
            connectedThresholdFilter = new ConnectedThresholdFilterUInt8();
        }
        else if (imp.getType() == ImagePlus.GRAY16) {
            connectedThresholdFilter = new ConnectedThresholdFilterUInt16();
        }
        else {
            IJ.showMessage("ERROR", "Unsupported image type. " +
                    " Supporter types are GRAY8 and GRAY16.");
            return;
        }

        GenericDialog gd = new GenericDialog("Grow options");
        gd.addMessage("Seed point coordinates");
        gd.addNumericField("x", seed.x, 0);
        gd.addNumericField("y", seed.y, 0);
        gd.addNumericField("z", seed.z, 0);
        gd.addMessage("Threshold limits");
        gd.addNumericField("min", valueMin, 0);
        gd.addNumericField("max", valueMax, 0);

        gd.showDialog();

        if (gd.wasCanceled()) {
            return;
        }

        seed.x = (int)gd.getNextNumber();
        seed.y = (int)gd.getNextNumber();
        seed.z = (int)gd.getNextNumber();
        valueMin = (int)gd.getNextNumber();
        valueMax = (int)gd.getNextNumber();

        connectedThresholdFilter.setValueMin(valueMin);
        connectedThresholdFilter.setValueMax(valueMax);
        ImageStack out = connectedThresholdFilter.run(imp.getStack(), seed);

        new ImagePlus("Connected region", out).show();
    }
}
