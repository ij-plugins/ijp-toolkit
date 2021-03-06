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

package ij_plugins.toolkit.concurrent;

import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.process.ImageProcessor;
import ij_plugins.toolkit.ui.progress.IJProgressBarAdapter;
import ij_plugins.toolkit.ui.progress.ProgressAccumulator;
import ij_plugins.toolkit.ui.progress.ProgressReporter4J;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * Example program that adds two multi stack images, processing each corresponding slice in parallel.
 * The example also illustrates use of progress reporters.
 *
 * @author Jarek Sacha
 * @since 3/8/11 8:54 PM
 */
public final class ParallelSliceProcessingExample {

    public static void main(String[] args) {

    }


    private static ImagePlus runEachSliceIndependently(final ImagePlus imp1, final ImagePlus imp2) {

        final int nbProcessorsToUse = Prefs.getThreads();
        final ExecutorService threadPool = Executors.newFixedThreadPool(nbProcessorsToUse);

        final BlitterSP[] producers = SliceProducerFactory.create(imp1, imp2);

        // Setup processing
        final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
        final ProgressAccumulator accumulator = new ProgressAccumulator();
        accumulator.addProgressListener(progressBarAdapter);
        final ImageStack stack = new ImageStack(imp1.getWidth(), imp1.getHeight());
        final List<Future<ImageProcessor>> futures = new ArrayList<>(imp1.getNSlices());
        for (final BlitterSP producer : producers) {
            final PCallable worker = new PCallable(producer);
            accumulator.addProgressReporter(worker);
            final Future<ImageProcessor> f = threadPool.submit(worker);
            futures.add(f);
        }


        // Wait for all futures to complete using Future.get()
        for (int i = 0; i < futures.size(); i++) {
            final Future<ImageProcessor> worker = futures.get(i);
            final String sliceLabel = imp1.getStack().getSliceLabel(i + 1);
            try {
                stack.addSlice(sliceLabel, worker.get());
            } catch (final InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return new ImagePlus("new", stack);
    }


    private static class PCallable extends ProgressReporter4J implements Callable<ImageProcessor> {

        final SliceProducer producer;


        public PCallable(final SliceProducer producer) {
            this.producer = producer;
        }


        final public ImageProcessor call() throws Exception {
            // Forward progress notification is using algorithm that supports it.
            producer.addProgressListener(e -> notifyProgressListeners(e.progress(), e.message()));

            notifyProgressListeners(0);
            ImageProcessor result = producer.produce();
            notifyProgressListeners(1);

            return result;
        }
    }

}
