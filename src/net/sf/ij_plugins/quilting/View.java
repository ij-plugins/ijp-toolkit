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

import ij.process.ImageProcessor;

/**
 * This class is provides a view of an image. The view is a small part of the larger image. This
 * provides a way to pass around parts of an image without actually copying the pixels. This does
 * not ensure that the whole view is inside the image, so be careful.
 */
public class View {
    protected final ImageProcessor image;
    protected int xoffset,
            yoffset;


    /**
     * This creates a new view of the given image.
     *
     * @param image This is the image to view.
     * @param x     This is the x coord of the upper left corner of the view in image's pixel
     *              coordinates.
     * @param y     This is the x coord of the upper left corner of the view in image's pixel
     *              coordinates.
     */
    public View(final ImageProcessor image, final int x, final int y) {

        this.image = image;
        setCorner(x, y);
    }

    /**
     * This moves the view to the specified position.
     */
    public void setCorner(final int x, final int y) {
        this.xoffset = x;
        this.yoffset = y;
    }

    /**
     * returns the image x coordinate of the upper left corner of the view
     */
    public int getCornerX() {
        return xoffset;
    }

    /**
     * returns the image y coordinate of the upper left corner of the view
     */
    public int getCornerY() {
        return yoffset;
    }

    /**
     * returns true iff getCornerX() == 0
     */
    public boolean isAtLeftEdge() {
        return xoffset == 0;
    }

    /**
     * returns true iff getCornerY() == 0
     */
    public boolean isAtTopEdge() {
        return yoffset == 0;
    }

    /**
     * This fetches the RGB values from the given view coordinates.
     */
    public int[] getSample(int x, int y, int[] out) {
        // TODO: optimize pixel access
        x = imageX(x);
        y = imageY(y);
        if (out == null) {
            out = new int[3];
        }
        image.getPixel(x, y, out);
        return out;
    }

    /**
     * This sets the RGB sample at the given view coordinates.
     */
    public void putSample(int x, int y, final int[] newvals) {
        // TODO: optimize pixel access
        x = imageX(x);
        y = imageY(y);
        image.putPixel(x, y, newvals);
    }

    /**
     * This returns the image that this is a view of.
     */
    public ImageProcessor getImage() {
        return image;
    }

    /* This converts view coordinates into image coordinates. */
    protected int imageX(final int x) {
        return x + xoffset;
    }

    /* This converts view coordinates into image coordinates. */
    protected int imageY(final int y) {
        return y + yoffset;
    }

}
