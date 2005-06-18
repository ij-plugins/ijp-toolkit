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
package net.sf.ij_plugins.color;

import ij.process.ColorProcessor;

import java.awt.*;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
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

    final public void setBinsPerBand(int binsPerBand) {
        this.binsPerBand = binsPerBand;
    }

    /**
     * Create histogram
     *
     * @param cp
     */
    final public void run(ColorProcessor cp) {

        initialize();

        // Accumulate histogram
        int width = cp.getWidth();
        int height = cp.getHeight();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                Color color = cp.getColor(x, y);
                int[] index = whichBin(color);
                ++bins[index[0]][index[1]][index[2]];
                double[] binMean = binMeans[index[0]][index[1]][index[2]];
                binMean[0] += color.getRed();
                binMean[1] += color.getGreen();
                binMean[2] += color.getBlue();
            }
        }

        // Compute center averages
        for (int i = 0; i < binMeans.length; i++) {
            int[][] binsGB = bins[i];
            double[][][] binMeanGB = binMeans[i];
            for (int j = 0; j < binMeanGB.length; j++) {
                int[] binsB = binsGB[j];
                double[][] binMeanB = binMeanGB[j];
                for (int k = 0; k < binMeanB.length; k++) {
                    int count = binsB[k];
                    double[] binMean = binMeanB[k];
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
        for (int r = 0; r < bins.length; r++) {
            int[][] binsGB = bins[r];
            for (int g = 0; g < binsGB.length; g++) {
                int[] binsB = binsGB[g];
                for (int b = 0; b < binsB.length; b++) {
                    count += binsB[b];
                }
            }
        }

        double[][][] normalized = new double[binsPerBand][binsPerBand][binsPerBand];
        for (int r = 0; r < bins.length; r++) {
            int[][] binsGB = bins[r];
            double[][] normalizedGB = normalized[r];
            for (int g = 0; g < binsGB.length; g++) {
                int[] binsB = binsGB[g];
                double[] normalizedB = normalizedGB[g];
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
     *
     * @param color
     * @return
     */
    private int[] whichBin(Color color) {
        int[] binIndex = new int[3];
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
            int red = (int) Math.round((r + 0.5) * binWidth);
            Color[][] colorsGB = binColors[r];
            for (int g = 0; g < colorsGB.length; g++) {
                int green = (int) Math.round((g + 0.5) * binWidth);
                Color[] colorsB = colorsGB[g];
                for (int b = 0; b < colorsB.length; b++) {
                    int blue = (int) Math.round((b + 0.5) * binWidth);
                    colorsB[b] = new Color(red, green, blue);
                }
            }
        }

    }

    public Color[][][] getBinMeanColors() {
        Color[][][] colors = new Color[binsPerBand][binsPerBand][binsPerBand];
        for (int r = 0; r < colors.length; r++) {
            Color[][] colorsGB = colors[r];
            double[][][] binMeansGB = binMeans[r];
            for (int g = 0; g < colorsGB.length; g++) {
                Color[] colorsB = colorsGB[g];
                double[][] binMeansB = binMeansGB[g];
                for (int b = 0; b < colorsB.length; b++) {
                    double[] binMean = binMeansB[b];
                    int red = (int) Math.round(binMean[0]);
                    int green = (int) Math.round(binMean[1]);
                    int blue = (int) Math.round(binMean[2]);
                    colorsB[b] = new Color(red, green, blue);
                }
            }
        }
        return colors;
    }
}
