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
import ij.*;
import ij.gui.*;
import ij.process.ImageProcessor;
import ij.plugin.filter.PlugInFilter;

import net.sf.ij.im3d.Util;
import net.sf.ij.im3d.morphology.Morpho;

import vtk.vtkImageSobel2D;
import vtk.vtkImageData;

/**
 *  Performs morphological dilation (max) for 2D and 3D images, using 8- or
 *  26-connectedness, respectively.
 *
 * @author     Jarek Sacha
 * @created    July 14, 2002
 * @version    $Revision: 1.1 $
 */

public class VTK_Sobel_2D implements PlugInFilter {

  public int setup(String arg, ImagePlus imp) {
    return PlugInFilter.DOES_8G;
  }

  /**
   *  Main processing method for the VTK_Writer plugin
   *
   * @param  arg  Optional argument required by ij.plugin.PlugIn interface (not
   *      used).
   */
  public void run(ImageProcessor ip) {
//    vtkImageData vid = new vtkImageData();
//    vid.
//    vtkImageSobel2D sobel2D = new vtkImageSobel2D();
//    sobel2D.SetInput(vid);

  }
}
