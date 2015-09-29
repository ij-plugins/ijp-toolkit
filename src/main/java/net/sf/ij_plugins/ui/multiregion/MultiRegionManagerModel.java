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
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import net.sf.ij_plugins.beans.AbstractModel;
import net.sf.ij_plugins.ui.AbstractModelAction;
import net.sf.ij_plugins.ui.ShapeUtils;
import net.sf.ij_plugins.ui.UIUtils;
import net.sf.ij_plugins.util.IJUtils;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;


/**
 * Presentation model for a MultiRegionManager view.
 *
 * @author Jarek Sacha
 */
public final class MultiRegionManagerModel extends AbstractModel {

    // FIXME: A specific ImagePlus should be selected and remembered, rather than accessed by UIUtils.getImage().

    private static final Color[] COLORS = {
            Color.GREEN, Color.RED, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.PINK,
    };
    private int colorCount;
    private static final int OVERLAY_ALPHA = 128;

    private Component parent;

    private final ArrayListModel<Region> regions = new ArrayListModel<>();

    private Region selectedRegion;
    private SubRegion selectedSubRegion;
    private ImagePlus lastSourceImage;
    private int regionCount = 1;


    public MultiRegionManagerModel() {

        regions.addListDataListener(new ListListener());

        addRegion(new Region("Background", nextColor()));
        addRegion(new Region("Region-" + regionCount++, nextColor()));

        // Monitor GUI for possible changes to lastSourceImage
        ImagePlus.addImageListener(new ImageListener() {
            @Override
            public void imageOpened(final ImagePlus imp) {
                if (lastSourceImage == imp) {
                    updateShapes(lastSourceImage);
                }
            }


            @Override
            public void imageClosed(final ImagePlus imp) {
                if (lastSourceImage == imp) {
                    lastSourceImage = null;
                }
            }


            @Override
            public void imageUpdated(final ImagePlus imp) {
                if (lastSourceImage == imp) {
                    updateShapes(lastSourceImage);
                }
            }
        });
    }


    public ArrayListModel<Region> getRegions() {
        return regions;
    }


    public Region getSelectedRegion() {
        return selectedRegion;
    }


    public void setSelectedRegion(final Region selectedRegion) {
        firePropertyChange("selectedRegion", this.selectedRegion, this.selectedRegion = selectedRegion);
    }


    public SubRegion getSelectedSubRegion() {
        return selectedSubRegion;
    }


    public void setSelectedSubRegion(final SubRegion selectedSubRegion) {
        firePropertyChange("selectedSubRegion", this.selectedSubRegion, this.selectedSubRegion = selectedSubRegion);
    }


    void setParent(final Component parent) {
        firePropertyChange("parent", this.parent, this.parent = parent);
    }


    Action createNewRegionAction() {
        return new AbstractModelAction<MultiRegionManagerModel>("Add Current ROI", this) {
            private static final long serialVersionUID = 1L;


            @Override
            public void actionPerformed(final ActionEvent e) {
                getModel().actionAddRegion();
            }
        };
    }


    Action createRemoveRegionAction() {
        return new AbstractModelAction<MultiRegionManagerModel>("Remove", this) {
            private static final long serialVersionUID = 1L;


            @Override
            public void actionPerformed(final ActionEvent e) {
                getModel().actionRemoveRegion();
            }


            @Override
            public boolean isEnabled() {
                return super.isEnabled() && getModel().getSelectedRegion() != null;
            }
        };
    }


    Action createAddCurrentROIAction() {
        return new AbstractModelAction<MultiRegionManagerModel>("Add Current ROI", this) {
            private static final long serialVersionUID = 1L;


            @Override
            public void actionPerformed(final ActionEvent e) {
                getModel().actionAddSubRegion();
            }


            @Override
            public boolean isEnabled() {
                return super.isEnabled() && getModel().getSelectedRegion() != null;
            }
        };
    }


    Action createRemoveSubRegionAction() {
        return new AbstractModelAction<MultiRegionManagerModel>("Remove ROI", this) {
            private static final long serialVersionUID = 1L;


            @Override
            public void actionPerformed(final ActionEvent e) {
                getModel().actionRemoveSubRegion();
            }


            @Override
            public boolean isEnabled() {
                return super.isEnabled() && getModel().getSelectedSubRegion() != null;
            }
        };
    }


    Action createRedrawOverlaysAction() {
        return new AbstractModelAction<MultiRegionManagerModel>("Redraw Overlays", this) {
            private static final long serialVersionUID = 1L;


            @Override
            public void actionPerformed(final ActionEvent e) {
                updateShapes(WindowManager.getCurrentImage());
            }


            @Override
            public boolean isEnabled() {
                return super.isEnabled() && getModel().getRegions().size() > 0;
            }
        };
    }


    Action createRemoveOverlaysAction() {
        return new AbstractModelAction<MultiRegionManagerModel>("Remove Overlays", this) {
            private static final long serialVersionUID = 1L;


            @Override
            public void actionPerformed(final ActionEvent e) {
                updateShapes(null);
            }
        };
    }


