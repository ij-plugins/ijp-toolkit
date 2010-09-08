/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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

package net.sf.ij_plugins.im3d.grow;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ByteStatistics;
import net.sf.ij_plugins.util.Pair;
import net.sf.ij_plugins.util.Validate;
import net.sf.ij_plugins.util.progress.DefaultProgressReporter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * @author Jarek Sacha
 * @since Sep 7, 2010 9:44:59 PM
 */
abstract class SRG2DBase extends DefaultProgressReporter {

    private static final String NAME = "Seeded Region Growing";

    private ByteProcessor seeds;
    private ByteProcessor regionMarkers;
    private ByteProcessor mask;
    private ImageStack animationStack;
    private int numberOfAnimationFrames;

    // Internal variables
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    protected int xSize;
    protected int ySize;

    private byte[] regionMarkerPixels;

    private SortedSet<Candidate> ssl;
    private RegionInfo[] regionInfos;
    private long processedPixelCount;

    private final SRGSupport srgSupport = new SRGSupport();


    /**
     * Set region seeds. The first index in the {@code seeds} array refers to the region.
     * Second index is a seeds with a region.
     *
     * @param seeds region seeds.
     * @see #toSeedImage(java.awt.Point[][], int, int)
     */
    public void setSeeds(final ByteProcessor seeds) {
        Validate.argumentNotNull(seeds, "seeds");

        if (seeds.getWidth() < 1 || seeds.getHeight() < 1) {
            throw new IllegalArgumentException("Seed image cannot be empty.");
        }

        this.seeds = (ByteProcessor) seeds.duplicate();
    }


    /**
     * Set ROI mask, if not set full image is processed.
     *
     * @param mask Mask of the region of interest used for processing.
     */
    public void setMask(final ByteProcessor mask) {
        this.mask = mask;
    }


    /**
     * Sets how many animation frames should be recorded. If less then 1 animation will not be recorded.
     *
     * @param numberOfAnimationFrames number of animation frames.
     */
    public void setNumberOfAnimationFrames(final int numberOfAnimationFrames) {
        this.numberOfAnimationFrames = numberOfAnimationFrames;
    }


    public ByteProcessor getRegionMarkers() {
        return regionMarkers;
    }


    /**
     * Return progress of growing.
     *
     * @return animation stack with region markers.
     */
    public ImageStack getAnimationStack() {
        return animationStack;
    }


    /**
     * <p>
     * Convert array of point seeds to a seed image.
     * The first index in the {@code seeds} array refers to the region.
     * Region number if the first index + 1.
     * Second index is a seeds with a region.
     * </p>
     * Conditions:                                              .
     * <ol>
     * <li>Argument {@code seeds} cannot be {@code link} null.</li>
     * <li>There must be at least 2 regions.</li>
     * <li>There must be at most 253 regions.</li>
     * <li>Each region has to have at least one seed point.</li>
     * <li>All seed points have to be unique.</li>
     * </ol>
     *
     * @param seeds  region seeds.
     * @param width  width of the seed image
     * @param height height of the seed image
     * @return seed image.
     */
    public static ByteProcessor toSeedImage(final Point[][] seeds, final int width, final int height) {
        Validate.argumentNotNull(seeds, "seeds");

        if (seeds.length < 2) {
            throw new IllegalArgumentException("Seeds for at least two regions required, got " + seeds.length + ".");
        }

        if (seeds.length > SRGSupport.MAX_REGION_NUMBER) {
            throw new IllegalArgumentException(
                    "Maximum number of regions is " + SRGSupport.MAX_REGION_NUMBER + ", got " + seeds.length + ".");
        }

        for (int i = 0; i < seeds.length; i++) {
            if (seeds[i] == null || seeds[i].length < 1) {
                throw new IllegalArgumentException(
                        "Regions have to have at least one seeds point. Region " + i + ", got " + seeds[i].length + ".");
            }
        }

        final ByteProcessor seedBP = new ByteProcessor(width, height);
        for (int i = 0; i < seeds.length; i++) {
            final int region = i + 1;
            for (final Point seed : seeds[i]) {
                if (seed.x >= 0 && seed.x < width && seed.y >= 0 && seed.y < height) {
                    if (seedBP.get(seed.x, seed.y) == 0) {
                        seedBP.set(seed.x, seed.y, region);
                    } else {
                        throw new IllegalArgumentException(
                                "Duplicate seed at (" + seed.x + "," + seed.y + ") in region " + region + ".");
                    }
                } else {
                    throw new IllegalArgumentException("In region " + region +
                            " seed at (" + seed.x + "," + seed.y + ") is outside image " + width + "x" + height + ".");
                }

            }
        }

        return seedBP;
    }


