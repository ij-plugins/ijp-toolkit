/***
 * Image/J Plugins
 * Copyright (C) 2002-2004 Jarek Sacha
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
package net.sf.ij_plugins.operators;

import ij.process.ByteProcessor;

import java.util.Iterator;

/**
 * Does not support {@link Iterator#remove}.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class RunningIterator implements Iterator {

    final private Pixel pixel = new Pixel();
    private int x = 0;
    private int y = 0;

    public RunningIterator(final ByteProcessor ip) {
    }

    public boolean hasNext() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object next() {

        pixel.x = x;
        pixel.y = y;

        return pixel;
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException always.
     */
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Remove is not supported by "
                + this.getClass().getName() + " iterator.");
    }

    public static class Pixel {
        int x;
        int y;
        byte value;
    }

}
