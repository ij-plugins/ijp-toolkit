/*
 * IJ-Plugins
 * Copyright (C) 2002-2019 Jarek Sacha
 * Author's email: jpsacha at gmail dot com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Latest release available at https://github.com/ij-plugins/ijp-toolkit/
 */

package net.sf.ij_plugins.grow;

import net.sf.ij_plugins.ui.AbstractModelAction;
import net.sf.ij_plugins.ui.GlassPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Jarek Sacha
 * @since Feb 18, 2008
 */
abstract class AbstractWorkerAction extends AbstractModelAction<RegionGrowingModel> {

    private static final long serialVersionUID = 2565133222011989518L;
    private final Component parent;
    private final String actionErrorDescription;

    AbstractWorkerAction(String name, final RegionGrowingModel model, final Component parent, String actionErrorDescription) {
        super(name, model);
        this.parent = parent;
        this.actionErrorDescription = actionErrorDescription;
    }

    @Override
    public final void actionPerformed(final ActionEvent e) {

        // Block SRG dialog
        final GlassPane glassPane = new GlassPane();
        SwingUtilities.getRootPane(parent).setGlassPane(glassPane);
        glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        glassPane.setVisible(true);

        // Setup background execution
        final SwingWorker<Boolean, Object> worker = new SwingWorker<Boolean, Object>() {

            Throwable error;

            @Override
            protected Boolean doInBackground() {
                // Run region growing
                try {
                    workerAction();
                } catch (final Throwable t) {
                    error = t;
                    return false;
                }
                return true;
            }

            @Override
            protected void done() {
                if (error != null) {
                    getModel().showError(actionErrorDescription, error);
                }
                // Unblock dialog.
                glassPane.setCursor(Cursor.getDefaultCursor());
                glassPane.setVisible(false);
            }
        };

        // Execute region growing
        worker.execute();
    }


    /**
     * The actual action performed by the worker.
     */
    abstract protected void workerAction();
}
