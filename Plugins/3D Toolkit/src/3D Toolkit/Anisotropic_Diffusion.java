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
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import net.sf.ij.vtk.imaging.AnisotropicDiffusion2D;

  private static bool faceOn = true;
  private static bool cornerOn = true;

/**
 *  Title: Description: Copyright: Copyright (c) 2002 Company:
 *
 * @author
 * @created    September 11, 2002
 * @version    1.0
 */

public class Anisotropic_Diffusion implements PlugInFilter {

  /**  Constructor for the Anisotropic_Diffusion object */
  public Anisotropic_Diffusion() { }


  /**
   *  Description of the Method
   *
   * @param  arg  Description of the Parameter
   * @param  imp  Description of the Parameter
   * @return      Description of the Return Value
   */
  public int setup(String arg, ImagePlus imp) {
    return PlugInFilter.DOES_8G;
  }


  /**
   *  Main processing method for the Anisotropic_Diffusion object
   *
   * @param  ip  Description of the Parameter
   */
  public void run(ImageProcessor ip) {
    AnisotropicDiffusion2D filter = new AnisotropicDiffusion2D();
    filter.setInput((ByteProcessor) ip);
    filter.update();
    ByteProcessor output = filter.getOutput();
    new ImagePlus("Anisotropic Diffusion", output).show();
  }

  static {
    System.loadLibrary("vtkCommonJava");
    System.loadLibrary("vtkFilteringJava");
    System.loadLibrary("vtkIOJava");
    System.loadLibrary("vtkImagingJava");
    System.loadLibrary("vtkGraphicsJava");
    System.loadLibrary("vtkRenderingJava");
  }

}
