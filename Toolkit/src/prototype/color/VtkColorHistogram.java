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
package prototype.color;

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ColorProcessor;
import vtk.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class VtkColorHistogram extends JComponent {
    private vtkPanel renWin;

    public VtkColorHistogram(double sizeFactor, double[][][] bins, Color[][][] binColors) {

        final double BAND_RANGE = 255.0;

        int size = bins.length;

        // Convert 3D bins array to 1D array
        java.util.List centerList = new ArrayList();
        java.util.List colorList = new ArrayList();
        java.util.List radiiList = new ArrayList();
        for (int r = 0; r < bins.length; r++) {
            double binsGB[][] = bins[r];
            for (int g = 0; g < binsGB.length; g++) {
                double binsB[] = binsGB[g];
                for (int b = 0; b < binsB.length; b++) {
                    radiiList.add(new Double(Math.pow(binsB[b], 1. / 3.0)));
                    centerList.add(new double[]{r + 0.5, g + 0.5, b + 0.5});
                    Color color = binColors[r][g][b];
                    colorList.add(new double[]{
                        color.getRed() / BAND_RANGE,
                        color.getGreen() / BAND_RANGE,
                        color.getBlue() / BAND_RANGE
                    });
                }
            }
        }

        Double[] radii = (Double[]) radiiList.toArray(new Double[radiiList.size()]);
        double[][] centers = (double[][]) centerList.toArray(new double[centerList.size()][]);
        double[][] colors = (double[][]) colorList.toArray(new double[colorList.size()][]);

        // Setup VTK rendering panel
        renWin = new vtkPanel();

        // Setup cone rendering pipeline
        vtkSphereSource[] spheres = new vtkSphereSource[centers.length];
        for (int i = 0; i < spheres.length; i++) {
            vtkSphereSource sphere = new vtkSphereSource();
            sphere.SetCenter(centers[i]);
            sphere.SetRadius(radii[i].doubleValue());
            spheres[i] = sphere;
        }

        vtkPolyDataMapper[] sphereMappers = new vtkPolyDataMapper[spheres.length];
        for (int i = 0; i < sphereMappers.length; i++) {
            vtkPolyDataMapper sphereMapper = new vtkPolyDataMapper();
            sphereMapper.SetInput(spheres[i].GetOutput());
            sphereMappers[i] = sphereMapper;

        }

        for (int i = 0; i < sphereMappers.length; ++i) {
            vtkActor sphereActor = new vtkActor();
            sphereActor.GetProperty().SetColor(colors[i]);
//            sphereActor.GetProperty().SetOpacity(0.99);
            sphereActor.SetMapper(sphereMappers[i]);
            renWin.GetRenderer().AddActor(sphereActor);
        }

        createColorBox(size, renWin.GetRenderer());

        renWin.GetRenderer().SetBackground(0.5, 0.5, 0.5);


//        VtkPanelUtil.setSize(renWin, 300, 300);

        // Place renWin in the center of this panel
        setLayout(new BorderLayout());
        add(renWin, BorderLayout.CENTER);
    }

    static private void createColorBox(int size, vtkRenderer ren) {
        createLine(size, ren, new double[]{0, 0, 0}, new double[]{1, 0, 0});
        createLine(size, ren, new double[]{0, 0, 0}, new double[]{0, 1, 0});
        createLine(size, ren, new double[]{0, 0, 0}, new double[]{0, 0, 1});

        createLine(size, ren, new double[]{1, 0, 0}, new double[]{1, 1, 0});
        createLine(size, ren, new double[]{1, 0, 0}, new double[]{1, 0, 1});

        createLine(size, ren, new double[]{0, 1, 0}, new double[]{1, 1, 0});
        createLine(size, ren, new double[]{0, 1, 0}, new double[]{0, 1, 1});


        createLine(size, ren, new double[]{0, 0, 1}, new double[]{0, 1, 1});
        createLine(size, ren, new double[]{0, 0, 1}, new double[]{1, 0, 1});

        createLine(size, ren, new double[]{1, 1, 0}, new double[]{1, 1, 1});
        createLine(size, ren, new double[]{0, 1, 1}, new double[]{1, 1, 1});
        createLine(size, ren, new double[]{1, 0, 1}, new double[]{1, 1, 1});
    }

    static private void createLine(int size, vtkRenderer ren, double[] start, double[] end) {
        for (int i = 0; i < size; ++i) {
            vtkLineSource line = new vtkLineSource();
            double[] s = new double[3];
            double[] e = new double[3];
            double[] c = new double[3];
            for (int j = 0; j < 3; j++) {
                s[j] = (end[j] - start[j]) * i + start[j] * size;
                e[j] = (end[j] - start[j]) * (i + 1) + start[j] * size;
                c[j] = (end[j] - start[j]) / size * (i + 0.5) + start[j];
            }
            line.SetPoint1(s);
            line.SetPoint2(e);
            line.SetResolution(21);
            vtkPolyDataMapper lineMapper = new vtkPolyDataMapper();
            lineMapper.SetInput(line.GetOutput());
            vtkActor lineActor = new vtkActor();
            lineActor.GetProperty().SetColor(c);
            lineActor.SetMapper(lineMapper);
            ren.AddActor(lineActor);
        }
    }

    public vtkPanel getRenWin() {
        return renWin;
    }


    public static void main(String s[]) {

        try {
            // Read test image
            final File imageFile = new File("test_images/clown24.tif");
//            final File imageFile = new File("/home/jarek/PG_projects/WHS2582-162-4.tif");

            // Read test image
            final Opener opener = new Opener();
            final ImagePlus imp = opener.openImage(imageFile.getAbsolutePath());
            if (imp == null) {
                throw new Exception("Cannot open image: " + imageFile.getAbsolutePath());
            }

            if (imp.getType() != ImagePlus.COLOR_RGB) {
                throw new Exception("Expecting color image.");
            }

            int binsPerBand = 8;
            ColorHistogram colorHistogram = new ColorHistogram();
            colorHistogram.setBinsPerBand(binsPerBand);
            colorHistogram.run((ColorProcessor) imp.getProcessor());

            double[][][] bins = colorHistogram.getNormalizedBins();
            Color[][][] binColors = colorHistogram.getBinColors();

            double sizeFactor = binsPerBand;
            VtkColorHistogram panel = new VtkColorHistogram(sizeFactor, bins, binColors);

            JFrame frame = new JFrame("Color Histogram");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add("Center", panel);
            frame.pack();
            frame.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
