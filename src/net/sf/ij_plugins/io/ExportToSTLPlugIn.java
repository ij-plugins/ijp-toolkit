/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
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

package net.sf.ij_plugins.io;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.SaveDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.IJPluginsException;
import net.sf.ij_plugins.io.ExportToSTL.FileType;
import net.sf.ij_plugins.util.TextUtil;
import net.sf.ij_plugins.util.progress.IJProgressBarAdapter;

import java.io.File;


/**
 * @author Jarek Sacha
 * @since 11/29/10 10:49 PM
 */
public final class ExportToSTLPlugIn implements PlugIn {

    private static final String TITLE = "Export to STL";
    private static FileType fileType = FileType.BINARY;
    private static boolean saveSides = true;


    @Override
    public void run(final String arg) {
        // Get current image
        final ImagePlus imp = IJ.getImage();
        if (imp == null) {
            return;
        }

        // Ask for file name to save to
        final SaveDialog sd = new SaveDialog("Save as STL", imp.getTitle(), ".stl");
        if (sd.getFileName() == null) {
            return;
        }

        //
        final GenericDialog gc = new GenericDialog(TITLE);
        final FileType[] fileTypes = FileType.values();
        final String[] fileTypeStrings = new String[fileTypes.length];
        for (int i = 0; i < fileTypes.length; i++) {
            fileTypeStrings[i] = fileTypes[i].toString().toLowerCase();
        }
        gc.addChoice("File_encoding", fileTypeStrings, fileType.toString().toLowerCase());
        gc.addCheckbox("Save_sides", saveSides);
        gc.showDialog();

        if (gc.wasCanceled()) {
            return;
        }

        fileType = fileTypes[gc.getNextChoiceIndex()];
        saveSides = gc.getNextBoolean();

        // Write to STL
        final File file = new File(sd.getDirectory(), sd.getFileName());
        final ImageProcessor ip = imp.getProcessor();
        final Calibration c = imp.getCalibration();

        final ExportToSTL exporter = new ExportToSTL();
        exporter.addProgressListener(new IJProgressBarAdapter());
        try {
            if (FileType.BINARY == fileType) {
                exporter.writeBinary(file, ip, c.pixelWidth, c.pixelHeight);
            } else {
                exporter.writeASCII(file, ip, c.pixelWidth, c.pixelHeight);
            }
        } catch (final IJPluginsException e) {
            IJ.error(TITLE, e.getMessage() + "\n" + TextUtil.toString(e));
        } finally {
            exporter.removeAllProgressListener();
        }
    }
}
