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
package net.sf.ij_plugins.filters;

import ij.ImagePlus;
import ij.process.FloatProcessor;
import junit.framework.TestCase;
import net.sf.ij_plugins.io.IOUtils;

/**
 * @author Jarek Sacha
 */
public class RunningFilterTest extends TestCase {
    public RunningFilterTest(String test) {
        super(test);
    }

    /**
     * The fixture set up called before every test method.
     */
    protected void setUp() throws Exception {
    }

    /**
     * The fixture clean up called after every test method.
     */
    protected void tearDown() throws Exception {
    }

    public void testFilter() throws Exception {
//        ImagePlus imp = IOUtils.openImage("test_images/boats_x2.png");
        ImagePlus imp = IOUtils.openImage("test/data/blobs_noise.png");
        final FloatProcessor fp = (FloatProcessor) imp.getProcessor().convertToFloat();

        //        final RunningFilter filter = new RunningFilter(new RunningMedianOperator(), 29, 29);
        final RunningFilter filter = new RunningFilter(new RunningMedianRBTOperator(), 29, 29);
        filter.run(fp);
    }
}