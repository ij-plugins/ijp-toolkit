package net.sf.ij.plugin;

/***
 * Image/J Plugins
 * Copyright (C) 2002-2004 Jarek Sacha
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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.plugin.filter.Duplicater;
import ij.process.ByteProcessor;
import ij.process.ImageConverter;
import ij.process.StackConverter;
import net.sf.ij.clustering.KMeans;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

/**
 * ImageJ plugin wrapper for k-means clustering algorithm.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.7 $
 * @see KMeans
 */
public final class KMeansClustering implements PlugIn {

    private static KMeans.Config config = new KMeans.Config();
    private static boolean showCentroidImage = false;

    private static final boolean applayLut = false;
    private static final boolean autoBrightness = true;

    private static final String aboutMessage =
            "k-means Clustering performs pixel-based segmentation of multi-band\n" +
            "images. An image stack is interpreted as a set of bands corresponding to\n" +
            "the same image. For instance, an RGB color images has three bands: red,\n" +
            "green, and blue. Each pixels is represented by an n-valued vector , where\n" +
            "n is a number of bands, for instance, a 3-value vector [r,g,b] in case of\n" +
            "a color image.\n" +
            "Each cluster is defined by its centroid in n-dimensional space. Pixels are\n" +
            "grouped by their proximity to cluster's centroids.\n" +
            "Cluster centroids are determined using a heuristics: initially centroids\n" +
            "are randomly initialized and then their location is interactively\n" +
            "optimized.\n" +
            "For more information on this and other clustering approaches see:\n" +
            "Anil K. Jain and Richard C. Dubes, \"Algorithms for Clustering Data\",\n" +
            "Prentice Hall, 1988.\n" +
            "http://homepages.inf.ed.ac.uk/rbf/BOOKS/JAIN/Clustering_Jain_Dubes.pdf\n";

    final public void run(final String s) {

        if ("about".equalsIgnoreCase(s)) {
            IJ.showMessage("About k-means Clustering", aboutMessage);
            return;
        }

        // Get reference to current image
        final ImagePlus imp = IJ.getImage();

        if (imp.getType() == ImagePlus.COLOR_256) {
            throw new RuntimeException("Unsupporte image type: COLOR_256");
        }

        // Create configuration dialog
        final GenericDialog dialog = new GenericDialog("K-means Configuration");
        dialog.addNumericField("Number of clusters", config.getNumberOfClusters(), 0);
        dialog.addNumericField("Cluster center tolerance", config.getTolerance(), 8);
        dialog.addCheckbox("Enable randomization seed", config.isRandomizationSeedEnabled());
        dialog.addNumericField("Randomization seed", config.getRandomizationSeed(), 0);
        dialog.addCheckbox("Show clusters as centrid value", showCentroidImage);
        dialog.addCheckbox("Enable clustering animation", config.isClusterAnimationEnabled());
        dialog.addCheckbox("Print optimization trace", config.isPrintTraceEnabled());

        // Show dialog
        dialog.showDialog();
        if (dialog.wasCanceled()) {
            //TODO: Should the macro be cancelled here?
            return;
        }

        // Read configuration from dialog
        config.setNumberOfClusters((int) Math.round(dialog.getNextNumber()));
        config.setTolerance(dialog.getNextNumber());
        config.setRandomizationSeedEnabled(dialog.getNextBoolean());
        config.setRandomizationSeed((int) Math.round(dialog.getNextNumber()));
        showCentroidImage = dialog.getNextBoolean();
        config.setClusterAnimationEnabled(dialog.getNextBoolean());
        config.setPrintTraceEnabled(dialog.getNextBoolean());

        // Convert to a stack of float images
        final ImagePlus stack = convertToFloatStack(imp);

        // Run clustering
        final KMeans kMeans = new KMeans(config);
        final ByteProcessor bp = kMeans.run(stack.getStack());

        // Applay default color map
        if (applayLut) {
            bp.setColorModel(defaultColorModel());
        }
        if (autoBrightness) {
            bp.setMinAndMax(0, config.getNumberOfClusters());
        }

        // Show result image
        final ImagePlus r = new ImagePlus("Clusters", bp);
        r.show();

        // Show animation
        if (config.isClusterAnimationEnabled()) {
            final ImageStack animationStack = kMeans.getClusterAnimation();
            if (applayLut) {
                animationStack.setColorModel(defaultColorModel());
            }
            final ImagePlus animation = new ImagePlus("Cluster animation", animationStack);
            animation.show();
            if (autoBrightness) {
                for (int i = 0; i < animationStack.getSize(); i++) {
                    animation.setSlice(i + 1);
                    animation.getProcessor().setMinAndMax(0, config.getNumberOfClusters());
                }
                animation.setSlice(1);
                animation.updateAndDraw();
            }
        }

        // Show centroid image
        if (showCentroidImage) {
            ImagePlus cvImp = createCentroidImage(imp.getType(),
                    kMeans.getCentroidValueImage());
            cvImp.show();
        }
    }

