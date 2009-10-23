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

package net.sf.ij_plugins.grow;

import ij.Prefs;
import ij.gui.GUI;
import ij.plugin.PlugIn;
import net.sf.ij_plugins.ui.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


/**
 * ImageJ plugin for running Seeded Region Growing with UI for drawing regions.
 *
 * @author Jarek Sacha
 * @since Feb 8, 2008
 */
public final class RegionGrowingToolPlugIn implements PlugIn {

    private static final String LOC_KEY = "RegionGrowingToolPlugIn.loc";
    private static final String WIDTH_KEY = "RegionGrowingToolPlugIn.width";
    private static final String HEIGHT_KEY = "RegionGrowingToolPlugIn.height";

    private static final String TITLE = "Seeded Region Growing Tool";

    private static RegionGrowingView view;
    private static JDialog dialog;


    public void run(final String arg) {
        if (view == null) {
            view = new RegionGrowingView();
            dialog = new JDialog((Frame) null, TITLE, false);
            dialog.setIconImage(UIUtils.getImageJIconImage());
            dialog.getContentPane().add(view);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

            // Add listener to store window location and size on close
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    Prefs.saveLocation(LOC_KEY, dialog.getLocation());
                    final Dimension d = dialog.getSize();
                    Prefs.set(WIDTH_KEY, d.width);
                    Prefs.set(HEIGHT_KEY, d.height);
                }
            });

            // Restore location and size
            final Point loc = Prefs.getLocation(LOC_KEY);
            final int w = (int) Prefs.get(WIDTH_KEY, 0.0);
            final int h = (int) Prefs.get(HEIGHT_KEY, 0.0);
            if (loc != null && w > 0 && h > 0) {
                dialog.setSize(w, h);
                dialog.setLocation(loc);
            } else {
//                setSize(width, height);
                GUI.center(dialog);
            }
        }

        dialog.setVisible(true);
    }
}