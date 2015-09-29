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
package net.sf.ij_plugins.im3d.filters;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;


/**
 * Reduce size of an image to smallest one containing all non-zero pixels. Stacks are treated as 3D
 * images. The 'origin' property of an image is modified to reflect clipping performed.
 *
 * @author Jarek Sacha
 * @since May 8, 2002
 */

public class AutoCrop3DPlugin implements PlugIn {

    /**
     * Main processing method for the net.sf.ij_plugins.im3d.filters.AutoClipVolumePlugin plugin
     *
     * @param arg Optional argument required by ij.plugin.PlugIn interface (not used).
     */
    @Override
    public void run(final String arg) {
        final ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        if (imp.getType() != ImagePlus.GRAY8) {
            IJ.showMessage("Auto Clip Volume", "This plugin works only with GRAY8 images.");
            return;
        }

        AutoCrop3D.run(imp).show();
    }
}