    /**
     * Create 3-3-2-RGB color model
     */
    static final private ColorModel defaultColorModel() {
        final byte[] reds = new byte[256];
        final byte[] greens = new byte[256];
        final byte[] blues = new byte[256];
        for (int i = 0; i < 256; i++) {
            reds[i] = (byte) (i & 0xe0);
            greens[i] = (byte) ((i << 3) & 0xe0);
            blues[i] = (byte) ((i << 6) & 0xc0);
        }

        return new IndexColorModel(8, 256, reds, greens, blues);
    }

    /**
     * Convert image to a stack of FloatProcessors.
     *
     * @param imp
     * @return
     */
    static final private ImagePlus convertToFloatStack(ImagePlus imp) {

        imp = duplicate(imp);

        // Remember scaling setup
        boolean doScaling = ImageConverter.getDoScaling();

        try {
            // Disable scaling
            ImageConverter.setDoScaling(false);

            if (imp.getType() == ImagePlus.COLOR_RGB) {
                if (imp.getStackSize() > 1) {
                    throw new RuntimeException("Unsupported image type: stack of COLOR_RGB");
                }
                final ImageConverter ic = new ImageConverter(imp);
                ic.convertToRGBStack();
            }

            if (imp.getStackSize() > 1) {
                final StackConverter sc = new StackConverter(imp);
                sc.convertToGray32();
            } else {
                final ImageConverter ic = new ImageConverter(imp);
                ic.convertToGray32();
            }

            return imp;
        } finally {
            // Restore original scaling option
            ImageConverter.setDoScaling(doScaling);

        }
    }

    private static ImagePlus createCentroidImage(int originalImageType, ImageStack centroidValueStack) {
        boolean doScaling = ImageConverter.getDoScaling();
        try {
            ImageConverter.setDoScaling(false);
            ImagePlus cvImp = new ImagePlus("Cluster centroid values", centroidValueStack);
            if (centroidValueStack.getSize() > 1) {
                StackConverter sc = new StackConverter(cvImp);
                switch (originalImageType) {
                    case ImagePlus.COLOR_RGB:
                        sc.convertToGray8();
                        ImageConverter ic = new ImageConverter(cvImp);
                        ic.convertRGBStackToRGB();
                        break;
                    case ImagePlus.GRAY8:
                        sc.convertToGray8();
                        break;
                    case ImagePlus.GRAY16:
                        sc.convertToGray16();
                        break;
                    case ImagePlus.GRAY32:
                        // No actcion needed
                        break;
                    default:
                        throw new RuntimeException("Unsupported input image type: "
                                + originalImageType);
                }
            } else {
                ImageConverter ic = new ImageConverter(cvImp);
                // Convert image back to original type
                switch (originalImageType) {
                    case ImagePlus.COLOR_RGB:
                        throw new RuntimeException("Internal error: RGB image cannot have a single band.");
                    case ImagePlus.GRAY8:
                        ic.convertToGray8();
                        break;
                    case ImagePlus.GRAY16:
                        ic.convertToGray16();
                        break;
                    case ImagePlus.GRAY32:
                        // No actcion needed
                        break;
                    default:
                        throw new RuntimeException("Unsupported input image type: "
                                + originalImageType);
                }
            }

            return cvImp;
        } finally {
            ImageConverter.setDoScaling(doScaling);
        }

    }

    /**
     * Duplicate input image.
     *
     * @param imp
     * @return
     */
    static final private ImagePlus duplicate(final ImagePlus imp) {
        final Duplicater duplicater = new Duplicater();
        duplicater.setup(null, imp);
        return duplicater.duplicateStack(imp, imp.getTitle() + "-duplicate");
    }
}
