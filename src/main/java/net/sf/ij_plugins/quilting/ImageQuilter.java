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

package net.sf.ij_plugins.quilting;


import ij.IJ;
import ij.ImagePlus;
import ij.process.*;
import net.sf.ij_plugins.IJPluginsRuntimeException;

import java.util.LinkedList;

/**
 * This class implements the texture synthesis algorithm described by the paper "Image Quilting for
 * Texture Synthesis and Transfer". This only knows how to make a new texture image (ie it does not
 * implement hole filling or other uses of the algorithm).
 * <br>
 * This class adds some optional extensions to improve the quality of the output given more run
 * time.
 */
public class ImageQuilter {
    private final ImageProcessor input;
    private final int patchsize;
    private final int overlapsize;

    public static final int DEFAULT_PATCH_SIZE = 36;
    public static final int DEFAULT_OVERLAP_SIZE = 6;

    private final boolean allowHorizontalPaths;
    private final double pathCostWeight;

    private ImagePlus previewImp;

    /**
     * This sets up the algorithm.
     *
     * @param input                This is the texture to sample from.
     * @param patchsize            This is the width (pixels) of the square patches used.
     * @param overlapsize          This is the width (pixels) of the overlap region.
     * @param allowHorizontalPaths When finding min paths, can the path travel along a stage?
     * @param pathCostWeight       The SSD for the overlap region and the min SSD path cost have the
     *                             same range. The total cost is then pathCost*pathCostWeight plus
     *                             ssd*(1-pathCostWeight).
     */
    public ImageQuilter(final ImageProcessor input, final int patchsize, final int overlapsize,
                        final boolean allowHorizontalPaths, final double pathCostWeight) {

        this.overlapsize = overlapsize;
        this.patchsize = patchsize;
        this.input = input;

        this.allowHorizontalPaths = allowHorizontalPaths;
        this.pathCostWeight = pathCostWeight;
    }


    /**
     * This synthesizes a new texture image with the given dimensions.
     */
    public ImageProcessor synthesize(int outwidth, int outheight) {

        if (outwidth < patchsize || outheight < patchsize) {
            throw new IllegalArgumentException("Output size is too small");
        }

        // check to see if the output size is acceptable and fix it if not
        int patchcols = Math.round((float) (outwidth - patchsize) /
                (patchsize - overlapsize));
        int patchrows = Math.round((float) (outheight - patchsize) /
                (patchsize - overlapsize));
        final int okwidth = patchcols * (patchsize - overlapsize) + patchsize;
        final int okheight = patchrows * (patchsize - overlapsize) + patchsize;
        patchcols++;
        patchrows++;
        if (okwidth != outwidth || okheight != outheight) {
            IJ.log("Your output size requires partial patches that are currently not supported.");
            outwidth = okwidth;
            outheight = okheight;
            IJ.log("Using width = " + outwidth + " and height = " + outheight + " instead.");
        }

        // create the output image
        final ImageProcessor output = input.createProcessor(outwidth, outheight);

        // choose a patch at random to get started
        final int x = (int) (Math.random() * (input.getWidth() - patchsize));
        final int y = (int) (Math.random() * (input.getHeight() - patchsize));
        final View inView = new View(input, x, y);
        final Patch outPatch = new Patch(output, 0, 0, patchsize, patchsize);
        SynthAide.copy(inView, outPatch, 0, 0, patchsize, patchsize);

        // done already?
        if (!outPatch.nextColumn(overlapsize)) {
            return output;
        }

        if (previewImp != null) {
            previewImp.setProcessor(null, output);
            previewImp.updateAndDraw();
        }

        // loop over the rows of output patches
        int currow = 0;
        final double dists[][] = new double[input.getHeight() - patchsize + 1]
                [input.getWidth() - patchsize + 1];
        double progress = 0;
        do {

            int currcolumn = 0;
            // loop over the patches in this row
            do {

                IJ.showStatus("Quilting row " + (currow + 1) + "/" + patchrows
                        + ", column " + (currcolumn + 1) + "/" + patchcols
                        + " (" + ((int) (progress * 100)) + "%)");

                // get the distances for this neighborhood
                final TwoDLoc bestloc = calcDists(dists, outPatch);
                final double bestval = dists[bestloc.getRow()][bestloc.getCol()];

                // pick one of the close matches
                final double threshold = bestval * 1.1;
                final LinkedList<TwoDLoc> loclist = SynthAide.lessThanEqual(dists, threshold);
                final int choice = (int) (Math.random() * loclist.size());
                final TwoDLoc loc = loclist.get(choice);

                // copy in the patch
                //fillAndBlend(outPatch, loc);
                pathAndFill(outPatch, loc);

                if (previewImp != null) {
                    previewImp.updateAndDraw();
                }

                ++currcolumn;
                progress = (currow + (double) currcolumn / (double) patchcols) / patchrows;
                IJ.showProgress(progress);

            } while (outPatch.nextColumn(overlapsize));

            currow++;
            if (IJ.debugMode) {
                IJ.log("done with row " + currow + " / " + patchrows);
            }

        } while (outPatch.nextRow(overlapsize));


        return output;
    }


//    /**
//     * This calculates the difference between the path costs of the left overlap region and the top
//     * overlap region.
//     */
//    private double getOverlapDistDifference(Patch outPatch, TwoDLoc loc) {
//
//        Patch inPatch = new Patch(input, loc.getX(), loc.getY(),
//                patchsize, patchsize);
//        double left = 0.0, top = 0.0;
//        double tolap[][] = getTopOverlapDists(outPatch, inPatch);
//        double lolap[][] = getLeftOverlapDists(outPatch, inPatch);
//        for (int r = 0; r < lolap.length; r++) {
//            for (int c = 0; c < lolap[r].length; c++) {
//                top += tolap[r][c];
//                left += lolap[r][c];
//            }
//        }
//
//        return (top > left ? top - left : left - top);
//    }


