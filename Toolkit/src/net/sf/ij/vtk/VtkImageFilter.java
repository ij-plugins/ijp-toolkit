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
package net.sf.ij.vtk;

import ij.ImagePlus;
import ij.IJ;

/**
 * Abstract class foe wrapping VTK image to image filters.
 *
 * @author   Jarek Sacha
 * @version  $Revision: 1.3 $
 */
public abstract class VtkImageFilter {

  static {
    // Load VTK libraries
    try {
      IJ.showStatus("Loading VTK dynamic libraries...");
      long tStart = System.currentTimeMillis();

      System.loadLibrary("vtkCommonJava");
      System.loadLibrary("vtkFilteringJava");
      System.loadLibrary("vtkIOJava");
      System.loadLibrary("vtkImagingJava");
      System.loadLibrary("vtkGraphicsJava");
      System.loadLibrary("vtkRenderingJava");

      long tEnd = System.currentTimeMillis();
      IJ.showStatus("VTK dynamic libraries loaded in " + (tEnd - tStart) + "ms.");

    } catch (UnsatisfiedLinkError ex) {
      ex.printStackTrace();
    }
  }


  protected ImagePlus inputImage = null;
  protected ImagePlus outputImage = null;

  /**
   *  Sets the input attribute of the AnisotropicDiffusion object
   *
   * @param  imp  The new input value
   */
  public void setInput(ImagePlus imp) {
    inputImage = imp;
  }

  /**
   *  Get the filtered image. Can return null in updae was not called.
   *
   * @return    The output value
   */
  public ImagePlus getOutput() {
    return outputImage;
  }

  public abstract void update();

  /**
   *
   * @return help string for this operator.
   */
  public abstract String getHelpString();
}
