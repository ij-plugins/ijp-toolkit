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
 *  Title: Description: Copyright: Copyright (c) 2002 Company:
 *
 * @author
 * @created    September 11, 2002
 * @version    1.0
 */

public class Util {

  private Util() { }


  /**
   *  Description of the Method
   *
   * @param  data                   Description of the Parameter
   * @return                        Description of the Return Value
   * @exception  VtkImageException  Description of the Exception
   * @exception  IOException        Description of the Exception
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
