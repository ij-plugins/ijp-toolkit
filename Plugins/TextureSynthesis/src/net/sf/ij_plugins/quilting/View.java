/***
 * Copyright (C) 2002 Nick Vavra
 *
 * Image/J Plugins
 * Copyright (C) 2004 Jarek Sacha
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

    protected ImageProcessor image;
    protected int xoffset, yoffset;


//    // this is used in the constructor, see the comment there
//    private static boolean invalid_indices = true;

    /**
     * This creates a new view of the given image.
     *
     * @param image This is the image to view.
     * @param x     This is the x coord of the upper left corner of the view in image's pixel
     *              coordinates.
     * @param y     This is the x coord of the upper left corner of the view in image's pixel
     *              coordinates.
     */
    public View(ImageProcessor image, int x, int y) {

        this.image = image;
        setCorner(x, y);
    }

    /**
     * This moves the view to the specified position.
     */
    public void setCorner(int x, int y) {
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
        // FIXME: pixel access
        x = imageX(x);
        y = imageY(y);
        if (out == null) {
            out = new int[3];
        }
        image.getPixel(x, y, out);
        return out;
    }

//    /**
//     * Get the sample from the (x,y) view coordinate from the given channel.
//     */
//    public int getSample(int channel, int x, int y) {
//        x = imageX(x);
//        y = imageY(y);
//        return image.getSample(channel, x, y);
//    }

    /**
     * This sets the RGB sample at the given view coordinates.
     */
    public void putSample(int x, int y, int[] newvals) {
        // FIXME: pixel access
        x = imageX(x);
        y = imageY(y);
        image.putPixel(x, y, newvals);
    }

//    /**
//     * Set the sample at the (x,y) view coordinate for the given channel.
//     */
//    public void putSample(int channel, int x, int y, int newval) {
//        x = imageX(x);
//        y = imageY(y);
//        image.putSample(channel, x, y, newval);
//    }

    /**
     * This returns the image that this is a view of.
     */
    public ImageProcessor getImage() {
        return image;
    }

    /* This converts view coordinates into image coordinates. */
    protected int imageX(int x) {
        return x + xoffset;
    }

    /* This converts view coordinates into image coordinates. */
    protected int imageY(int y) {
        return y + yoffset;
    }

}
