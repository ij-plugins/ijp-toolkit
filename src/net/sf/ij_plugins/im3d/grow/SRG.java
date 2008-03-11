/***
 * Image/J Plugins
 * Copyright (C) 2002-2008 Jarek Sacha
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
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Seeded region growing algorithm based on article by Rolf Adams and Leanne Bischof, "Seeded Region
 * Growing", <i>IEEE Transactions on Pattern Analysis and Machine Intelligence</i>, vol. 16, no. 6,
 * June 1994.
 * <p/>
 * The algorithms assumes that seeds for objects and background be provided. The condition of growth
 * is difference of a gray level of a candidate pixel and mean grey level intensity of a neighboring
 * region. At each step of the algorithm a candidate with a smallest difference to some neighboring
 * region is added to that region and all neighboring points of that that are not yet assigned to
 * any region are added to candidate list.
 *
 * @author Jarek Sacha
 */
public final class SRG {
    private static final int MAX_REGION_NUMBER = 254;
    private static final byte BACKGROUND_MARK = (byte) 0x00;
    private static final byte CANDIDATE_MARK = (byte) 0xff;

    // External properties
    private FloatProcessor image;
    private Point[][] seeds;
    private ByteProcessor regionMask;
    private ImageStack animationStack;
    private int nbAnimationFrames;


    // Internal variables
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    private int xSize;
    private byte[] regionMaskPixels;
    private float[] imagePixels;

    private SortedSet<Candidate> ssl;
    private RegionInfo[] regionInfos;
    private long processedPixelCount;


    public void setImage(final ByteProcessor image) {
        this.image = (FloatProcessor) image.convertToFloat();
    }

    public void setImage(final ShortProcessor image) {
        this.image = (FloatProcessor) image.convertToFloat();
    }

    public void setImage(final FloatProcessor image) {
        this.image = (FloatProcessor) image.convertToFloat();
    }

    public void setSeeds(final Point[][] seeds) {
        this.seeds = seeds;
    }

    public void setNumberOfAnimationFrames(final int nbAnimationFrames) {
        this.nbAnimationFrames = nbAnimationFrames;
    }

    public ByteProcessor getRegionMask() {
        return regionMask;
    }

    public ImageStack getAnimationStack() {
        return animationStack;
    }

