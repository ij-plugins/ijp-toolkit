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
import net.sf.ij_plugins.util.progress.IJProgressBarAdapter;

/**
 * @author Jarek Sacha
 */

public class PeronaMalikAnisotropicDiffusionPlugin implements PlugInFilter {

    protected final String title = "Peron-Malik Anisotropic Diffusion";

    @Override
    public int setup(final String s, final ImagePlus imagePlus) {
        return DOES_8G | DOES_16 | DOES_32 | DOES_STACKS | NO_CHANGES;
    }

    @Override
    public void run(final ImageProcessor ip) {

        final FloatProcessor src = (FloatProcessor) ip.convertToFloat();
        final PeronaMalikAnisotropicDiffusion filter = new PeronaMalikAnisotropicDiffusion();

        // Show options dialog
        final GenericDialog dialog = new GenericDialog(title);
        dialog.addNumericField("k", filter.getK(), 2, 6, "");
        dialog.addNumericField("Mean_square_error", filter.getMeanSquareError(), 2, 8, "");
        dialog.addNumericField("Number_of_iterations", filter.getNumberOfIterations(), 0, 8, "");
        dialog.addNumericField("Time_step", filter.getTimeStep(), 2, 8, "");
        dialog.addCheckbox("Use_big_region_function", filter.isBigRegionFunction());

        dialog.showDialog();

        if (dialog.wasCanceled()) {
            return;
        }

        filter.setK(dialog.getNextNumber());
        filter.setMeanSquareError(dialog.getNextNumber());
        filter.setNumberOfIterations((int) Math.round(dialog.getNextNumber()));
        filter.setTimeStep(dialog.getNextNumber());
        filter.setBigRegionFunction(dialog.getNextBoolean());

        // Filter
        final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
        filter.addProgressListener(progressBarAdapter);
        try {
            final FloatProcessor dest = filter.process(src);
            new ImagePlus(title, dest).show();
        } finally {
            filter.removeProgressListener(progressBarAdapter);
        }
    }

}
