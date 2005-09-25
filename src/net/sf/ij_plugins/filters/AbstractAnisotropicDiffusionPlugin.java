/***
 * Image/J Plugins
 * Copyright (C) 2002-2005 Jarek Sacha
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
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.IJPluginsRuntimeException;
import net.sf.ij_plugins.util.progress.IJProgressBarAdapter;
import net.sf.ij_plugins.util.DialogUtil;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.*;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Jarek Sacha
 * @version $ Revision: $
 */

abstract public class AbstractAnisotropicDiffusionPlugin implements PlugInFilter {

    protected final String title;

    protected AbstractAnisotropicDiffusionPlugin(final String title) {
        this.title = title;
    }

    public int setup(String s, ImagePlus imagePlus) {
        return DOES_8G | DOES_16 | DOES_32 | DOES_STACKS | NO_CHANGES;
    }

    public void run(final ImageProcessor ip) {

        final FloatProcessor src = (FloatProcessor) ip.convertToFloat();
        final AbstractAnisotropicDiffusion filter = createFilter();
        final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
        filter.addProgressListener(progressBarAdapter);
        try {

            if (!DialogUtil.showGenericDialog(filter, title)) {
                return;
            }

            long start = System.currentTimeMillis();
            FloatProcessor dest = filter.process(src);
            long end = System.currentTimeMillis();

            new ImagePlus(title, dest).show();
//            IJ.showStatus("Filtering completed in " + (end - start) + "ms.");
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
