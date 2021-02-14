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

package ij_plugins.toolkit.io.metaimage;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij_plugins.toolkit.util.IJPUtils;

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
    private static final String DESCRIPTION = "<html>" +
            "Read image (including stacks) in MetaImage format. <br> " +
            "MetaImage is one of the formats supported by <a href=\"http://www.itk.org\">ITK</a>." +
            "</html>";
    private static final String HELP_URL = "https://github.com/ij-plugins/ijp-toolkit/wiki/3D-IO";


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
        optionsDialog.addPanel(IJPUtils.createInfoPanel(TITLE, DESCRIPTION));
        optionsDialog.addCheckbox("Use_virtual_stack", virtual);
        optionsDialog.addHelp(HELP_URL);
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
