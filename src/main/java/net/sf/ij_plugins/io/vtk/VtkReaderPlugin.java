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
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import java.io.File;


/**
 * Read image in <a HREF="http://public.kitware.com/VTK/">VTK</a> format.
 *
 * @author Jarek Sacha
 * @see net.sf.ij_plugins.io.vtk.VtkDecoder
 * @since June 18, 2002
 */

public final class VtkReaderPlugin implements PlugIn {

    private static final String DIALOG_CAPTION = "VTK Reader";


    @Override
    public void run(final String arg) {
        final OpenDialog openDialog = new OpenDialog("Open as VTK...", arg);
        if (openDialog.getFileName() == null) {
            return;
        }

        final File file = new File(openDialog.getDirectory(), openDialog.getFileName());
        try {
            IJ.showStatus("Opening VTK image: " + file.getName());
            final long tStart = System.currentTimeMillis();
            final ImagePlus imp = VtkDecoder.open(file);
            final long tStop = System.currentTimeMillis();
            imp.show();
            IJ.showStatus("VTK image loaded in " + (tStop - tStart) + " ms.");
        } catch (final VtkImageException ex) {
            IJ.showMessage(DIALOG_CAPTION, "Error opening image: '"
                    + file.getAbsolutePath() + "'\n" + ex.getMessage());
        }
    }

}
