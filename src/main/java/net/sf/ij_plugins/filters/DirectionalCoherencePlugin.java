/*
 * IJ-Plugins
 * Copyright (C) 2002-2016 Jarek Sacha
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
 *  Latest release available at http://sourceforge.net/projects/ij-plugins/
 */
package net.sf.ij_plugins.filters;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * @author Jarek Sacha
 */
public class DirectionalCoherencePlugin implements PlugInFilter {
    private final static String TITLE = "Directional Coherence";

    @Override
    public int setup(final String s, final ImagePlus imagePlus) {
        return DOES_8G | DOES_16 | DOES_32 | DOES_STACKS | NO_CHANGES;
    }

    @Override
    public void run(final ImageProcessor ip) {
        final FloatProcessor src = (FloatProcessor) ip.convertToFloat();

        // Show options dialog
        final DirectionalCoherenceFilter filter = new DirectionalCoherenceFilter();
        final GenericDialog dialog = new GenericDialog(TITLE);
        dialog.addNumericField("Space_scale", filter.getSpaceScale(), 2, 6, "");
        dialog.showDialog();

        if (dialog.wasCanceled()) {
            return;
        }

        // Filter
        filter.setSpaceScale(dialog.getNextNumber());
        final FloatProcessor dest = filter.run(src);
        new ImagePlus(TITLE, dest).show();
    }

}
