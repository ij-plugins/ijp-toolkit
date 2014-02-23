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

package net.sf.ij_plugins.clustering;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.text.TextWindow;
import net.sf.ij_plugins.multiband.VectorProcessor;
import net.sf.ij_plugins.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * ImageJ plugin that reapplies k-means clustering to another image, using earlier computed clusters.
 *
 * @author Jarek Sacha
 * @see net.sf.ij_plugins.clustering.KMeans2D
 */
public final class KMeansClusteringReapplyPlugin implements PlugIn {

    private static final Config CONFIG = new Config();
    private static final String TITLE = "k-means Clustering Reapply";
    private static final String ABOUT = "" +
            "Applies k-means Clustering computed on one image to another image of the same type.";

    private static Pair<List<ResultsTable>, List<String>> listTextWindows() {
        final Frame[] windows = WindowManager.getNonImageWindows();
        final List<ResultsTable> resultTables = new ArrayList<>();
        final List<String> names = new ArrayList<>();
        for (Frame frame : windows) {
            if (frame instanceof TextWindow && ((TextWindow) frame).getTextPanel().getResultsTable() != null) {
                final TextWindow textWindow = (TextWindow) frame;
                resultTables.add(textWindow.getTextPanel().getResultsTable());
                names.add(textWindow.getTitle());
            }
        }
        return new Pair<>(resultTables, names);
    }

    private static Pair<List<ImagePlus>, List<String>> listSupportedImages() {
        final int[] ids = WindowManager.getIDList();
        final List<ImagePlus> images = new ArrayList<>();
        final List<String> titles = new ArrayList<>();
        for (int id : ids) {
            final ImagePlus imp = WindowManager.getImage(id);
            if (imp != null && imp.getType() != ImagePlus.COLOR_256) {
                images.add(imp);
                titles.add(imp.getTitle());
            }
        }
        return new Pair<>(images, titles);
    }

    @Override
    public void run(String arg) {
        if ("about".equalsIgnoreCase(arg)) {
            IJ.showMessage("About " + TITLE, ABOUT);
            return;
        }

        final Pair<List<ResultsTable>, List<String>> resultTables = listTextWindows();
        if (resultTables.getFirst().size() < 1) {
            IJ.error("Expecting at least one open Result Table window.");
            return;
        }

        final Pair<List<ImagePlus>, List<String>> images = listSupportedImages();
        if (images.getFirst().size() < 1) {
            IJ.error("Expecting at least one open image (that is not indexed color).");
            return;
        }

        // Ask user for image, results table, and other options
        if (!showOptionsDialog(resultTables.getSecond(), images.getSecond())) {
            return;
        }

        if (CONFIG.interpretStackAs3D) {
            IJ.error(TITLE, "Interpreting stacks as 3D images not yet supported.");
            return;
        }

        final ResultsTable rt = resultTables.getFirst().get(CONFIG.tableIndex);
        final ImagePlus imp = images.getFirst().get(CONFIG.imageIndex);

        //
        // Verify that table headings match image bands
        //
        final ImagePlus stack = KMeansClusteringPlugin.convertToFloatStack(imp);

        final int stackSize = stack.getStackSize();
        final String[] bandLabels = stack.getStack().getSliceLabels();
        final String[] expectedHeadings = new String[stackSize + 1];
        expectedHeadings[0] = "Cluster";
        System.arraycopy(bandLabels, 0, expectedHeadings, 1, stackSize);
        final String[] tableHeadings = rt.getHeadings();
        if (tableHeadings.length < expectedHeadings.length) {
            IJ.error(TITLE, "Not enough headings, expecting: " + Arrays.toString(expectedHeadings));
            return;
        }
        for (int i = 0; i < expectedHeadings.length; i++) {
            if (!expectedHeadings[i].equals(tableHeadings[i])) {
                IJ.error(TITLE, "Expecting heading " + (i + 1) + " to be " + expectedHeadings[i] + ", but got: " + tableHeadings[i] + ".");
                return;
            }
        }

        // Read cluster centers from the table
        final int nbClusters = rt.getCounter();
        final float[][] clusterCenters = new float[nbClusters][expectedHeadings.length - 1];
        for (int clusterIndex = 0; clusterIndex < nbClusters; clusterIndex++) {
            for (int bandIndex = 1; bandIndex < expectedHeadings.length; bandIndex++)
                clusterCenters[clusterIndex][bandIndex - 1] = (float) rt.getValueAsDouble(bandIndex, clusterIndex);
        }

        // Apply clustering to input image
        final VectorProcessor vp = new VectorProcessor(stack);
        final ByteProcessor bp = KMeans2D.encodeSegmentedImage(vp, clusterCenters);
        // Apply default color map
        if (KMeansClusteringPlugin.APPLY_LUT) {
            bp.setColorModel(KMeansClusteringPlugin.defaultColorModel());
        }
        if (KMeansClusteringPlugin.AUTO_BRIGHTNESS) {
            bp.setMinAndMax(0, nbClusters);
        }
        new ImagePlus("Clusters", bp).show();

        // Apply clustering
        if (CONFIG.showCentroidImage) {
            final ImageStack clustered = KMeansUtils.encodeCentroidValueImage(clusterCenters, new VectorProcessor(stack));
            final ImagePlus cvImp = KMeansUtils.createCentroidImage(imp.getType(), clustered);
            cvImp.show();
        }
    }

    private boolean showOptionsDialog(final List<String> resultTableNames, final List<String> imageNames) {

        final GenericDialog dialog = new GenericDialog(TITLE);
        dialog.addMessage("Select result table containing cluster centers produced by k-means clustering plugin.");
        dialog.addChoice("Table with cluster centers", resultTableNames.toArray(new String[resultTableNames.size()]),
                resultTableNames.get(0));
        dialog.addChoice("Image to apply clusters", imageNames.toArray(new String[imageNames.size()]),
                imageNames.get(0));
        dialog.addCheckbox("Interpret_stack_as_3D (not supported)", CONFIG.interpretStackAs3D);
        dialog.addCheckbox("Show_clusters_as_centroid_value", CONFIG.showCentroidImage);
        dialog.addHelp(KMeansClusteringPlugin.HELP_URL);

        dialog.showDialog();

        if (dialog.wasCanceled()) return false;

        CONFIG.tableIndex = dialog.getNextChoiceIndex();
        CONFIG.imageIndex = dialog.getNextChoiceIndex();
        CONFIG.interpretStackAs3D = dialog.getNextBoolean();
        CONFIG.showCentroidImage = dialog.getNextBoolean();

        return true;
    }

    private static class Config {
        boolean showCentroidImage;
        boolean interpretStackAs3D;
        int tableIndex = -1;
        int imageIndex = -1;
    }
}
