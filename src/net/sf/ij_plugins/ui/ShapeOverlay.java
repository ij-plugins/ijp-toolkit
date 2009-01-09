/***
 * Image/J Plugins
 * Copyright (C) 2002-2009 Jarek Sacha
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

import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.ByteProcessor;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;

/**
 * Generalized representation of regions based on Java2D {@link Shape}. A region can be a mask, ROI, region of
 * interest, object detected in an image, or other concept that can be represented by a 2D shape.
 *
 * @author Jarek Sacha
 * @since Feb 11, 2008
 */
public class ShapeOverlay {
    private final String name;
    private final Shape shape;
    private final Stroke stroke;
    private final Color color;
    private final boolean fill;


    public ShapeOverlay(final String name, final Shape shape, final Color color, final boolean fill) {
        this(name, shape, color, fill, new BasicStroke());
    }


    public ShapeOverlay(final String name, final Shape shape, final Color color, final boolean fill, final Stroke stroke) {
        this.name = name;
        this.shape = shape;
        this.color = color;
        this.fill = fill;
        this.stroke = stroke;
    }


    public String getName() {
        return name;
    }


    public Shape getShape() {
        return shape;
    }


    public Stroke getStroke() {
        return stroke;
    }


    public Color getColor() {
        return color;
    }


    public boolean isFill() {
        return fill;
    }


    public static Shape toShape(final Roi roi) {
        final Shape result;
        if (roi instanceof PointRoi) {
            final ByteProcessor mask = (ByteProcessor) roi.getMask();
            final byte[] maskPixels = (byte[]) mask.getPixels();
            final Rectangle maskBounds = roi.getBounds();
            final int maskWidth = mask.getWidth();
            final int maskHeight = mask.getHeight();
            final Area area = new Area();
            for (int y = 0; y < maskHeight; y++) {
                final int yOffset = y * maskWidth;
                for (int x = 0; x < maskWidth; x++) {
                    if (maskPixels[x + yOffset] != 0) {
                        area.add(new Area(new Rectangle(x + maskBounds.x, y + maskBounds.y, 1, 1)));
                    }
                }
            }
            result = area;
        } else {
            result = makeShapeFromArray(new ShapeRoi(roi).getShapeAsArray());
        }

        return result;
    }


    private static Shape makeShapeFromArray(final float[] array) {
        final Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        int index = 0;
        while (true) {
            final float[] segment = new float[7];
            final int len = extractSegment(array, segment, index);
            if (len < 0) {
                break;
            }
            index += len;
            final int type = (int) segment[0];
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    path.moveTo(segment[1], segment[2]);
                    break;
                case PathIterator.SEG_LINETO:
                    path.lineTo(segment[1], segment[2]);
                    break;
                case PathIterator.SEG_QUADTO:
                    path.quadTo(segment[1], segment[2], segment[3], segment[4]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    path.curveTo(segment[1], segment[2], segment[3], segment[4], segment[5], segment[6]);
                    break;
                case PathIterator.SEG_CLOSE:
                    path.closePath();
                    break;
                default:
                    break;
            }
        }
        return path;
    }


    private static int extractSegment(final float[] array, final float[] segment, int index) {
        final int length = array.length;

        if (index >= length)
            return -1;

        segment[0] = array[index++];
        final int type = (int) segment[0];
        if (type == PathIterator.SEG_CLOSE)
            return 1;
        if (index >= length)
            return -1;
        segment[1] = array[index++];
        if (index >= length)
            return -1;
        segment[2] = array[index++];
        if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO)
            return 3;
        if (index >= length)
            return -1;
        segment[3] = array[index++];
        if (index >= length)
            return -1;
        segment[4] = array[index++];
        if (type == PathIterator.SEG_QUADTO)
            return 5;
        if (index >= length)
            return -1;
        segment[5] = array[index++];
        if (index >= length)
            return -1;
        segment[6] = array[index];
        if (type == PathIterator.SEG_CUBICTO)
            return 7;
        return -1;
    }

}
