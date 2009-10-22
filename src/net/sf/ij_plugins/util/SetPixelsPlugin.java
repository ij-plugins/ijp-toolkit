/*
 * Image/J Plugins
 * Copyright (C) 2002-2009 Jarek Sacha
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
    final private static AtomicInteger value = new AtomicInteger(1);

    public int setup(final String arg, final ImagePlus imp) {
        return DOES_8G + ROI_REQUIRED + SNAPSHOT + PARALLELIZE_STACKS;
    }

    public void run(final ImageProcessor ip) {

        final GenericDialog gd = new GenericDialog(TITLE);
        gd.addMessage("Set pixels in current ROI to a specified value [0 to 255].");
        gd.addNumericField("Value:", value.get(), 0);

        gd.showDialog();

        if (gd.wasCanceled()) {
            return;
        }

        value.set(Math.max(0, Math.min(255, (int) Math.round(gd.getNextNumber()))));
        IJ.showStatus("Setting ROI pixels to " + value);

        ByteProcessor bp = (ByteProcessor) ip;
        bp.setColor(value.get());
        bp.fill();
    }
}
