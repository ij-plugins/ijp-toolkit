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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;

/**
 * The GlassPane in front of a window to indicate that the window is busy performing an action and
 * is not accepting user input. GlassPane dims appearance of the window.
 * Translucency and color of the GlassPane can be controlled setting a color, for instance
 * <pre>
 * glassPane.set(new Color(255, 255, 255, 128);
 * </pre>
 * sets the GlassPane to be white (255,255,255) and semi transparent (128).
 * <p/>
 * Inspired on class created by Alexander Potochkin,
 * http://weblogs.java.net/blog/alexfromsun/archive/2006/09/a_wellbehaved_g.html
 * This implementation restores automatically captures and restores the focus to correct component after
 * glass pane in no longer visible.
 * <p/>
 * Sample use:
 * <pre>
 * final JFrame frame = ...
 * frame.setGlassPane(new GlassPane());
 * // ...
 * // Block the frame
 * frame.getGlassPane().setVisible(true);
 * // ...
 * // Unblock the frame
 * frame.getGlassPane().setVisible(false);
 * </pre>
 *
 * @author Jarek Sacha
 * @since Nov 13, 2008
 */
public final class GlassPane extends JPanel {

    private static final long serialVersionUID = 1L;

    private Color color = new Color(255, 255, 255, 128);

    private Component focusOwner;


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
            @Override
            public boolean verify(JComponent input) {
                return !isVisible();
            }
        });

        // Gain and restore the focus when glass pane is set visible or hidden.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent e) {
                // Find component with focus
                focusOwner = componentWithFocus(getParent());

                if (isShowing()) {
                    requestFocusInWindow();
                }
            }


            @Override
            public void componentHidden(final ComponentEvent e) {
                if (focusOwner != null) {
                    focusOwner.requestFocus();
                }
                focusOwner = null;
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


    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(color);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }


    private static Component componentWithFocus(final Container parent) {

        if (parent == null) {
            return null;
        } else if (parent.isFocusOwner()) {
            return parent;
        }

        for (final Component child : parent.getComponents()) {
            if (child.isFocusOwner()) {
                return child;
            } else if (child instanceof Container) {
                final Component f = componentWithFocus((Container) child);
                if (f != null) {
                    return f;
                }
            }
        }

        return null;
    }

}
