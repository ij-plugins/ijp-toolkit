/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
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
package net.sf.ij_plugins.util;

import ij.IJ;

/**
 * Simple helper for logging using IJ.log() , but only when IJ.debugMode is <code>true</code>.
 *
 * @author Jarek Sacha
 */
public class IJDebug {
    private IJDebug() {
    }


    /**
     * Call <code>IJ.log(System.currentTimeMillis() + " - " + message)</code> when
     * <code>IJ.debugMode</code> is <code>true</code>, otherwise do nothing.
     *
     * @param message message to log.
     */
    public static void log(final String message) {
        if (IJ.debugMode) {
            final long t = System.currentTimeMillis();
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(t);
            stringBuilder.append(" - ");
            stringBuilder.append(message);
            IJ.log(stringBuilder.toString());
        }

    }
}
