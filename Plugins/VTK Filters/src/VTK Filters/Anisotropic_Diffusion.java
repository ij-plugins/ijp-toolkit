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
import ij.plugin.filter.PlugInFilter;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import net.sf.ij.vtk.AnisotropicDiffusion;

/**
 *  Plugin for running anisotropic diffusion filter from VTK.
 *
 * @author   Jarek Sacha
 * @since    September 11, 2002
 * @version  $Revision: 1.5 $
 */

public class Anisotropic_Diffusion implements PlugIn {

  private static AnisotropicDiffusion adFilter;


  /**  Constructor for the Anisotropic_Diffusion object */
  public Anisotropic_Diffusion() { }


  /**
   *  Plugin setup
   *
   * @param  arg  Description of the Parameter
   * @param  imp  Description of the Parameter
   * @return      Description of the Return Value
   */
  public int setup(String arg, ImagePlus imp) {
    return PlugInFilter.DOES_8G | PlugInFilter.DOES_16 |PlugInFilter.NO_CHANGES;
  }


  /**
   *  Main processing method for the Anisotropic_Diffusion object
   *
   * @param  ip  Description of the Parameter
   */
  public void run(ImageProcessor ip) {
//    if (vtkLoadError != null) {
//      IJ.showMessage("VTK setup error",
//          "Unable to load VTK libraries.\n" + vtkLoadError + "\n" +
//          "To use this plugin you need to install VTK with Java bindings from www.vtk.org.");
//      return;
//    }

  }

    public void run(String arg) {
        // Get current image
        ImagePlus imp = WindowManager.getCurrentImage();
        if(imp == null) {
            IJ.noImage();
            return;
        }

        // Allocate Anisotropic Diffusion filter
        if (adFilter == null) {
          adFilter = new AnisotropicDiffusion();
        }

        GenericDialog dialog = new GenericDialog("Anisotropic Diffusion Options");
        dialog.addNumericField("Diffusion factor", adFilter.getDiffusionFactor(), 3);
        dialog.addNumericField("Diffusion threshold", adFilter.getDiffusionThreshold(), 3);
        dialog.addNumericField("Number of iterations", adFilter.getNumberOfIterations(), 0);
        dialog.addCheckbox("Gradient magnitude threshold", adFilter.isGradientMagnitudeThreshold());
        dialog.showDialog();

        while (dialog.invalidNumber() && !dialog.wasCanceled()) {
          IJ.showMessage("Error",
              "At least one of the fields is not a valid number.");
          dialog.show();
        }

        if (dialog.wasCanceled()) {
          return;
        }

        adFilter.setDiffusionFactor(dialog.getNextNumber());
        adFilter.setDiffusionThreshold(dialog.getNextNumber());
        adFilter.setNumberOfIterations((int) (dialog.getNextNumber() + 0.5));
        adFilter.setGradientMagnitudeThreshold(dialog.getNextBoolean());
        adFilter.setInput(imp);

        adFilter.update();

        ImagePlus output = adFilter.getOutput();
        output.setTitle(imp.getTitle() + "Anisotropic Diffusion");
        output.show();
    }
}
