/*
 * IJ-Plugins
 * Copyright (C) 2002-2016 Jarek Sacha
 * Author's email: jpsacha at gmail dot com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Latest release available at http://sourceforge.net/projects/ij-plugins/
 */

package net.sf.ij_plugins.color;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import static ij.measure.Measurements.*;

/**
 * Measure bands of color (RGB) image or slices in a stack recording results in a single row.
 * If image is 8-bit gray, 16-bit gray, or 32-bit gray,
 * each slice is measured and results reported in columns of the same row.
 * If image is an RGB then only the current slice (if more than one) is measured.
 * Column names are the names measurements prepended with slice labels.
 *
 * @author Jarek Sacha
 * @since 4/10/12 8:04 PM
 */
public final class MeasureBandsPlugin implements PlugIn {

    private static final String TITLE = "Measure Bands";
    private static final ResultsTable resultsTable = new ResultsTable();

    private static final String ABOUT_MESSAGE = "" +
            "Measure bands of color (RGB) image or slices in a stack recording results in a single row.\n" +
            "If image is 8-bit gray, 16-bit gray, or 32-bit gray, each slice is measured and results \n" +
            "reported in columns of the same row. \n" +
            "If image is an RGB then only the current slice (if more than one) is measured. \n" +
            "Column names are the names measurements prepended with slice labels. \n" +
            " \n" +
            "Reported measurements can be selected using Analyze > Set Measurements. \n" +
            "Supported measurements include:\n" +
            "  area (ROI), \n" +
            "  centroid (ROI), \n" +
            "  mean, \n" +
            "  standard deviation, \n" +
            "  mode, \n" +
            "  min, max, \n" +
            "  median, \n" +
            "  skewness, and \n" +
            "  kurtosis.";

    private int measurements = AREA + MEDIAN;


    @Override
    public void run(String arg) {
        if ("about".equalsIgnoreCase(arg)) {
            IJ.showMessage("About " + TITLE, ABOUT_MESSAGE);
            return;
        }

        // Check if an image is opened.
        final ImagePlus imp = IJ.getImage();
        if (imp == null) {
            return;
        }

        // Verify if of correct type
        final int type = imp.getType();
        switch (type) {
            case ImagePlus.GRAY8:
            case ImagePlus.GRAY16:
            case ImagePlus.GRAY32:
            case ImagePlus.COLOR_RGB:
                break;
            default:
                IJ.error(TITLE, "Unsupported image type, expecting GRAY8, GRAY16, GRAY32, or COLOR_RGB.");
                return;
        }

        // Check current "Set Measurements..."
        measurements = Analyzer.getMeasurements();

        // Record image title
        resultsTable.incrementCounter();
        resultsTable.addLabel(imp.getTitle());

        // Record ROI measurements
        {
            final ImageStatistics stats = imp.getStatistics(measurements);
            if (match(AREA)) resultsTable.addValue("Area", stats.area);
            if (match(CENTROID)) {
                resultsTable.addValue("X", stats.xCentroid);
                resultsTable.addValue("Y", stats.xCentroid);
            }
        }

        // In case of RGB image, use current slice but convert it to a RGB stack for measurement.
        final ImageStack stack = type == ImagePlus.COLOR_RGB
                ? ColorProcessorUtils.toStack((ColorProcessor) imp.getProcessor())
                : imp.getStack();
        final ImageProcessor mask = imp.getMask();
        final Roi roi = imp.getRoi();
        final Calibration calibration = imp.getCalibration();
        for (int i = 1; i <= stack.getSize(); i++) {
            final ImageProcessor ip = stack.getProcessor(i);
            // Transfer ROI
            if (mask != null) {
                ip.setMask(mask);
                if (roi != null) {
                    ip.setRoi(roi.getBounds());
                }
            } else {
                ip.setRoi(roi);
            }
            // Measure
            final ImageStatistics stats = ImageStatistics.getStatistics(ip, measurements, calibration);
            // Record results
            final String label = stack.getSliceLabel(i);
            if (match(MEAN)) resultsTable.addValue(label + " mean", stats.mean);
            if (match(STD_DEV)) resultsTable.addValue(label + " stdDev", stats.stdDev);
            if (match(MODE)) resultsTable.addValue(label + " mode", stats.mode);
            if (match(MIN_MAX)) {
                resultsTable.addValue(label + " min", stats.min);
                resultsTable.addValue(label + " max", stats.max);
            }
            if (match(MEDIAN)) resultsTable.addValue(label + " median", stats.median);
            if (match(SKEWNESS)) resultsTable.addValue(label + " skewness", stats.skewness);
            if (match(KURTOSIS)) resultsTable.addValue(label + " kurtosis", stats.kurtosis);
        }

        resultsTable.show(TITLE + " Results");
    }

    private boolean match(int v) {
        return (measurements & v) != 0;
    }
}
