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
import ij.plugin.PlugIn;

import net.sf.ij.io.vtk.VtkEncoder;

/**
 *  Save image in <a HREF="http://public.kitware.com/VTK/">VTK</a> format.
 *  Supported image types: GRAY8, GRAY16, GRAY32.
 *
 *@author     Jarek Sacha
 *@created    April 28, 2002
 *@version    $Revision: 1.2 $
 *@see        net.sf.ij.io.vtk.VtkEncoder
 */

public class VTK_Writer implements PlugIn {
  /**
   *  Main processing method for the VTK_Writer plugin
   *
   *@param  arg  If equal "ASCII" file will be saved in text format otherwise in
   *      binary format (MSB).
   */
  public void run(String param1) {
    new VtkEncoder().run(param1);
  }
}
