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

import java.io.IOException;

import ij.ImagePlus;
import ij.process.ByteProcessor;

import net.sf.ij.io.vtk.VtkDecoder;
import net.sf.ij.io.vtk.VtkEncoder;

import vtk.vtkImageData;
import vtk.vtkStructuredPointsReader;

/**
 *  Factory creating instances of class vtk.vtkImageData. In general performs
 *  translation of images from ImageJ representatin to VTK representation.
 *
 * @author     Jarek Sacha
 * @created    September 7, 2002
 * @version    1.0
 */

public class VtkImageDataFactory {

  private final static String VTK_TMP_FILE = "tmp.vtk";


  /**  Constructor for the VtkImageDataFactory object */
  public VtkImageDataFactory() { }


  /**
   *  Create a vtkImageData object from ImagePlus object.
   *
   * @param  bp  Description of the Parameter
   * @return     Description of the Return Value
   */
  public static vtkImageData create(ByteProcessor bp) throws IOException {
    // Save ImagePlus in VTK format in a temporary file.
    VtkEncoder.save(VTK_TMP_FILE, new ImagePlus(VTK_TMP_FILE, bp));

    // Read the temporary file using VTK.
    vtkStructuredPointsReader reader = new vtkStructuredPointsReader();
    reader.SetFileName(VTK_TMP_FILE);
    reader.Update();
    reader.CloseVTKFile();

    vtkImageData data = (vtkImageData)reader.GetOutput();

    // Remove the temporary file.
    // TODO:

    // Return reference to the VTK image.
    return data;
  }
}
