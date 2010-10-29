/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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

package net.sf.ij_plugins.io.vtk;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;

import java.io.File;


/**
 * Save image in <a HREF="http://public.kitware.com/VTK/">VTK</a> format. Supported image types:
 * GRAY8, GRAY16, GRAY32.
 *
 * @author Jarek Sacha
 * @see net.sf.ij_plugins.io.vtk.VtkEncoder
 * @since April 28, 2002
 */

public final class VtkWriterPlugin implements PlugIn {

    private static final String DIALOG_CAPTION = "VTK Writer";


    /**
     * Main processing method for the VtkEncoder plugin
     *
     * @param arg If equal "ASCII" file will be saved in text format otherwise in binary format
     *            (MSB).
     */
    @Override
    public void run(final String arg) {

        final ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.showMessage(DIALOG_CAPTION, "No image to save.");
            return;
        }

        final SaveDialog saveDialog = new SaveDialog("Save as VTK", imp.getTitle(), ".vtk");

        if (saveDialog.getFileName() == null) {
            return;
        }

        IJ.showStatus("Saving current image as '" + saveDialog.getFileName() + "'...");
        final String fileName = saveDialog.getDirectory() + File.separator + saveDialog.getFileName();

        try {
            final long tStart = System.currentTimeMillis();
            VtkEncoder.save(fileName, imp, "ASCII".compareToIgnoreCase(arg) == 0);
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

            IJ.showMessage(DIALOG_CAPTION, "Error writing file '" + fileName + "'." + msg);
        }
    }

}
