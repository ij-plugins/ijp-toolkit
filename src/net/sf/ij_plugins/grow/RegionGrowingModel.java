/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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

package net.sf.ij_plugins.grow;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.beans.AbstractModel;
import net.sf.ij_plugins.im3d.grow.SRG;
import net.sf.ij_plugins.im3d.grow.SRG2DVector;
import net.sf.ij_plugins.ui.UIUtils;
import net.sf.ij_plugins.ui.multiregion.MultiRegionManagerModel;
import net.sf.ij_plugins.ui.multiregion.Region;
import net.sf.ij_plugins.ui.multiregion.SubRegion;
import net.sf.ij_plugins.util.IJUtils;
import net.sf.ij_plugins.util.progress.IJProgressBarAdapter;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * Model for {@link net.sf.ij_plugins.grow.RegionGrowingView}.
 *
 * @author Jarek Sacha
 * @since Feb 18, 2008
 */
final class RegionGrowingModel extends AbstractModel {

    private final MultiRegionManagerModel multiRegionManagerModel;
    private final SpinnerNumberModel numberOfAnimationFramesSM = new SpinnerNumberModel(0, 0, 1000, 1);
    private static final String CAPTION = "Region Growing";


    public RegionGrowingModel(final MultiRegionManagerModel multiRegionManagerModel) {
        this.multiRegionManagerModel = multiRegionManagerModel;
    }


    public MultiRegionManagerModel getMultiRegionManagerModel() {
        return multiRegionManagerModel;
    }


    public SpinnerNumberModel getNumberOfAnimationFramesSM() {
        return numberOfAnimationFramesSM;
    }


    public void actionRun() {

        final ImagePlus imp = UIUtils.getImage();
        if (imp == null) {
            return;
        }

        final List<Region> regions = multiRegionManagerModel.getRegions();
        if (regions.size() < 2) {
            IJ.error(CAPTION, "Cannot perform growing, at least two regions required, got " + regions.size() + ".");
            return;
        }

        // Prepare seeds
        final ByteProcessor seeds = new ByteProcessor(imp.getWidth(), imp.getHeight());
        for (int i = 0; i < regions.size(); ++i) {
            int nbPoints = markSeedPoints(regions.get(i), seeds, i + 1);
            if (nbPoints < 1) {
                IJ.error(CAPTION, "Cannot perform growing, region '" + regions.get(i).getName() + "' is empty (no unique seeds).");
                return;
            }
        }

        // Run region growing
        final Result result = runSRG(imp, seeds, numberOfAnimationFramesSM.getNumber().intValue());

        // Display results
        displayResults(regions, result);
    }


    private static Result runSRG(final ImagePlus imp, final ByteProcessor seeds, final int numberOfAnimationFrames) {
        final ImageProcessor ip = imp.getProcessor().duplicate();
        if (ip instanceof ColorProcessor) {
            final ColorProcessor cp = (ColorProcessor) ip;
            final SRG2DVector srg = new SRG2DVector();
            final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
            srg.addProgressListener(progressBarAdapter);
            try {
                srg.setImage(cp);
                srg.setNumberOfAnimationFrames(numberOfAnimationFrames);
                srg.setSeeds(seeds);
                srg.setNumberOfAnimationFrames(numberOfAnimationFrames);
                srg.run();
            } finally {
                srg.removeProgressListener(progressBarAdapter);
            }

            return new Result(srg.getRegionMarkers(), srg.getAnimationStack());

        } else {
            final SRG srg = new SRG();
            final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
            srg.addProgressListener(progressBarAdapter);
            try {
                srg.setImage(ip.convertToFloat());
                srg.setNumberOfAnimationFrames(numberOfAnimationFrames);
                srg.setSeeds(seeds);
                srg.setNumberOfAnimationFrames(numberOfAnimationFrames);
                srg.run();
            } finally {
                srg.removeProgressListener(progressBarAdapter);
            }

            return new Result(srg.getRegionMarkers(), srg.getAnimationStack());
        }
    }


    private static void displayResults(final List<Region> regions, final Result result) {
        final ImagePlus regionsImp = new ImagePlus("ROIs", result.segments);
        regionsImp.show();

        // Add result ROIs to ROI Manager
        final RoiManager roiManager = IJUtils.getRoiManager();
        roiManager.runCommand("Reset");
        final int n = regions.size();
        for (int i = 0; i < n; i++) {
            // Convert segment i to current selection (ROI)
            result.segments.setThreshold(i + 1, i + 1, ImageProcessor.NO_LUT_UPDATE);
            IJ.runPlugIn("ij.plugin.filter.ThresholdToSelection", "");

            // Add current selection to ROI Manager
            roiManager.runCommand("add");

            // Rename to selection match region name
            final int count = roiManager.getCount();
            roiManager.select(count - 1);
            roiManager.runCommand("rename", regions.get(i).getName());
        }

        // Color code segments to match region names
        final List<Color> colors = new ArrayList<Color>();
        colors.add(Color.BLACK);
        for (final Region region : regions) {
            colors.add(region.getColor());
        }
        result.segments.setColorModel(UIUtils.createIndexColorModel(colors));
        regionsImp.updateAndRepaintWindow();

        // Show animation frames
        if (result.animationFrames != null && result.animationFrames.getSize() > 0) {
            result.animationFrames.setColorModel(UIUtils.createIndexColorModel(colors));
            new ImagePlus("Grow animation", result.animationFrames).show();
        }

    }


    private static int markSeedPoints(final Region region, final ByteProcessor seedImage, final int regionId) {

        int seedCount = 0;
        for (final SubRegion subRegion : region.getSubRegions()) {
            final Roi roi = subRegion.getRoi();
            final Rectangle bounds = roi.getBounds();
            for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
                for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
                    if (roi.contains(x, y) && seedImage.get(x, y) == 0) {
                        seedImage.set(x, y, regionId);
                        seedCount++;
                    }
                }
            }
        }

        return seedCount;
    }


    public void showError(final String message, final Throwable error) {
        error.printStackTrace();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintWriter writer = new PrintWriter(out);
        error.printStackTrace(writer);

        IJ.error(CAPTION, message + " " + error.getMessage() + "\n" + out.toString());
    }


    private static final class Result {

        final ByteProcessor segments;
        final ImageStack animationFrames;


        private Result(final ByteProcessor segments, final ImageStack animationFrames) {
            this.segments = segments;
            this.animationFrames = animationFrames;
        }
    }
}
