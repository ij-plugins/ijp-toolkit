/***
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
 */
package net.sf.ij_plugins.grow;

import ij.IJ;
import ij.plugin.PlugIn;

import javax.swing.*;


/**
 * Date: Feb 8, 2008
 * Time: 8:47:19 PM
 *
 * @author Jarek Sacha
 */
public final class RegionGrowingPlugIn implements PlugIn {

    private static final String TITLE = "Seeded Region Growing";

    private static RegionGrowingView view;
    private static JDialog dialog;

    public void run(String arg) {
        if (view == null) {
            view = new RegionGrowingView();
            dialog = new JDialog(IJ.getInstance(), TITLE, false);
            dialog.getContentPane().add(view);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }

        dialog.setVisible(true);
    }
}
