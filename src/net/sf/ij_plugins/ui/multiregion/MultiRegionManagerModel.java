/***
 * Image/J Plugins
 * Copyright (C) 2002-2008 Jarek Sacha
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

import com.jgoodies.binding.list.ArrayListModel;
import ij.ImageListener;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.gui.StackWindow;
import net.sf.ij_plugins.beans.AbstractModel;
import net.sf.ij_plugins.ui.*;

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

    private static final Color[] COLORS = {
            Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.YELLOW,
    };
    private int colorCount;
    private final int overlayAlpha = 128;

    private Component parent;

    private final ArrayListModel<Region> regions = new ArrayListModel<Region>();

    private Region selectedRegion;
    private SubRegion selectedSubRegion;
    private ImagePlus lastSourceImage;

    public MultiRegionManagerModel() {

        regions.addListDataListener(new ListListener());

        addRegion(new Region("Background", colorWithAlpha(Color.GREEN, overlayAlpha)));
        addRegion(new Region("Region-1", colorWithAlpha(Color.RED, overlayAlpha)));

        // Monitor GUI for possible changes to lastSourceImage
        ImagePlus.addImageListener(new ImageListener() {
            public void imageOpened(ImagePlus imp) {
                System.out.println("MultiRegionManagerModel.imageOpened");
                if (lastSourceImage == imp) {
                    updateShapes(lastSourceImage);
                }
            }

            public void imageClosed(ImagePlus imp) {
                System.out.println("MultiRegionManagerModel.imageClosed");
                if (lastSourceImage == imp) {
                    lastSourceImage = null;
                }
            }

            public void imageUpdated(ImagePlus imp) {
                System.out.println("MultiRegionManagerModel.imageUpdated");
                if (lastSourceImage == imp) {
                    updateShapes(lastSourceImage);
                }
            }
        });
    }

    private static Color colorWithAlpha(final Color color, final int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

    }

    public ArrayListModel<Region> getRegions() {
        return regions;
    }

    public Region getSelectedRegion() {
        return selectedRegion;
    }

    public void setSelectedRegion(final Region selectedRegion) {
        System.out.println("MultiRegionManagerModel.setSelectedRegion: " + selectedRegion);
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
            public void actionPerformed(ActionEvent e) {
                getModel().actionAddRegion();
            }
        };
    }

    Action createRemoveRegionAction() {
        return new AbstractModelAction<MultiRegionManagerModel>("Remove", this) {
            public void actionPerformed(ActionEvent e) {
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
            public void actionPerformed(ActionEvent e) {
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
            public void actionPerformed(ActionEvent e) {
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
            public void actionPerformed(ActionEvent e) {
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
            public void actionPerformed(ActionEvent e) {
                updateShapes(null);
            }
        };
    }


    void actionAddRegion() {
        final String name = JOptionPane.showInputDialog(parent, "Enter new object name", "Region-X");
        if (name != null && !name.trim().isEmpty()) {
            final Color rc = COLORS[colorCount++ % COLORS.length];
            final Color color = new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), overlayAlpha);
            addRegion(new Region(name, color));
        }
    }

    void addRegion(final Region region) {
        regions.add(region);
        autoSelectedRegion();
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

        if (lastSourceImage != imp && lastSourceImage != null) {
            // Remove overlay canvas from the previous image
            if (lastSourceImage.getWindow().getCanvas() instanceof OverlayCanvas) {
                if (lastSourceImage.getStackSize() > 1)
                    new StackWindow(lastSourceImage);
                else
                    new ImageWindow(lastSourceImage);
            }
        }

        lastSourceImage = imp;

        if (imp == null) {
            return;
        }

        // Prepare overlay canvas
        final ImageWindow imageWindow = imp.getWindow();
        final ImageCanvas imageCanvas = imageWindow.getCanvas();
        final OverlayCanvas overlayCanvas;
        if (imageCanvas instanceof OverlayCanvas) {
            overlayCanvas = (OverlayCanvas) imageCanvas;
        } else {
            overlayCanvas = new OverlayCanvas(imp);
            if (imp.getStackSize() > 1)
                new StackWindow(imp, overlayCanvas);
            else
                new ImageWindow(imp, overlayCanvas);
        }

        final List<ShapeOverlay> overlays = new ArrayList<ShapeOverlay>();
        for (final Region region : regions) {
            final Area area = new Area();
            for (final SubRegion subRegion : region.getSubRegions()) {
                final Shape shape = ShapeOverlay.toShape(subRegion.getRoi());
                area.add(new Area(shape));
            }
            overlays.add(new ShapeOverlay(region.getName(), area, region.getColor(), true));
        }

        overlayCanvas.setOverlays(overlays);
        overlayCanvas.invalidate();
        overlayCanvas.repaint();
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
        Rectangle r = roi.getBounds();
        int xc = r.x + r.width / 2;
        int yc = r.y + r.height / 2;
        if (n >= 0) {
            xc = yc;
            yc = n;
        }
        if (xc < 0) xc = 0;
        if (yc < 0) yc = 0;
        int digits = 4;
        String xs = "" + xc;
        if (xs.length() > digits) digits = xs.length();
        String ys = "" + yc;
        if (ys.length() > digits) digits = ys.length();
        xs = "000000" + xc;
        ys = "000000" + yc;
        String label = ys.substring(ys.length() - digits) + "-" + xs.substring(xs.length() - digits);
        if (imp.getStackSize() > 1) {
            String zs = "000000" + imp.getCurrentSlice();
            label = zs.substring(zs.length() - digits) + "-" + label;
        }
        return label;
    }

    public void autoSelectedRegion() {
        if (selectedRegion == null && regions.size() > 0) {
            setSelectedRegion(regions.get(0));
        }
    }


    private class ListListener implements  ListDataListener {
        public void intervalAdded(ListDataEvent e) {
            System.out.println("MultiRegionManagerModel$ListListener.intervalAdded");
            updateShapes(lastSourceImage);
            MultiRegionManagerModel.this.firePropertyChange("regions", null, null);
        }

        public void intervalRemoved(ListDataEvent e) {
            System.out.println("MultiRegionManagerModel$ListListener.intervalRemoved");
            updateShapes(lastSourceImage);
            MultiRegionManagerModel.this.firePropertyChange("regions", null, null);
        }

        public void contentsChanged(ListDataEvent e) {
            System.out.println("MultiRegionManagerModel$ListListener.contentsChanged");
            updateShapes(lastSourceImage);
            MultiRegionManagerModel.this.firePropertyChange("regions", null, null);
        }
    }
}