    /**
     * This calculates the distance (SSD) between the overlap part of outPatch and the corresponding
     * parts of the possible input patches. If the pathCostWeight extension has been activated, this
     * will also calculate the path cost and weight the distance based on that cost. This returns
     * the array index of the smallest distance found.
     *
     * @param dists This will be filled in. The return value in dists[y][x] will be the SSD between
     *              an input patch with corner (x,y) and the given output patch.
     */
    private TwoDLoc calcDists(final double[][] dists, final Patch outPatch) {

        double best = Double.MAX_VALUE;
        TwoDLoc bestloc = null;

        // loop over the possible input patch row locations
        final Patch inPatch = new Patch(input, 0, 0, patchsize, patchsize);
        do {

            // loop over the possible input patch column locations
            do {

                double sum = 0.0;
                double leftoverlap[][] = null;
                double topoverlap[][] = null;
                int count = 0;

                // handle the left overlap part
                if (!outPatch.isAtLeftEdge()) {

                    leftoverlap = getLeftOverlapDists(outPatch, inPatch);
                    for (final double[] aLeftoverlap : leftoverlap) {
                        for (final double anALeftoverlap : aLeftoverlap) {
                            sum += anALeftoverlap;
                        }
                    }
                    count += leftoverlap.length * leftoverlap[0].length;
                }

                // handle the top overlap part
                if (!outPatch.isAtTopEdge()) {

                    topoverlap = getTopOverlapDists(outPatch, inPatch);
                    for (final double[] aTopoverlap : topoverlap) {
                        for (final double anATopoverlap : aTopoverlap) {
                            sum += anATopoverlap;
                        }
                    }
                    count += topoverlap.length * topoverlap[0].length;
                }

                // don't double count the upper left corner;
                if (leftoverlap != null && topoverlap != null) {
                    for (int x = 0; x < overlapsize; x++) {
                        for (int y = 0; y < overlapsize; y++) {
                            sum -= SynthAide.ssd(outPatch, inPatch, x, y) / 3.0;
                        }
                    }
                    count -= overlapsize * overlapsize;
                }

                // make this an average SSD instead
                sum = sum / (count * 255 * 255);

                // do we weight the SSD with the min cost path cost?
                if (pathCostWeight > 0) {

                    double cost = avgCostOfBestPath(leftoverlap, topoverlap);

                    // update the sum appropriately
                    cost = cost / (255 * 255);
                    sum = sum * (1 - pathCostWeight) + pathCostWeight * cost;
                }

                // save the total and compare to the best yet
                final int y = inPatch.getCornerY();
                final int x = inPatch.getCornerX();
                dists[y][x] = sum;
                if (sum < best) {
                    best = sum;
                    bestloc = new TwoDLoc(y, x);
                }

            } while (inPatch.rightOnePixel());

        } while (inPatch.nextPixelRow());

        return bestloc;
    }

