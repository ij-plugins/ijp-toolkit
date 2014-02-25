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
package net.sf.ij_plugins.im3d.grow;

import ij.ImageStack;
import net.sf.ij_plugins.im3d.Point3DInt;

import java.util.LinkedList;

/**
 * Simple region growing algorithm that extracts all pixels connected to the seed as long as they
 * intensities are within given threshold limits. Min limit is inclusive, max limit is exclusive.
 *
 * @author Jarek Sacha
 * @since April 29, 2002
 */

public abstract class ConnectedThresholdFilterBase {
    /**
     * Value of a member pixel in output image
     */
    public static final byte MARKER = (byte) 0xff;

    /**
     * Value of background (not member) pixel in output image
     */
    public static final byte BACKGROUND = (byte) 0x00;

    /**
     * Used to mark pixels that were determined to not be members. This value is changed to
     * BACKGROUND in the output image.
     */
    protected static final byte NOT_MEMBER = (byte) 0x01;

    /**
     * Pixels of the destination image
     */
    protected byte[][] destPixels = null;

    /**
     * Grow candidates
     */
    protected LinkedList<Point3DInt> candidatePoints = new LinkedList<>();

    protected int xSize, ySize, zSize;
    protected int xMin, xMax;
    protected int yMin, yMax;
    protected int zMin, zMax;
    protected int valueMin, valueMax;


    /**
     * Set max threshold value.
     *
     * @param valueMax The new ValueMax value
     */
    public final void setValueMax(final int valueMax) {
        this.valueMax = valueMax;
    }


    /**
     * Set min threshold value.
     *
     * @param valueMin The new ValueMin value
     */
    public final void setValueMin(final int valueMin) {
        this.valueMin = valueMin;
    }


    /**
     * Gets the ValueMax attribute of the ConnectedThresholdFilter object
     *
     * @return The ValueMax value
     */
    public final int getValueMax() {
        return valueMax;
    }


    /**
     * Gets the ValueMin attribute of the ConnectedThresholdFilter object
     *
     * @return The ValueMin value
     */
    public final int getValueMin() {
        return valueMin;
    }


    /**
     * Main processing method for the ConnectedThresholdFilter object
     *
     * @param src  Input image.
     * @param seed Seed point.
     * @return Image in which extracted pixels have value MARKER all other pixels have value
     * BACKGROUND.
     */
    public final ImageStack run(final ImageStack src, final Point3DInt seed) {

        initialize(src);

        // Verify that seed point can be a member point.
        checkForGrow(seed.x, seed.y, seed.z);

        // Iterate while there are still candidates to check.
        while (!candidatePoints.isEmpty()) {
            final Point3DInt p = candidatePoints.removeFirst();
            checkForGrow(p.x - 1, p.y, p.z);
            checkForGrow(p.x + 1, p.y, p.z);
            checkForGrow(p.x, p.y - 1, p.z);
            checkForGrow(p.x, p.y + 1, p.z);
            checkForGrow(p.x, p.y, p.z - 1);
            checkForGrow(p.x, p.y, p.z + 1);
        }

        // Remove NOT_MEMBER markers and create output image.
        return createOutputStack();
    }

    /*
    *
    */

    /**
     * Description of the Method
     *
     * @param src Description of the Parameter
     */
    private void initialize(final ImageStack src) {
        createHandleToSrcPixels(src);

        candidatePoints.clear();
        xSize = src.getWidth();
        xMin = 0;
        xMax = xSize;
        ySize = src.getHeight();
        yMin = 0;
        yMax = ySize;
        zSize = src.getSize();
        zMin = 0;
        zMax = zSize;

        destPixels = new byte[zSize][];
        final int sliceSize = xSize * ySize;
        for (int z = 0; z < zSize; ++z) {
            destPixels[z] = new byte[sliceSize];
        }
    }


    /**
     * Create handle to source pixels, e.g. byte[][] srcPixels. This method is called at the
     * beginning of the initialize method. This method should only modify handle to source pixels that
     * is defined in the derived class. This handle is used by checkForGrow()
     *
     * @param src source stack.
     */
    protected abstract void createHandleToSrcPixels(final ImageStack src);


    /**
     * Check if point with coordinates (x,y,z) is a new candidate. Point is a candidate if 1) its
     * coordinates are within ROI, 2) it was not yet analyzed, 3) its value is within limits. <p>
     * <br>
     * This method modifies 'candidatePoints' and 'destPixels'.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    protected abstract void checkForGrow(final int x, final int y, final int z);


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    private ImageStack createOutputStack() {
        final ImageStack dest = new ImageStack(xSize, ySize);
        final int sliceSize = xSize * ySize;
        for (int z = 0; z < zSize; ++z) {
            final byte[] slicePixels = destPixels[z];
            for (int i = 0; i < sliceSize; ++i) {
                if (slicePixels[i] == NOT_MEMBER) {
                    slicePixels[i] = BACKGROUND;
                }
            }
            dest.addSlice(null, slicePixels);
        }

        return dest;
    }

}