    void actionAddRegion() {
        final String name = JOptionPane.showInputDialog(parent, "Enter new object name", "Region-" + regionCount++);
        if (name != null && !name.trim().isEmpty()) {
            final Color rc = nextColor();
            final Color color = new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), OVERLAY_ALPHA);
            addRegion(new Region(name, color));
        }
    }


    private Color nextColor() {
        final Color rc = COLORS[colorCount++ % COLORS.length];
        return new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), OVERLAY_ALPHA);
    }


    void addRegion(final Region region) {
        regions.add(region);
        setSelectedRegion(region);
        updateShapes(lastSourceImage);
    }


    void actionRemoveRegion() {
        if (selectedRegion != null && regions.contains(selectedRegion)) {
            final int removedIndex = regions.indexOf(selectedRegion);
            regions.remove(selectedRegion);
            // Select closes region
            if (regions.size() > 0) {
                final int newSelection = Math.min(removedIndex, regions.size() - 1);
                setSelectedRegion(regions.get(newSelection));
            }
            updateShapes(lastSourceImage);
        }
    }


    void actionAddSubRegion() {
        if (selectedRegion == null) {
            UIUtils.error("Need to select region to add ROI to.");
            return;
        }
        final ImagePlus imp = UIUtils.getImage();
        if (imp == null) {
            return;
        }
        final Roi roi = imp.getRoi();
        if (roi == null) {
            UIUtils.error("The active image does not have a selection.");
            return;
        }

        final String name = roi.getName();
        final String label = name != null ? name : getLabel(imp, roi, -1);

        selectedRegion.add(new SubRegion(label, roi));

        updateShapes(imp);
    }


    private void updateShapes(final ImagePlus imp) {

        lastSourceImage = imp;

        if (imp == null) {
            return;
        }

        // Prepare overlay
        final Overlay overlay = new Overlay();
        for (final Region region : regions) {

            final Area area = new Area();
            for (final SubRegion subRegion : region.getSubRegions()) {
                final Shape shape = ShapeUtils.toShape(subRegion.getRoi());
                area.add(new Area(shape));
            }
            final ShapeRoi roi = new ShapeRoi(area);
            roi.setStrokeColor(region.getColor());
            roi.setFillColor(region.getColor());
            roi.setName(region.getName());
            overlay.add(roi);
        }

        imp.setOverlay(overlay);
    }


    void actionRemoveSubRegion() {
        if (selectedRegion != null && regions.contains(selectedRegion)
                && selectedSubRegion != null
                && selectedRegion.getSubRegions().contains(selectedSubRegion)) {
            selectedRegion.getSubRegions().remove(selectedSubRegion);
            updateShapes(lastSourceImage);
        }
    }


    static String getLabel(final ImagePlus imp, final Roi roi, final int n) {
        final Rectangle r = roi.getBounds();
        int xc = r.x + r.width / 2;
        int yc = r.y + r.height / 2;
        if (n >= 0) {
            xc = yc;
            yc = n;
        }
        if (xc < 0) {
            xc = 0;
        }
        if (yc < 0) {
            yc = 0;
        }
        int digits = 4;
        String xs = "" + xc;
        if (xs.length() > digits) {
            digits = xs.length();
        }
        String ys = "" + yc;
        if (ys.length() > digits) {
            digits = ys.length();
        }
        xs = "000000" + xc;
        ys = "000000" + yc;
        String label = ys.substring(ys.length() - digits) + "-" + xs.substring(xs.length() - digits);
        if (imp.getStackSize() > 1) {
            final String zs = "000000" + imp.getCurrentSlice();
            label = zs.substring(zs.length() - digits) + "-" + label;
        }
        return label;
    }


    public void sentCurrentRegionToROIManager() {
        final Region region = getSelectedRegion();
        if (region != null) {
            final List<Roi> rois = new ArrayList<Roi>();
            for (final SubRegion subRegion : region.getSubRegions()) {
                rois.add(subRegion.getRoi());
            }
            IJUtils.addToROIManager(rois);
        }
    }


    public void loadCurrentRegionFromROIManager() {
        if (selectedRegion == null) {
            return;
        }

        final Roi[] rois = IJUtils.getRoiManager().getSelectedRoisAsArray();
        if (rois == null || rois.length < 1) {
            return;
        }

        for (final Roi roi : rois) {
            selectedRegion.add(new SubRegion(roi.getName(), roi));
        }

        final ImagePlus imp = UIUtils.getImage();
        if (imp != null) {
            updateShapes(imp);
        }
    }


    private class ListListener implements ListDataListener {

        @Override
        public void intervalAdded(final ListDataEvent e) {
            updateShapes(lastSourceImage);
            MultiRegionManagerModel.this.firePropertyChange("regions", null, null);
        }


        @Override
        public void intervalRemoved(final ListDataEvent e) {
            updateShapes(lastSourceImage);
            MultiRegionManagerModel.this.firePropertyChange("regions", null, null);
        }


        @Override
        public void contentsChanged(final ListDataEvent e) {
            updateShapes(lastSourceImage);
            MultiRegionManagerModel.this.firePropertyChange("regions", null, null);
        }
    }
}
