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

import java.io.File;
import java.io.IOException;

import net.sf.ij.io.vtk.VtkDecoder;
import net.sf.ij.io.vtk.VtkImageException;

import vtk.vtkImageData;
import vtk.vtkStructuredPointsWriter;

/**
 *  VTK related utilities.
 *
 * @author
 * @created    September 11, 2002
 * @version    1.0
 */

public class VtkUtil {

  private VtkUtil() { }


  /**
   *  Create Image/J's ImagePlus object from VTK's vtkImageData object. Current
   *  implementation performs the transformation by writing/reading data in VTK
   *  format to/from a temporary file.
   *
   * @param  data                   VTK's vtkImageData object.
   * @return                        Image/J's ImagePlus object.
   * @exception  VtkImageException  Problem reading temporary VTK file.
   * @exception  IOException        Problems creating/accessing/deleting
   *      temporary VTK image data file.
   */
  public static ImagePlus createImagePlus(vtkImageData data)
       throws VtkImageException, IOException {

    File tmpFile = File.createTempFile("ijImageData", ".vtk");
    String tmpFileName = tmpFile.getAbsolutePath();

    vtkStructuredPointsWriter writer = new vtkStructuredPointsWriter();

    writer.SetFileName(tmpFileName);
    writer.SetInput(data);
    writer.SetFileTypeToBinary();
    writer.Write();

    ImagePlus imp = VtkDecoder.open(tmpFileName);

    tmpFile.delete();

    return imp;
  }
}
