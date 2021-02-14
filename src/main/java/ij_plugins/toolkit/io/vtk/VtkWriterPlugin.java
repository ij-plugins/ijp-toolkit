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

package ij_plugins.toolkit.io.vtk;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;
import ij_plugins.toolkit.util.IJPUtils;

import java.io.File;


/**
 * Save image in <a HREF="http://public.kitware.com/VTK/">VTK</a> format. Supported image types:
 * GRAY8, GRAY16, GRAY32.
 *
 * @author Jarek Sacha
 * @see VtkEncoder
 * @since April 28, 2002
 */

public final class VtkWriterPlugin implements PlugIn {

    private static final String TITLE = "VTK Writer";
    private static final String DESCRIPTION = "<html>" +
            "Writes a 2D image or a 3D stack in format used by <a href=\"http://www.vtk.org\">VTK</a>. <br>" +
            "When <strong>Save as ASCII </strong> is selected the image is saved in text format, <br> " +
            "when not selected the image is saved in binary format (MSB)." +
            "</html>";
    private static final String HELP_URL = "https://github.com/ij-plugins/ijp-toolkit/wiki/3D-IO";
    private static boolean saveAsAscii = false;


    @Override
    public void run(final String arg) {

        final ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.showMessage(TITLE, "No image to save.");
            return;
        }

        final SaveDialog saveDialog = new SaveDialog("Save as VTK", imp.getTitle(), ".vtk");
        if (saveDialog.getFileName() == null) {
            return;
        }

        final GenericDialog dialog = new GenericDialog(TITLE);
        dialog.addPanel(IJPUtils.createInfoPanel(TITLE, DESCRIPTION));
        dialog.addCheckbox("Save_as_ASCII", saveAsAscii);
        dialog.addHelp(HELP_URL);
        dialog.showDialog();

        if (dialog.wasCanceled()) {
            return;
        }

        saveAsAscii = dialog.getNextBoolean();

        IJ.showStatus("Saving current image as '" + saveDialog.getFileName() + "'...");
        final String fileName = saveDialog.getDirectory() + File.separator + saveDialog.getFileName();

        try {
            final long tStart = System.currentTimeMillis();
            VtkEncoder.save(fileName, imp, saveAsAscii);
            final long tStop = System.currentTimeMillis();
            IJ.showStatus("Saving of '" + saveDialog.getFileName() + "' completed in " + (tStop - tStart) + " ms.");
        } catch (final Exception ex) {
            ex.printStackTrace();
            String msg = ex.getMessage();
            if (msg == null) {
                msg = "";
            } else {
                msg = "\n" + msg;
            }

            IJ.showMessage(TITLE, "Error writing file '" + fileName + "'." + msg);
        }
    }

}
