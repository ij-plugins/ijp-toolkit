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

package ij_plugins.toolkit.im3d.filters;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij_plugins.toolkit.im3d.morphology.Morpho;


/**
 * @author Jarek Sacha
 * @since Sep 8, 2010 6:17:55 PM
 */
public final class Median3DPlugin implements PlugIn {

    @Override
    public void run(final String arg) {
        final ImagePlus src = WindowManager.getCurrentImage();
        if (src == null) {
            IJ.noImage();
            return;
        }

        if (src.getType() != ImagePlus.GRAY8) {
            IJ.showMessage("Median 3D", "This plugin works only with GRAY8 images.");
            return;
        }

        Morpho.median(src).show();
    }
}