    /**
     * Perform region growing.
     */
    public void run() {

        this.notifyProgressListeners(0, NAME + "initializing..");

        initializeStructures();


        // Initialize markers and create initial region info
        initializeMarkersAnRegionInfo();

        // Initialize candidates
        initializeCandidates();

        if (numberOfAnimationFrames > 1) {
            addAnimationFrame("Seeds", (ByteProcessor) regionMarkers.duplicate());
        }

        final long pixelsToProcess = (xMax - xMin) * (yMax - yMin);
        final long frameIncrement = this.numberOfAnimationFrames > 2
                ? pixelsToProcess / (numberOfAnimationFrames - 2)
                : Long.MAX_VALUE;

        // Calculate increment, make sure that different/larger than 0 otherwise '%' operation will fail.
        final long progressIncrement = Math.max(pixelsToProcess / 25, 1);
        this.notifyProgressListeners(processedPixelCount / (double) pixelsToProcess);


        // Process candidates
        while (!ssl.isEmpty()) {
            // Get best candidate
            final Candidate c = ssl.first();
            // Remove it from the candidate set
            ssl.remove(c);

            final int offset = c.point.x + c.point.y * xSize;
            final byte marker = regionMarkerPixels[offset];
            assert marker == SRGSupport.CANDIDATE_MARK : "Expecting point (" + c.point.x + "," + c.point.y + ") to be candidate got " + (int) marker;

            // Add this point to its most similar region
            regionMarkerPixels[offset] = (byte) (c.mostSimilarRegionId & 0xff);

            // Update region info to include this point
            regionInfos[c.mostSimilarRegionId].addPoint(c.point);

            ++processedPixelCount;
            candidatesFromNeighbours(c.point);

            if (processedPixelCount % progressIncrement == 0) {
                assert processedPixelCount <= pixelsToProcess;
                this.notifyProgressListeners(processedPixelCount / (double) pixelsToProcess, NAME + " processing..");
            }

            if (processedPixelCount % frameIncrement == 0) {
                addAnimationFrame(null, (ByteProcessor) regionMarkers.duplicate());
            }
        }


        if (numberOfAnimationFrames > 0) {
            addAnimationFrame("Final regions", (ByteProcessor) regionMarkers.duplicate());
        }

        // Mark pixels outside of the mask as 0
        SRGSupport.fillOutsideMask(regionMarkerPixels, (byte) 0, mask);
        restoreOriginalSeedIDs(regionMarkerPixels);
        if (animationStack != null) {
            for (int i = 1; i <= animationStack.getSize(); i++) {
                final byte[] animationPixels = (byte[]) animationStack.getPixels(i);
                SRGSupport.fillOutsideMask(animationPixels, (byte) 0, mask);
                restoreOriginalSeedIDs(animationPixels);
            }
        }
        this.notifyProgressListeners(1);
    }


    private void initializeCandidates() {
        for (int y = 0; y < ySize; ++y) {
            for (int x = 0; x < xSize; ++x) {
                final int offset = x + y * xSize;
                final int regionId = regionMarkerPixels[offset];
                if (regionId == SRGSupport.BACKGROUND_MARK
                        || regionId == SRGSupport.OUTSIDE_MARK
                        || regionId == SRGSupport.CANDIDATE_MARK) {
                    continue;
                }

                // Initialize SSL - ordered list of bordering at least one of the regions
                candidatesFromNeighbours(new Point(x, y));

                ++processedPixelCount;
            }
        }
    }


    private void initializeMarkersAnRegionInfo() {
        for (int y = 0; y < ySize; ++y) {
            for (int x = 0; x < xSize; ++x) {
                final int offset = x + y * xSize;
                if (regionMarkerPixels[offset] == SRGSupport.OUTSIDE_MARK) {
                    continue;
                }

                final int regionID = srgSupport.seedToRegionLookup[seeds.get(x, y)];
                if (regionID < 1) {
                    continue;
                }


                // Add seed to regionMarkers
                regionMarkerPixels[offset] = (byte) (regionID & 0xff);

                // Add seed to region info
                regionInfos[regionID].addPoint(new Point(x, y));
            }
        }
    }


