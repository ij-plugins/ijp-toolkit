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
package ij_plugins.toolkit.filters;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij_plugins.toolkit.util.IJPUtils;

/**
 * @author Jarek Sacha
 */
public class DirectionalCoherencePlugin implements PlugInFilter {
    private final static String TITLE = "Directional Coherence";
    private static final String DESCRIPTION = "";
    private static final String HELP_URL = "https://github.com/ij-plugins/ijp-toolkit/wiki/Filters";

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
        dialog.addPanel(IJPUtils.createInfoPanel(TITLE, DESCRIPTION));
        dialog.addNumericField("Space_scale", filter.getSpaceScale(), 2, 6, "");
        dialog.addHelp(HELP_URL);

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
