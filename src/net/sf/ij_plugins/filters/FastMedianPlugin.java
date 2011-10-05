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
package net.sf.ij_plugins.filters;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.Blitter;
import ij.process.ImageProcessor;

import java.awt.*;

/**
 * @author Jarek Sacha
 */

public class FastMedianPlugin implements ExtendedPlugInFilter, DialogListener {

    private static final int FLAGS = DOES_8G | DOES_16 | DOES_32 | DOES_RGB | KEEP_PREVIEW | PARALLELIZE_STACKS;
    private static final String TITLE = "Fast Median Filter";
    private static final String PREFERENCES_PREFIX = FastMedianPlugin.class.getName();
    private static final String PROPERTYNAME_FILTER_SIZE = "filterSize";

    private int filterSize = 5;

    @Override
    public int setup(final String s, final ImagePlus imagePlus) {
        return FLAGS;
    }


    @Override
    public void run(final ImageProcessor ip) {

        final long start = System.currentTimeMillis();
        process(ip, filterSize);
        final long end = System.currentTimeMillis();

        if (IJ.debugMode) {
            IJ.log("Median filtering completed in " + (end - start) + "ms.");
        }
    }


    @Override
    public int showDialog(final ImagePlus imp, final String command, final PlugInFilterRunner pfr) {
        loadFromIJPref();

        final GenericDialog dialog = new GenericDialog(TITLE);
        dialog.addNumericField("Filter size (n x n)", filterSize, 0, 3, "pixels");
        dialog.addPreviewCheckbox(pfr);
        dialog.addDialogListener(this);

        dialog.showDialog();

        if (dialog.wasCanceled()) {
            return DONE;
        }

        saveToIJPref();
        return IJ.setupDialog(imp, FLAGS);
    }


    @Override
    public void setNPasses(final int nPasses) {
        // ?
    }


    @Override
    public boolean dialogItemChanged(final GenericDialog dialog, final AWTEvent e) {
        filterSize = (int) Math.round(dialog.getNextNumber());
        return filterSize >= 0;
    }


    private void saveToIJPref() {
        Prefs.set(PREFERENCES_PREFIX + "." + PROPERTYNAME_FILTER_SIZE, filterSize);
    }


    private void loadFromIJPref() {
        filterSize = (int) Math.round(Prefs.get(PREFERENCES_PREFIX + "." + PROPERTYNAME_FILTER_SIZE, filterSize));
    }


    private static void process(final ImageProcessor ip, final int filterSize) {
        final ImageProcessor dest = FastMedian.process(ip, filterSize);
        ip.copyBits(dest, 0, 0, Blitter.COPY);
    }
}
