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
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import net.sf.ij.vtk.VtkUtil;

import net.sf.ij.vtk.VtkImageDataFactory;

import vtk.vtkImageAnisotropicDiffusion2D;
import vtk.vtkImageData;
import vtk.vtkImageAnisotropicDiffusion3D;

/**
 *  Wrapper for vtkImageAnisotropicDiffusion2D.
 *
 * @author
 * @created    September 11, 2002
 * @version    1.0
 */

public class AnisotropicDiffusion {

  static {
    // Load VTK libraries
    try {
      System.loadLibrary("vtkCommonJava");
      System.loadLibrary("vtkFilteringJava");
      System.loadLibrary("vtkIOJava");
      System.loadLibrary("vtkImagingJava");
      System.loadLibrary("vtkGraphicsJava");
      System.loadLibrary("vtkRenderingJava");
    }
    catch (UnsatisfiedLinkError ex) {
      ex.printStackTrace();
    }
  }


  private ImagePlus inputImage = null;
  private ImagePlus outputImage = null;
  private vtkImageAnisotropicDiffusion3D filter = null;


  /**  Constructor for the AnisotropicDiffusion object */
  public AnisotropicDiffusion() {
      filter = new vtkImageAnisotropicDiffusion3D();
  }


  /**
   *  Sets the numberOfIterations attribute of the AnisotropicDiffusion object
   *
   * @param  i  The new numberOfIterations value
   */
  public void setNumberOfIterations(int i) {
    filter.SetNumberOfIterations(i);
  }


  /**
   *  Sets the diffusionFactor attribute of the AnisotropicDiffusion object
   *
   * @param  d  The new diffusionFactor value
   */
  public void setDiffusionFactor(double d) {
    filter.SetDiffusionFactor(d);
  }


  /**
   *  Sets the gradientMagnitudeThreshold attribute of the
   *  AnisotropicDiffusion object
   *
   * @param  enabled  The new gradientMagnitudeThreshold value
   */
  public void setGradientMagnitudeThreshold(boolean enabled) {
    if (enabled) {
      filter.GradientMagnitudeThresholdOn();
    }
    else {
      filter.GradientMagnitudeThresholdOff();
    }
  }


  /**
   *  Sets the diffusionThreshold attribute of the AnisotropicDiffusion object
   *
   * @param  t  The new diffusionThreshold value
   */
  public void setDiffusionThreshold(double t) {
    filter.SetDiffusionThreshold(t);
  }


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
    }
    catch (Exception ex) {
      ex.printStackTrace();

    }
  }


  /**
   *  Gets the diffusionFactor attribute of the AnisotropicDiffusion object
   *
   * @return    The diffusionFactor value
   */
  public double getDiffusionFactor() {
    return filter.GetDiffusionFactor();
  }


  /**
   *  Gets the diffusionThreshold attribute of the AnisotropicDiffusion object
   *
   * @return    The diffusionThreshold value
   */
  public double getDiffusionThreshold() {
    return filter.GetDiffusionThreshold();
  }


  /**
   *  Gets the gradientMagnitudeThreshold attribute of the
   *  AnisotropicDiffusion object
   *
   * @return    The gradientMagnitudeThreshold value
   */
  public boolean isGradientMagnitudeThreshold() {
    return filter.GetGradientMagnitudeThreshold() != 0;
  }


  /**
   *  Gets the numberOfIterations attribute of the AnisotropicDiffusion object
   *
   * @return    The numberOfIterations value
   */
  public int getNumberOfIterations() {
    return filter.GetNumberOfIterations();
  }
}
