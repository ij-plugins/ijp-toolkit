/*
 * IJ-Plugins
 * Copyright (C) 2002-2021 Jarek Sacha
 * Author's email: jpsacha at gmail dot com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Latest release available at https://github.com/ij-plugins/ijp-toolkit/
 */

package ij_plugins.toolkit.util

import ij.IJ

import java.awt._
import java.awt.event.{MouseAdapter, MouseEvent}
import java.io.IOException
import java.net.{URI, URISyntaxException}
import javax.swing._
import javax.swing.border.EmptyBorder
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkEvent.EventType._
import javax.swing.text.html.HTMLDocument
import scala.util.control.NonFatal

/**
  */
object IJPUtils {
  private val helpURL: String = "https://github.com/ij-plugins/ijp-toolkit"

  /**
    * Load icon as a resource for given class without throwing exceptions.
    *
    * @param aClass Class requesting resource.
    * @param path   Icon file path.
    * @return Icon or null if loading failed.
    */
  def loadIcon(aClass: Class[_], path: String): ImageIcon = {
    try {
      val url = aClass.getResource(path)
      if (url == null) {
        IJ.log("Unable to find resource '" + path + "' for class '" + aClass.getName + "'.")
        return null
      }
      return new ImageIcon(url)
    } catch {
      case NonFatal(t) =>
        IJ.log("Error loading icon from resource '" + path + "' for class '" + aClass.getName + "'. \n" + t.getMessage)
    }
    null
  }

  /**
    * Create pane for displaying a message that may contain HTLM formatting, including links.
    *
    * @param message the message.
    * @param  title  used in error dialogs.
    * @return component containg the message.
    */
  def createHTMLMessageComponent(message: String, title: String): JComponent = {
    val pane = new JEditorPane()
    pane.setContentType("text/html")
    pane.setEditable(false)
    pane.setOpaque(false)
    pane.setBorder(null)
    val htmlDocument = pane.getDocument.asInstanceOf[HTMLDocument]
    val font = UIManager.getFont("Label.font")
    val bodyRule = "body { font-family: " + font.getFamily + "; " + "font-size: " + font.getSize + "pt; }"
    htmlDocument.getStyleSheet.addRule(bodyRule)
    pane.addHyperlinkListener((e: HyperlinkEvent) => {
      if (e.getEventType == ACTIVATED) {
        openLinkInBrowser(e.getURL.toURI)
      }
    })
    pane.setText(message)
    pane
  }

  /**
    * Create simple info panel for a plugin dialog. Intended to be displayed at the top of a GenericDialog.
    *
    * @param title   title displayed in bold font larger than default.
    * @param message message that can contain HTML formatting.
    * @return a panel containing the message with a title and a default icon.
    */
  def createInfoPanel(title: String, message: String): Panel = {
    val rootPanel = new Panel(new BorderLayout(7, 7))
    val titlePanel = new Panel(new BorderLayout(7, 7))

    createLogoLabel().foreach(label => titlePanel.add(label, BorderLayout.WEST))

    titlePanel.add(createTitleLabel(title: String), BorderLayout.CENTER)

    rootPanel.add(titlePanel, BorderLayout.NORTH)

    val messageComponent = IJPUtils.createHTMLMessageComponent(message, title)
    rootPanel.add(messageComponent, BorderLayout.CENTER)

    // Add some spacing at the bottom
    val separatorPanel = new JPanel(new BorderLayout())
    separatorPanel.setBorder(new EmptyBorder(7, 0, 7, 0))
    separatorPanel.add(new JSeparator(), BorderLayout.SOUTH)
    rootPanel.add(separatorPanel, BorderLayout.SOUTH)

    rootPanel
  }

  /**
    * Create simple info panel for a plugin dialog. Intended to be displayed at the top.
    *
    * @param title   title displayed in bold font larger than default.
    * @param message message that can contain HTML formatting.
    * @return a panel containing the message with a title and a default icon.
    */
  def createInfoJPanel(title: String, message: String): JPanel = {
    val rootPanel = new JPanel(new BorderLayout(7, 7))
    val titlePanel = new JPanel(new BorderLayout(7, 7))

    createLogoLabel().foreach(label => titlePanel.add(label, BorderLayout.WEST))

    titlePanel.add(createTitleLabel(title: String), BorderLayout.CENTER)

    rootPanel.add(titlePanel, BorderLayout.NORTH)

    val messageComponent = IJPUtils.createHTMLMessageComponent(message, title)
    rootPanel.add(messageComponent, BorderLayout.CENTER)

    rootPanel
  }

  def openLinkInBrowser(uri: String): Unit = {
    openLinkInBrowser(new URI(uri))
  }

  def openLinkInBrowser(uri: URI): Unit = {
    try {
      Desktop.getDesktop.browse(uri)
    } catch {
      case ex@(_: IOException | _: URISyntaxException) =>
        IJ.error("Open Link in Browser",
          "Error following a link.\n" +
            "  " + uri.toString + "\n" +
            ex.getMessage)
    }
  }

  private def createLogoLabel(): Option[JLabel] = {
    val iconPath = "/ij_plugins/toolkit/IJP-48.png"
    Option(IJPUtils.loadIcon(this.getClass, iconPath)) map { logo =>
      val logoLabel = new JLabel(logo, SwingConstants.CENTER)
      logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT)
      logoLabel.addMouseListener(
        new MouseAdapter {
          private var _oldCursor: Option[Cursor] = None

          override def mouseClicked(e: MouseEvent): Unit = {
            openLinkInBrowser(helpURL)
          }

          override def mouseEntered(e: MouseEvent): Unit = {
            super.mouseEntered(e)
            _oldCursor = Option(logoLabel.getCursor)
            logoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR))
          }

          override def mouseExited(e: MouseEvent): Unit = {
            super.mouseExited(e)
            val cursor = _oldCursor match {
              case Some(c) => c
              case None => new Cursor(Cursor.DEFAULT_CURSOR)
            }
            logoLabel.setCursor(cursor)
          }
        })
      logoLabel
    }
  }

  private def createTitleLabel(title: String): JLabel = {
    val titleLabel = new JLabel(title)
    val font = titleLabel.getFont
    titleLabel.setFont(font.deriveFont(Font.BOLD, font.getSize * 2f))
    titleLabel
  }
}
