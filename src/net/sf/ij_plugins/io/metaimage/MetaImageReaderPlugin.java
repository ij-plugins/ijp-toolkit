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

package net.sf.ij_plugins.io.metaimage;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import java.io.File;


/**
 * Read image (including stacks) in MetaImage format. MetaImage is one of the formats supported by
 * ITK (http://www.itk.org). More information about MetaImage, including C++ code, can be found
 * http://caddlab.rad.unc.edu/technologies/MetaImage/ . This implementation is intended to be
 * compatible with ITK version of MetaImage.
 *
 * @author Jarek Sacha
 * @since June 18, 2002
 */

public final class MetaImageReaderPlugin implements PlugIn {

    private static final String TITLE = "MetaImage Reader";
    private static boolean virtual = false;


    @Override
    public void run(final String arg) {

        // Get file name
        final OpenDialog openDialog = new OpenDialog("Open as MetaImage...", arg);
        if (openDialog.getFileName() == null) {
            return;
        }

        // Get options (only virtual stack at the moment)
        final GenericDialog optionsDialog = new GenericDialog(TITLE);
        optionsDialog.addCheckbox("Use_virtual_stack", virtual);
        optionsDialog.showDialog();
        if (optionsDialog.wasCanceled()) {
            return;
        }
        virtual = optionsDialog.getNextBoolean();

        final File file = new File(openDialog.getDirectory(), openDialog.getFileName());
        try {
            IJ.showStatus("Opening MetaImage: " + file.getName());
            final long tStart = System.currentTimeMillis();
            final ImagePlus[] imps = MiDecoder.open(file, virtual);
            final long tStop = System.currentTimeMillis();
            for (final ImagePlus imp : imps) {
                imp.show();
            }
            IJ.showStatus("MetaImage loaded in " + (tStop - tStart) + " ms.");
        } catch (final MiException ex) {
            ex.printStackTrace();
            IJ.error(TITLE, "Error opening image: '"
                    + file.getAbsolutePath() + "'\n" + ex.getMessage());
        }
    }
}
