/*
 * IJ-Plugins
 * Copyright (C) 2002-2019 Jarek Sacha
 * Author's email: jpsacha at gmail dot com
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

package net.sf.ij_plugins.ui.multiregion;

import ij.gui.Roi;
import net.sf.ij_plugins.beans.AbstractModel;

/**
 * @author Jarek Sacha
 */
public class SubRegion extends AbstractModel {

    private String name = "?";
    private Roi roi;


    public SubRegion(final String name, final Roi roi) {
        setName(name);
        setRoi(roi);
    }


    public String getName() {
        return name;
    }


    public void setName(final String name) {
        firePropertyChange("name", this.name, this.name = name);
    }


    public Roi getRoi() {
        return roi;
    }


    public void setRoi(final Roi roi) {
        firePropertyChange("roi", this.roi, this.roi = roi);
    }


    @Override
    public String toString() {
        return name;
    }
}
