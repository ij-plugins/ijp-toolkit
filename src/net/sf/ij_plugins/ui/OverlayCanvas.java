/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import net.sf.ij_plugins.util.Validate;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;


/**
 * Custom ImageJ's image canvas for display of overlays.
 *
 * @author Jarek Sacha
 * @since Feb 11, 2008
 */
public final class OverlayCanvas extends ImageCanvas {

    private Iterable<ShapeOverlay> overlays = new ArrayList<ShapeOverlay>();

    private static final long serialVersionUID = 4763707694765672142L;


    public OverlayCanvas(final ImagePlus imp) {
        super(imp);
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (ShapeOverlay overlay : overlays) {
            draw(g, overlay);
        }
    }


    public void draw(final Graphics g, final ShapeOverlay overlay) {
        final double scale = getMagnification();

        final Graphics2D g2D = (Graphics2D) g;
        g2D.setColor(overlay.getColor());

        final AffineTransform transform = new AffineTransform();
        transform.translate(screenX(0), screenY(0));
        transform.scale(scale, scale);

        final Shape shape = overlay.getShape();
        final Shape transformedShape = transform.createTransformedShape(shape);

        if (overlay.isFill()) {
            g2D.fill(transformedShape);
        } else {
            g2D.draw(transformedShape);
        }

    }


    public void setOverlays(final Iterable<ShapeOverlay> overlays) {
        Validate.argumentNotNull(overlays, "overlays");
        this.overlays = overlays;
    }
}