    /**
     * Create growth candidates from background neighbours of <code>point</code>.
     *
     * @param point seed point.
     */
    private void candidatesFromNeighbours(final Point point) {
        // Get neighbours of this seed that are not assigned to any region yet
        final java.util.List<Point> backgroundPoints = backgroundNeighbours(point);

        // Update SSL
        for (final Point p : backgroundPoints) {
            ssl.add(createCandidate(p));
        }
    }


    protected abstract double distanceFromMean(final int offset, final RegionInfo regionInfo);


    private Candidate createCandidate(final Point point) {
        final int offset = point.x + point.y * xSize;

        // Mark as candidate
        regionMarkerPixels[offset] = SRGSupport.CANDIDATE_MARK;

        // Get flags of neighboring regions
        final boolean[] flags = neighbourRegionFlags(point);

        // Compute distance to most similar region
        double minSigma = Double.MAX_VALUE;
        int mostSimilarRegionId = -1;
        for (int regionID = 1; regionID < regionInfos.length; regionID++) {
            // Skip region if it is not a neighbor
            if (!flags[regionID])
                continue;

            final RegionInfo regionInfo = regionInfos[regionID];
            final double sigma = distanceFromMean(offset, regionInfo);
            if (sigma < minSigma) {
                minSigma = sigma;
                mostSimilarRegionId = regionID;
            }
        }
        assert mostSimilarRegionId > 0;

        return new Candidate(point, mostSimilarRegionId, minSigma);
    }


    private boolean[] neighbourRegionFlags(final Point point) {
        final boolean[] r = new boolean[regionInfos.length];
        // 4-connected
        assignRegionFlag(point.x, point.y - 1, r);
        assignRegionFlag(point.x, point.y + 1, r);
        assignRegionFlag(point.x - 1, point.y, r);
        assignRegionFlag(point.x + 1, point.y, r);
        // 8-connected
        assignRegionFlag(point.x - 1, point.y - 1, r);
        assignRegionFlag(point.x + 1, point.y - 1, r);
        assignRegionFlag(point.x - 1, point.y + 1, r);
        assignRegionFlag(point.x + 1, point.y + 1, r);
        return r;
    }


    private void assignRegionFlag(final int x, final int y, final boolean[] r) {
        if (x < xMin || x >= xMax || y < yMin || y >= yMax) {
            return;
        }

        final byte v = regionMarkerPixels[x + y * xSize];
        if (v != SRGSupport.BACKGROUND_MARK && v != SRGSupport.CANDIDATE_MARK && v != SRGSupport.OUTSIDE_MARK) {
            final int regionId = v & 0xff;
            r[regionId] = true;
        }
    }


    protected abstract void initializeImageStructures();


    protected abstract RegionInfo newRegionInfo(final int originalSeedID);


    protected void initializeStructures() {

        initializeImageStructures();

        if (seeds == null) {
            throw new IllegalStateException("Seeds image is not set ['null'].");
        }

        if (seeds.getWidth() != xSize || seeds.getHeight() != ySize) {
            throw new IllegalArgumentException("Seeds image [" + seeds.getWidth() + "x" + seeds.getHeight() + "] " +
                    "has to have the same dimension as input image [" + xSize + "x" + ySize + "].");
        }

        if (mask != null && (mask.getWidth() != xSize || mask.getHeight() != ySize)) {
            throw new IllegalArgumentException("Mask has to have the same dimension as input image.");
        }

        xMin = 0;
        xMax = xSize;
        yMin = 0;
        yMax = ySize;

        regionMarkers = new ByteProcessor(xSize, ySize);
        regionMarkerPixels = (byte[]) regionMarkers.getPixels();
        animationStack = new ImageStack(xSize, ySize);

        seeds.setMask(mask);
//        final ByteStatistics statistics = new ByteStatistics(seeds);
//        int regionCount = 0;
//        seedToRegionLookup = new int[MAX_REGION_NUMBER + 1];
//        final int[] regionToSeedLookup = new int[MAX_REGION_NUMBER + 1];
//        for (int seed = 1; seed < statistics.histogram.length; seed++) {
//            if (statistics.histogram[seed] > 0) {
//                if (seed > MAX_REGION_NUMBER) {
//                    throw new IllegalArgumentException("Seed ID cannot be larger than " + MAX_REGION_NUMBER
//                            + ", got " + seed + ".");
//                }
//
//                regionCount++;
//                seedToRegionLookup[seed] = regionCount;
//                regionToSeedLookup[regionCount] = seed;
//            }
//
//        }

        final Pair<int[], Integer> p = srgSupport.createSeedToRegionLookup(new ByteStatistics(seeds).histogram);
        final int[] regionToSeedLookup = p.getFirst();
        final int regionCount = p.getSecond();

        // Initialize region info structures
        regionInfos = new RegionInfo[regionCount + 1];
        for (int i = 1; i < regionInfos.length; i++) {
            regionInfos[i] = newRegionInfo(regionToSeedLookup[i]);
        }

        // Create candidate list and define rules for ordering of its elements
        ssl = new TreeSet<Candidate>();

        // Mark pixels outside of the mask
        SRGSupport.fillOutsideMask(regionMarkerPixels, SRGSupport.OUTSIDE_MARK, mask);
    }


