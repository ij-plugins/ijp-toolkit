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

import vtk.vtkImageData;
import vtk.vtkImageContinuousDilate3D;

/**
 *  Wrapper for vtkImageContinuousDilate3D.
 *
 *  vtkImageContinuousDilate3D replaces a pixel with the maximum over an
 *  ellipsoidal neighborhood. If KernelSize of an axis is 1, no processing
 *  is done on that axis.
 *
 *
 * @author   Jarek Sacha
 * @version  $Revision: 1.1 $
 */

public class ContinuousDilate3D extends VtkImageFilter {

  private vtkImageContinuousDilate3D filter = null;
  private VtkProgressObserver progressObserver;


  /**  Constructor for the AnisotropicDiffusion object */
  public ContinuousDilate3D() {
    filter = new vtkImageContinuousDilate3D();
    progressObserver = new VtkProgressObserver(filter);
  }


  public void setKernelSize(int dx, int dy, int dz) {
    filter.SetKernelSize(dx, dy, dz);
  }

  public int[] getKernelSize() {
    return filter.GetKernelSize();
  }

  /**  Description of the Method */
  public void update() {
    try {
      // Push input to VTK pipeline
      vtkImageData inputImageData = VtkImageDataFactory.create(inputImage);

      // update VTK pipeline
      filter.SetInput(inputImageData);
      filter.Update();

      // Pull output from VTK pipeleine
      vtkImageData outputImageData = filter.GetOutput();
      outputImage = VtkUtil.createImagePlus(outputImageData);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
