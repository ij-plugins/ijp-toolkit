/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
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

/**
 * Interface of a class that issues an {@link net.sf.ij_plugins.util.progress.ProgressEvent}.
 *
 * @author Jarek Sacha
 */
public interface ProgressReporter {
    /**
     * Report what fraction of the processing task was already completed.
     * Progress value is between 0 and 1 (meaning 100% completed).
     * For instance, 0.25 means that 25% con the processing was completed.
     *
     * @return current progress.
     */
    double currentProgress();

    /**
     * Add progress listener.
     *
     * @param l listener to add.
     */
    void addProgressListener(ProgressListener l);

    /**
     * Remove progress listener.
     *
     * @param l listener to remove.
     */
    void removeProgressListener(ProgressListener l);

    /**
     * Remove all progress listener.
     */
    void removeAllProgressListener();
}
