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
package net.sf.ij.swing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * Initial implemntation based on class PaintModel from Andrei Cioroianu article
 * <i>Prototyping Desktop Applications</i> (http://www.onjava.com/pub/a/onjava/2004/04/28/desktop.html).
 */
public class ImageCanvasModel extends PropertyChangeSupport {
    public static final String BACK_COLOR_PROPERTY
            = "BACK_COLOR_PROPERTY";
    public static final String BACK_IMAGE_PROPERTY
            = "BACK_IMAGE_PROPERTY";
    public static final String ZOOM_FACTOR_PROPERTY
            = "ZOOM_FACTOR_PROPERTY";
    public static final String TOOL_CLASS_PROPERTY
            = "TOOL_CLASS_PROPERTY";
    public static final String TOOL_COLOR_PROPERTY
            = "TOOL_COLOR_PROPERTY";
    public static final String TOOL_STROKE_PROPERTY
            = "TOOL_STROKE_PROPERTY";
    public static final String LAST_TOOL_PROPERTY
            = "LAST_TOOL_PROPERTY";

    public static final String IMAGE_PROPERTY = "image";

    public static final Color BACK_COLOR_INIT_VALUE
            = Color.white;
    public static final Image BACK_IMAGE_INIT_VALUE
            = null;
    public static final float ZOOM_FACTOR_INIT_VALUE
            = 1.0f;
//    public static final Class TOOL_CLASS_INIT_VALUE
//            = NoteTool.class;
    public static final Color TOOL_COLOR_INIT_VALUE
            = Color.black;
    public static final BasicStroke TOOL_STROKE_INIT_VALUE
            = new BasicStroke(5, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND);

    private Color backColor;
    private Image backImage;
    private float zoomFactor;
    private Class toolClass;
    private Color toolColor;
    private Stroke toolStroke;
//    private LinkedList toolList;
    private BufferedImage image;
    private Map itemMap = new HashMap();

    public ImageCanvasModel(Object source) {
        super(source);
        backColor = BACK_COLOR_INIT_VALUE;
        backImage = BACK_IMAGE_INIT_VALUE;
        zoomFactor = ZOOM_FACTOR_INIT_VALUE;
//        toolClass = TOOL_CLASS_INIT_VALUE;
        toolColor = TOOL_COLOR_INIT_VALUE;
        toolStroke = TOOL_STROKE_INIT_VALUE;
//        toolList = new LinkedList();
    }

    /**
     *
     * Associates the specified <code>item</code> with the specified
     * <code>key</code> that is displayed on this canvas. If the canvas
     * previously contained a mapping for this <code>key</code>, the old item is replaced by
     * the specified <code>item</code>. (A map m is said to contain a mapping for a key k if
     * and only if m.containsKey(k) would return true.)) Parameters: key - key
     * with which the specified value is to be associated. value - value to be
     * associated with the specified key. Returns: previous value associated
     * with specified key, or null if there was no mapping for key. A null
     * return can also indicate that the map previously associated null with the
     * specified key, if the implementation supports null values. Throws:
     * java.lang.UnsupportedOperationException - if the put operation is not
     * supported by this map. java.lang.ClassCastException - if the class of the
     * specified key or value prevents it from being stored in this map.
     * java.lang.IllegalArgumentException - if some aspect of this key or value
     * prevents it from being stored in this map. java.lang.NullPointerException
     * - this map does not permit null keys or values, and the specified key or
     * value is null.
     *
     * @param key
     * @param item
     */
    public void putItem(String key, ImageCanvasItem item) {
        // TODO: Correct javadoc
        itemMap.put(key, item);
    }

    public Color getBackColor() {
        return backColor;
    }

    public void setBackColor(Color newBackColor) {
        Color oldBackColor = backColor;
        backColor = newBackColor;
        firePropertyChange(BACK_COLOR_PROPERTY,
                oldBackColor, newBackColor);
    }

    public Image getBackImage() {
        return backImage;
    }

    public void setBackImage(Image newBackImage) {
        Image oldBackImage = backImage;
        backImage = newBackImage;
        firePropertyChange(BACK_IMAGE_PROPERTY,
                oldBackImage, newBackImage);
    }

    public float getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(float newZoomFactor) {
        float oldZoomFactor = zoomFactor;
        zoomFactor = newZoomFactor;
        firePropertyChange(ZOOM_FACTOR_PROPERTY,
                new Float(oldZoomFactor),
                new Float(newZoomFactor));
    }

    public Class getToolClass() {
        return toolClass;
    }

    public void setToolClass(Class newToolClass) {
        Class oldToolClass = toolClass;
        toolClass = newToolClass;
        firePropertyChange(TOOL_CLASS_PROPERTY,
                oldToolClass, newToolClass);
    }

    public Color getToolColor() {
        return toolColor;
    }

    public void setToolColor(Color newToolColor) {
        Color oldToolColor = toolColor;
        toolColor = newToolColor;
        firePropertyChange(TOOL_COLOR_PROPERTY,
                oldToolColor, newToolColor);
    }

    public Stroke getToolStroke() {
        return toolStroke;
    }

    public void setToolStroke(Stroke newToolStroke) {
        Stroke oldToolStroke = toolStroke;
        toolStroke = newToolStroke;
        firePropertyChange(TOOL_STROKE_PROPERTY,
                oldToolStroke, newToolStroke);
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        BufferedImage oldImage = this.image;
        this.image = image;
        firePropertyChange(IMAGE_PROPERTY, oldImage, image);
    }

//    public AbstractTool getLastTool() {
//        if (toolList.isEmpty())
//            return null;
//        else
//            return (AbstractTool) toolList.getLast();
//    }
//
//    public void setLastTool(AbstractTool newLastTool) {
//        AbstractTool oldLastTool = getLastTool();
//        toolList.add(newLastTool);
//        firePropertyChange(LAST_TOOL_PROPERTY,
//                oldLastTool, newLastTool);
//    }
//
//    public Iterator getToolIterator() {
//        return toolList.iterator();
//    }
//
//    public AbstractTool createTool(int paintStyle) {
//        AbstractTool tool = null;
//        try {
//            tool = (AbstractTool) toolClass.newInstance();
//        } catch (IllegalAccessException e) {
//            throw new InternalError(e.getMessage());
//        } catch (InstantiationException e) {
//            throw new InternalError(e.getMessage());
//        }
//        tool.setColor(toolColor);
//        tool.setStroke(toolStroke);
//        tool.setPaintStyle(paintStyle);
//        return tool;
//    }
//
//    public void removeTool(AbstractTool tool) {
//        toolList.remove(tool);
//    }

}