    /**
     * Perform region growing.
     */
    public void run() {
        initializeStructures();

        // Mask seeds and create initial region info
        for (int i = 0; i < seeds.length; i++) {
            final Point[] regionSeeds = seeds[i];
            final RegionInfo thisRegionInfo = regionInfos[i];
            final int regionId = i + 1;
            for (final Point seed : regionSeeds) {
                final int offset = seed.x + seed.y * xSize;

                // Verify seeding consistency
                final int oldRegionId = regionMaskPixels[offset] & 0xff;
                if (oldRegionId != 0 && oldRegionId != regionId) {
                    throw new IllegalArgumentException("Single point have two regions assignments. "
                            + "Point (" + seed.x + "," + seed.y + ") is assigned both to region " + oldRegionId
                            + " and region " + regionId + ".");
                }

                // Add seed to regionMask
                regionMaskPixels[offset] = (byte) (regionId & 0xff);

                // Add seed to region info
                thisRegionInfo.addPoint(seed);

                ++processedPixelCount;
            }
        }

        // Initialize SSL - ordered list of bordering at least one of the regions
        for (final Point[] regionSeeds : seeds) {
            for (final Point regionSeed : regionSeeds) {
                candidatesFromNeighbours(regionSeed);
            }
        }

        if (nbAnimationFrames > 1) {
            addAnimationFrame("Seeds", (ByteProcessor) regionMask.duplicate());
        }

        final long frameIncrement = this.nbAnimationFrames > 2
                ? ((xMax - xMin) * (yMax - yMin)) / (nbAnimationFrames - 2)
                : Long.MAX_VALUE;

        // Process candidates
        while (!ssl.isEmpty()) {
            // Get best candidate
            final Candidate c = ssl.first();
            // Remove it from the candidate set
            ssl.remove(c);

            // Add this point to its most similar region
            regionMaskPixels[c.point.x + c.point.y * xSize] = (byte) (c.mostSimilarRegionId & 0xff);

            // Update region info to include this point
            regionInfos[c.mostSimilarRegionId - 1].addPoint(c.point);

            ++processedPixelCount;

            candidatesFromNeighbours(c.point);

            if (processedPixelCount % frameIncrement == 0) {
                addAnimationFrame(null, (ByteProcessor) regionMask.duplicate());
            }
        }

        if (nbAnimationFrames > 0) {
            addAnimationFrame("Final regions", (ByteProcessor) regionMask.duplicate());
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
        regionMaskPixels[offset] = CANDIDATE_MARK;

        // Get flags of neighbouring regions
        final boolean[] flags = neighbourRegionFlags(point);

        // Compute distance to most similar region
        double minSigma = Double.MAX_VALUE;
        int mostSimilarRegionId = -1;
        for (int i = 0; i < regionInfos.length; i++) {
            // Skip region if it is not a neighbour
            if (!flags[i])
                continue;

            final RegionInfo regionInfo = regionInfos[i];
            double sigma = Math.abs(value - regionInfo.mean());
            if (sigma < minSigma) {
                minSigma = sigma;
                mostSimilarRegionId = i + 1;
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
        if (x < xMin || x >= xMax ||
                y < yMin || y >= yMax) {
            return;
        }
        final byte v = regionMaskPixels[x + y * xSize];
        if (v != BACKGROUND_MARK && v != CANDIDATE_MARK) {
            int regionId = v & 0xff;
            r[regionId - 1] = true;
        }
    }


    private void initializeStructures() {
        final int nbRegions = seeds.length;
        // Verify that we can feet all region ID in the regionMask.
        if (nbRegions > MAX_REGION_NUMBER) {
            throw new IllegalArgumentException("Number of regions cannot be larger than " + MAX_REGION_NUMBER + ".");
        }
        regionInfos = new RegionInfo[nbRegions];

        xSize = image.getWidth();
        final int ySize = image.getHeight();
        xMin = 0;
        xMax = xSize;
        yMin = 0;
        yMax = ySize;
        regionMask = new ByteProcessor(xSize, ySize);
        regionMaskPixels = (byte[]) regionMask.getPixels();
        imagePixels = (float[]) image.getPixels();
        animationStack = new ImageStack(xSize, ySize);

        // Initialize region info structures
        for (int i = 0; i < seeds.length; i++) {
            final RegionInfo thisRegionInfo = new RegionInfo(image);
            regionInfos[i] = thisRegionInfo;
        }

        // Create candidate list and define rules for ordering of its elements
        ssl = new TreeSet<Candidate>(new Comparator<Candidate>() {
            public int compare(final Candidate c1, final Candidate c2) {
                if (c1.point.equals(c2.point)) {
                    return 0;
                } else if (c1.similarityDifference < c2.similarityDifference) {
                    return -1;
                } else if (c1.similarityDifference > c2.similarityDifference) {
                    return 1;
                } else if (c1.point.x < c2.point.x) {
                    return -1;
                } else if (c1.point.x > c2.point.x) {
                    return 1;
                } else if (c1.point.y < c2.point.y) {
                    return -1;
                } else if (c1.point.y > c2.point.y) {
                    return 1;
                } else {
                    throw new IllegalStateException("This condition should never happen.");
                }
            }
        });
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
        if (regionMaskPixels[offset] == BACKGROUND_MARK) {
            // Add to unassigned pixels
            backgroundPoints.add(new Point(x, y));
        }
    }


    private static class RegionInfo {
        private long pointCount;
        private double sumIntensity;
        private final FloatProcessor image;

        public RegionInfo(FloatProcessor image) {
            this.image = image;
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

    private static class Candidate {
        public final Point point;
        public final int mostSimilarRegionId;
        public final double similarityDifference;

        public Candidate(final Point point, final int mostSimilarRegionId, final double similarityDifference) {
            this.point = point;
            this.mostSimilarRegionId = mostSimilarRegionId;
            this.similarityDifference = similarityDifference;
        }
    }

}
