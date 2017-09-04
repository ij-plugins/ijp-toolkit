IJ-Plugins Toolkit
==================

[![Build Status](https://travis-ci.org/ij-plugins/ijp-toolkit.svg?branch=develop)](https://travis-ci.org/ij-plugins/ijp-toolkit)

[IJ-Plugins Toolkit](http://ij-plugins.sourceforge.net/plugins/toolkit.html) is a set of plugins for [ImageJ]. The plugins are grouped into:

* **[3D IO]** - import and export of data in 3D formats.

* **[3D Toolkit]** - operations on stacks interpreted as 3D images, including
  morphological operations.

* **[Color]** - color space conversion, color edge detection (color and
  multi-band images).

* **[Filters]** - fast median filters, coherence enhancing diffusion, and various anisotropic diffusion filters.

* **[Graphics]** - Texture Synthesis - A plugin to perform texture synthesis
  using the image quilting algorithm of Efros and Freeman.

* **[Segmentation]** - image segmentation through clustering, thresholding, and
  region growing.

ImageJ Plugins Installation
---------------------------

### Automatic Installation Through Update Center

For [ImageJ 2] or [Fiji] you can install IJ-Plugins Toolkit using update center:

http://sites.imagej.net/IJ-Plugins/

### Manual Installation

1. [Download](https://sourceforge.net/projects/ij-plugins/files/ij-plugins_toolkit/)
   latest binaries for IJ-Plugins Toolkit. Look for version with the highest number.
   Plugin binaries will be in file named: `ij-plugins_toolkit_bin_*.zip`.

2. Uncompress content of `ij-plugins_toolkit_bin_*.zip` to ImageJ's `plugins` directory.
   You can find location of ImageJ plugins directory by selecting in ImageJ
   "Plugins"/"Utilities"/"ImageJ Properties", look for value of tag "plugins dir"
   near the bottom of the displayed Properties' window.

3. Restart ImageJ to load newly installed plugins.

Using as a Stand-alone Library
------------------------------

There are some examples of using IJ-Plugins Toolkit as a stand-alone library in the [examples] folder. You will need to add dependency on:

```
groupId   : net.sf.ij-plugins
artifactId: ijp-toolkit
version   : 2.1.1
```
For instance, for [SBT] it would be:

```
"net.sf.ij-plugins" %% "ijp-toolkit" % "2.1.1"
```

Running from source
-------------------

You can build and run the plugins within ImageJ using [SBT] task `ijRun`

```
sbt ijRun
```

It will build the code, setup plugins directory, and the start ImageJ. `ijRun` is provided by SBT plugin [sbt-imagej].


[ImageJ]:     http://rsbweb.nih.gov/ij/
[ImageJ 2]:   http://imagej.net
[Fiji]:       http://imagej.net/Fiji
[sbt-imagej]: https://github.com/jpsacha/sbt-imagej
[SBT]:        http://www.scala-sbt.org/

[3D IO]:        http://ij-plugins.sourceforge.net/plugins/3d-io/index.html
[3D Toolkit]:   http://ij-plugins.sourceforge.net/plugins/3d-toolkit/index.html
[Color]:        http://ij-plugins.sourceforge.net/plugins/color/index.html
[Filters]:      http://ij-plugins.sourceforge.net/plugins/filters/index.html
[Graphics]:     http://ij-plugins.sourceforge.net/plugins/texturesynthesis/index.html
[Segmentation]: http://ij-plugins.sourceforge.net/plugins/segmentation/index.html
