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
import net.sf.ij.vtk.GaussianSmooth;

/**
 *  Plugin for running anisotropic diffusion filter from VTK.
 *
 * @author   Jarek Sacha
 * @since    September 11, 2002
 * @version  $Revision: 1.1 $
 */

public class Gaussian_Smooth implements PlugIn {

  private static GaussianSmooth vtkFilter;
  private static double[] radiusFactors;
  private static double[] standardDeviations;

  /**  Constructor for the Anisotropic_Diffusion object */
  public Gaussian_Smooth() {
  }


  public void run(String arg) {
    // Get current image
    ImagePlus imp = WindowManager.getCurrentImage();
    if (imp == null) {
      IJ.noImage();
      return;
    }

    // Allocate Anisotropic Diffusion filter
    if (vtkFilter == null)
      vtkFilter = new GaussianSmooth();
    if (radiusFactors == null)
      radiusFactors = vtkFilter.getRadiusFactors();
    if (standardDeviations == null)
      standardDeviations = vtkFilter.getStandardDeviations();

    int dimensionality = imp.getStackSize() > 1 ? 3 : 2;

    GenericDialog dialog = new GenericDialog("Gaussian Smooth Options");
    dialog.addNumericField("Radius factor X", radiusFactors[0], 3);
    dialog.addNumericField("Radius factor Y", radiusFactors[1], 3);
    if (dimensionality == 3) {
      dialog.addNumericField("Radius factor Z", radiusFactors[2], 3);
    }
    dialog.addNumericField("Standard deviation X", standardDeviations[0], 3);
    dialog.addNumericField("Standard deviation Y", standardDeviations[1], 3);
    if (dimensionality == 3) {
      dialog.addNumericField("Standard deviation X", standardDeviations[2], 3);
    }
    dialog.addTextAreas(vtkFilter.getHelpString(), null, 7, 40);
    dialog.showDialog();

    while (dialog.invalidNumber() && !dialog.wasCanceled()) {
      IJ.showMessage("Error", "At least one of the fields is not a valid number.");
      dialog.show();
    }

    if (dialog.wasCanceled()) {
      return;
    }

    radiusFactors[0] = dialog.getNextNumber();
    radiusFactors[1] = dialog.getNextNumber();
    if (dimensionality == 3) {
      radiusFactors[2] = dialog.getNextNumber();
    }
    standardDeviations[0] = dialog.getNextNumber();
    standardDeviations[1] = dialog.getNextNumber();
    if (dimensionality == 3) {
      standardDeviations[2] = dialog.getNextNumber();
    }

    vtkFilter.setInput(imp);
    vtkFilter.setDimensionality(dimensionality);
    vtkFilter.setRadiusFactors(radiusFactors);
    vtkFilter.setStandardDeviations(standardDeviations);

    vtkFilter.update();

    ImagePlus output = vtkFilter.getOutput();

    // Free pipeline memory
    vtkFilter = null;

    output.setTitle(imp.getTitle() + "-Gaussian Smooth");
    output.show();
  }
}
