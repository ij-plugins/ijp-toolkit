/***
 * Image/J Plugins
 * Copyright (C) 2002,2003 Jarek Sacha
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
package net.sf.ij.im3d.grow;

import ij.process.ByteProcessor;
import ij.ImageStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Seeged region growing algorithm based on article by Rolf Adams and Leanne
 * Bischof, "Seeded Region Growing", <i>IEEE Transactions on Pattern
 * Analysis and Machine Intelligence</i>, vol. 16, no. 6, June 1994.
 *
 * The algorithms resutes that seeds for objects and background be provided.
 * The condition of growth is difference of a gray level of a candidate pixel
 * and mean grey level intensity of a neighboring region. At each step of the
 * algorithm a candidate with a smalles diffetence to some neighboring region
 * is added to that region and all neighboring points of that that are not yet
 * assigned to any region are added to candidate list.
 *
 * @author  Jarek Sacha
 * @version $ Revision: $
 */
public class SRG {

  private static final int MAX_REGION_NUMBER = 254;
  private static final byte BACKGROUND_MARK = (byte) 0x00;
  private static final byte CANDIDATE_MARK = (byte) 0xff;

  // External properties
  private ByteProcessor image;
  private Point[][] seeds;
  private ByteProcessor regionMask;
  private ImageStack animationStack;
  private int nbAnimationFrames = 0;


  // Internal variables
  private int xMin;
  private int xMax;
  private int yMin;
  private int yMax;
  private int xSize;
  private int ySize;
  private byte[] regionMaskPixels;
  private byte[] imagePixels;

  private SortedSet ssl;
  private RegionInfo[] regionInfos;
  private long processedPixelCount = 0;


  public void setImage(ByteProcessor image) {
    this.image = image;
  }

  public void setSeeds(Point[][] seeds) {
    this.seeds = seeds;
  }

