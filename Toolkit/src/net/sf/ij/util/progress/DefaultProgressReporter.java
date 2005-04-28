/***
 * Image/J Plugins
 * Copyright (C) 2002-2005 Jarek Sacha
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
package net.sf.ij.util.progress;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link ProgressReporter} interface.
 * Can be used as a base class for implementing ProgressReporter interface.
 * Alternatively,
 * it can be instantiated and used as a help object to manage and notify listeners of a
 * ProgressEvent.
 *
 * @author Jarek Sacha
 */
public class DefaultProgressReporter implements ProgressReporter {
    final protected List progressListeners = new ArrayList();
    private double currentProgress = 0;

    public double getCurrentProgress() {
        return currentProgress;
    }

    public void addProgressListener(ProgressListener l) {
        progressListeners.add(l);
    }

    public void removeProgressListener(ProgressListener l) {
        progressListeners.remove(l);
    }

    public void removeAllProgressListener() {
        progressListeners.clear();
    }

    /**
     * Update value of {@link #currentProgress}, value must be not less than 0 and not more than 1.
     *
     * @param progress new value of {@link #currentProgress}.
     */
    protected void setCurrentProgress(final double progress) {
        if (progress < 0 || progress > 1) {
            throw new IllegalArgumentException(
                    "Argument progress cannot be less than 0 or more than 1 [" + progress + "].");
        }
        currentProgress = progress;
    }

    /**
     * Notify listeners of current value of progress.
     *
     * @see #notifyProgressListeners(double)
     */
    protected void notifyProgressListeners() {
        final int numberOfListeners = progressListeners.size();
        if (numberOfListeners > 0) {
            final ProgressEvent e = new ProgressEvent(this, currentProgress);
            for (int i = 0; i < numberOfListeners; i++) {
                final ProgressListener l = (ProgressListener) progressListeners.get(i);
                l.progressNotification(e);
            }
        }
    }

    /**
     * Set new progress value and notify listeners. A convinience method that can be used instread calling
     * {@link #setCurrentProgress(double)} and {@link #notifyProgressListeners()}.
     *
     * @param progress new value of {@link #getCurrentProgress}.
     * @see #setCurrentProgress(double)
     * @see #notifyProgressListeners()
     */
    protected void notifyProgressListeners(final double progress) {
        setCurrentProgress(progress);
        final int numberOfListeners = progressListeners.size();
        if (numberOfListeners > 0) {
            final ProgressEvent e = new ProgressEvent(this, currentProgress);
            for (int i = 0; i < numberOfListeners; i++) {
                final ProgressListener l = (ProgressListener) progressListeners.get(i);
                l.progressNotification(e);
            }
        }
    }


}
