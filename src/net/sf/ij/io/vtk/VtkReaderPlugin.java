/***
 * Image/J Plugins
 * Copyright (C) 2002-2005 Jarek Sacha
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
package net.sf.ij.io.vtk;

import ij.plugin.PlugIn;

/**
 * Read image in <a HREF="http://public.kitware.com/VTK/">VTK</a> format.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 * @created June 18, 2002
 * @see net.sf.ij.io.vtk.VtkDecoder
 */

public class VtkReaderPlugin implements PlugIn {
    /**
     * Main processing method for the net.sf.ij.io.vtk.VtkReaderPlugin object
     *
     * @param parm1 Description of Parameter
     */
    public void run(String parm1) {
        new VtkDecoder().run(parm1);
    }
}
