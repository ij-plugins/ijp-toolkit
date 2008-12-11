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

import net.sf.ij_plugins.ui.AbstractModelAction;
import net.sf.ij_plugins.ui.GlassPane;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.ActionEvent;

/**
 * @author Jarek Sacha
 * @since Feb 18, 2008
 */
final class RunAction extends AbstractModelAction<RegionGrowingModel> {

    private static final long serialVersionUID = 2565133222011989518L;
    private final Component parent;

    protected RunAction(final RegionGrowingModel model, final Component parent) {
        super("Run", model);
        this.parent = parent;
    }

    public void actionPerformed(final ActionEvent e) {

        // Block SRG dialog
        final GlassPane glassPane = new GlassPane();
        SwingUtilities.getRootPane(parent).setGlassPane(glassPane);
        glassPane.setVisible(true);

        // Setup background execution
        final SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            @Override
            protected String doInBackground() throws Exception {
                // Sun region growing
                getModel().actionRun();
                return "Done";
            }

            @Override
            protected void done() {
                // Unblock dialog.
                glassPane.setVisible(false);
            }
        };

        // Execute region growing
        worker.execute();
    }
}
