/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
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
import ij.process.ImageProcessor;

/**
 * This is a rectangular patch of pixels belonging to a image.
 */
public class Patch extends View {
    private final int width;
    private final int height;

    /**
     * This makes a patch based on the given dimensions and view parameters.
     */
    public Patch(final ImageProcessor image, final int x, final int y, final int width, final int height) {
        super(image, x, y);
        this.width = width;
        this.height = height;
        setCorner(x, y);
    }

    /**
     * This moves the patch to have the upper left corner at (x,y) in image coordinates.
     */
    @Override
    public void setCorner(final int x, final int y) {
        if (x + width > image.getWidth() || y + height > image.getHeight()
                || x < 0 || y < 0) {
            throw new IllegalArgumentException("can not create a patch there");
        }
        super.setCorner(x, y);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Bounds checks the input to make sure it is part of the patch and then gets the sample from
     * the view.
     */
    @Override
    public int[] getSample(final int x, final int y, final int[] out) {
        // TODO: optimize pixel access
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return super.getSample(x, y, out);
        }
        throw new IllegalArgumentException("Attempted to get (" + x + "," + y
                + ") from " + toString());
    }

    /**
     * Bounds checks the input to make sure it is part of the patch and then puts the sample into
     * the view.
     */
    @Override
    public void putSample(final int x, final int y, final int values[]) {
        // TODO: optimize pixel access
        if (x >= 0 && x < width && y >= 0 && y < height) {
            super.putSample(x, y, values);
        } else {
            throw new IllegalArgumentException("Attempted to put (" + x + "," + y
                    + ") in " + toString());
        }
    }

    /**
     * returns a string with the corner, width, and height listed
     */
    @Override
    public String toString() {
        return "patch with corner (" + xoffset + "," + yoffset + ") width "
                + width + " height " + height;
    }

    /**
     * This moves the patch one pixel to the right if able. If the move fails (because it puts part
     * of the patch out of bounds) then this returns false.
     */
    public boolean rightOnePixel() {
        if (xoffset + width < image.getWidth()) {
            setCorner(xoffset + 1, yoffset);
            return true;
        }
        return false;
    }

    /**
     * This moves the patch one pixel down and all the way to the left. If the move fails (because
     * it puts part of the patch out of bounds) then this returns false.
     */
    public boolean nextPixelRow() {
        if (yoffset + height < image.getHeight()) {
            setCorner(0, yoffset + 1);
            return true;
        }
        return false;
    }

    /**
     * This moves the patch to the left side of the image and down by height-overlap. If the move
     * fails, this returns false.
     */
    public boolean nextRow(final int overlap) {
        final int newy = yoffset + height - overlap;
        if (IJ.debugMode) {
            IJ.log("newy = " + newy);
            IJ.log("height = " + image.getHeight());
        }
        if (newy + height > image.getHeight()) {
            return false;
        }
        setCorner(0, newy);
        return true;
    }

    /**
     * This moves the patch to the right in this row of patches if able. The amount moved will be
     * width-overlap. This returns false on failure.
     */
    public boolean nextColumn(final int overlap) {
        final int newx = xoffset + width - overlap;
        if (newx + width > image.getWidth()) {
            return false;
        }
        setCorner(newx, yoffset);
        return true;
    }

    public int getXOffset() {
        return xoffset;
    }

    public int getYOffset() {
        return yoffset;
    }
}