    /**
     * This returns the cost of the path through the given overlap region divided by the length of
     * the path.
     */
    private double avgCostOfBestPath(final double[][] leftoverlap,
                                     final double[][] topoverlap) {
        double cost;
        int rowcnt;

        if (leftoverlap == null) {

            final MinPathFinder tpath = new MinPathFinder(topoverlap,
                    allowHorizontalPaths);
            final TwoDLoc loc = tpath.bestSourceLoc();
            cost = tpath.costOf(loc.getRow(), loc.getCol());
            rowcnt = patchsize;
        } else if (topoverlap == null) {

            final MinPathFinder lpath = new MinPathFinder(leftoverlap,
                    allowHorizontalPaths);
            final TwoDLoc loc = lpath.bestSourceLoc();
            cost = lpath.costOf(loc.getRow(), loc.getCol());
            rowcnt = patchsize;
        } else {
            final MinPathFinder lpath = new MinPathFinder(leftoverlap,
                    allowHorizontalPaths);
            final MinPathFinder tpath = new MinPathFinder(topoverlap,
                    allowHorizontalPaths);
            final TwoDLoc lloc = new TwoDLoc(0, 0);
            final TwoDLoc tloc = new TwoDLoc(0, 0);
            choosePathIntersection(lpath, tpath, lloc, tloc);

            // what is the total cost of the two paths?
            // this ignores the fact that the two have a pt in common
            cost = lpath.costOf(lloc.getRow(), lloc.getCol());
            cost += tpath.costOf(tloc.getRow(), tloc.getCol());

            // what is the combined length of the two paths
            rowcnt = 2 * patchsize - 2 - lloc.getRow() - tloc.getRow();
            rowcnt = 2 * patchsize - rowcnt;
        }

        return cost / rowcnt;
    }

    /**
     * This creates an array the size of the horizontal overlap region and fills that array with the
     * SSDs between the patches in that region. The array returned is upside down, such that
     * array[0][0] is the lower left corner of the overlap region. (it's that way to be convenient
     * input to net.sf.ij_plugins.quilting.MinPathFinder)
     */
    private double[][] getLeftOverlapDists(final Patch outPatch, final Patch inPatch) {
        final int rowcnt = outPatch.getHeight();
        final double dists[][] = new double[rowcnt][overlapsize];

        // Calculate using blitting
        final ImageProcessor outIP = outPatch.getImage();
        final ImageProcessor inIP = inPatch.getImage();
        final ImageProcessor overlapIP = outIP.createProcessor(overlapsize, rowcnt);
        overlapIP.setProgressBar(null);
        overlapIP.copyBits(outIP, -outPatch.getXOffset(), -outPatch.getYOffset(), Blitter.COPY);
        overlapIP.copyBits(inIP, -inPatch.getXOffset(), -inPatch.getYOffset(), Blitter.DIFFERENCE);

        int arrayr = rowcnt - 1;
        if (overlapIP instanceof ColorProcessor) {
            final int[] rgb = new int[3];
            for (int r = 0; r < rowcnt; r++) {
                final double[] dists_a = dists[arrayr];
                for (int c = 0; c < overlapsize; c++) {
                    overlapIP.getPixel(c, r, rgb);
                    final double rr = rgb[0];
                    final double gg = rgb[1];
                    final double bb = rgb[2];
                    final double v = (rr * rr + gg * gg + bb * bb) / 3.0;
                    dists_a[c] = v;
                }
                arrayr--;
            }
        } else if (overlapIP instanceof FloatProcessor) {
            for (int r = 0; r < rowcnt; r++) {
                final double[] dists_a = dists[arrayr];
                for (int c = 0; c < overlapsize; c++) {
                    final double bv = overlapIP.getPixelValue(c, r);
                    final double v = bv * bv;
                    dists_a[c] = v;
                }
                arrayr--;
            }
        } else if (overlapIP instanceof ByteProcessor || overlapIP instanceof ShortProcessor) {
            for (int r = 0; r < rowcnt; r++) {
                final double[] dists_a = dists[arrayr];
                for (int c = 0; c < overlapsize; c++) {
                    final double bv = overlapIP.getPixel(c, r);
                    final double v = bv * bv;
                    dists_a[c] = v;
                }
                arrayr--;
            }
        } else {
            throw new IJPluginsRuntimeException("Unsupported image type: " + overlapIP.getClass().getName());
        }


        return dists;
    }

