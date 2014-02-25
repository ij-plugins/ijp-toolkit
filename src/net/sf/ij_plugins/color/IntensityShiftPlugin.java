/*
 * Image/J Plugins
 * Copyright (C) 2002-2014 Jarek Sacha
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
package net.sf.ij_plugins.color;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * @author Jarek Sacha
 */
public class IntensityShiftPlugin implements PlugInFilter {
    final private static String PLUGIN_NAME = "Intensity shift";
    final private static String ABOUT_COMMAND = "about";
    final private static String ABOUT_MESSAGE =
            "Shifts (wraps around) GRAY8 image intensity. Shift value is assumed to be positive.";

    private static int shift = 128;

    @Override
    public int setup(final String arg, final ImagePlus imp) {
        if (ABOUT_COMMAND.equalsIgnoreCase(arg)) {
            IJ.showMessage("About " + PLUGIN_NAME, ABOUT_MESSAGE);
            return DONE;
        }

        return PlugInFilter.DOES_8G;
    }

    @Override
    public void run(final ImageProcessor ip) {

        final GenericDialog dialog = new GenericDialog(PLUGIN_NAME);
        dialog.addNumericField("Intensity shift", shift, 0);
        dialog.showDialog();

        if (dialog.wasCanceled()) {
            return;
        }

        shift = (int) Math.abs(Math.round(dialog.getNextNumber()));

        final ByteProcessor bp = (ByteProcessor) ip;
        final byte[] pixels = (byte[]) bp.getPixels();
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i] & 0xff;
            pixel += shift;
            pixel %= 256;
            pixels[i] = (byte) (pixel & 0xff);
        }
    }
}
