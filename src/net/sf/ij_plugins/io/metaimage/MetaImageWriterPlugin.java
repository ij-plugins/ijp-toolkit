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

package net.sf.ij_plugins.io.metaimage;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.SaveDialog;
import ij.plugin.PlugIn;

import java.io.File;


/**
 * Write image (including stacks) in MetaImage format. MetaImage is one of the formats supported by
 * ITK (http://www.itk.org). More information about MetaImage, including C++ code, can be found at
 * http://caddlab.rad.unc.edu/technologies/MetaImage/ . This implementation is intended to be
 * compatible with ITK version of MetaImage.
 *
 * @author Jarek Sacha
 * @since June 18, 2002
 */

public final class MetaImageWriterPlugin implements PlugIn {

    private static final String TITLE = "MetaImage Writer";


    @Override
    public void run(final String arg) {
        // Get current image
        final ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        if (imp.getType() == ImagePlus.COLOR_256 || imp.getType() == ImagePlus.COLOR_RGB) {
            IJ.error(TITLE, "COLOR_256 and COLOR_RGB images are not supported.");
            return;
        }


        // Get file name
        final SaveDialog saveDialog = new SaveDialog(TITLE, imp.getTitle(), ".mha");
        if (saveDialog.getFileName() == null) {
            return;
        }

        final File file = new File(saveDialog.getDirectory(), saveDialog.getFileName());
        try {
            MiEncoder.write(imp, file.getAbsolutePath(), true);
        } catch (final MiException ex) {
            ex.printStackTrace();
            IJ.error(TITLE, ex.getMessage());
            return;
        }

        IJ.showStatus("MetaImage " + saveDialog.getFileName() + " saved.");
    }
}
