/*
 * IJ-Plugins
 * Copyright (C) 2002-2020 Jarek Sacha
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
 *  Latest release available at https://github.com/ij-plugins/ijp-toolkit/
 */

package net.sf.ij_plugins.util;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jarek Sacha
 * @since Sep 22, 2009 9:54:28 PM
 */
public final class SetPixelsPlugin implements PlugInFilter {

    final private static String TITLE = "Set Pixels";
    private static final String DESCRIPTION = "<html>" +
            "Helper plugin for creation of seed images while performing <br>" +
            "<a href=\"https://github.com/ij-plugins/ijp-toolkit/wiki/Seeded-Region-Growing\">" +
            "Seeded Region Growing</a>." +
            "</html>";
    final private static AtomicInteger value = new AtomicInteger(1);
    final private static int flags = DOES_8G + ROI_REQUIRED + SUPPORTS_MASKING;

    @Override
    public int setup(final String arg, final ImagePlus imp) {
        return IJ.setupDialog(imp, flags);
    }

    @Override
    public void run(final ImageProcessor ip) {

        final GenericDialog gd = new GenericDialog(TITLE);
        gd.addPanel(IJPUtils.createInfoPanel(TITLE, DESCRIPTION));
        gd.addMessage("Set pixels in current ROI to a specified value [0 to 255].");
        gd.addNumericField("Value:", value.get(), 0);

        gd.showDialog();

        if (gd.wasCanceled()) {
            return;
        }

        value.set(Math.max(0, Math.min(255, (int) Math.round(gd.getNextNumber()))));
        IJ.showStatus("Setting ROI pixels to " + value);

        final ByteProcessor bp = (ByteProcessor) ip;
        bp.setColor(value.get());
        bp.fill();
    }
}
