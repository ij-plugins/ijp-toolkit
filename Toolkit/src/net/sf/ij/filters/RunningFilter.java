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
package net.sf.ij.filters;

import ij.ImagePlus;
import ij.gui.ProgressBar;
import ij.process.FloatProcessor;
import net.sf.ij.io.IOUtils;

import java.awt.*;
import java.io.IOException;

/**
 * Implemnts iteratoin over an {@link RunningMedianOperator}.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.2 $
 */
public class RunningFilter {

    final private RunningMedianOperator operator;
    final int filterWidth;
    final int filterHeight;
    private ProgressBar progressBar;


    /**
     * Construct a filter using a given <code>operator</code>.
     *
     * @param operator operator over which this filter iterates.
     */
    public RunningFilter(final RunningMedianOperator operator,
                         final int filterWidth, final int filterHeight) {
        if (operator == null) {
            throw new IllegalArgumentException("Argument operator cannot be null.");
        }

        this.operator = operator;
        this.operator.reset(filterWidth, filterHeight);
        this.filterWidth = filterWidth;
        this.filterHeight = filterHeight;
    }

    public FloatProcessor run(final FloatProcessor src) {

        final int width = src.getWidth();
        final int height = src.getHeight();
        final FloatProcessor dest = new FloatProcessor(width, height);

        final float[] srcPixels = (float[]) src.getPixels();
        final float[] destPixels = (float[]) dest.getPixels();

        final int xr = filterWidth / 2;
        final int yr = filterHeight / 2;

        final Rectangle roi = src.getRoi();

        final int xMin = roi.x;
        final int xMax = roi.x + roi.width;
        final int yMin = roi.y;
        final int yMax = roi.y + roi.height;

        final int progressIncrement = (yMax - yMin) / 100;

        final float[] packet = new float[filterHeight];
        for (int y = yMin; y < yMax; ++y) {
            final int yOffset = y * width;

            // Initialize median operator, with all but the last column in the structural key
            operator.clear();
            final int yyMin = Math.max(y - yr, yMin);
            final int yyMax = Math.min(y + yr + 1, yMax);
            final int xxMin = xMin;
            final int xxMax = Math.min(xMin + xr, xMax);
            for (int xx = xxMin; xx < xxMax; ++xx) {
                for (int yy = yyMin; yy < yyMax; ++yy) {
                    packet[yy - yyMin] = srcPixels[xx + yy * width];
                }
                operator.push(yyMax - yyMin, packet);
            }


            for (int x = xMin; x < xMax; ++x) {
                if (x + xr < xMax) {
                    for (int yy = yyMin; yy < yyMax; ++yy) {
                        packet[yy - yyMin] = srcPixels[x + xr + yy * width];
                    }
                    operator.push(yyMax - yyMin, packet);
                }

                destPixels[x + yOffset] = operator.evaluate();
            }

            if (y % progressIncrement == 0) {
                showProgress((double) (y - yMin) / (yMax - yMin));
            }

        }

        hideProgress();

        return dest;

    }


    public void setProgressBar(final ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    protected void showProgress(double percentDone) {
        if (progressBar != null)
            progressBar.show(percentDone);
    }

    protected void hideProgress() {
        showProgress(1.0);
    }

    public static void main(String[] args) throws IOException {
        ij.ImageJ.main(null);
        ImagePlus imp = IOUtils.openImage("test_images/blobs_noise.tif");
        final FloatProcessor fp = (FloatProcessor) imp.getProcessor().convertToFloat();

        final RunningFilter filter = new RunningFilter(new RunningMedianOperator(), 3, 3);
        FloatProcessor dest = filter.run(fp);
        new ImagePlus("result", dest).show();
    }

}
