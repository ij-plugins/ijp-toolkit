------------------------
ij-3D-Toolkit - 8/7/2002
------------------------

3D Toolkit is a set of plugins for 3D and 2D operations on images in Image/J (
http://rsb.info.nih.gov/ij/). The first part of the toolkit, 3D IO, is a set of
plugins in formats two of the formats used for 3D image representation: VTK ()
and MetaImage (http://www.itk.org).

The toolkit also conatins some prototypes plugins for:

 * Morphological dilation (max)
 * Morphological erosion (min)
 * Connected threshold region growing
 * Auto volume clipping
 
All prototype algorithms should work on 2D and 3D images. Unlike the 3D IO
plugins, the prototypes are currently limited to processing GRAY8 (byte) images.


SOURCE

The source code consist two components:

* Toolkit - library containing implementation of all classes responsible for
	    plugins functionality.

* Plugins - plugin loaders that interface the Toolkit to Image/J. Ecah subproject
	    within Plugins directory corresponds to a some functional grouping of
	    plugins. Each of those groupings is intended to show up as a sub-menu
	    in Image/J plugins menu.

Binaries can be build from the source code using the build file (build.xml) for
Ant build tool ( http://jakarta.apache.org/ant/).


CONTACT INFORMATION

Author : Jarek Sacha 
E-mail : jsacha@users.sourceforge.net
Webpage: http://ij-plugins.sourceforge.net/