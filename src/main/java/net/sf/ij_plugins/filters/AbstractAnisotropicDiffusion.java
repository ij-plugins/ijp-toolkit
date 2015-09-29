/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
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
package net.sf.ij_plugins.filters;

import ij.process.FloatProcessor;
import net.sf.ij_plugins.util.IJDebug;
import net.sf.ij_plugins.util.progress.ProgressEvent;
import net.sf.ij_plugins.util.progress.ProgressListener;
import net.sf.ij_plugins.util.progress.ProgressReporter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Base class for implementing anisotropic diffusion filters. extending classes need only to
 * implement single diffusion step: {@link #diffuse(ij.process.FloatProcessor,
 * ij.process.FloatProcessor)} .
 *
 * @author Jarek Sacha
 */
public abstract class AbstractAnisotropicDiffusion implements ProgressReporter {

    // Properties
    private int numberOfIterations = 100;
    private double timeStep = 0.05;
    private double meanSquareError = 0.01;

    // Internal variables
    protected final List<ProgressListener> progressListeners = new ArrayList<>();
    private double currentProgress;
    private double lastReportedProgress;
    private final double minProgress = 0.01;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.######");
    private double time;


    public int getNumberOfIterations() {
        return numberOfIterations;
    }


    public void setNumberOfIterations(final int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }


    public double getTimeStep() {
        return timeStep;
    }


    public void setTimeStep(final double timeStep) {
        this.timeStep = timeStep;
    }


    public double getMeanSquareError() {
        return meanSquareError;
    }


    public void setMeanSquareError(final double meanSquareError) {
        this.meanSquareError = meanSquareError;
    }


    @Override
    public double currentProgress() {
        return currentProgress;
    }


    private void updateCurrentProgress(final double progress, final String message) {
        this.currentProgress = progress;
        if (progressListeners.size() > 0 && Math.abs(progress - lastReportedProgress) > minProgress) {
            final ProgressEvent event = new ProgressEvent(this, this.currentProgress, message);
            for (final ProgressListener progressListener : progressListeners) {
                progressListener.progressNotification(event);
            }
            lastReportedProgress = currentProgress;
        }
    }


    @Override
    public void addProgressListener(final ProgressListener l) {
        if (!progressListeners.contains(l)) {
            progressListeners.add(l);
        }
    }


    @Override
    public void removeProgressListener(final ProgressListener l) {
        progressListeners.remove(l);
    }


    @Override
    public void removeAllProgressListener() {
        progressListeners.clear();
    }


    /**
     * Performs anisotropic diffusion. Makes <code>numberOfIterations</code> calls to {@link
     * #diffuse(ij.process.FloatProcessor, ij.process.FloatProcessor)}, updating value of
     * <code>time</code> before each call.
     *
     * @param src input image to which to apply anisotropic diffusion.
     * @return result of anisotropic diffusion filtering.
     */
    public FloatProcessor process(final FloatProcessor src) {
        updateCurrentProgress(0, "");

        FloatProcessor fp1 = (FloatProcessor) src.duplicate();
        FloatProcessor fp2 = (FloatProcessor) src.duplicate();

        for (int i = 0; i < numberOfIterations; i++) {
            time = i * timeStep;
            diffuse(fp1, fp2);

            // swap
            final FloatProcessor tmp = fp2;
            fp2 = fp1;
            fp1 = tmp;

            // test change in images
            final double mse = meanSquareDifference((float[]) fp1.getPixels(), (float[]) fp2.getPixels());
            final String msg = "Iteration: " + i + ", mean square error: " + decimalFormat.format(mse);
            updateCurrentProgress((double) (i + 1) / (double) numberOfIterations, msg);
            IJDebug.log(msg);
            if (mse <= meanSquareError) {
                break;
            }
        }

        updateCurrentProgress(1, "");

        return fp1;
    }


    /**
     * Perform single diffusion operation, called iteratively by {@link
     * #process(ij.process.FloatProcessor)}.
     *
     * @param src  source image
     * @param dest destination image
     */
    protected abstract void diffuse(final FloatProcessor src, final FloatProcessor dest);


    protected double time() {
        return time;
    }


    private double meanSquareDifference(final float a[], final float b[]) {
        assert a != null;
        assert b != null;
        assert a.length == b.length;

        if (a.length == 0) {
            return 0;
        }

        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            final float d = a[i] - b[i];
            sum += d * d;
        }

        return Math.sqrt(sum / a.length);
    }
}
