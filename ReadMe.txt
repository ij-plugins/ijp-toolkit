------------------------
ij-3D-Toolkit - 8/7/2002
------------------------

3D Toolkit is a set of plugins for 3D and 2D operations on images in
Image/J ( http://rsb.info.nih.gov/ij/). The first part of the toolkit, 3D
IO, is a set of plugins in formats two of the formats used for 3D image
representation: VTK (http://public.kitware.com/VTK/) and MetaImage (
http://www.itk.org).

The toolkit also conations some prototypes plugins for:

 * Morphological dilation (max)
 * Morphological erosion (min)
 * Connected threshold region growing
 * Auto volume clipping
 
All prototype algorithms should work on 2D and 3D images. Unlike the 3D IO
plugins, the prototypes are currently limited to processing GRAY8 (byte)
images.


INSTALLATION

Following steps assume that you downloaded binary archive 
ij-3D-Toolkit_bin_0.2.zip or newer from http://ij-plugins.sf.net

1. Locate your Image/J plugins folder. Remove from Image/J plugins folder
   subdirectories named '3D IO' and '3D Toolkit' (if their exist).

2. Unzip content of ij-3D-Toolkit_bin_0.2.zip into Image/J plugins
   directory.

3. Add ij-plugins-tookit.jar to Image/J class path. One of the ways to do
   it is by extending classpath in Image/J startup script, for instance:
   
   java -cp ij.jar;plugins/ij-plugins-toolkit.jar ij.ImageJ


SOURCE

The source code consist two components:

* Toolkit - library containing implementation of all classes responsible
	    for plugins functionality.

* Plugins - plugin loaders that interface the Toolkit to Image/J. Each
	    subproject within Plugins directory corresponds to a some
	    functional grouping of plugins. Each of those groupings is
	    intended to show up as a sub-menu in Image/J plugins menu.

Binaries can be build from the source code using the build file
(build.xml) for Ant build tool ( http://jakarta.apache.org/ant/).


CONTACT INFORMATION

Author : Jarek Sacha 
E-mail : jsacha@users.sourceforge.net
Webpage: http://ij-plugins.sourceforge.net/