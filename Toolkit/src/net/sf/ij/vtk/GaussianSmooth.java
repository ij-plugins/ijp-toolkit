/***
 * Image/J Plugins
 * Copyright (C) 2002-2003 Jarek Sacha
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
import vtk.vtkImageCast;
import vtk.vtkImageGaussianSmooth;

/**
 *  Wrapper for vtkImageGaussianSmooth.
 *
 * vtkImageGaussianSmooth implements a convolution of the input image
 * with a gaussian. Supports from one to three dimensional convolutions.
 *
 *
 *
 * @author   Jarek Sacha
 * @version  $Revision: 1.1 $
 */

public class GaussianSmooth extends VtkImageFilter {

  private vtkImageGaussianSmooth filter;
  private vtkImageCast inputCast;
  private VtkProgressObserver progressObserver;
  private static final String HELP_STRING =
      "This is a wrapper for vtkImageGaussianSmooth filter.\n" +
      "vtkImageGaussianSmooth implements a convolution of the input image with a gaussian. "+
      "Supports from one to three dimensional convolutions.\n"+
      "The radius factors determine how far out the gaussian kernel will go before being clamped to zero.\n"+
      "Standard deviation of the gaussian is in pixel units.";

  /**  Constructor for the GaussianSmooth object */
  public GaussianSmooth() {
//    inputCast = new vtkImageCast();
//    inputCast.SetOutputScalarTypeToFloat();
    filter = new vtkImageGaussianSmooth();
//    filter.SetInput(inputCast.GetOutput());
    progressObserver = new VtkProgressObserver(filter);
  }


  /**
   *  Set/Get the dimensionality of this filter. This determines whether a
   * one, two, or three dimensional gaussian is performed.
   *
   * @param  dimentionality
   */
  public void setDimensionality(int dimentionality) {
    filter.SetDimensionality(dimentionality);
  }

  /**
   *  Set/Get the dimensionality of this filter. This determines whether a
   * one, two, or three dimensional gaussian is performed.
   *
   */
  public int getDimensionality() {
    return filter.GetDimensionality();
  }

  /**
   * Sets/Gets the Radius Factors of the gaussian in pixel units. The
   * radius factors determine how far out the gaussian kernel will go
   * before being clamped to zero.
   *
   * @param radiusFactor
   */
  public void setRadiusFactor(double radiusFactor) {
    filter.SetRadiusFactor(radiusFactor);
  }

  /**
   * Sets/Gets the Radius Factors of the gaussian in pixel units. The
   * radius factors determine how far out the gaussian kernel will go
   * before being clamped to zero.
   *
   * @param radiusFactorX
   * @param radiusFactorY
   */
  public void setRadiusFactors(double radiusFactorX, double radiusFactorY) {
    filter.SetRadiusFactors(radiusFactorX, radiusFactorY);
  }

  /**
   * Sets/Gets the Radius Factors of the gaussian in pixel units. The
   * radius factors determine how far out the gaussian kernel will go
   * before being clamped to zero.
   *
   * @param radiusFactorX
   * @param radiusFactorY
   * @param radiusFactorZ
   */
  public void setRadiusFactors(double radiusFactorX, double radiusFactorY, double radiusFactorZ) {
    filter.SetRadiusFactors(radiusFactorX, radiusFactorY, radiusFactorZ);
  }

  /**
   * Sets/Gets the Radius Factors of the gaussian in pixel units. The
   * radius factors determine how far out the gaussian kernel will go
   * before being clamped to zero.
   */
  public void setRadiusFactors(double[] radiusFactors) {
    filter.SetRadiusFactors(radiusFactors);
  }

  /**
   * Sets/Gets the Radius Factors of the gaussian in pixel units. The
   * radius factors determine how far out the gaussian kernel will go
   * before being clamped to zero.
   */
  public double[] getRadiusFactors() {
    return filter.GetRadiusFactors();
  }

  /**
   * Sets/Gets the Standard deviation of the gaussian in pixel units.
   *
   * @param standardDeviation
   */
  public void setStandardDeviation(double standardDeviation) {
    filter.SetStandardDeviation(standardDeviation);
  }

  /**
   * Sets/Gets the Standard deviation of the gaussian in pixel units.
   *
   * @param standardDeviationX
   * @param standardDeviationY
   */
  public void setStandardDeviations(double standardDeviationX, double standardDeviationY) {
    filter.SetStandardDeviations(standardDeviationX, standardDeviationY);
  }

  /**
   * Sets/Gets the Standard deviation of the gaussian in pixel units.
   *
   * @param standardDeviationX
   * @param standardDeviationY
   * @param standardDeviationZ
   */
  public void setStandardDeviations(double standardDeviationX, double standardDeviationY, double standardDeviationZ) {
    filter.SetStandardDeviations(standardDeviationX, standardDeviationY, standardDeviationZ);
  }

  /**
   * Sets/Gets the Standard deviation of the gaussian in pixel units.
   */
  public void setStandardDeviations(double[] standardDeviations) {
    filter.SetStandardDeviations(standardDeviations);
  }

  /**
   * Sets/Gets the Standard deviation of the gaussian in pixel units.
   */
  public double[] getStandardDeviations() {
    return filter.GetStandardDeviations();
  }

  /**  Description of the Method */
  public void update() {
    try {
      // Push input to VTK pipeline
      vtkImageData inputImageData = VtkImageDataFactory.create(inputImage);

      // update VTK pipeline
//      inputCast.SetInput(inputImageData);
      filter.SetInput(inputImageData);
      filter.Update();

      // Pull output from VTK pipeleine
      vtkImageData outputImageData = filter.GetOutput();
      outputImage = VtkUtil.createImagePlus(outputImageData);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public String getHelpString() {
    return HELP_STRING;
  }
}
