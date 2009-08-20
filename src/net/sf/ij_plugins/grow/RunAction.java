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

import net.sf.ij_plugins.ui.AbstractModelAction;
import net.sf.ij_plugins.ui.GlassPane;

import javax.swing.*;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;

/**
 * @author Jarek Sacha
 * @since Feb 18, 2008
 */
final class RunAction extends AbstractModelAction<RegionGrowingModel> {

    private static final long serialVersionUID = 2565133222011989518L;
    private final Component parent;

    RunAction(final RegionGrowingModel model, final Component parent) {
        super("Run", model);
        this.parent = parent;
    }

    public void actionPerformed(final ActionEvent e) {

        // Block SRG dialog
        final GlassPane glassPane = new GlassPane();
        SwingUtilities.getRootPane(parent).setGlassPane(glassPane);
        glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        glassPane.setVisible(true);

        // Setup background execution
        final SwingWorker<Boolean, Object> worker = new SwingWorker<Boolean, Object>() {

            Throwable error;

            @Override
            protected Boolean doInBackground() throws Exception {
                // Run region growing
                try {
                    getModel().actionRun();
                } catch (final Throwable t) {
                    error = t;
                    return false;
                }
                return true;
            }

            @Override
            protected void done() {
                if (error != null) {
                    getModel().showError("Error running region growing.", error);
                }
                // Unblock dialog.
                glassPane.setCursor(Cursor.getDefaultCursor());
                glassPane.setVisible(false);
            }
        };

        // Execute region growing
        worker.execute();
    }
}
