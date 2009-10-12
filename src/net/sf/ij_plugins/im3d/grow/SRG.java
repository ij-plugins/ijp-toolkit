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

package net.sf.ij_plugins.im3d.grow;

import ij.ImageStack;
import ij.process.*;
import net.sf.ij_plugins.util.progress.DefaultProgressReporter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>
 * Seeded region growing algorithm based on article by Rolf Adams and Leanne Bischof, "Seeded Region
 * Growing", <i>IEEE Transactions on Pattern Analysis and Machine Intelligence</i>, vol. 16, no. 6,
 * June 1994.
 * </p>
 * <p>
 * The algorithm assumes that seeds for objects and the background be provided.
 * Seeds are used to compute initial mean gray level for each region.
 * The condition of growth is difference of a gray level of a candidate pixel and mean grey level
 * intensity of a neighboring region.
 * At each step of the algorithm a candidate with a smallest difference to some neighboring
 * region is added to that region and all neighboring points of that that are not yet assigned to
 * any region are added to candidate list.
 * </p>
 * <p>
 * Part of the image that will be segmented can be restricted by setting a mask.
 * </p>
 * <p>
 * Progress of segmentation can be recorded on a animation stack.
 * To enable recording set {@code numberOfAnimationFrames} to a value larger than 0.
 * </p>
 * <p>
 * An example of segmenting an image is below. Seeds are set for three regions: background, blob 1, and blob 2.
 * </p>
 * <pre>
 * // Setup growing
 * final ByteProcessor image = ...;
 * final Point[][] seeds = {
 *         {new Point(107, 144)}, // Background
 *         {new Point(91, 159)},  // Blob 1
 *         {new Point(119, 143)}, // Blob 2
 *     };
 * final SRG srg = new SRG();
 * srg.setImage(image);
 * srg.setSeeds(seeds);
 * srg.setNumberOfAnimationFrames(50);
 * // Run growing
 * srg.run();
 * // Extract results
 * final ByteProcessor regionMask = srg.getRegionMarkers();
 * final ImageStack animationStack = srg.getAnimationStack();
 * </pre>
 *
 * @author Jarek Sacha
 */
public final class SRG extends DefaultProgressReporter {

    private static final String NAME = "Seeded Region Growing";

    private static final int MAX_REGION_NUMBER = 253;
    private static final byte BACKGROUND_MARK = (byte) 0x00;
    private static final byte CANDIDATE_MARK = (byte) 0xff;
    private static final byte OUTSIDE_MARK = (byte) 0xfe;

    // External properties
    private FloatProcessor image;
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
    private int xSize;
    private byte[] regionMarkerPixels;
    private float[] imagePixels;

    private SortedSet<Candidate> ssl;
    private int[] seedToRegonLookup;
    private RegionInfo[] regionInfos;
    private long processedPixelCount;
    private int ySize;


    /**
     * Set image to be segmented. Supported types are: {@link ByteProcessor}, {@link ShortProcessor}, and {@link FloatProcessor}.
     *
     * @param image image.
     */
    public void setImage(final ImageProcessor image) {
        validateNotNull(image, "image");
        if (image instanceof ByteProcessor) {
            setImage((ByteProcessor) image);
        } else if (image instanceof ShortProcessor) {
            setImage((ShortProcessor) image);
        } else if (image instanceof FloatProcessor) {
            setImage((FloatProcessor) image);
        } else {
            throw new IllegalArgumentException("Unsuported image type: " + image.getClass().getName());
        }
    }


    /**
     * Set image to be segmented.
     *
     * @param image image.
     */
    public void setImage(final ByteProcessor image) {
        validateNotNull(image, "image");
        this.image = (FloatProcessor) image.convertToFloat();
    }


    /**
     * Set image to be segmented.
     *
     * @param image image.
     */
    public void setImage(final ShortProcessor image) {
        validateNotNull(image, "image");
        this.image = (FloatProcessor) image.convertToFloat();
    }


