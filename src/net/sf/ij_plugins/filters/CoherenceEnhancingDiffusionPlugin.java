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
package net.sf.ij_plugins.filters;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.util.DialogUtil;

/**
 * @author Jarek Sacha
 */

public class CoherenceEnhancingDiffusionPlugin implements PlugInFilter {
    private final static String TITLE = "Coherence Enhancing Diffusion";

    @Override
    public int setup(final String s, final ImagePlus imagePlus) {
        return DOES_8G | DOES_16 | DOES_32 | DOES_STACKS | NO_CHANGES;
    }

    @Override
    public void run(final ImageProcessor ip) {
        final FloatProcessor src = (FloatProcessor) ip.convertToFloat();

        final CoherenceEnhancingDiffusionFilter filter = new CoherenceEnhancingDiffusionFilter();
        if (!DialogUtil.showGenericDialog(filter, TITLE)) {
            return;
        }

        final FloatProcessor dest = filter.run(src);

        new ImagePlus(TITLE, dest).show();
    }
}
