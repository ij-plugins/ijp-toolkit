/***
 * Image/J Plugins
 * Copyright (C) 2002-2004 Jarek Sacha
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
package net.sf.ij.plugin;

import ij.ImagePlus;
import ij.IJ;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import net.sf.ij.color.ColorHistogram;
import net.sf.ij.vtk.RGBColorHistogramComponent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import vtk.vtkVersion;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class VtkColorHistogram implements PlugInFilter {
    static {
      System.loadLibrary("vtkCommonJava");
      System.loadLibrary("vtkFilteringJava");
      System.loadLibrary("vtkIOJava");
      System.loadLibrary("vtkImagingJava");
      System.loadLibrary("vtkGraphicsJava");
      System.loadLibrary("vtkRenderingJava");
      try {
        System.loadLibrary("vtkHybridJava");
      } catch (Throwable e) {
        System.out.println("cannot load vtkHybrid, skipping...");
      }
    }

    private static Config config = new Config();
    private String imageShortTitle;

    public int setup(String arg, ImagePlus imp) {

        //TODO: Check for presence of VTK
        try {
            IJ.showStatus("Looking for VTK libraries...");
            vtkVersion version = new vtkVersion();
            IJ.showStatus("Using VTK version: "+version.GetVTKVersion());

            if(imp!=null) {
            imageShortTitle = imp.getShortTitle();
            }
            return PlugInFilter.DOES_RGB;
        } catch (Throwable t) {
            throw new RuntimeException("Unable to locate supported VTK library.\n"+
                    "For more information on using this plugins see Help/About Plugins/Color", t);
        }
    }

    public void run(ImageProcessor ip) {
        ColorProcessor cp = (ColorProcessor) ip;

        // TODO: Update configuration

        // Create histogram
        final int nbBins = config.getBinsPerBand();
        final ColorHistogram colorHistogram = new ColorHistogram();
        colorHistogram.setBinsPerBand(config.getBinsPerBand());
        colorHistogram.run(cp);

        final double[][][] bins = colorHistogram.getNormalizedBins();
        final Color[][][] binColors = colorHistogram.getBinColors();

        // Convert 3D bins array to 1D array
        java.util.List binPropertiesList = new ArrayList();
        for (int r = 0; r < bins.length; r++) {
            double binsGB[][] = bins[r];
            for (int g = 0; g < binsGB.length; g++) {
                double binsB[] = binsGB[g];
                for (int b = 0; b < binsB.length; b++) {
                    double relativeSize = Math.pow(binsB[b], 1. / 3.0);
                    double[] center = new double[]{r + 0.5, g + 0.5, b + 0.5};
                    Color color = binColors[r][g][b];
                    binPropertiesList.add(
                            new RGBColorHistogramComponent.BinProperty(
                                    relativeSize, color, center));
                }
            }
        }
        RGBColorHistogramComponent.BinProperty[] binProperties = (RGBColorHistogramComponent.BinProperty[]) binPropertiesList.toArray(new RGBColorHistogramComponent.BinProperty[binPropertiesList.size()]);



        // Create color histogram component
        RGBColorHistogramComponent panel = new RGBColorHistogramComponent(config.getBinsPerBand(), binProperties);

        final String title = "Color Histogram "
                + nbBins + "x"+ nbBins + "x"+ nbBins + " - "+imageShortTitle;
        final JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add("Center", panel);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    public static class Config {
        int binsPerBand = 6;
        boolean useBinCentroid = true;

        public int getBinsPerBand() {
            return binsPerBand;
        }

        public void setBinsPerBand(int binsPerBand) {
            this.binsPerBand = binsPerBand;
        }

        public boolean isUseBinCentroid() {
            return useBinCentroid;
        }

        public void setUseBinCentroid(boolean useBinCentroid) {
            this.useBinCentroid = useBinCentroid;
        }
    }

}
