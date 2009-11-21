/*
 * Image/J Plugins
 * Copyright (C) 2002-2009 Jarek Sacha
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

import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.im3d.grow.SRG;
import net.sf.ij_plugins.im3d.grow.SRG3D;
import net.sf.ij_plugins.util.progress.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


/**
 * ImageJ plugin for running Seeded Region Growing.
 *
 * @author Jarek Sacha
 * @since Feb 8, 2008
 */
public final class RegionGrowingPlugIn implements PlugIn {

    private static final String TITLE = "Seeded Region Growing";
    private static final String RUN_CURRENT_SLICE = "Current slice only";
    private static final String RUN_INDEPENDENT_SLICES = "All slices independent";
    private static final String RUN_3D = "3D volume";
    private static final String[] STACK_TREATMENT = {RUN_CURRENT_SLICE, RUN_INDEPENDENT_SLICES, RUN_3D};

    final AtomicReference<String> stackTreatment = new AtomicReference<String>(STACK_TREATMENT[0]);
    final AtomicBoolean growHistoryEnabled = new AtomicBoolean(false);


    public void run(final String arg) {

        final int[] wList = WindowManager.getIDList();
        if (wList == null) {
            IJ.noImage();
            return;
        }

        final List<String> imageTitleList = new ArrayList<String>();
        final List<String> seedTitleList = new ArrayList<String>();
        for (final int id : wList) {
            final ImagePlus imp = WindowManager.getImage(id);
            if (imp != null && !imp.getTitle().trim().isEmpty()) {
                if (isSupportedImage(imp)) {
                    imageTitleList.add(imp.getTitle());
                }
                if (isSupportedSeed(imp)) {
                    seedTitleList.add(imp.getTitle());
                }
            }
        }

        if (imageTitleList.size() < 1) {
            IJ.error(TITLE, "No supported images open.");
            return;
        }
        if (seedTitleList.size() < 1) {
            IJ.error(TITLE, "No supported seed images open.");
            return;
        }

        final String[] imageTitles = imageTitleList.toArray(new String[imageTitleList.size()]);
        final String[] seedTitles = seedTitleList.toArray(new String[seedTitleList.size()]);
        final GenericDialog gd = new GenericDialog(TITLE, IJ.getInstance());
        gd.addChoice("Image:", imageTitles, imageTitles[0]);
        gd.addChoice("Seeds:", seedTitles, seedTitles[0]);
        gd.addChoice("Stack treatment:", STACK_TREATMENT, stackTreatment.get());
        gd.addCheckbox("Save_grow_history (for " + RUN_3D + " only)", growHistoryEnabled.get());
        gd.addMessage("Seeds image should be of the same size as the image for segmentation.");
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }

        final ImagePlus image = WindowManager.getImage(imageTitles[gd.getNextChoiceIndex()]);
        final ImagePlus seeds = WindowManager.getImage(seedTitles[gd.getNextChoiceIndex()]);
        stackTreatment.set(gd.getNextChoice());
        growHistoryEnabled.set(gd.getNextBoolean());

        // Validate image types
        if (!isSupportedImage(image)) {
            IJ.error(TITLE, "Unsupported image type. Must be GRAY8, GRAY16 or GRAY32.");
            return;
        }
        if (!isSupportedSeed(seeds)) {
            IJ.error(TITLE, "Unsupported seed image type. Must be GRAY8 or COLOR_256 (indexed color).");
            return;
        }

        // Run SRG
        final long startTime = System.currentTimeMillis();

        run(image, seeds, stackTreatment.get(), growHistoryEnabled.get());

