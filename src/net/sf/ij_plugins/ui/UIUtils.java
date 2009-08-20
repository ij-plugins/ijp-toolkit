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

package net.sf.ij_plugins.ui;

import ij.*;

import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

/**
 * Miscellaneous UI utilities.
 *
 * @author Jarek Sacha
 * @since Feb 16, 2008
 */
public final class UIUtils {
    private UIUtils() {
    }

    /**
     * Create icon of given size and color.
     *
     * @param width  width.
     * @param height height.
     * @param color  color.
     * @return new icon.
     */
    public static Icon createColorIcon(final int width, final int height, final Color color) {

        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();

        return new ImageIcon(image);
    }


    /**
     * Return image in currently selected ImageJ window, or {@code null} is no image window is selected.
     *
     * @return currently selected image or {@code null}.
     */
    public static ImagePlus getImage() {
        final ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            // TODO: throw exception on error.
            error("There are no images open.");
            return null;
        } else
            return imp;
    }


    /**
     * Returns icon used by ImageJ main frame. Returns {@code null} if main frame is not instantiated or has no icon.
     *
     * @return ImageJ icon or {@code null}.
     */
    public static Image getImageJIconImage() {
        // Set ImageJ icon
        final ImageJ imageJ = IJ.getInstance();
        return imageJ != null ? imageJ.getIconImage() : null;
    }


    /**
     * Show error message and abort macro execution.
     *
     * @param msg error message.
     */
    public static void error(final String msg) {
        IJ.error(msg);
        Macro.abort();
    }


    /**
     * Create index color model from a list of colors.
     *
     * @param colors list of colors.
     * @return index color model.
     */
    public static IndexColorModel createIndexColorModel(final java.util.List<Color> colors) {
        final int mapSize = 256;
        byte[] reds = new byte[mapSize];
        byte[] greens = new byte[mapSize];
        byte[] blues = new byte[mapSize];
        for (int i = 0; i < mapSize; i++) {
            final Color color = colors.get(i % colors.size());
            reds[i] = (byte) (color.getRed() & 0xff);
            greens[i] = (byte) (color.getGreen() & 0xff);
            blues[i] = (byte) (color.getBlue() & 0xff);
        }
        return new IndexColorModel(8, mapSize, reds, greens, blues);
    }
}