  public void setNumberOfAnimationFrames(int nbAnimationFrames) {
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
      Point[] regionSeeds = seeds[i];
      RegionInfo thisRegionInfo = regionInfos[i];
      final int regionId = i + 1;
      for (int j = 0; j < regionSeeds.length; j++) {
        Point seed = regionSeeds[j];
        int offset = seed.x + seed.y * xSize;

        // Verify seeding consistency
        if (regionMaskPixels[offset] != 0) {
          int oldRegionId = regionMaskPixels[offset] & 0xff + 1;
          throw new IllegalArgumentException("Single point have two regions assignemts. "
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
    for (int i = 0; i < seeds.length; i++) {
      Point[] regionSeeds = seeds[i];
      for (int j = 0; j < regionSeeds.length; j++) {
        candidatesFromNeighbours(regionSeeds[j]);
      }
    }

    if (nbAnimationFrames > 1) {
      addAnimationFrame("Seeds", (ByteProcessor) regionMask.duplicate());
    }

    long frameIncrement = this.nbAnimationFrames > 2
        ? ((xMax - xMin) * (yMax - yMin)) / (nbAnimationFrames-2)
        : Long.MAX_VALUE;


    // Process cansidates
    while (!ssl.isEmpty()) {
      // Get best candidate
      Candidate c = (Candidate) ssl.first();
      // Remove it from the candidate set
      ssl.remove(c);

      // Add this point to its most simillar region
      regionMaskPixels[c.point.x + c.point.y * xSize] = (byte) (c.mostSimillarRegionId & 0xff);

      // Update region info to include this point
      regionInfos[c.mostSimillarRegionId - 1].addPoint(c.point);

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

  private void addAnimationFrame(String title, ByteProcessor bp) {
    byte[] pixels = (byte[]) bp.getPixels();
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
   * @param point
   */
  private void candidatesFromNeighbours(Point point) {
    // Get neighbours of this seed that are not assigned to any region yet
    ArrayList backgroundPoints = backgroundNeighbours(point);

    // Update SSL
    for (int k = 0; k < backgroundPoints.size(); k++) {
      Point p = (Point) backgroundPoints.get(k);
      ssl.add(createCandidate(p));
    }
  }

  private Candidate createCandidate(Point point) {
    int offset = point.x + point.y * xSize;
    int value = imagePixels[offset] & 0xff;

    // Mark as candidate
    regionMaskPixels[offset] = CANDIDATE_MARK;

    // Get flags of neighbouring regions
    boolean[] flags = neighbourRegionFlags(point);


    // Compute distance to most simillar region
    double minSigma = Double.MAX_VALUE;
    int mostSimillarRegionId = -1;
    for (int i = 0; i < regionInfos.length; i++) {
      // Skip region if it is not a neighbour
      if (!flags[i])
        continue;

      RegionInfo regionInfo = regionInfos[i];
      double sigma = Math.abs(value - regionInfo.mean());
      if (sigma < minSigma) {
        minSigma = sigma;
        mostSimillarRegionId = i + 1;
      }
    }

    Candidate candidate = new Candidate(point, mostSimillarRegionId, minSigma);
    return candidate;
  }

  private boolean[] neighbourRegionFlags(Point point) {
    boolean[] r = new boolean[regionInfos.length];
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

  private void assignRegionFlag(int x, int y, boolean[] r) {
    if (x < xMin || x >= xMax ||
        y < yMin || y >= yMax) {
      return;
    }
    byte v = regionMaskPixels[x + y * xSize];
    if (v != BACKGROUND_MARK && v != CANDIDATE_MARK) {
      int regionId = v & 0xff;
      r[regionId - 1] = true;
    }
  }


  private void initializeStructures() {
    final int nbRegions = seeds.length;
    // Verify that we can feet all region ID in the regionMask.
    if (nbRegions > MAX_REGION_NUMBER) {
      throw new IllegalArgumentException("Numbrer of regions cannnot be larger than "
          + MAX_REGION_NUMBER + ".");
    }
    regionInfos = new RegionInfo[nbRegions];

    xSize = image.getWidth();
    ySize = image.getHeight();
    xMin = 0;
    xMax = xSize;
    yMin = 0;
    yMax = ySize;
    regionMask = new ByteProcessor(xSize, ySize);
    regionMaskPixels = (byte[]) regionMask.getPixels();
    imagePixels = (byte[]) image.getPixels();
    animationStack = new ImageStack(xSize, ySize);


    // Initialize region info structures
    for (int i = 0; i < seeds.length; i++) {
      RegionInfo thisRegionInfo = new RegionInfo(image);
      regionInfos[i] = thisRegionInfo;
    }


    // Create candidate list and define rules for ordring of its elements
    ssl = new TreeSet(new Comparator() {
      public int compare(Object o1, Object o2) {
        Candidate c1 = (Candidate) o1;
        Candidate c2 = (Candidate) o2;
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

  private ArrayList backgroundNeighbours(Point point) {
    ArrayList backgroundPoints = new ArrayList(7);
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

  final protected void addIfBackground(int x, int y, ArrayList backgroundPoints) {
    if (x < xMin || x >= xMax ||
        y < yMin || y >= yMax) {
      return;
    }

    int offset = x + y * xSize;
    if (regionMaskPixels[offset] == BACKGROUND_MARK) {
      // Add to unassigned pixels
      backgroundPoints.add(new Point(x, y));
    }
  }


  private static class RegionInfo {
    long pointCount = 0;
    double sumIntensity = 0.0;
    ByteProcessor image;

    public RegionInfo(ByteProcessor image) {
      this.image = image;
    }

    public void addPoint(Point point) {
      ++pointCount;
      sumIntensity += image.getPixel(point.x, point.y);
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
    public Point point;
    public int mostSimillarRegionId;
    public double similarityDifference;

    public Candidate(Point point, int mostSimillarRegionId, double similarityDifference) {
      this.point = point;
      this.mostSimillarRegionId = mostSimillarRegionId;
      this.similarityDifference = similarityDifference;
    }
  }

}
