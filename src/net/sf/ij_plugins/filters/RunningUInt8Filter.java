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
package net.sf.ij_plugins.filters;

import ij.gui.ProgressBar;
import ij.process.ByteProcessor;
import net.sf.ij_plugins.util.Validate;

import java.awt.*;

/**
 * Implements iterating over an {@link IRunningUInt8Operator}.
 *
 * @author Jarek Sacha
 */
class RunningUInt8Filter implements IRunningUInt8Filter {
    final private IRunningUInt8Operator operator;
    private ProgressBar progressBar;


    /**
     * Construct a filter using a given <code>operator</code>.
     *
     * @param operator operator over which this filter iterates.
     */
    public RunningUInt8Filter(final IRunningUInt8Operator operator) {
        Validate.argumentNotNull(operator, "operator");

        this.operator = operator;
    }

    @Override
    public ByteProcessor run(final ByteProcessor src, final int filterWidth, final int filterHeight) {

        final int width = src.getWidth();
        final ByteProcessor dest = (ByteProcessor) src.duplicate();

        final byte[] srcPixels = (byte[]) src.getPixels();
        final byte[] destPixels = (byte[]) dest.getPixels();

        final int xr = filterWidth / 2;
        final int yr = filterHeight / 2;

        final Rectangle roi = src.getRoi();

        final int xMin = roi.x;
        final int xMax = roi.x + roi.width;
        final int yMin = roi.y;
        final int yMax = roi.y + roi.height;

        // Calculate increment, make sure that different/larger than 0 otherwise '%' operation will fail.
        final int progressIncrement = Math.max((yMax - yMin) / 100, 1);

        for (int y = yMin; y < yMax; ++y) {
            final int yOffset = y * width;

            // Initialize median operator, with all but the last column in the structural element
            operator.clear();
            final int yyMin = Math.max(y - yr, yMin);
            final int yyMax = Math.min(y + yr + 1, yMax);
            final int xxMin = xMin;
            final int xxMax = Math.min(xMin + xr, xMax);
            for (int yy = yyMin; yy < yyMax; ++yy) {
                final int yyOfset = yy * width;
                for (int xx = xxMin; xx < xxMax; ++xx) {
                    operator.add(srcPixels[xx + yyOfset]);
                }
            }


            for (int x = xMin; x < xMax; ++x) {
                if (x + xr < xMax) {
                    for (int yy = yyMin; yy < yyMax; ++yy) {
                        operator.add(srcPixels[x + xr + yy * width]);
                    }
                }
                if (x - xr - 1 >= xMin) {
                    for (int yy = yyMin; yy < yyMax; ++yy) {
                        operator.remove(srcPixels[x - xr - 1 + yy * width]);
                    }
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

    @Override
    public void setProgressBar(final ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    protected void showProgress(final double percentDone) {
        if (progressBar != null) {
            progressBar.show(percentDone);
        }
    }

    protected void hideProgress() {
        showProgress(1.0);
    }


}