    private List<Point> backgroundNeighbours(final Point point) {
        final List<Point> backgroundPoints = new ArrayList<Point>(7);
        // 4-connected
        addIfBackground(point.x - 1, point.y, backgroundPoints);
        addIfBackground(point.x + 1, point.y, backgroundPoints);
        addIfBackground(point.x, point.y - 1, backgroundPoints);
        addIfBackground(point.x, point.y + 1, backgroundPoints);
        // 8-connected
        addIfBackground(point.x - 1, point.y - 1, backgroundPoints);
        addIfBackground(point.x + 1, point.y - 1, backgroundPoints);
        addIfBackground(point.x - 1, point.y + 1, backgroundPoints);
        addIfBackground(point.x + 1, point.y + 1, backgroundPoints);

        return backgroundPoints;
    }


    private void addIfBackground(final int x, final int y, final List<Point> backgroundPoints) {
        if (x < xMin || x >= xMax ||
                y < yMin || y >= yMax) {
            return;
        }

        final int offset = x + y * xSize;
        if (regionMarkerPixels[offset] == SRGSupport.BACKGROUND_MARK) {
            // Add to unassigned pixels
            backgroundPoints.add(new Point(x, y));
        }
    }


    private void restoreOriginalSeedIDs(final byte[] pixels) {
        for (int i = 1; i < pixels.length; i++) {
            final int regionID = pixels[i] & 0xFF;
            if (regionID != SRGSupport.BACKGROUND_MARK) {
                pixels[i] = (byte) (regionInfos[regionID].originalSeedID & 0xFF);
            }
        }
    }


    private void addAnimationFrame(final String title, final ByteProcessor bp) {
        final byte[] pixels = (byte[]) bp.getPixels();
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] == SRGSupport.CANDIDATE_MARK) {
                pixels[i] = SRGSupport.BACKGROUND_MARK;
            }
        }
        animationStack.addSlice(title, bp);
    }


    protected abstract static class RegionInfo {

        private final int originalSeedID;


        public RegionInfo(final int originalSeedID) {
            this.originalSeedID = originalSeedID;
        }


        public abstract void addPoint(final Point point);
    }


    final private static class Candidate implements Comparable<Candidate> {

        public final Point point;
        public final int mostSimilarRegionId;
        public final double similarityDifference;


        public Candidate(final Point point, final int mostSimilarRegionId, final double similarityDifference) {
            this.point = point;
            this.mostSimilarRegionId = mostSimilarRegionId;
            this.similarityDifference = similarityDifference;
        }


        public int compareTo(final Candidate c) {
            Validate.argumentNotNull(c, "c");

            if (this.point.equals(c.point)) {
                return 0;
            } else if (this.similarityDifference < c.similarityDifference) {
                return -1;
            } else if (this.similarityDifference > c.similarityDifference) {
                return 1;
            } else if (this.point.x < c.point.x) {
                return -1;
            } else if (this.point.x > c.point.x) {
                return 1;
            } else if (this.point.y < c.point.y) {
                return -1;
            } else if (this.point.y > c.point.y) {
                return 1;
            } else {
                throw new IllegalStateException("This condition should never happen.");
            }
        }


        @Override
        public boolean equals(final Object obj) {
            return obj instanceof Candidate && compareTo((Candidate) obj) == 0;
        }


        @Override
        public int hashCode() {
            return new Double(similarityDifference).hashCode() + point.hashCode();
        }
    }


}
