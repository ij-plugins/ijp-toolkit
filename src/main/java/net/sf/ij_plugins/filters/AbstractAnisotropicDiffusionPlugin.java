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

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.util.DialogUtil;
import net.sf.ij_plugins.util.progress.IJProgressBarAdapter;

/**
 * Helper class for creating anisotropic diffusion plugins.
 *
 * @author Jarek Sacha
 */

abstract public class AbstractAnisotropicDiffusionPlugin implements PlugInFilter {

    protected final String title;

    protected AbstractAnisotropicDiffusionPlugin(final String title) {
        this.title = title;
    }

    @Override
    public int setup(final String s, final ImagePlus imagePlus) {
        return DOES_8G | DOES_16 | DOES_32 | DOES_STACKS | NO_CHANGES;
    }

    @Override
    public void run(final ImageProcessor ip) {

        final FloatProcessor src = (FloatProcessor) ip.convertToFloat();
        final AbstractAnisotropicDiffusion filter = createFilter();
        final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
        filter.addProgressListener(progressBarAdapter);
        try {

            if (!DialogUtil.showGenericDialog(filter, title)) {
                return;
            }

            final FloatProcessor dest = filter.process(src);

            new ImagePlus(title, dest).show();
        } finally {
            filter.removeProgressListener(progressBarAdapter);
        }
    }

    /**
     * Create instance of the filter wrapped by this plugin.
     *
     * @return filter used by this plugin.
     */
    abstract AbstractAnisotropicDiffusion createFilter();

}