        final long endTime = System.currentTimeMillis();
        IJ.showStatus(String.format("%1$s: %2$5.3f seconds.", TITLE, (endTime - startTime) * 0.001));
    }


    private boolean isSupportedImage(final ImagePlus imp) {
        final int type = imp.getType();
        return type == ImagePlus.GRAY8 || type == ImagePlus.GRAY16 || type == ImagePlus.GRAY32;
    }


    private boolean isSupportedSeed(final ImagePlus imp) {
        final int type = imp.getType();
        return type == ImagePlus.GRAY8 || type == ImagePlus.COLOR_256;
    }


    private static void run(final ImagePlus image, final ImagePlus seeds, final String stackTreatment, final boolean growHistoryEnabled) {
        if (RUN_CURRENT_SLICE.equalsIgnoreCase(stackTreatment)) {
            run(image.getProcessor(), (ByteProcessor) seeds.getProcessor(), image.getTitle() + Integer.toString(image.getCurrentSlice()));
        } else if (RUN_INDEPENDENT_SLICES.equalsIgnoreCase(stackTreatment)) {
            runEachSliceIndependently(image, seeds);
        } else if (RUN_3D.equalsIgnoreCase(stackTreatment)) {
            run(image.getStack(), seeds.getStack(), image.getTitle() + Integer.toString(image.getCurrentSlice()), growHistoryEnabled);
        } else {
            IJ.error(TITLE, "Not supported stack option: " + stackTreatment);
        }
    }


    private static void run(final ImageProcessor image, final ByteProcessor seeds, final String prefix) {
        final SRG srg = new SRG();
        srg.setImage(image);
        srg.setSeeds(seeds);

        // Forward progress notification
        srg.addProgressListener(new IJProgressBarAdapter());

        // Run segmentation
        srg.run();

        final ByteProcessor r = srg.getRegionMarkers();
        r.setColorModel(seeds.getColorModel());
        new ImagePlus(prefix + "-SRG", r).show();
    }

    private static void run(final ImageStack image, final ImageStack seeds, final String prefix, final boolean growHistoryEnabled) {
        final SRG3D srg = new SRG3D();
        srg.setImage(image);
        srg.setSeeds(seeds);
        srg.setGrowHistoryEnabled(growHistoryEnabled);
        srg.setGrowHistoryDirectory(new File(System.getProperty("java.io.tmpdir", "./tmp")));

        // Forward progress notification
        srg.addProgressListener(new IJProgressBarAdapter());

        // Run segmentation
        srg.run();

        new ImagePlus(prefix + "-SRG", srg.getRegionMarkers()).show();
    }


    private static void runEachSliceIndependently(final ImagePlus image, final ImagePlus seeds) {

        // Validate size
        if (image.getWidth() != seeds.getWidth() || image.getHeight() != seeds.getHeight() || image.getStackSize() != seeds.getStackSize()) {
            IJ.error(TITLE, "Size of the image [" + image.getWidth() + "x" + image.getHeight() + "x" + image.getStackSize() + "] " +
                    "must match size of seeds [" + seeds.getWidth() + "x" + seeds.getHeight() + "x" + seeds.getStackSize() + "].");
            return;
        }

        final ExecutorService threadPool = Executors.newFixedThreadPool(Prefs.getThreads());

        // Process
        final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
        final ProgressAccumulator accumulator = new ProgressAccumulator();
        accumulator.addProgressListener(progressBarAdapter);
        final ImageStack stack = new ImageStack(image.getWidth(), image.getHeight());
        final List<Future<ByteProcessor>> futures = new ArrayList<Future<ByteProcessor>>(image.getNSlices());
        for (int i = 0; i < image.getNSlices(); ++i) {
            final ByteProcessor bp = (ByteProcessor) image.getStack().getProcessor(i + 1);
            final ByteProcessor s = (ByteProcessor) seeds.getStack().getProcessor(i + 1);
            final SRGCallable worker = new SRGCallable(bp, s);

            accumulator.addProgressReporter(worker);
            final Future<ByteProcessor> f = threadPool.submit(worker);
            futures.add(f);
        }


        // Wait for all futures to complete using Future.get()
        for (int i = 0; i < futures.size(); i++) {
            final Future<ByteProcessor> worker = futures.get(i);
            final String sliceLabel = image.getStack().getSliceLabel(i + 1);
            try {
                stack.addSlice(sliceLabel, worker.get());
            } catch (final InterruptedException e) {
                e.printStackTrace();
            } catch (final ExecutionException e) {
                e.printStackTrace();
            }
        }

        new ImagePlus(image.getTitle() + "-SRG", stack).show();
    }


    private static class SRGCallable extends DefaultProgressReporter implements Callable<ByteProcessor> {

        final ByteProcessor image;
        final ByteProcessor seeds;


        private SRGCallable(final ByteProcessor image, final ByteProcessor seeds) {
            this.image = image;
            this.seeds = seeds;
        }


        public ByteProcessor call() throws Exception {
            // Setup SRG
            final SRG srg = new SRG();
            srg.setImage(image);
            srg.setSeeds(seeds);
            // Forward progress notification
            srg.addProgressListener(new ProgressListener() {
                public void progressNotification(final ProgressEvent e) {
                    notifyProgressListeners(e.getProgress(), e.getMessage());
                }
            });

            // Run segmentation
            srg.run();
            final ByteProcessor r = srg.getRegionMarkers();
            r.setColorModel(seeds.getCurrentColorModel());
            return r;
        }
    }
}
