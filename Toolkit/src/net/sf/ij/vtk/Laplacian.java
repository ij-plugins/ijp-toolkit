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

import vtk.vtkImageCast;
import vtk.vtkImageData;
import vtk.vtkImageLaplacian;

/**
 *  Wrapper for vtkImageLaplacian.
 *
 * vtkImageLaplacian computes the Laplacian_ (like a second derivative) of a
 * scalar image. The operation is the same as taking the divergence after a
 * gradient. Boundaries are handled, so the input is the same as the output.
 *
 * @author   Jarek Sacha
 * @version  $Revision: 1.2 $
 */

public class Laplacian extends VtkImageFilter {

  private vtkImageLaplacian filter = null;
  private vtkImageCast inputCast;
  private VtkProgressObserver progressObserver;



  /**  Constructor for the AnisotropicDiffusion object */
  public Laplacian() {
    inputCast = new vtk.vtkImageCast();
    inputCast.SetOutputScalarTypeToFloat();
    filter = new vtkImageLaplacian();
    filter.SetInput(inputCast.GetOutput());
    filter.SetDimensionality(3);
    progressObserver = new VtkProgressObserver(filter);
  }


  /**  Description of the Method */
  public void update() {
    try {
      // Push input to VTK pipeline
      vtkImageData inputImageData = VtkImageDataFactory.create(inputImage);

      // update VTK pipeline
      inputCast.SetInput(inputImageData);
      filter.SetDimensionality(inputImage.getStackSize() == 1 ? 2 : 3);
      filter.Update();

      // Pull output from VTK pipeleine
      vtkImageData outputImageData = filter.GetOutput();
      outputImage = VtkUtil.createImagePlus(outputImageData);
    } catch (Exception ex) {
      ex.printStackTrace();

    }
  }
}
