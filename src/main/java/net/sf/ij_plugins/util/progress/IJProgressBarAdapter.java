/*
 * Image/J Plugins
 * Copyright (C) 2002-2014 Jarek Sacha
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
package net.sf.ij_plugins.util.progress;

import ij.IJ;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Simple adapter for attaching a {@link ProgressReporter} to ImageJ's progress bar.
 * <br>
 * Example use:
 * <pre>
 *   final ProgressReporter r = ...;
 *   final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
 *   r.addProgressListener(progressBarAdapter);
 *   ...
 *   r.removeProgressListener(progressBarAdapter);
 * </pre>
 * Always remember to remove listeners when processing is done to avoid memory leaks.
 *
 * @author Jarek Sacha
 */
public class IJProgressBarAdapter implements ProgressListener {
    @Override
    public void progressNotification(final ProgressEvent e) {
        if (SwingUtilities.isEventDispatchThread()) {
            IJ.showStatus(e.getMessage());
            IJ.showProgress(e.getProgress());
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        IJ.showStatus(e.getMessage());
                        IJ.showProgress(e.getProgress());
                    }
                });
            } catch (final InterruptedException ex) {
                IJ.log("InterruptedException " + ex.getMessage());
            } catch (final InvocationTargetException ex) {
                IJ.log("InvocationTargetException" + ex.getMessage());
            }
        }
    }
}
