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

/**
 *  Plugin for running anisotropic diffusion filter from VTK.
 *
 * @author   Jarek Sacha
 * @since    September 11, 2002
 * @version  $Revision: 1.8 $
 */

public class Anisotropic_Diffusion implements PlugIn {

  private static AnisotropicDiffusion vtkFilter;
  private static double diffusionFactor = 1;
  private static double diffusionThreshold = 128;
  private static int numberOfIterations = 3;
  private static boolean useGradientMagnitudeThreshold = false;

  /**  Constructor for the Anisotropic_Diffusion object */
  public Anisotropic_Diffusion() {
  }


  public void run(String arg) {
    // Get current image
    ImagePlus imp = WindowManager.getCurrentImage();
    if (imp == null) {
      IJ.noImage();
      return;
    }

    // Allocate Anisotropic Diffusion filter
    if (vtkFilter == null) {
      vtkFilter = new AnisotropicDiffusion();
    }

    GenericDialog dialog = new GenericDialog("Anisotropic Diffusion Options");
    dialog.addNumericField("Diffusion factor", diffusionFactor, 3);
    dialog.addNumericField("Diffusion threshold", diffusionThreshold, 0);
    dialog.addNumericField("Number of iterations", numberOfIterations, 0);
    dialog.addCheckbox("Gradient magnitude threshold", useGradientMagnitudeThreshold);
    dialog.addTextAreas(vtkFilter.getHelpString(), null, 5, 40);
    dialog.showDialog();

    while (dialog.invalidNumber() && !dialog.wasCanceled()) {
      IJ.showMessage("Error", "At least one of the fields is not a valid number.");
      dialog.show();
    }

    if (dialog.wasCanceled()) {
      return;
    }

    diffusionFactor = dialog.getNextNumber();
    diffusionThreshold = dialog.getNextNumber();
    numberOfIterations = ((int) (dialog.getNextNumber() + 0.5));
    useGradientMagnitudeThreshold = dialog.getNextBoolean();

    vtkFilter.setInput(imp);
    vtkFilter.setDiffusionFactor(diffusionFactor);
    vtkFilter.setDiffusionThreshold(diffusionThreshold);
    vtkFilter.setNumberOfIterations(numberOfIterations);
    vtkFilter.setGradientMagnitudeThreshold(useGradientMagnitudeThreshold);

    vtkFilter.update();

    ImagePlus output = vtkFilter.getOutput();

    // Free pipeline memory
    vtkFilter = null;

    output.setTitle(imp.getTitle() + "-Anisotropic Diffusion");
    output.show();
  }
}