    /**
     * Set image to be segmented.
     *
     * @param image image.
     */
    public void setImage(final FloatProcessor image) {
        validateNotNull(image, "image");
        this.image = (FloatProcessor) image.duplicate();
    }


    /**
     * Set region seeds. The first index in the {@code seeds} array refers to the region.
     * Second index is a seeds with a region.
     *
     * @param seeds region seeds.
     * @see #toSeedImage(java.awt.Point[][], int, int)
     */
    public void setSeeds(final ByteProcessor seeds) {
        if (seeds == null) {
            throw new IllegalArgumentException("Argument 'seeds' cannot be null.");
        }

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
     * Perform region growing.
     */
    public void run() {

        this.notifyProgressListeners(0, NAME + "initailizing..");

        initializeStructures();


        // Initialize markers and create initial region info
        for (int y = 0; y < ySize; ++y) {
            for (int x = 0; x < xSize; ++x) {
                final int offset = x + y * xSize;
                if (regionMarkerPixels[offset] == OUTSIDE_MARK) {
                    continue;
                }

                final int regonID = seedToRegonLookup[seeds.get(x, y)];
                if (regonID < 1) {
                    continue;
                }


                // Add seed to regionMarkers
                regionMarkerPixels[offset] = (byte) (regonID & 0xff);

                // Add seed to region info
                regionInfos[regonID].addPoint(new Point(x, y));
            }
        }

        // Initialize candidates
        for (int y = 0; y < ySize; ++y) {
            for (int x = 0; x < xSize; ++x) {
                final int offset = x + y * xSize;
                final int regionId = regionMarkerPixels[offset];
                if (regionId == BACKGROUND_MARK || regionId == OUTSIDE_MARK || regionId == CANDIDATE_MARK) {
                    continue;
                }

                // Initialize SSL - ordered list of bordering at least one of the regions
                candidatesFromNeighbours(new Point(x, y));

                ++processedPixelCount;
            }
        }

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
            assert marker == CANDIDATE_MARK : "Expecting point (" + c.point.x + "," + c.point.y + ") to be candidate got " + (int) marker;

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
        fillOutsideMask(regionMarkerPixels, (byte) 0);
        restoreOriginalSeedIDs(regionMarkerPixels);
        if (animationStack != null) {
            for (int i = 1; i <= animationStack.getSize(); i++) {
                final byte[] animationPixels = (byte[]) animationStack.getPixels(i);
                fillOutsideMask(animationPixels, (byte) 0);
                restoreOriginalSeedIDs(animationPixels);
            }
        }
        this.notifyProgressListeners(1);
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
     * <li>There must be at most {@value #MAX_REGION_NUMBER} regions.</li>
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
        validateNotNull(seeds, "Argument 'seeds' cannot be null");

        if (seeds.length < 2) {
            throw new IllegalArgumentException("Seeds for at least two regions required, got " + seeds.length + ".");
        }

        if (seeds.length > MAX_REGION_NUMBER) {
            throw new IllegalArgumentException(
                    "Maximum number of regions is " + MAX_REGION_NUMBER + ", got " + seeds.length + ".");
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


    private void restoreOriginalSeedIDs(final byte[] pixels) {
        for (int i = 1; i < pixels.length; i++) {
            final int regionID = pixels[i];
            if (regionID != BACKGROUND_MARK) {
                pixels[i] = (byte) (regionInfos[regionID].originalSeedID & 0xFF);
            }
        }
    }


    private void fillOutsideMask(final byte[] pixels, final byte value) {
        if (mask != null) {
            final byte[] maskPixels = (byte[]) mask.getPixels();
            for (int i = 0; i < pixels.length; i++) {
                if (maskPixels[i] == 0) {
                    pixels[i] = value;
                }
            }
        }

    }


    private void addAnimationFrame(final String title, final ByteProcessor bp) {
        final byte[] pixels = (byte[]) bp.getPixels();
        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] == CANDIDATE_MARK) {
                pixels[i] = BACKGROUND_MARK;
            }
        }
        animationStack.addSlice(title, bp);
    }


    /**
     * Create growth candidates from background neighbours of <code>point</code>.
     *
     * @param point seed point.
     */
    private void candidatesFromNeighbours(final Point point) {
        // Get neighbours of this seed that are not assigned to any region yet
        final List<Point> backgroundPoints = backgroundNeighbours(point);

        // Update SSL
        for (final Point p : backgroundPoints) {
            ssl.add(createCandidate(p));
        }
    }


    private Candidate createCandidate(final Point point) {
        final int offset = point.x + point.y * xSize;
        final float value = imagePixels[offset];

        // Mark as candidate
        regionMarkerPixels[offset] = CANDIDATE_MARK;

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
            double sigma = Math.abs(value - regionInfo.mean());
            if (sigma < minSigma) {
                minSigma = sigma;
                mostSimilarRegionId = regionID;
            }
        }

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
        if (v != BACKGROUND_MARK && v != CANDIDATE_MARK && v != OUTSIDE_MARK) {
            final int regionId = v & 0xff;
            r[regionId] = true;
        }
    }


