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
import ij_plugins.toolkit.ui.progress.IJProgressBarAdapter;
import ij_plugins.toolkit.util.IJPUtils;

/**
 * @author Jarek Sacha
 */

public class SRADPlugin implements PlugInFilter {

    private final static String TITLE = "Speckle Reducing Anisotropic Diffusion";
    private static final String DESCRIPTION = "<html>" +
            "<ul>" +
            "<li><em>Diffusion coefficient threshold</em> - When diffusion coefficient is lower than threshold it is set to 0.</li>" +
            "<li><em>Initial coefficient of variation</em> - Speckle coefficient of variation in the observer image, <br>" +
            "for correlated data it should be set to less than 1</li>" +
            "<li><em>Coefficient of variation decay rate</em> - Spackle coefficient of variation decay rate: <br>" +
            "q0(t)=q0*exp(-ro*t), ro < 1</li>" +
            "</ul>" +
            "</html>";
    private static final String HELP_URL = "https://github.com/ij-plugins/ijp-toolkit/wiki/Filters";


    @Override
    public int setup(final String s, final ImagePlus imagePlus) {
        return DOES_8G | DOES_16 | DOES_32 | DOES_STACKS | NO_CHANGES;
    }

    @Override
    public void run(final ImageProcessor ip) {

        final FloatProcessor src = (FloatProcessor) ip.convertToFloat();
        final SRAD filter = new SRAD();
        final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
        filter.addProgressListener(progressBarAdapter);

        // Show options dialog
        final GenericDialog dialog = new GenericDialog(TITLE);
        dialog.addPanel(IJPUtils.createInfoPanel(TITLE, DESCRIPTION));
        dialog.addNumericField("Diffusion coefficient threshold", filter.getCThreshold(), 4, 8, "");
        dialog.addNumericField("Mean square error limit", filter.getMeanSquareError(), 4, 8, "");
        dialog.addNumericField("Max iterations", filter.getNumberOfIterations(), 0, 8, "");
        dialog.addNumericField("Initial coefficient of variation", filter.getQ0(), 4, 8, "");
        dialog.addNumericField("Coefficient of variation decay rate", filter.getRo(), 4, 8, "");
        dialog.addNumericField("Time step", filter.getTimeStep(), 4, 8, "");
        dialog.addHelp(HELP_URL);

        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }

        filter.setCThreshold(dialog.getNextNumber());
        filter.setMeanSquareError(dialog.getNextNumber());
        filter.setNumberOfIterations((int) Math.round(dialog.getNextNumber()));
        filter.setQ0(dialog.getNextNumber());
        filter.setRo(dialog.getNextNumber());
        filter.setTimeStep(dialog.getNextNumber());

        // Filter
        try {
            final FloatProcessor dest = filter.process(src);
            new ImagePlus(TITLE, dest).show();
        } finally {
            filter.removeProgressListener(progressBarAdapter);
        }
    }
}
