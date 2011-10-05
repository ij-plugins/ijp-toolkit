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
package net.sf.ij_plugins.quilting;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.IJPluginsRuntimeException;
import net.sf.ij_plugins.io.IOUtils;

import java.io.IOException;

public class ImageQuilterPlugin implements PlugInFilter {
    private static final String DEST_WIDTH_LABEL = "Output width";
    private static final String DEST_HEIGHT_LABEL = "Output height";
    private static final String PATCH_SIZE_LABEL = "Patch size";
    private static final String PATCH_OVERLAP_LABEL = "Patch overlap";
    private static final String ENABLE_HORIZ_PATHS_LABEL = "Allow horizontal paths";
    private static final String PATCH_COST_WEIGHT_LABEL = "Patch cost weight";

    private static final String PLUGIN_NAME = "Image Quilter";
    private static final String ABOUT_COMMAND = "about";
    private static final String ABOUT_MESSAGE =
            "Image Quilter plugin performs texture synthesis using image quilting\n"
                    + "algorithms of Efros and Freeman:\n" +
                    "http://www.cs.berkeley.edu/~efros/research/quilting.html\n" +
                    "  \n" +
                    "Parameters:\n" +
                    "   " + DEST_WIDTH_LABEL + " - desired width of the output image, actual width may be\n " +
                    "       slightly smaller, depending on patch size.\n" +
                    "   " + DEST_HEIGHT_LABEL + " - desired height of the output image, actual height may be\n" +
                    "       slightly smaller, depending on patch size.\n" +
                    "   " + PATCH_SIZE_LABEL + " - width and height of a patch used for quilting.\n" +
                    "   " + PATCH_OVERLAP_LABEL + " - amount of overlap between patches when matching.\n" +
                    "   " + ENABLE_HORIZ_PATHS_LABEL + " - enable improved matching by weighted paths.\n" +
                    "   " + PATCH_COST_WEIGHT_LABEL + " - weight used for improved matching.\n" +
                    "  \n" +
                    "The code of this plugin was originally developed by by Nick Vavra.\n" +
                    "Original code and description is available at: http://www.cs.wisc.edu/~vavra/cs766/.\n" +
                    "ImageJ port and info is available at: http://ij-plugins.sf.net/plugins/texturesynthesis/.";

    private static final Config CONFIG = new Config();

    private String imageTitle;


    private static final class Config implements Cloneable {
        int width = 256;
        int height = 256;
        int patchSize = ImageQuilter.DEFAULT_PATCH_SIZE;
        int overlapSize = ImageQuilter.DEFAULT_OVERLAP_SIZE;
        boolean allowHorizontalPaths;
        double pathCostWeight = 0.1;

        public Config duplicate() {
            try {
                return (Config) clone();
            } catch (final CloneNotSupportedException e) {
                throw new IJPluginsRuntimeException(e);
            }
        }
    }


    @Override
    public int setup(final String s, final ImagePlus imagePlus) {

        if (imagePlus != null) {
            imageTitle = imagePlus.getTitle();
        }

        if (ABOUT_COMMAND.equalsIgnoreCase(s)) {
            IJ.showMessage("About " + PLUGIN_NAME, ABOUT_MESSAGE);
            return DONE;
        } else {
            return DOES_8G | DOES_16 | DOES_32 | DOES_RGB | NO_CHANGES;
        }
    }


    @Override
    public void run(final ImageProcessor input) {

        final GenericDialog dialog = new GenericDialog(PLUGIN_NAME + " options");
        synchronized (CONFIG) {
            dialog.addNumericField(DEST_WIDTH_LABEL, CONFIG.width, 0);
            dialog.addNumericField(DEST_HEIGHT_LABEL, CONFIG.height, 0);
            dialog.addNumericField(PATCH_SIZE_LABEL, CONFIG.patchSize, 0);
            dialog.addNumericField(PATCH_OVERLAP_LABEL, CONFIG.overlapSize, 0);
            dialog.addCheckbox(ENABLE_HORIZ_PATHS_LABEL, CONFIG.allowHorizontalPaths);
            dialog.addNumericField(PATCH_COST_WEIGHT_LABEL, CONFIG.pathCostWeight, 4);
        }

        dialog.showDialog();
        if (dialog.wasCanceled()) {
            return;
        }

        final Config localConfig;
        synchronized (CONFIG) {
            CONFIG.width = (int) Math.round(dialog.getNextNumber());
            CONFIG.height = (int) Math.round(dialog.getNextNumber());
            CONFIG.patchSize = (int) Math.round(dialog.getNextNumber());
            CONFIG.overlapSize = (int) Math.round(dialog.getNextNumber());
            CONFIG.allowHorizontalPaths = dialog.getNextBoolean();
            CONFIG.pathCostWeight = dialog.getNextNumber();
            localConfig = CONFIG.duplicate();
        }

        final ImagePlus impPreview = new ImagePlus("Preview: Quilting of " + imageTitle, input.duplicate());
        impPreview.show();

        final ImageProcessor output = quilt(localConfig, input, impPreview);

        impPreview.setProcessor("Quilting of " + imageTitle, output);
        impPreview.updateAndDraw();
    }


    public static ImageProcessor quilt(final Config config,
                                       final ImageProcessor input,
                                       final ImagePlus previewImp) {
        // run the synthesis algorithm
        final ImageQuilter synther = new ImageQuilter(input,
                config.patchSize,
                config.overlapSize,
                config.allowHorizontalPaths,
                config.pathCostWeight);

        synther.setPreviewImage(previewImp);

        IJ.log("Quilting started at " + new java.util.Date());

        final ImageProcessor output = synther.synthesize(config.width, config.height);

        IJ.log("Quilting ended at " + new java.util.Date());

        return output;
    }


    public static void main(final String[] args) throws IOException {
        final Config config = new Config();
        config.width = 128;
        config.height = 128;
        config.patchSize = 64;
        config.overlapSize = 12;
        config.allowHorizontalPaths = false;
        config.pathCostWeight = 0.1;

        final ImagePlus imp = IOUtils.openImage("images/3.tif");

        System.out.println("Input image size:" + imp.getWidth() + "x" + imp.getHeight());

        quilt(config, imp.getProcessor(), null);

    }
}
