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
package net.sf.ij_plugins.filters;

import ij.plugin.filter.PlugInFilter;
import ij.ImagePlus;
import ij.IJ;
import net.sf.ij_plugins.filters.PeronMalikAnisotropicDiffusion;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;

import javax.swing.*;
import java.beans.BeanInfo;
import java.beans.SimpleBeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;

import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.PropertySheet;

/**
 * @author  Jarek Sacha
 * @version $ Revision: $
 */

public class PeronMalikAnisotropicDiffusionPlugin implements PlugInFilter {
  public int setup(String s, ImagePlus imagePlus) {
    return DOES_8G | DOES_16 | DOES_32 | DOES_STACKS | NO_CHANGES;
  }

  public void run(ImageProcessor ip) {
    FloatProcessor src = (FloatProcessor) ip.convertToFloat();
    PeronMalikAnisotropicDiffusion gad = new PeronMalikAnisotropicDiffusion();


    if( !showBeanEditDialog(gad) ) {
        return;
    }

    long start = System.currentTimeMillis();
    FloatProcessor dest = gad.process(src);
    long end = System.currentTimeMillis();

    new ImagePlus("Peron-Malik Anisotropic Diffusion", dest).show();
    IJ.showStatus("Filtering completed in "+(end-start)+"ms.");
  }

    private boolean showBeanEditDialog(Object bean) {
        BeanInfo beanInfo = new SimpleBeanInfo();
        try {
            beanInfo = Introspector.getBeanInfo(bean.getClass());
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }

        final PropertySheetPanel sheet = new PropertySheetPanel();
        sheet.setMode(PropertySheet.VIEW_AS_FLAT_LIST);
        sheet.setToolBarVisible(true);
        sheet.setDescriptionVisible(true);
        sheet.setBeanInfo(beanInfo);
        sheet.readFromObject(bean);

        int status = JOptionPane.showConfirmDialog(null, sheet,
                "Flux Anisotropic Diffusion", JOptionPane.YES_NO_CANCEL_OPTION);

        if(status == JOptionPane.YES_OPTION) {
            sheet.writeToObject(bean);
            return true;
        } else {
            return false;
        }
    }
}
