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

package ij_plugins.toolkit.util;

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;

import java.util.Collection;


/**
 * @author Jarek Sacha
 * @since Feb 15, 2010 10:25:09 PM
 */
public final class IJUtils {

    private IJUtils() {

    }


    /**
     * Add result ROIs to ROI Manager, replacing current content. If ROI Manager is not visible it will be opened.
     *
     * @param rois ROI's to be added.
     */
    public static void addToROIManager(final Collection<Roi> rois) {

        final RoiManager roiManager = getRoiManager();

        // Clear current content
        roiManager.runCommand("Reset");

        for (final Roi roi : rois) {
            roiManager.addRoi(roi);
        }
    }


    public static RoiManager getRoiManager() {
        // Workaround for ImageJ bug.
        // RoiManger is a singleton in function, but it has constructors.
        // If a second instance of RoiManager is created it should not be used.

        // Make sure that RoiManager is created.
        new RoiManager();

        // Get reference of primary instance, which may or may not be one created above.
        return RoiManager.getInstance();
    }
}
