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
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import net.sf.ij.vtk.AnisotropicDiffusion2D;

/**
 *  Plugin for running anisotropic diffusion filter from VTK.
 *
 * @author
 * @created    September 11, 2002
 * @version    1.0
 */

public class Anisotropic_Diffusion implements PlugInFilter {

  private static AnisotropicDiffusion2D adFilter;
  private static String vtkLoadError = null;


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
    return PlugInFilter.DOES_8G | PlugInFilter.NO_CHANGES;
  }


  /**
   *  Main processing method for the Anisotropic_Diffusion object
   *
   * @param  ip  Description of the Parameter
   */
  public void run(ImageProcessor ip) {
    if (vtkLoadError != null) {
      IJ.showMessage("VTK setup error",
          "Unable to load VTK libraries.\n" + vtkLoadError + "\n" +
          "To use this plugin you need to install VTK with Java bindings from www.vtk.org.");
      return;
    }

    if (adFilter == null) {
      adFilter = new AnisotropicDiffusion2D();
    }

    GenericDialog dialog = new GenericDialog("Anisotropic Difusion Options");
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
    adFilter.setInput((ByteProcessor) ip);

    adFilter.update();

    ByteProcessor output = adFilter.getOutput();
    new ImagePlus("Anisotropic Diffusion", output).show();
  }

  static {
    // Load VTK libraries
    try {
      System.loadLibrary("vtkCommonJava");
      System.loadLibrary("vtkFilteringJava");
      System.loadLibrary("vtkIOJava");
      System.loadLibrary("vtkImagingJava");
      System.loadLibrary("vtkGraphicsJava");
      System.loadLibrary("vtkRenderingJava");
      vtkLoadError = null;
    }
    catch (UnsatisfiedLinkError ex) {
      vtkLoadError = ex.toString();
    }
  }
}