    /**
     * This creates an array the size of the horizontal overlap region and fills that array with the
     * SSDs between the patches in that region. The array is set up to be vertical with one array
     * row per column of the overlap region and array[0][0] being the upper right corner of the
     * overlap region.
     */
    private double[][] getTopOverlapDists(final Patch outPatch, final Patch inPatch) {
        // so arrayr = patchwidth-1-patchx  and arrayc = patchy
        final int rowcnt = outPatch.getWidth();
        final double dists[][] = new double[rowcnt][overlapsize];

        // Calculate using blitting
        final ImageProcessor outIP = outPatch.getImage();
        final ImageProcessor inIP = inPatch.getImage();
        final ImageProcessor overlapIP = outIP.createProcessor(rowcnt, overlapsize);
        overlapIP.setProgressBar(null);
        overlapIP.copyBits(outIP, -outPatch.getXOffset(), -outPatch.getYOffset(), Blitter.COPY);
        overlapIP.copyBits(inIP, -inPatch.getXOffset(), -inPatch.getYOffset(), Blitter.DIFFERENCE);

        // Compare results
        if (overlapIP instanceof ColorProcessor) {
            final int[] rgb = new int[3];
            for (int patchx = 0; patchx < rowcnt; patchx++) {
                final int arrayr = rowcnt - patchx - 1;
                for (int patchy = 0; patchy < overlapsize; patchy++) {
                    overlapIP.getPixel(patchx, patchy, rgb);
                    final double rr = rgb[0];
                    final double gg = rgb[1];
                    final double bb = rgb[2];
                    final double v = (rr * rr + gg * gg + bb * bb) / 3.0;
                    dists[arrayr][patchy] = v;
                }
            }

        } else if (overlapIP instanceof FloatProcessor) {
            for (int patchx = 0; patchx < rowcnt; patchx++) {
                final int arrayr = rowcnt - patchx - 1;
                for (int patchy = 0; patchy < overlapsize; patchy++) {
                    final double bv = overlapIP.getPixelValue(patchx, patchy);
                    final double v = bv * bv;
                    dists[arrayr][patchy] = v;
                }
            }

        } else if (overlapIP instanceof ByteProcessor || overlapIP instanceof ShortProcessor) {
            for (int patchx = 0; patchx < rowcnt; patchx++) {
                final int arrayr = rowcnt - patchx - 1;
                for (int patchy = 0; patchy < overlapsize; patchy++) {
                    final double bv = overlapIP.getPixel(patchx, patchy);
                    final double v = bv * bv;
                    dists[arrayr][patchy] = v;
                }
            }

        } else {
            throw new IJPluginsRuntimeException("Unsupported image type: " + overlapIP.getClass().getName());
        }

        return dists;
    }


//    /**
//     * This copies a patch from the input image at location loc into outPatch. The overlap regions
//     * will be blended.
//     */
//    private void fillAndBlend(Patch outPatch, TwoDLoc loc) {
//
//        Patch inPatch = new Patch(input, loc.getX(), loc.getY(),
//                patchsize, patchsize);
//
//        if (outPatch.isAtTopEdge()) {
//
//            // blend the overlap area on the left
//            for (int r = 0; r < patchsize; r++) {
//                for (int c = 0; c < overlapsize; c++) {
//                    double inpart = (double) c / overlapsize;
//                    SynthAide.blend(inPatch, outPatch, c, r, inpart);
//                }
//            }
//            SynthAide.copy(inPatch, outPatch, overlapsize,
//                    0, patchsize - overlapsize, overlapsize);
//        } else if (outPatch.isAtLeftEdge()) {
//
//            // blend the overlap area on top
//            for (int c = 0; c < patchsize; c++) {
//                for (int r = 0; r < overlapsize; r++) {
//                    double inpart = (double) r / overlapsize;
//                    SynthAide.blend(inPatch, outPatch, c, r, inpart);
//                }
//            }
//            SynthAide.copy(inPatch, outPatch, 0, overlapsize,
//                    overlapsize, patchsize - overlapsize);
//        } else {
//
//            // blend the overlap area on top
//            for (int c = overlapsize; c < patchsize; c++) {
//                for (int r = 0; r < overlapsize; r++) {
//                    double inpart = (double) r / overlapsize;
//                    SynthAide.blend(inPatch, outPatch, c, r, inpart);
//                }
//            }
//
//            // blend the overlap area on the left
//            for (int r = overlapsize; r < patchsize; r++) {
//                for (int c = 0; c < overlapsize; c++) {
//                    double inpart = (double) c / overlapsize;
//                    SynthAide.blend(inPatch, outPatch, c, r, inpart);
//                }
//            }
//
//            // blend the combined overlap
//            for (int r = 0; r < overlapsize; r++) {
//                for (int c = 0; c < overlapsize; c++) {
//                    double inpart = (double) c * r / (overlapsize * overlapsize);
//                    SynthAide.blend(inPatch, outPatch, c, r, inpart);
//                }
//            }
//        }
//
//        // copy in the remaining part
//        int size = patchsize - overlapsize;
//        SynthAide.copy(inPatch, outPatch, overlapsize, overlapsize, size, size);
//    }


