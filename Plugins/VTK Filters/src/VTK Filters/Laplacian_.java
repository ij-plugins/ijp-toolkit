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

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import net.sf.ij.vtk.AnisotropicDiffusion;
import net.sf.ij.vtk.Laplacian;

/**
 *  Plugin for running anisotropic diffusion filter from VTK.
 *
 * @author   Jarek Sacha
 * @since    September 11, 2002
 * @version  $Revision: 1.2 $
 */

public class Laplacian_ implements PlugIn {

  private static Laplacian vtkFilter;


  public void run(String arg) {
    // Get current image
    ImagePlus imp = WindowManager.getCurrentImage();
    if (imp == null) {
      IJ.noImage();
      return;
    }

    // Allocate Anisotropic Diffusion filter
    if (vtkFilter == null) {
      vtkFilter = new Laplacian();
    }

    vtkFilter.setInput(imp);

    vtkFilter.update();

    ImagePlus output = vtkFilter.getOutput();

    // Free pipeline memory
    vtkFilter = null;

    output.setTitle(imp.getTitle() + "-Laplacian");
    output.show();
  }
}
