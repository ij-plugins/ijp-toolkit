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
package net.sf.ij.vtk;

import vtk.*;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.2 $
 */
public class RGBColorHistogramComponent extends JComponent {
    private vtkPanel renWin;

    public RGBColorHistogramComponent(int binsPerBand, BinProperty[] binProperties) {
        // Setup VTK rendering panel
        renWin = new vtkPanel();

        // Setup cone rendering pipeline
        vtkSphereSource[] spheres = new vtkSphereSource[binProperties.length];
        for (int i = 0; i < spheres.length; i++) {
            vtkSphereSource sphere = new vtkSphereSource();
            BinProperty binProperty = binProperties[i];
            sphere.SetCenter(binProperty.getLocation());
            sphere.SetRadius(binProperty.getRelativeSize());
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
            sphereActor.GetProperty().SetColor(binProperties[i].getVTKColor());
//            sphereActor.GetProperty().SetOpacity(0.99);
            sphereActor.SetMapper(sphereMappers[i]);
            renWin.GetRenderer().AddActor(sphereActor);
        }

        createColorBox(binsPerBand, renWin.GetRenderer());

        renWin.GetRenderer().SetBackground(0.5, 0.5, 0.5);


//        VtkPanelUtil.setSize(renWin, 300, 300);

        // Place renWin in the center of this panel
        setLayout(new BorderLayout());
        add(renWin, BorderLayout.CENTER);
    }


    static private void createColorBox(int size, vtkRenderer ren) {
        final double max = BinProperty.COLOR_RANGE;
        // FIX correct size
        size = 1;
        createLine(size, ren, new double[]{0, 0, 0}, new double[]{max, 0, 0});
        createLine(size, ren, new double[]{0, 0, 0}, new double[]{0, max, 0});
        createLine(size, ren, new double[]{0, 0, 0}, new double[]{0, 0, max});

        createLine(size, ren, new double[]{max, 0, 0}, new double[]{max, max, 0});
        createLine(size, ren, new double[]{max, 0, 0}, new double[]{max, 0, max});

        createLine(size, ren, new double[]{0, max, 0}, new double[]{max, max, 0});
        createLine(size, ren, new double[]{0, max, 0}, new double[]{0, max, max});


        createLine(size, ren, new double[]{0, 0, max}, new double[]{0, max, max});
        createLine(size, ren, new double[]{0, 0, max}, new double[]{max, 0, max});

        createLine(size, ren, new double[]{max, max, 0}, new double[]{max, max, max});
        createLine(size, ren, new double[]{0, max, max}, new double[]{max, max, max});
        createLine(size, ren, new double[]{max, 0, max}, new double[]{max, max, max});
    }

    static private void createLine(int size, vtkRenderer ren, double[] start, double[] end) {
        for (int i = 0; i < size; ++i) {
            vtkLineSource line = new vtkLineSource();
            double[] s = new double[3];
            double[] e = new double[3];
            double[] color = new double[3];
            for (int j = 0; j < 3; j++) {
                s[j] = (end[j] - start[j]) * i + start[j] * size;
                e[j] = (end[j] - start[j]) * (i + 1) + start[j] * size;
                color[j] = ((end[j] - start[j]) / size * (i + 0.5) + start[j]) / size;
            }
            line.SetPoint1(s);
            line.SetPoint2(e);
            line.SetResolution(21);
            vtkPolyDataMapper lineMapper = new vtkPolyDataMapper();
            lineMapper.SetInput(line.GetOutput());
            vtkActor lineActor = new vtkActor();
            lineActor.GetProperty().SetColor(color);
            lineActor.SetMapper(lineMapper);
            ren.AddActor(lineActor);
        }
    }

    public static class BinProperty {
        final static private double COLOR_RANGE = 255.0;
        double relativeSize;
        Color color;
        double[] location;

        public BinProperty(double relativeSize, Color color, double[] location) {
            // TODO: clone of mutable objects
            this.relativeSize = relativeSize;
            this.color = color;
            this.location = location;
        }

        public double getRelativeSize() {
            return relativeSize;
        }

        public Color getColor() {
            return color;
        }

        public double[] getLocation() {
            return location;
        }

        public double[] getVTKColor() {
            return new double[]{
                color.getRed() / COLOR_RANGE,
                color.getGreen() / COLOR_RANGE,
                color.getBlue() / COLOR_RANGE};
        }
    }
}