    private void initializeStructures() {
        xSize = image.getWidth();
        ySize = image.getHeight();

        if (seeds == null) {
            throw new IllegalStateException("Seeds image is not set ['null'].");
        }

        if (seeds.getWidth() != xSize || seeds.getHeight() != ySize) {
            throw new IllegalArgumentException("Seeds image has to have the same dimension as input image.");
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
        imagePixels = (float[]) image.getPixels();
        animationStack = new ImageStack(xSize, ySize);

        seeds.setMask(mask);
        final ByteStatistics statistics = new ByteStatistics(seeds);
        int regionCount = 0;
        seedToRegonLookup = new int[MAX_REGION_NUMBER + 1];
        final int[] regionToSeedLooup = new int[MAX_REGION_NUMBER + 1];
        for (int seed = 1; seed < statistics.histogram.length; seed++) {
            if (statistics.histogram[seed] > 0) {
                if (seed > MAX_REGION_NUMBER) {
                    throw new IllegalArgumentException("Seed ID cannot be larger than " + MAX_REGION_NUMBER
                            + ", got " + seed + ".");
                }

                regionCount++;
                seedToRegonLookup[seed] = regionCount;
                regionToSeedLooup[regionCount] = seed;
            }

        }

        // Initialize region info structures
        regionInfos = new RegionInfo[regionCount + 1];
        for (int i = 1; i < regionInfos.length; i++) {
            regionInfos[i] = new RegionInfo(image, regionToSeedLooup[i]);
        }

        // Create candidate list and define rules for ordering of its elements
        ssl = new TreeSet<Candidate>();

        // Mark pixels outside of the mask
        fillOutsideMask(regionMarkerPixels, OUTSIDE_MARK);
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
        if (regionMarkerPixels[offset] == BACKGROUND_MARK) {
            // Add to unassigned pixels
            backgroundPoints.add(new Point(x, y));
        }
    }


    private static void validateNotNull(final Object object, final String name) throws IllegalArgumentException {
        if (object == null) {
            throw new IllegalArgumentException(
                    name != null
                            ? "Argument '" + name + "' cannot be null."
                            : "Argument cannot be null.");
        }
    }


    private static class RegionInfo {
        private long pointCount;
        private double sumIntensity;
        private final FloatProcessor image;
        private final int originalSeedID;

        public RegionInfo(final FloatProcessor image, final int originalSeedID) {
            this.image = image;
            this.originalSeedID = originalSeedID;
        }

        public void addPoint(final Point point) {
            ++pointCount;
            sumIntensity += image.getf(point.x, point.y);
        }

        public double mean() {
            if (pointCount == 0) {
                return 0;
            } else {
                return sumIntensity / pointCount;
            }
        }
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
            if (c == null) {
                throw new IllegalArgumentException("Argument 'c' cannot be null.");
            }
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
