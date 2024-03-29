/*
 *  IJ-Plugins
 *  Copyright (C) 2002-2021 Jarek Sacha
 *  Author's email: jpsacha at gmail dot com
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
 *  Latest release available at https://github.com/ij-plugins/ijp-toolkit/
 */

package ij_plugins.toolkit.io;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.SaveDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij_plugins.toolkit.IJPluginsException;
import ij_plugins.toolkit.ui.progress.IJProgressBarAdapter;
import ij_plugins.toolkit.util.IJPUtils;
import ij_plugins.toolkit.util.TextUtil;

import java.io.File;


/**
 * @author Jarek Sacha
 * @since 11/29/10 10:49 PM
 */
public final class ExportAsSTLPlugIn implements PlugIn {

    private static final String TITLE = "Export as STL";
    private static final String DESCRIPTION = "<html>" +
            "Interprets intensity in 2D image as a surface height and writes result in " +
            "<a href=\"https://en.wikipedia.org/wiki/STL_(file_format)\">STL format</a>." +
            "<ul>" +
            "  <li>Data can be saved either in <em>binary</em> or <em>ascii</em> (text) format</li>" +
            "  <li>Option <em>Save sides</em> enables generation of the mesh for sides and the bottom</li>" +
            "</ul>" +
            "</html>";
    private static final String HELP_URL = "https://github.com/ij-plugins/ijp-toolkit/wiki/3D-IO";

    private static ExportAsSTL.FileType fileType = ExportAsSTL.FileType.BINARY;
    private static boolean saveSides = true;


    @Override
    public void run(final String arg) {
        // Get current image
        final ImagePlus imp = IJ.getImage();
        if (imp == null) {
            return;
        }

        // Ask for options
        final GenericDialog dialog = new GenericDialog(TITLE);
        dialog.addPanel(IJPUtils.createInfoPanel(TITLE, DESCRIPTION));
        final ExportAsSTL.FileType[] fileTypes = ExportAsSTL.FileType.values();
        final String[] fileTypeStrings = new String[fileTypes.length];
        for (int i = 0; i < fileTypes.length; i++) {
            fileTypeStrings[i] = fileTypes[i].toString().toLowerCase();
        }
        dialog.addChoice("File_encoding", fileTypeStrings, fileType.toString().toLowerCase());
        dialog.addCheckbox("Save_sides", saveSides);
        dialog.addHelp(HELP_URL);
        dialog.showDialog();

        if (dialog.wasCanceled()) {
            return;
        }

        fileType = fileTypes[dialog.getNextChoiceIndex()];
        saveSides = dialog.getNextBoolean();

        // Ask for file name to save to
        final SaveDialog sd = new SaveDialog(TITLE, imp.getTitle(), ".stl");
        if (sd.getFileName() == null) {
            return;
        }

        // Write to STL
        final File file = new File(sd.getDirectory(), sd.getFileName());
        final ImageProcessor ip = imp.getProcessor();
        final Calibration c = imp.getCalibration();

        final ExportAsSTL exporter = new ExportAsSTL();
        exporter.addProgressListener(new IJProgressBarAdapter());
        final Calibration cal = imp.getCalibration();
        try {
            if (ExportAsSTL.FileType.BINARY == fileType) {
                exporter.writeBinary(file, ip, c.pixelWidth, c.pixelHeight, cal.xOrigin, cal.yOrigin, saveSides);
            } else {
                exporter.writeASCII(file, ip, c.pixelWidth, c.pixelHeight, cal.xOrigin, cal.yOrigin, saveSides);
            }
        } catch (final IJPluginsException e) {
            IJ.error(TITLE, e.getMessage() + "\n" + TextUtil.toString(e));
        } finally {
            exporter.removeAllProgressListener();
        }
    }
}
