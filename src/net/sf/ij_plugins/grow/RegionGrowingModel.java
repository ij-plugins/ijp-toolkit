/***
 * Image/J Plugins
 * Copyright (C) 2002-2008 Jarek Sacha
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
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.beans.AbstractModel;
import net.sf.ij_plugins.im3d.grow.SRG;
import net.sf.ij_plugins.ui.UIUtils;
import net.sf.ij_plugins.ui.multiregion.MultiRegionManagerModel;
import net.sf.ij_plugins.ui.multiregion.Region;
import net.sf.ij_plugins.ui.multiregion.SubRegion;
import net.sf.ij_plugins.util.progress.IJProgressBarAdapter;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Jarek Sacha
 * @since Feb 18, 2008
 */
final class RegionGrowingModel extends AbstractModel {

    private final MultiRegionManagerModel multiRegionManagerModel;


    public RegionGrowingModel(final MultiRegionManagerModel multiRegionManagerModel) {
        this.multiRegionManagerModel = multiRegionManagerModel;
    }


    public MultiRegionManagerModel getMultiRegionManagerModel() {
        return multiRegionManagerModel;
    }


    public void actionRun() {
        final String title = "Region Growing";

        final ImagePlus imp = UIUtils.getImage();
        if (imp == null) {
            return;
        }

        final List<Region> regions = multiRegionManagerModel.getRegions();
        if (regions.size() < 2) {
            IJ.error(title, "Cannot perform growing, at least two regions required, got " + regions.size() + ".");
            return;
        }

        // Prepare seeds
        final Point[][] seeds = new Point[regions.size()][];
        for (int i = 0; i < regions.size(); ++i) {
            final Point[] points = extractPoints(regions.get(i));
            if (points.length < 1) {
                IJ.error(title, "Cannot perform growing, region '" + regions.get(i).getName() + "' is empty (no seeds).");
                return;
            }
            seeds[i] = points;
        }

        // Run region growing
        final ByteProcessor segments = runSRG(imp, seeds);

        // Display results
        displayResults(regions, segments);
    }


    private ByteProcessor runSRG(ImagePlus imp, Point[][] seeds) {
        final SRG srg = new SRG();
        final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
        srg.addProgressListener(progressBarAdapter);
        try {
            srg.setImage((ByteProcessor) imp.getProcessor().duplicate().convertToByte(true));
            srg.setNumberOfAnimationFrames(0);
            srg.setSeeds(seeds);
            srg.run();
        } finally {
            srg.removeProgressListener(progressBarAdapter);
        }

        return srg.getRegionMarkers();
    }


    private void displayResults(List<Region> regions, ByteProcessor segments) {
        final ImagePlus regionsImp = new ImagePlus("ROIs", segments);
        regionsImp.show();

        // Add result ROIs to ROI Manager
        final RoiManager roiManager = new RoiManager();
        roiManager.runCommand("Reset");
        final int n = regions.size();
        for (int i = 0; i < n; i++) {
            // Convert segment i to current selection (ROI)
            segments.setThreshold(i + 1, i + 1, ImageProcessor.NO_LUT_UPDATE);
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
        segments.setColorModel(UIUtils.createIndexColorModel(colors));
        regionsImp.updateAndRepaintWindow();
    }


    Point[] extractPoints(final Region region) {

        final Set<Point> points = new TreeSet<Point>(new PointYXComparator());
        for (final SubRegion subRegion : region.getSubRegions()) {
            final Roi roi = subRegion.getRoi();
            final Rectangle bounds = roi.getBounds();
            for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
                for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
                    if (roi.contains(x, y)) {
                        points.add(new Point(x, y));
                    }
                }
            }
        }

        return points.toArray(new Point[points.size()]);
    }
}
