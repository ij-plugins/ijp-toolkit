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
package net.sf.ij.vtk.imaging;

import ij.ImagePlus;
import ij.process.ByteProcessor;

import vtk.vtkImageAnisotropicDiffusion2D;
import vtk.vtkImageData;

import net.sf.ij.vtk.VtkImageDataFactory;
import net.sf.ij.vtk.Util;


/**
 *  Title: Description: Copyright: Copyright (c) 2002 Company:
 *
 * @author
 * @created    September 11, 2002
 * @version    1.0
 */

public class AnisotropicDiffusion2D {

  private ByteProcessor inputProcessor = null;
  private ByteProcessor outputProcessor = null;
  private vtkImageAnisotropicDiffusion2D filter = null;


  /**  Constructor for the AnisotropicDiffusion2D object */
  public AnisotropicDiffusion2D() {
    // Create VTK pipeline for the filter
    filter = new vtkImageAnisotropicDiffusion2D();
//    filter.CornersOn();
//    filter.FacesOn();
//    filter.EdgesOn();
//    filter.SetNumberOfIterations(1);
//    filter.SetDiffusionFactor(128);
//    filter.SetDiffusionThreshold(128);
//    filter.SetGradientMagnitudeThreshold(128);
  }


  /**
   *  Sets the input attribute of the AnisotropicDiffusion2D object
   *
   * @param  bp  The new input value
   */
  public void setInput(ByteProcessor bp) {
    inputProcessor = bp;
  }


  /**
   *  Get the filtered image. Can return null in updae was not called.
   *
   * @return    The output value
   */
  public ByteProcessor getOutput() {
    return outputProcessor;
  }


  /**  Description of the Method */
  public void update() {
    try {
      // Push input to VTK pipeline
      vtkImageData inputImageData = VtkImageDataFactory.create(inputProcessor);

      // update VTK pipeline
      filter.SetInput(inputImageData);
      filter.Update();

      // Pull output from VTK pipeleine
      vtkImageData outputImageData = filter.GetOutput();
      ImagePlus imp = Util.createImagePlus(outputImageData);

      outputProcessor = (ByteProcessor)imp.getProcessor();
    }
    catch(Exception ex) {
      ex.printStackTrace();

    }
  }
}
