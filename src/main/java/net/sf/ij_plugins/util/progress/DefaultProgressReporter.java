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

import java.util.ArrayList;
import java.util.List;


/**
 * Default implementation of {@link ProgressReporter} interface. Can be used as a base class for
 * implementing ProgressReporter interface. Alternatively, it can be instantiated and used as a help
 * object to manage and notify listeners of a ProgressEvent.
 *
 * @author Jarek Sacha
 */
public class DefaultProgressReporter implements ProgressReporter {

    private final List<ProgressListener> progressListeners = new ArrayList<>();
    private double currentProgress;


    @Override
    public double currentProgress() {
        return currentProgress;
    }


    @Override
    public void addProgressListener(final ProgressListener l) {
        synchronized (progressListeners) {
            progressListeners.add(l);
        }
    }


    @Override
    public void removeProgressListener(final ProgressListener l) {
        synchronized (progressListeners) {
            progressListeners.remove(l);
        }
    }


    @Override
    public void removeAllProgressListener() {
        // Remove all listeners using call to #removeProgressListener to avoid problems if the class in inherited
        // Iterate over a copy of the progressListeners, otherwise you get java.util.ConcurrentModificationException
        for (final ProgressListener progressListener : new ArrayList<ProgressListener>(progressListeners)) {
            removeProgressListener(progressListener);
        }
    }


    /**
     * Update value of {@link #currentProgress}, value must be not less than 0 and not more than 1.
     * <br>
     * This method is used when progress needs to be updated <strong>without notifying the listeners</strong>.
     * To update progress and update listeners use {@link #notifyProgressListeners(double)}.
     *
     * @param progress new value of {@link #currentProgress}. The value must be not less than 0 and not more than 1.
     * @see #notifyProgressListeners()
     * @see #notifyProgressListeners(double)
     * @see #notifyProgressListeners(double, String)
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
     * Progress message is an empty string.
     *
     * @see #notifyProgressListeners(double)
     */
    protected void notifyProgressListeners() {
        final int numberOfListeners = progressListeners.size();
        if (numberOfListeners > 0) {
            final ProgressEvent e = new ProgressEvent(this, currentProgress);
            for (final ProgressListener l : progressListeners) {
                l.progressNotification(e);
            }
        }
    }


    /**
     * Set new progress value and notify listeners. A convenience method that can be used instead
     * calling {@link #setCurrentProgress(double)} and {@link #notifyProgressListeners()}.
     * Progress message is an empty string.
     *
     * @param progress new value of {@link #currentProgress}. The value must be not less than 0 and not more than 1.
     * @see #setCurrentProgress(double)
     * @see #notifyProgressListeners()
     */
    protected void notifyProgressListeners(final double progress) {
        notifyProgressListeners(progress, "");
    }


    /**
     * Set new progress value and notify listeners.
     *
     * @param progress new value of {@link #currentProgress}. The value must be not less than 0 and not more than 1.
     * @param message  message that will be send to listeners within {#link ProgressEvent}
     */
    protected void notifyProgressListeners(final double progress, final String message) {
        setCurrentProgress(progress);
        final int numberOfListeners = progressListeners.size();
        if (numberOfListeners > 0) {
            final ProgressEvent e = new ProgressEvent(this, currentProgress, message);
            for (final ProgressListener l : progressListeners) {
                l.progressNotification(e);
            }
        }
    }
}