    /**
     * Uses the min path boundary method to fill in outPatch from the input image patch at loc.
     */
    private void pathAndFill(final Patch outPatch, final TwoDLoc loc) {

        final boolean allow = allowHorizontalPaths;
        final Patch inPatch = new Patch(input, loc.getX(), loc.getY(),
                patchsize, patchsize);

        if (outPatch.isAtLeftEdge()) {

            SynthAide.copy(inPatch, outPatch, 0, 0, overlapsize, patchsize);
            final double[][] topOverlap = getTopOverlapDists(outPatch, inPatch);
            final MinPathFinder topFinder = new MinPathFinder(topOverlap, allow);
            final TwoDLoc source = topFinder.bestSourceLoc();
            followTopOverlapPath(outPatch, inPatch, topFinder, source);
        } else if (outPatch.isAtTopEdge()) {

            SynthAide.copy(inPatch, outPatch, 0, 0, patchsize, overlapsize);
            final double[][] leftOverlap = getLeftOverlapDists(outPatch, inPatch);
            final MinPathFinder leftFinder = new MinPathFinder(leftOverlap, allow);
            final TwoDLoc source = leftFinder.bestSourceLoc();
            followLeftOverlapPath(outPatch, inPatch, leftFinder, source);
        } else {

            final double[][] topOverlap = getTopOverlapDists(outPatch, inPatch);
            final double[][] leftOverlap = getLeftOverlapDists(outPatch, inPatch);
            final MinPathFinder topFinder = new MinPathFinder(topOverlap, allow);
            final MinPathFinder leftFinder = new MinPathFinder(leftOverlap, allow);
            TwoDLoc leftloc = new TwoDLoc(0, 0);
            TwoDLoc toploc = new TwoDLoc(0, 0);

            // find the best combined source
            choosePathIntersection(leftFinder, topFinder, leftloc, toploc);

            // fill in the corner

            // first figure out where to take each pixel from
            final boolean where[][] = new boolean[overlapsize][overlapsize];

            // figure out where the left overlap says to take each pixel from
            while (leftloc.getRow() < overlapsize) {
                final int r = leftloc.getRow();
                for (int c = leftloc.getCol(); c < overlapsize; c++) {
                    where[r][c] = true;
                }
                leftloc = leftFinder.follow(leftloc);
            }

            // figure out where the top overlap agrees with the left overlap
            while (toploc.getRow() < overlapsize) {
                final int r = toploc.getRow();
                for (int c = 0; c < overlapsize; c++) {
                    where[c][r] = where[c][r] && c >= toploc.getCol();
                }
                toploc = topFinder.follow(toploc);
            }

            // fill in the corner for real now
            final int[] sample = new int[3];
            for (int r = 0; r < overlapsize; r++) {
                for (int c = 0; c < overlapsize; c++) {
                    if (where[r][c]) {
                        outPatch.putSample(c, r, inPatch.getSample(c, r, sample));
                    }
                }
            }

            // handle the rest of the overlap regions
            followLeftOverlapPath(outPatch, inPatch, leftFinder, leftloc);
            followTopOverlapPath(outPatch, inPatch, topFinder, toploc);
        }

        // fill in the non-overlap area
        final int size = patchsize - overlapsize;
        SynthAide.copy(inPatch, outPatch, overlapsize, overlapsize, size, size);
    }


