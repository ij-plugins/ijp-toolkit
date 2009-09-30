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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import net.sf.ij_plugins.im3d.grow.SRG;

import java.util.ArrayList;
import java.util.List;


/**
 * ImageJ plugin for running Seeded Region Growing.
 *
 * @author Jarek Sacha
 * @since Feb 8, 2008
 */
public final class RegionGrowingPlugIn implements PlugIn {

    private static final String TITLE = "Seeded Region Growing";
    private static final String[] STACK_TREATMENT = {"Independent slices", "3D Volume", "Multi-band"};

    public void run(final String arg) {
        final int[] wList = WindowManager.getIDList();
        if (wList == null) {
            IJ.noImage();
            return;
        }


        final List<String> titleList = new ArrayList<String>();
        for (final int id : wList) {
            final ImagePlus imp = WindowManager.getImage(id);
            if (imp != null && !imp.getTitle().trim().isEmpty()) {
                titleList.add(imp.getTitle());
            }
        }

        final String[] titles = titleList.toArray(new String[titleList.size()]);
        final GenericDialog gd = new GenericDialog(TITLE, IJ.getInstance());
        gd.addChoice("Image:", titles, titles[0]);
        gd.addChoice("Seeds:", titles, titles[1]);
//        gd.addChoice("Stack treatment:", STACK_TREATMENT, STACK_TREATMENT[0]);
        gd.addMessage("Seeds image should be of the same size as the image for segmentation.");
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }

        final ImagePlus image = WindowManager.getImage(wList[gd.getNextChoiceIndex()]);
        final ImagePlus seeds = WindowManager.getImage(wList[gd.getNextChoiceIndex()]);
//        final String stackTreatment = gd.getNextString();

//        run(image, seeds, stackTreatment.trim());
        run(image, seeds, STACK_TREATMENT[0]);
    }


    void run(final ImagePlus image, final ImagePlus seeds, final String stackTreatment) {

        if (!STACK_TREATMENT[0].equalsIgnoreCase(stackTreatment)) {
            for (int i = 1; i < STACK_TREATMENT.length; ++i) {
                if (STACK_TREATMENT[i].equalsIgnoreCase(stackTreatment)) {
                    IJ.error(TITLE, "Not yet implemented");
                    return;
                }
            }
            IJ.error(TITLE, "Not supported.");
            return;
        }


        // FIXME: validate size and type

        // Process
        final ImageStack stack = new ImageStack(image.getWidth(), image.getHeight());
        for (int i = 1; i <= image.getNSlices(); ++i) {
            final ByteProcessor bp = (ByteProcessor) image.getStack().getProcessor(i);
            final ByteProcessor s = (ByteProcessor) seeds.getStack().getProcessor(i);
            final ByteProcessor destBP = run(bp, s);
            stack.addSlice(image.getStack().getSliceLabel(i), destBP);
        }

        new ImagePlus(image.getTitle() + "-SRG", stack).show();
    }

    final ByteProcessor run(final ByteProcessor image, final ByteProcessor seeds) {
        final SRG srg = new SRG();
        srg.setImage(image);
        srg.setSeeds(seeds);
        srg.run();
        return srg.getRegionMarkers();
    }

}
