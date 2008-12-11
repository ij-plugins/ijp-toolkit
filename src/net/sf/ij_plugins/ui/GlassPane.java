/*
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
 *
 */
package net.sf.ij_plugins.ui;

import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;

/**
 * GlassPane it is transparent for MouseEvents,
 * and respects underneath component's cursors by default,
 * it is also friedly for other users,
 * if someone adds a mouseListener to this GlassPane
 * or set a new cursor it will respect them
 * <p/>
 * Inspired on class created by Alexander Potochkin,
 * http://weblogs.java.net/blog/alexfromsun/archive/2007/06/_enablingdisabl_1.html
 *
 * @author Jarek Sacha
 * @since Nov 13, 2008
 */
public final class GlassPane extends JPanel {

    private static final long serialVersionUID = 1L;
    private Color color = new Color(255, 255, 255, 128);


    public GlassPane() {
        setOpaque(false);

        // This is breaking the "mouseEvents transparency"
        // see also
        // http://weblogs.java.net/blog/alexfromsun/archive/2006/09/index.html
        // http://weblogs.java.net/blog/alexfromsun/archive/2005/10/index.html
        addMouseListener(new MouseAdapter() {
        });

        // This component keeps the focus until is made hidden
        setInputVerifier(new InputVerifier() {
            public boolean verify(JComponent input) {
                return !isVisible();
            }
        });
    }


    /**
     * @return glass pane color.
     */
    public Color getColor() {
        return color;
    }


    /**
     * Glass pane color including transparency.
     * It should be semi-transparent to see blocked content.
     * For instance,  Color(255, 255, 255, 128).
     *
     * @param color new color
     */
    public void setColor(final Color color) {
        this.color = color;
    }


    protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