    /**
     * Fills in the left overlap area of toPatch using values from fromPatch while following the
     * path from source in finder.
     */
    private void followLeftOverlapPath(final Patch toPatch, final Patch fromPatch,
                                       final MinPathFinder finder, TwoDLoc source) {

        // loop until we reach the destination
        final int[] sample = new int[3];
        while (source != null) {

            final int y = patchsize - source.getRow() - 1;
            int x = source.getCol();

            // values to the right of x are filled in from fromPatch
            for (x++; x < overlapsize; x++) {
                toPatch.putSample(x, y, fromPatch.getSample(x, y, sample));
            }

            // values at that low point are averaged
            x = source.getCol();
            SynthAide.blend(fromPatch, toPatch, x, y, 0.5);
            //int red[] = {255,0,0};  // DEBUG
            //toPatch.putSample(x, y, red);  // DEBUG

            // values to the left are untouched

            // continue to the next row
            //   we should probably check for this ahead of time and blend
            //   (instead of replace or ignore) all pixels along the path
            final int oldrow = source.getRow();
            do {
                source = finder.follow(source);
            } while (source != null && source.getRow() == oldrow);
        }

    }

    /**
     * Fills in the top overlap area of toPatch using values from fromPatch while following the path
     * from source in finder.
     */
    private void followTopOverlapPath(final Patch toPatch, final Patch fromPatch,
                                      final MinPathFinder finder, TwoDLoc source) {

        // loop until we reach the destination
        final int[] sample = new int[3];
        while (source != null) {

            final int x = patchsize - source.getRow() - 1;
            int y = source.getCol();

            // values below y are filled in from fromPatch
            for (y++; y < overlapsize; y++) {
                toPatch.putSample(x, y, fromPatch.getSample(x, y, sample));
            }

            // values at that low point are averaged
            y = source.getCol();
            SynthAide.blend(fromPatch, toPatch, x, y, 0.5);
            //int red[] = {255,0,0};  // DEBUG
            //toPatch.putSample(x, y, red);  // DEBUG

            // values above are untouched

            // continue to the next row
            //   we should probably check for this ahead of time and blend
            //   (instead of replace or ignore) all pixels along the path
            final int oldrow = source.getRow();
            do {
                source = finder.follow(source);
            } while (source != null && source.getRow() == oldrow);
        }

    }

    /**
     * This finds the intersection of the two given paths. The intersection point (in each path's
     * coordinates) is put into the leftloc and toploc params.
     */
    private void choosePathIntersection(final MinPathFinder leftpath,
                                        final MinPathFinder toppath,
                                        final TwoDLoc leftloc,
                                        final TwoDLoc toploc) {

        // find the best combined source
        leftloc.set(patchsize - 1, 0);          // upper left corner
        toploc.set(patchsize - 1, 0);           //  of the image
        double bestcost = leftpath.costOf(patchsize - 1, 0)
                + toppath.costOf(patchsize - 1, 0);

        for (int y = 0; y < overlapsize; y++) {
            for (int x = 0; x < overlapsize; x++) {
                final double cost = leftpath.costOf(patchsize - 1 - y, x)
                        + toppath.costOf(patchsize - 1 - x, y);
                if (bestcost > cost) {
                    leftloc.set(patchsize - 1 - y, x);
                    toploc.set(patchsize - 1 - x, y);
                    bestcost = cost;
                }
            }
        }
    }

    public void setPreviewImage(final ImagePlus previewImp) {
        this.previewImp = previewImp;
    }
}

