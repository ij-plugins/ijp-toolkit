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


package net.sf.ij_plugins.ui.multiregion;

import com.jgoodies.common.collect.ArrayListModel;
import com.jgoodies.common.collect.ObservableList;
import net.sf.ij_plugins.beans.AbstractModel;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;


/**
 * @author Jarek Sacha
 */
public class Region extends AbstractModel {

    public static final String PROPERTYNAME_SUB_REGIONS = "subRegions";
    public static final String PROPERTYNAME_COLOR = "color";
    public static final String PROPERTYNAME_NAME = "name";

    private String name = "?";
    private Color color = Color.RED;
    private final ObservableList<SubRegion> subRegions = new ArrayListModel<>();


    public Region(final String name, final Color color) {
        setName(name);
        setColor(color);
        subRegions.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(final ListDataEvent e) {
                firePropertyChange(PROPERTYNAME_SUB_REGIONS, null, null);
            }


            @Override
            public void intervalRemoved(final ListDataEvent e) {
                firePropertyChange(PROPERTYNAME_SUB_REGIONS, null, null);
            }


            @Override
            public void contentsChanged(final ListDataEvent e) {
                firePropertyChange(PROPERTYNAME_SUB_REGIONS, null, null);
            }
        });
    }


    public Color getColor() {
        return color;
    }


    public void setColor(final Color color) {
        firePropertyChange(PROPERTYNAME_COLOR, this.color, this.color = color);
    }


    public String getName() {
        return name;
    }


    public void setName(final String name) {
        firePropertyChange(PROPERTYNAME_NAME, this.name, this.name = name);
    }


    public void add(final SubRegion e) {
        subRegions.add(e);
        firePropertyChange(PROPERTYNAME_SUB_REGIONS, null, null);
    }


    public ObservableList<SubRegion> getSubRegions() {
        return subRegions;
    }
}
