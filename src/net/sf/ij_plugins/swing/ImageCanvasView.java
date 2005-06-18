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
package net.sf.ij_plugins.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Initial implemntation based on class PaintModel from Andrei Cioroianu article <i>Prototyping
 * Desktop Applications</i> (http://www.onjava.com/pub/a/onjava/2004/04/28/desktop.html).
 */
public class ImageCanvasView extends JComponent {
    private static final long serialVersionUID = 1L;
    private ImageCanvasModel model;
//    private AbstractTool currentTool;

    public ImageCanvasView() {
        model = new ImageCanvasModel(this);
        registerListeners();
    }

    public ImageCanvasModel getModel() {
        return model;
    }

    protected void registerListeners() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    requestFocus();
//                    currentTool = model.createTool(
//                        AbstractTool.DRAW_STYLE);
//                    toolAction(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
//                    toolAction(e);
//                    model.setLastTool(currentTool);
//                    currentTool = null;
                    repaint();
                }
            }
        });

//        addMouseMotionListener(new MouseMotionAdapter() {
//            public void mouseDragged(MouseEvent e) {
//                if (SwingUtilities.isLeftMouseButton(e))
//                    toolAction(e);
//            }
//        });

        model.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (isShowing())
                    repaint();
            }
        });
    }

//    protected void toolAction(MouseEvent e) {
//        e.consume();
//        Graphics2D g2 = (Graphics2D) getGraphics();
//        float zoomFactor = model.getZoomFactor();
//        g2.scale(zoomFactor, zoomFactor);
//        float x = e.getX() / zoomFactor;
//        float y = e.getY() / zoomFactor;
//        currentTool.action(e.getID(), x, y, g2);
//        g2.dispose();
//    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Image image = model.getImage();
        if (image == null) {
            g.setColor(Color.black);
            Dimension size = getSize();
            g.drawRect(0, 0, size.width - 1, size.height - 1);
        }
        Graphics2D g2 = (Graphics2D) g;
        float zoomFactor = model.getZoomFactor();
        g2.scale(zoomFactor, zoomFactor);
        if (image != null)
            g2.drawImage(image, 0, 0, this);
        g2.setBackground(getBackground());
//        Iterator iterator = model.getToolIterator();
//        while (iterator.hasNext()) {
//            AbstractTool tool
//                = (AbstractTool) iterator.next();
//            tool.paint(g2, backImage);
//        }
    }

    public static void main(String[] args) {
        ImageCanvasView canvas = new ImageCanvasView();
//        canvas.setPreferredSize(new Dimension(256,256));

        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);

        ImageCanvasModel imageCanvasModel = canvas.getModel();
        imageCanvasModel.setImage(image);

        final JFrame frame = new JFrame("ImageCanvasView Test");
        frame.getContentPane().add(canvas);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

}
