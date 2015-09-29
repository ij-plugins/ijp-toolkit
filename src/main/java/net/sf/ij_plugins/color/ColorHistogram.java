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
package net.sf.ij_plugins.color;

import ij.process.ColorProcessor;

import java.awt.*;

/**
 * @author Jarek Sacha
 */
public class ColorHistogram {
    private final static int BAND_RANGE = 256;
    private int binsPerBand = 8;
    private int[][][] bins;
    private double[][][][] binMeans;
    private Color[][][] binColors;
    private double binWidth;
//    private double[][][] binColorAverage;

    final public int getBinsPerBand() {
        return binsPerBand;
    }

    final public void setBinsPerBand(final int binsPerBand) {
        this.binsPerBand = binsPerBand;
    }

    /**
     * Create histogram
     *
     * @param cp
     */
    final public void run(final ColorProcessor cp) {

        initialize();

        // Accumulate histogram
        final int width = cp.getWidth();
        final int height = cp.getHeight();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final Color color = cp.getColor(x, y);
                final int[] index = whichBin(color);
                ++bins[index[0]][index[1]][index[2]];
                final double[] binMean = binMeans[index[0]][index[1]][index[2]];
                binMean[0] += color.getRed();
                binMean[1] += color.getGreen();
                binMean[2] += color.getBlue();
            }
        }

        // Compute center averages
        for (int i = 0; i < binMeans.length; i++) {
            final int[][] binsGB = bins[i];
            final double[][][] binMeanGB = binMeans[i];
            for (int j = 0; j < binMeanGB.length; j++) {
                final int[] binsB = binsGB[j];
                final double[][] binMeanB = binMeanGB[j];
                for (int k = 0; k < binMeanB.length; k++) {
                    final int count = binsB[k];
                    final double[] binMean = binMeanB[k];
                    for (int l = 0; l < binMean.length; l++) {
                        binMean[l] /= count;
                    }
                }
            }
        }
    }

    public int[][][] getBins() {
        //TODO make copy before returning
        return bins;
    }

    public double[][][][] getBinMeans() {
        //TODO make copy before returning
        return binMeans;
    }

    public double[][][] getNormalizedBins() {

        long count = 0;
        for (final int[][] binsGB : bins) {
            for (final int[] binsB : binsGB) {
                for (final int element : binsB) {
                    count += element;
                }
            }
        }

        final double[][][] normalized = new double[binsPerBand][binsPerBand][binsPerBand];
        for (int r = 0; r < bins.length; r++) {
            final int[][] binsGB = bins[r];
            final double[][] normalizedGB = normalized[r];
            for (int g = 0; g < binsGB.length; g++) {
                final int[] binsB = binsGB[g];
                final double[] normalizedB = normalizedGB[g];
                for (int b = 0; b < binsB.length; b++) {
                    normalizedB[b] = (double) binsB[b] / (double) count;
                }
            }
        }
        return normalized;
    }

    public Color[][][] getBinColors() {
        //TODO make copy before returning
        return binColors;
    }

    /**
     * Compute index of the bin where this colors belongs.
     */
    private int[] whichBin(final Color color) {
        final int[] binIndex = new int[3];
        binIndex[0] = (int) (color.getRed() / binWidth);
        binIndex[1] = (int) (color.getGreen() / binWidth);
        binIndex[2] = (int) (color.getBlue() / binWidth);
        for (int i = 0; i < binIndex.length; i++) {
            if (binIndex[i] >= binsPerBand) {
                binIndex[i] = binsPerBand - 1;
            }
        }
        return binIndex;
    }

    private void initialize() {
        bins = new int[binsPerBand][binsPerBand][binsPerBand];

        binMeans = new double[binsPerBand][binsPerBand][binsPerBand][3];

        binColors = new Color[binsPerBand][binsPerBand][binsPerBand];

        binWidth = (double) BAND_RANGE / (double) binsPerBand;

        for (int r = 0; r < binColors.length; r++) {
            final int red = (int) Math.round((r + 0.5) * binWidth);
            final Color[][] colorsGB = binColors[r];
            for (int g = 0; g < colorsGB.length; g++) {
                final int green = (int) Math.round((g + 0.5) * binWidth);
                final Color[] colorsB = colorsGB[g];
                for (int b = 0; b < colorsB.length; b++) {
                    final int blue = (int) Math.round((b + 0.5) * binWidth);
                    colorsB[b] = new Color(red, green, blue);
                }
            }
        }

    }

    public Color[][][] getBinMeanColors() {
        final Color[][][] colors = new Color[binsPerBand][binsPerBand][binsPerBand];
        for (int r = 0; r < colors.length; r++) {
            final Color[][] colorsGB = colors[r];
            final double[][][] binMeansGB = binMeans[r];
            for (int g = 0; g < colorsGB.length; g++) {
                final Color[] colorsB = colorsGB[g];
                final double[][] binMeansB = binMeansGB[g];
                for (int b = 0; b < colorsB.length; b++) {
                    final double[] binMean = binMeansB[b];
                    final int red = (int) Math.round(binMean[0]);
                    final int green = (int) Math.round(binMean[1]);
                    final int blue = (int) Math.round(binMean[2]);
                    colorsB[b] = new Color(red, green, blue);
                }
            }
        }
        return colors;
    }
}
