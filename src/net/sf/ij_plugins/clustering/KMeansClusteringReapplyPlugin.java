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

    @Override
    public void run(String arg) {
        if ("about".equalsIgnoreCase(arg)) {
            IJ.showMessage("About " + TITLE, ABOUT);
            return;
        }

        // Get reference to current image
        final ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        if (imp.getType() == ImagePlus.COLOR_256) {
            IJ.error(TITLE, "Indexed color images are not supported.");
            return;
        }

        final Frame[] windows = WindowManager.getNonImageWindows();
        final List<TextWindow> resultTables = new ArrayList<>();
        for (Frame frame : windows) {
            if (frame instanceof TextWindow) resultTables.add((TextWindow) frame);
        }

        if (resultTables.size() < 1) {
            IJ.error("Expecting at least one open Result Table window.");
            return;
        }

        final String[] resultTableNames = new String[resultTables.size()];
        for (int i = 0; i < resultTableNames.length; i++) {
            resultTableNames[i] = resultTables.get(i).getTitle();
        }

        if (!showOptionsDialog(resultTableNames)) {
            return;
        }

        if (CONFIG.interpretStackAs3D) {
            IJ.error(TITLE, "Inpreting stacks as 3D images not yet supported.");
            return;
        }

        final ResultsTable rt = resultTables.get(CONFIG.tableIndex).getTextPanel().getResultsTable();

        //
        // Verify that table has proper headings
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

    private boolean showOptionsDialog(final String[] resultTableNames) {

        final GenericDialog dialog = new GenericDialog(TITLE);
        dialog.addMessage("Select result table containing cluster centers produced by k-means clustering plugin.");
        dialog.addChoice("Table with cluster centers", resultTableNames, resultTableNames[0]);
        dialog.addCheckbox("Interpret_stack_as_3D", CONFIG.interpretStackAs3D);
        dialog.addCheckbox("Show_clusters_as_centroid_value", CONFIG.showCentroidImage);
        dialog.addHelp(KMeansClusteringPlugin.HELP_URL);

        dialog.showDialog();

        if (dialog.wasCanceled()) return false;

        CONFIG.tableIndex = dialog.getNextChoiceIndex();
        CONFIG.interpretStackAs3D = dialog.getNextBoolean();
        CONFIG.showCentroidImage = dialog.getNextBoolean();

        return true;
    }

    private static class Config {
        boolean showCentroidImage;
        boolean interpretStackAs3D;
        int tableIndex = -1;
    }
}
