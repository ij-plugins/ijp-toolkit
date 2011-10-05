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
import net.sf.ij_plugins.im3d.morphology.Morpho;


/**
 * Performs morphological dilation (max) for 2D and 3D images, using 8- or 26-connectedness,
 * respectively.
 *
 * @author Jarek Sacha
 * @since July 14, 2002
 */

public class MorphologicalDilate3DPlugin implements PlugIn {

    /**
     * Main processing method for the net.sf.ij_plugins.im3d.filters.MorphologicalDilate3DPlugin
     * plugin
     *
     * @param arg Optional argument required by ij.plugin.PlugIn interface (not used).
     */
    @Override
    public void run(final String arg) {
        final ImagePlus src = WindowManager.getCurrentImage();
        if (src == null) {
            IJ.noImage();
            return;
        }

        if (src.getType() != ImagePlus.GRAY8) {
            IJ.showMessage("Morphological Dilate 3D", "This plugin works only with GRAY8 images.");
            return;
        }

        Morpho.dilate(src).show();
    }
}
