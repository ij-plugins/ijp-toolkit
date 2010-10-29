/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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
package net.sf.ij_plugins.io.vtk;

import net.sf.ij_plugins.util.Enumeration;


/**
 * Represents supported values VTK DATA_SET tag.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 * @created June 21, 2002
 */

class VtkDataSetType extends Enumeration {

    /**
     * STRUCTURED_POINTS
     */
    public final static VtkDataSetType STRUCTURED_POINTS
            = new VtkDataSetType("STRUCTURED_POINTS");


    /**
     * Constructor for the VtkDataType object
     *
     * @param name Description of the Parameter
     */
    private VtkDataSetType(final String name) {
        super(name);
    }
}
