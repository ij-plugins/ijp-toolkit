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
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.beans.AbstractModel;
import net.sf.ij_plugins.im3d.grow.SRG;
import net.sf.ij_plugins.ui.OverlayCanvas;
import net.sf.ij_plugins.ui.ShapeOverlay;
import net.sf.ij_plugins.ui.UIUtils;
import net.sf.ij_plugins.ui.multiregion.MultiRegionManagerModel;
import net.sf.ij_plugins.ui.multiregion.Region;
import net.sf.ij_plugins.ui.multiregion.SubRegion;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Date: Feb 18, 2008
 * Time: 10:21:35 AM
 *
 * @author Jarek Sacha
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

        final Point[][] seeds = new Point[regions.size()][];
        for (int i = 0; i < regions.size(); ++i) {
            final Point[] points = extractPoints(regions.get(i));
            if (points.length < 1) {
                IJ.error(title, "Cannot perform growing, region '" + regions.get(i).getName() + "' is empty (no seeds).");
                return;
            }
            seeds[i] = points;
        }

        final SRG srg = new SRG();
        srg.setImage((ByteProcessor) imp.getProcessor().duplicate().convertToByte(true));
        srg.setNumberOfAnimationFrames(0);
        srg.setSeeds(seeds);
        srg.run();

        final ByteProcessor segments = srg.getRegionMarkers();
        final List<Color> colors = new ArrayList<Color>();
        colors.add(Color.BLACK);
        for (final Region region : regions) {
            colors.add(region.getColor());
        }
        segments.setColorModel(UIUtils.createIndexColorModel(colors));
        new ImagePlus("ROIs", segments).show();

//        IndexColorModel cm = new IndexColorModel(8, 256, fi.reds, fi.greens, fi.blues);
//        if (imp.isComposite())
//            ((CompositeImage)imp).setChannelColorModel(cm);
//        else
//            ip.setColorModel(cm);

//        // Convert segmented regions to ROIs.
//        final java.util.List<ShapeOverlay> overlays = new ArrayList<ShapeOverlay>();
//        for (int i = 0; i < regions.size(); i++) {
//            final ByteProcessor mask = (ByteProcessor) segments.duplicate();
//
//            final int level = i + 1;
//            mask.setThreshold(level, level, ImageProcessor.NO_LUT_UPDATE);
//
//            final ImagePlus maskImp = new ImagePlus("", mask);
//            final ThresholdToSelection thresholdToSelection = new ThresholdToSelection();
//            thresholdToSelection.setup(null, maskImp);
//            thresholdToSelection.run(mask);
//
//            final Roi maskRoi = maskImp.getRoi();
//            if (maskRoi != null) {
//                final Region region = regions.get(i);
//                final Shape shape = new Area(ShapeOverlay.toShape(maskRoi));
//                overlays.add(new ShapeOverlay(region.getName(), shape, region.getColor(), true));
//            }
//        }

//        show("Segmented", imp.getProcessor(), overlays);

//        if (roiManager==null) {
//				Frame frame = WindowManager.getFrame("ROI Manager");
//				if (frame==null)
//					IJ.run("ROI Manager...");
//				frame = WindowManager.getFrame("ROI Manager");
//				if (frame==null || !(frame instanceof RoiManager))
//					{addToManager=false; return;}
//				roiManager = (RoiManager)frame;
//				if (resetCounter)
//					roiManager.runCommand("reset");
//			}
//			roiManager.add(imp, roi, Analyzer.getCounter());
    }


    private static void show(final String title, final ImageProcessor ip, final Iterable<ShapeOverlay> overlays) {
        final ImagePlus imp = new ImagePlus(title, ip);
        final OverlayCanvas overlayCanvas = new OverlayCanvas(imp);
        new ImageWindow(imp, overlayCanvas);
        overlayCanvas.setOverlays(overlays);
        overlayCanvas.invalidate();
        overlayCanvas.repaint();
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
