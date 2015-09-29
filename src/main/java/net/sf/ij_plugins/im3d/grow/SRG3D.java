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

package net.sf.ij_plugins.im3d.grow;

import ij.IJ;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ByteStatistics;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.im3d.Point3DInt;
import net.sf.ij_plugins.io.IOUtils;
import net.sf.ij_plugins.util.IJDebug;
import net.sf.ij_plugins.util.Pair;
import net.sf.ij_plugins.util.Validate;
import net.sf.ij_plugins.util.progress.DefaultProgressReporter;

import java.io.File;
import java.io.IOException;
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
 * final ImageStack image = ...;
 * final Point3DInt[][] seeds = {
 *         {new Point3DInt(107, 144, 1)},   // Background
 *         {new Point3DInt(91, 159, 100)},  // Blob 1
 *         {new Point3DInt(119, 143, 51)},  // Blob 2
 *     };
 * final SRG3D srg = new SRG3D();
 * srg.setImage(image);
 * srg.setSeeds(seeds);
 * // Run growing
 * srg.run();
 * // Extract results
 * final ImageStack regionMask = srg.getRegionMarkers();
 * </pre>
 * <p>
 * If ImageJ is set to debug mode ({@code IJ.debugMode} property is {@code true}) additional information will be printed to
 * output window (using {@code IJ.log()}).
 * </p>
 * <p>
 * History of growing can be saved using {@link #setGrowHistoryEnabled(boolean)} and
 * {@link #setGrowHistoryDirectory(java.io.File)}.
 * Saved images represent internal representation of growing regions including {@code 0xff} marking of candidate voxels
 * (that were not yet assigned to a region).
 * </p>
 *
 * @author Jarek Sacha
 */
public final class SRG3D extends DefaultProgressReporter {

    private static final String NAME = "Seeded Region Growing";

    private static final Point3DInt NEIGHBOUR_OFFSET[] = {
            // 6-connected
            new Point3DInt(+0, +0, -1),
            new Point3DInt(-1, +0, +0),
            new Point3DInt(+1, +0, +0),
            new Point3DInt(+0, -1, +0),
            new Point3DInt(+0, +1, +0),
            new Point3DInt(+0, +0, +1),
            // 26-connected
            new Point3DInt(-1, -1, -1),
            new Point3DInt(-1, +0, -1),
            new Point3DInt(-1, +1, -1),
            new Point3DInt(+0, -1, -1),
            new Point3DInt(+0, +1, -1),
            new Point3DInt(+1, -1, -1),
            new Point3DInt(+1, +0, -1),
            new Point3DInt(+1, +1, -1),
            new Point3DInt(-1, -1, +0),
            new Point3DInt(+1, -1, +0),
            new Point3DInt(-1, +1, +0),
            new Point3DInt(+1, +1, +0),
            new Point3DInt(-1, -1, +1),
            new Point3DInt(-1, +0, +1),
            new Point3DInt(-1, +1, +1),
            new Point3DInt(+0, -1, +1),
            new Point3DInt(+0, +1, +1),
            new Point3DInt(+1, -1, +1),
            new Point3DInt(+1, +0, +1),
            new Point3DInt(+1, +1, +1),
    };

    // External properties
    private ImageStack image;
    private ImageStack seeds;
    private ImageStack regionMarkers;
    private ImageStack mask;
    private boolean growHistoryEnabled;
    private File growHistoryDirectory;

    // Internal variables
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    private int zMin;
    private int zMax;
    private int xSize;
    private int ySize;
    private int zSize;
    private byte[][] regionMarkerPixels;
    private byte[][] seedPixels;
    private float[][] imagePixels;

    private SortedSet<Candidate3D> ssl;
    private RegionInfo3D[] regionInfos;
    private long processedPixelCount;

    private final SRGSupport srgSupport = new SRGSupport();

    public static final String GROW_HISTORY_FILE_FORMAT = "SRG_markers_%03d.tif";


    /**
     * Set image to be segmented.
     *
     * @param image image.
     */
    public void setImage(final ImageStack image) {
        Validate.argumentNotNull(image, "image");
        this.image = image;
    }


    public void setSeeds(final ImageStack seeds) {
        Validate.argumentNotNull(seeds, "seeds");

        this.seeds = seeds;
    }


    /**
     * Set ROI mask, if not set full image is processed.
     *
     * @param mask Mask of the region of interest used for processing.
     */
    public void setMask(final ImageStack mask) {
        if (mask != null) {
            // TODO: validate that mask is of correct type
        }
        this.mask = mask;
    }


    public ImageStack getRegionMarkers() {
        return regionMarkers;
    }


    public boolean isGrowHistoryEnabled() {
        return growHistoryEnabled;
    }


    /**
     * Enable saving of images containing grow markers.
     * Output directory has to be selected using {@link #setGrowHistoryDirectory(java.io.File)} .
     *
     * @param growHistoryEnabled if {@code true} saving is enabled.
     */
    public void setGrowHistoryEnabled(final boolean growHistoryEnabled) {
        this.growHistoryEnabled = growHistoryEnabled;
    }


    public File getGrowHistoryDirectory() {
        return growHistoryDirectory;
    }


    /**
     * Directory where to save marker history growth. Format of each file name is: {@value #GROW_HISTORY_FILE_FORMAT}.
     *
     * @param growHistoryDirectory grow history directory.
     * @see #setGrowHistoryEnabled(boolean)
     */
    public void setGrowHistoryDirectory(final File growHistoryDirectory) {
        this.growHistoryDirectory = growHistoryDirectory;
    }


    /**
     * Perform region growing.
     */
    public void run() {

        this.notifyProgressListeners(0, NAME + " initailizing...");

        IJDebug.log("SRG3D.run - initializing structures");

        initializeStructures();

        // Initialize markers and create initial region info
        initializeMarkersAndRegionInfo();

        // Initialize candidates
        initializeCandidates();

        final long pixelsToProcess = (xMax - xMin) * (yMax - yMin) * (zMax - zMin);

        // Calculate increment, make sure that different/larger than 0 otherwise '%' operation will fail.
        final long progressIncrement = Math.max(pixelsToProcess / 100, 1);
        this.notifyProgressListeners(processedPixelCount / (double) pixelsToProcess);
        saveGrowHistory(processedPixelCount / (double) pixelsToProcess);

        IJDebug.log("SRG3D.run - process candidates");

        // Process candidates
        while (!ssl.isEmpty()) {
            // Get best candidate
            final Candidate3D c = ssl.first();
            // Remove it from the candidate set
            ssl.remove(c);

            final int offset = c.point.x + c.point.y * xSize;
            final byte marker = regionMarkerPixels[c.point.z][offset];
            assert marker == SRGSupport.CANDIDATE_MARK : "Expecting point (" + c.point.x + "," + c.point.y + ", " + c.point.z + ") to be candidate got " + (int) marker;

            // Add this point to its most similar region
            regionMarkerPixels[c.point.z][offset] = (byte) (c.mostSimilarRegionId & 0xff);

            // Update region info to include this point
            regionInfos[c.mostSimilarRegionId].addPoint(c.point);

            ++processedPixelCount;
            candidatesFromNeighbours(c.point);

            if (processedPixelCount % progressIncrement == 0) {
                assert processedPixelCount <= pixelsToProcess;
                final double progress = processedPixelCount / (double) pixelsToProcess;
                this.notifyProgressListeners(progress, NAME + " processing...");

                logProgressDebugInfo(pixelsToProcess, progress);

                saveGrowHistory(progress);
            }
        }

        IJDebug.log("SRG3D.run - encoding results");

        // Mark pixels outside of the mask as 0
        fillOutsideMask(regionMarkerPixels, (byte) 0);
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
     * <li>There must be at most 253 regions.</li>
     * <li>Each region has to have at least one seed point.</li>
     * <li>All seed points have to be unique.</li>
     * </ol>
     *
     * @param seeds region seeds.
     * @param xSize width of the seed image
     * @param ySize height of the seed image
     * @param zSize number of slices
     * @return seed image.
     */
    public static ImageStack toSeedImage(final Point3DInt[][] seeds, final int xSize, final int ySize, final int zSize) {
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

        final ImageStack seedImage = new ImageStack(xSize, ySize);
        for (int z = 0; z < zSize; z++) {
            seedImage.addSlice(Integer.toString(z + 1), new ByteProcessor(xSize, ySize));
        }
        for (int i = 0; i < seeds.length; i++) {
            final int region = i + 1;
            for (final Point3DInt seed : seeds[i]) {
                if (seed.x >= 0 && seed.x < xSize && seed.y >= 0 && seed.y < ySize) {
                    if (seedImage.getProcessor(seed.z + 1).get(seed.x, seed.y) == 0) {
                        seedImage.getProcessor(seed.z + 1).set(seed.x, seed.y, region);
                    } else {
                        throw new IllegalArgumentException(
                                "Duplicate seed at (" + seed.x + "," + seed.y + "," + seed.z + ") in region " + region + ".");
                    }
                } else {
                    throw new IllegalArgumentException("In region " + region +
                            " seed at (" + seed.x + "," + seed.y + ") is outside image " + xSize + "x" + ySize + "x" + zSize + ".");
                }

            }
        }

        return seedImage;
    }


    private void initializeMarkersAndRegionInfo() {
        for (int z = 0; z < zSize; ++z) {
            for (int y = 0; y < ySize; ++y) {
                for (int x = 0; x < xSize; ++x) {
                    final int offset = x + y * xSize;
                    if (regionMarkerPixels[z][offset] == SRGSupport.OUTSIDE_MARK) {
                        continue;
                    }
                    final int lookupIndex = seedPixels[z][offset] & 0xff;
                    final int regionID = srgSupport.seedToRegionLookup[lookupIndex];
                    if (regionID < 1) {
                        continue;
                    }

                    // Add seed to regionMarkers
                    regionMarkerPixels[z][offset] = (byte) (regionID & 0xff);

                    // Add seed to region info
                    regionInfos[regionID].addPoint(new Point3DInt(x, y, z));
                }
            }
        }
    }


    private void initializeCandidates() {
        final long pixelsToProcess = (xMax - xMin) * (yMax - yMin) * (zMax - zMin);

        // Calculate increment, make sure that different/larger than 0 otherwise '%' operation will fail.
        final long progressIncrement = Math.max(pixelsToProcess / 100, 1);

        for (int z = 0; z < zSize; ++z) {
            for (int y = 0; y < ySize; ++y) {
                for (int x = 0; x < xSize; ++x) {
                    final int offset = x + y * xSize;
                    final int regionId = regionMarkerPixels[z][offset];
                    if (regionId == SRGSupport.BACKGROUND_MARK
                            || regionId == SRGSupport.OUTSIDE_MARK
                            || regionId == SRGSupport.CANDIDATE_MARK) {
                        continue;
                    }

                    // Initialize SSL - ordered list of bordering at least one of the regions
                    candidatesFromNeighbours(new Point3DInt(x, y, z));

                    ++processedPixelCount;
                    if (processedPixelCount % progressIncrement == 0) {
                        assert processedPixelCount <= pixelsToProcess;
                        final double progress = processedPixelCount / (double) pixelsToProcess;
                        this.notifyProgressListeners(progress, NAME + " initializing...");

                        logProgressDebugInfo(pixelsToProcess, progress);
                    }
                }
            }
        }
    }


    private void saveGrowHistory(final double progress) {
        if (growHistoryEnabled && growHistoryDirectory != null) {
            final long progressPercent = Math.round(progress * 100);
            final File f = new File(growHistoryDirectory, String.format(GROW_HISTORY_FILE_FORMAT, progressPercent));
            IJ.log(String.format("Saving growth history at %d%% to %s", progressPercent, f.getAbsolutePath()));
            try {
                final File parent = f.getParentFile();
                if (parent.exists() || f.getParentFile().mkdirs()) {
                    IOUtils.saveAsTiff(regionMarkers, f);
                } else {
                    throw new IOException("Cannot create grow history directory: " + f.getParentFile().getAbsolutePath());
                }
            } catch (final IOException e) {
                IJ.log("Error saving grow history: " + e.getMessage());
            }
        }
    }


    private void logProgressDebugInfo(final long pixelsToProcess, final double progress) {
        final StringBuilder builder = new StringBuilder();
        builder.append(
                String.format(
                        "SRG3D.run - candidates: %,10d, processed: %,10d, remaining %,10d, [%3d%%] ",
                        ssl.size(),
                        processedPixelCount,
                        (pixelsToProcess - processedPixelCount - ssl.size()),
                        Math.round(progress * 100)));
        for (final RegionInfo3D regionInfo : regionInfos) {
            if (regionInfo != null) {
                builder.append(String.format("(%,10.4f, %,8d)", regionInfo.mean(), regionInfo.pointCount()));
            }
        }
        IJDebug.log(builder.toString());
    }


    private void fillOutsideMask(final byte[][] pixels, final byte value) {
        if (mask != null) {
            for (int z = 0; z < zMax; ++z) {
                SRGSupport.fillOutsideMask(pixels[z], value, (ByteProcessor) mask.getPixels(z + 1));
            }
        }
    }


    /**
     * Create growth candidates from background neighbours of <code>point</code>.
     *
     * @param point seed point.
     */
    private void candidatesFromNeighbours(final Point3DInt point) {
        // Get neighbours of this seed that are not assigned to any region yet
        final List<Point3DInt> backgroundPoints = backgroundNeighbours(point);

        // Update SSL
        for (final Point3DInt p : backgroundPoints) {
            ssl.add(createCandidate(p));
        }
    }


    private Candidate3D createCandidate(final Point3DInt point) {
        final int offset = point.x + point.y * xSize;
        final float value = imagePixels[point.z][offset];

        // Mark as candidate
        regionMarkerPixels[point.z][offset] = SRGSupport.CANDIDATE_MARK;

        // Get flags of neighbouring regions
        final boolean[] flags = neighbourRegionFlags(point);

        // Compute distance to most similar region
        double minSigma = Double.MAX_VALUE;
        int mostSimilarRegionId = -1;
        for (int regionID = 1; regionID < regionInfos.length; regionID++) {
            // Skip region if it is not a neighbour
            if (!flags[regionID]) {
                continue;
            }

            final RegionInfo3D regionInfo = regionInfos[regionID];
            final double sigma = Math.abs(value - regionInfo.mean());
            if (sigma < minSigma) {
                minSigma = sigma;
                mostSimilarRegionId = regionID;
            }
        }
        assert mostSimilarRegionId > 0;

        return new Candidate3D(point, mostSimilarRegionId, minSigma);
    }


    private boolean[] neighbourRegionFlags(final Point3DInt point) {
        final boolean[] r = new boolean[regionInfos.length];
        for (final Point3DInt offset : NEIGHBOUR_OFFSET) {
            assignRegionFlag(point.x + offset.x, point.y + offset.y, point.z + offset.z, r);
        }
        return r;
    }


    private void assignRegionFlag(final int x, final int y, final int z, final boolean[] r) {
        if (x < xMin || x >= xMax || y < yMin || y >= yMax || z < zMin || z >= zMax) {
            return;
        }

        final byte v = regionMarkerPixels[z][x + y * xSize];
        if (v != SRGSupport.BACKGROUND_MARK && v != SRGSupport.CANDIDATE_MARK && v != SRGSupport.OUTSIDE_MARK) {
            final int regionId = v & 0xff;
            r[regionId] = true;
        }
    }


    private void initializeStructures() {
        xSize = image.getWidth();
        ySize = image.getHeight();
        zSize = image.getSize();

        if (seeds == null) {
            throw new IllegalStateException("Seeds image is not set ['null'].");
        }

        if (seeds.getWidth() != xSize || seeds.getHeight() != ySize || seeds.getSize() != zSize) {
            throw new IllegalArgumentException("Seeds image has to have the same dimension as input image.");
        }

        // TODO: compare each mask slice to input image
        if (mask != null && (mask.getWidth() != xSize || mask.getHeight() != ySize || mask.getSize() != zSize)) {
            throw new IllegalArgumentException("Mask has to have the same dimension as input image.");
        }

        xMin = 0;
        xMax = xSize;
        yMin = 0;
        yMax = ySize;
        zMin = 0;
        zMax = zSize;

        regionMarkers = new ImageStack(xSize, ySize);
        regionMarkerPixels = new byte[zSize][];
        imagePixels = new float[zSize][];
        seedPixels = new byte[zSize][];
        for (int z = 0; z < zSize; z++) {
            final ByteProcessor bp = new ByteProcessor(xSize, ySize);
            regionMarkerPixels[z] = (byte[]) bp.getPixels();
            regionMarkers.addSlice("" + z, bp);
            imagePixels[z] = (float[]) image.getProcessor(z + 1).convertToFloat().getPixels();
            seedPixels[z] = (byte[]) seeds.getPixels(z + 1);
        }
        regionMarkers.setColorModel(seeds.getColorModel());

        final Pair<int[], Integer> p = srgSupport.createSeedToRegionLookup(histogram(seeds));
        final int[] regionToSeedLookup = p.getFirst();
        final int regionCount = p.getSecond();

        // Initialize region info structures
        regionInfos = new RegionInfo3D[regionCount + 1];
        for (int i = 1; i < regionInfos.length; i++) {
            regionInfos[i] = new RegionInfo3D(image, regionToSeedLookup[i]);
        }

        // Create candidate list and define rules for ordering of its elements
        ssl = new TreeSet<>();

        // Mark pixels outside of the mask
        fillOutsideMask(regionMarkerPixels, SRGSupport.OUTSIDE_MARK);
    }


    private int[] histogram(final ImageStack stack) {
        final int[] hist = new ByteStatistics(stack.getProcessor(1)).histogram;
        for (int i = 2; i <= stack.getSize(); i++) {
            final int[] hist1 = new ByteStatistics(stack.getProcessor(i)).histogram;
            for (int j = 0; j < hist.length; j++) {
                hist[j] += hist1[j];
            }
        }
        return hist;
    }


    private List<Point3DInt> backgroundNeighbours(final Point3DInt point) {
        final List<Point3DInt> backgroundPoints = new ArrayList<Point3DInt>(26);
        for (final Point3DInt offset : NEIGHBOUR_OFFSET) {
            addIfBackground(point.x + offset.x, point.y + offset.y, point.z + offset.z, backgroundPoints);
        }
        return backgroundPoints;
    }


    private void addIfBackground(final int x, final int y, final int z, final List<Point3DInt> backgroundPoints) {
        if (x < xMin || x >= xMax ||
                y < yMin || y >= yMax ||
                z < zMin || z >= zMax) {
            return;
        }

        final int offset = x + y * xSize;
        if (regionMarkerPixels[z][offset] == SRGSupport.BACKGROUND_MARK) {
            // Add to unassigned pixels
            backgroundPoints.add(new Point3DInt(x, y, z));
        }
    }


    private static class RegionInfo3D {

        private long pointCount;
        private double sumIntensity;
        final int originalSeedID;
        final ImageProcessor[] imageProcessors;


        public RegionInfo3D(final ImageStack image, final int originalSeedID) {
            this.originalSeedID = originalSeedID;
            imageProcessors = new ImageProcessor[image.getSize()];
            for (int i = 0; i < imageProcessors.length; i++) {
                imageProcessors[i] = image.getProcessor(i + 1);
            }
        }


        public void addPoint(final Point3DInt point) {
            ++pointCount;
            sumIntensity += imageProcessors[point.z].getf(point.x, point.y);
        }


        public double mean() {
            if (pointCount == 0) {
                return 0;
            } else {
                return sumIntensity / pointCount;
            }
        }


        public long pointCount() {
            return pointCount;
        }
    }


    private static class Candidate3D implements Comparable<Candidate3D> {

        public final Point3DInt point;
        public final int mostSimilarRegionId;
        public final double similarityDifference;


        public Candidate3D(final Point3DInt point, final int mostSimilarRegionId, final double similarityDifference) {
            this.point = point;
            this.mostSimilarRegionId = mostSimilarRegionId;
            this.similarityDifference = similarityDifference;
        }


        @Override
        public int compareTo(final Candidate3D c) {
            Validate.argumentNotNull(c, "c");

            if (point.compareTo(c.point) == 0) {
                return 0;
            } else if (similarityDifference < c.similarityDifference) {
                return -1;
            } else if (similarityDifference > c.similarityDifference) {
                return 1;
            } else {
                return point.compareTo(c.point);
            }
        }
    }

}