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

package ij_plugins.toolkit.grow;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.BrowserLauncher;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij_plugins.toolkit.beans.AbstractModel;
import ij_plugins.toolkit.im3d.grow.SRG;
import ij_plugins.toolkit.im3d.grow.SRG2DVector;
import ij_plugins.toolkit.ui.UIUtils;
import ij_plugins.toolkit.ui.multiregion.MultiRegionManagerModel;
import ij_plugins.toolkit.ui.multiregion.Region;
import ij_plugins.toolkit.ui.multiregion.SubRegion;
import ij_plugins.toolkit.ui.progress.IJProgressBarAdapter;
import ij_plugins.toolkit.util.IJUtils;
import ij_plugins.toolkit.util.TextUtil;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Model for {@link RegionGrowingView}.
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
        final ByteProcessor seeds = createSeedImage(regions, imp.getWidth(), imp.getHeight());
        if (seeds == null) {
            return;
        }

        // Run region growing
        final Result result = runSRG(imp, seeds, numberOfAnimationFramesSM.getNumber().intValue());

        // Display results
        displayResults(regions, result);
    }

    public void actionSeedImage() {
        final ImagePlus imp = UIUtils.getImage();
        if (imp == null) {
            return;
        }

        final List<Region> regions = multiRegionManagerModel.getRegions();
        if (regions.size() < 1) {
            IJ.error(CAPTION, "Cannot create seed image, at least one regions required.");
            return;
        }

        final ByteProcessor seeds = createSeedImage(regions, imp.getWidth(), imp.getHeight());
        if (seeds == null) {
            return;
        }

        seeds.setMinAndMax(0, regions.size());
        new ImagePlus("Seeds", seeds).show();
    }

    public void actionHelp() {
        final String helpLink = RegionGrowingPlugIn.HELP_URL;
        try {
            BrowserLauncher.openURL(helpLink);
        } catch (IOException e) {
            e.printStackTrace();
            IJ.error(CAPTION,
                    "Error opening help link: " + helpLink + "\n" +
                            e.getMessage()
            );
        }
    }

    private ByteProcessor createSeedImage(final List<Region> regions, final int width, final int height) {
        // Prepare seeds
        final ByteProcessor seeds = new ByteProcessor(width, height);
        for (int i = 0; i < regions.size(); ++i) {
            final int nbPoints = markSeedPoints(regions.get(i), seeds, i + 1);
            if (nbPoints < 1) {
                IJ.error(CAPTION, "Cannot create seed image, region '" + regions.get(i).getName() + "' is empty (no unique seeds).");
                return null;
            }
        }

        return seeds;
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
        final List<Color> colors = new ArrayList<>();
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
        IJ.error(CAPTION, message + " " + error.getMessage() + "\n" + TextUtil.toString(error));
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
