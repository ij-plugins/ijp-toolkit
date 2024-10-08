ij-plugins Toolkit Changelog
============================

Version 2.3.0
-------------

This release contains mostly change to API:

* Root package renamed from `net.sf.ij_plugins` to `ij_plugins.toolkit` [#14]

Version 2.2.0
-------------

### Plugin User Visible

* Add branding to plugin dialogs enhancement [#6]
* Add help button to SRG Tool dialog [#8]
* "Auto Clip 3D" ClassNotFoundException [#10]
* Prefix all plugins added to "Help > About Plugins" with "About: " [#11]
* Constant capitalization of plugin names bug [#12]


### API Changes

* Remove duplicate implementation of Progress classes: in `net.sf.ij_plugins.util.progress`
  and `net.sf.ij_plugins.ui.progress` [#16]
* Use 'master" branch for development and 'release' branch for stable releases [#9]

### Documentation Enhancements

* Move plugin Help from old website to wiki in this project  [#5]
* Update ReadMe with links to Wiki instead the old web page [#7]
* Redirect old website to this Wiki [#13]
* Remove deprecated references to old SourceForge project [#15]

[#5]: https://github.com/ij-plugins/ijp-toolkit/issues/5
[#6]: https://github.com/ij-plugins/ijp-toolkit/issues/6
[#7]: https://github.com/ij-plugins/ijp-toolkit/issues/7
[#8]: https://github.com/ij-plugins/ijp-toolkit/issues/8
[#9]: https://github.com/ij-plugins/ijp-toolkit/issues/9
[#10]: https://github.com/ij-plugins/ijp-toolkit/issues/10
[#11]: https://github.com/ij-plugins/ijp-toolkit/issues/11
[#12]: https://github.com/ij-plugins/ijp-toolkit/issues/12

[#13]: https://github.com/ij-plugins/ijp-toolkit/issues/13

[#15]: https://github.com/ij-plugins/ijp-toolkit/issues/14

[#15]: https://github.com/ij-plugins/ijp-toolkit/issues/15
[#16]: https://github.com/ij-plugins/ijp-toolkit/issues/16


Version 2.1.2
-------------------

* Support Scala 2.13

Version 2.1.1
-------------------

* Minor API implementation correction to fix [issue #4](https://github.com/ij-plugins/ijp-toolkit/issues/4): 
  `ProgressReporter.addProgressListener()` cannot be used in Scala 2.12.


Version 2.0 and 2.1
-------------------

* Source code moved to GitHub [ij-plugins/ijp-toolkit](https://github.com/ij-plugins/ijp-toolkit)
* New [SBT](http://www.scala-sbt.org/)-based build system and new packaging approach.
* Coherence Enhancing Diffusion was rewritten and significantly improved.
  The new algorithm is over 7 times faster (on a quad-core processor).
* Seeded Region Growing Tool: add button to create seed image.
* MetaImage Reader/Writer respect `Origin` tag, mapped into calibration x.y.z origin.
* MetaImage Reader - does not break on unknown tags, just ignores them.
* Removed deprecated DialogInfo and dependency in beanutil library.
* ijp-toolkit2 merged back into ijp-toolkit.


Version 1.9.1
-------------

This release is intended to correct 1.9.0 issue with missing classes in binary
distribution. There are also some small improvements in JavaDocs.

Bug fix:
* Issue #31 - Some plugins in 1.9.0 do not work - ClassNotFoundException


Version 1.9.0
-------------

New:
* Plugin to apply k-means clustering to another image

Developer visible:
* Dependent libraries updated to latest "full" release versions.

Requirements:
* Java 7 or better.


Version 1.8.0
-------------

New:
* Plugin to convert L\*a\*b\* stack to XYZ color space
* Plugin to convert XYZ stack to L\*a\*b\* color space

Improved:
* Create color space stacks, like L\*a\*b\*, have now slices labeled
  using band names rather than numbers

Developer visible:
* KMeans verifies that input stack is of correct type
* CubicSplineFunction is now part of ij-plugins toolkit, moved from ij-VTK

Requirements:
* Java 7 or better.


Version 1.7.1
-------------

Improvements:
* Update ReadMe file that got quite out od date
* Fast filters modified to reproduce content of the image that is outside of
  the ROI to follow ImageJ convention.

Requirements:
* Java 7 or better.


Version 1.7.0
-------------

New:
* k-means plugin now supports segmentation of 3D images.

Developer visible:
* k-means implementation renamed to KMeans2D. New class KMeans3D.

Requirements:
* Java 7 or better.


Version 1.6.1
-------------

New:
* Plugins > Color > Measure Bands - measures color images and stacks writing
  results in a single row, rather than a separate row for each band or slice.
  Works on RGB images or gray level stacks.

Improvements:
* Stacks created by RGB to CIE L\*a\*b\* and RGB to XYZ converters now have
  correct labels for each slice (name of the color band).
* Updated dependent libraries to recent versions.
* Add support for virtual stack to metaimage IO, based on patch provided 
  by Eric Nodwell.
* Add help button to plugin options dialog
  - Maximum Entropy Multi-Threshold plugin
  - k-means Clustering plugin
  - Region Growing plugin

Developer visible:
* Some utility methods added to VectorProcessor and ColorProcessorUtils.
* Support for color space conversions with custom white point


Version 1.6.0
-------------

New:
* Export as STL plugin allows saving image surfaces are a STL mesh data.
* MetaImage reader now supports multi-channel 3D images.

Improvements:
* Correct cluster distance measure in k-means.
* Order of plugin changed in Filter menu.
* Improved error reporting for SRG.
* MetaImage writer ask if data should be saved to a single file.
* 3D filters maintain image calibration (dilate, erode, median, clip).
* Rename AutoClipVolume to AutoCropVolume.

Developer visible:

* SRG Tools is now using standard overlays provided in newer versions of ImageJ.
* Signature of MiDecoder.open changed to support multi-channel MetaImage files.
* Improvements to JavaDoc.
* Separate distribution building Ant script into separate file for simpler
  dependency.


Version 1.5.2
-------------

Improved:
 * RFE-3095953: MetaImage writer saved images in a single file (no header/raw separation)

Bug fixes:
 * Bug-3097792: VTK Reader can handle images that spaces at the end of header lines.


...
---


Version 1.4.2
-------------

New:
 * SRG: Add action icons.
 * SRG: Send/load regions from ROI Manager.

Improved:
 * Better Glass Pane implementation.
 * Update RGB <-> Lab conversions to better match http://www.brucelindbloom.com

Developer visible:
 * Add KMeans.closestCluster(final float[] x) to enable using result to
   cluster new image.


Version 1.4.1
-------------

New:
 * SRG: Support for seeded region growing segmentation of 3D images.

Improved:
 * SRG: Support non-consecutive seed numbers.
 * SRG: Option to process single or all slices in the image (2D mode).
 * SRG: Copy lookup table (LUT) from the seed image to the output segmented image.

Change:
 * SRG API: seeds are specified as an image rather than lists of points.
 
Bug fixes:
 * Set pixels plugin: correct behavior with stacks.
 
Developer visible:
 * Use SVNAnt 1.3 for release number generation.
 

Version 1.4.0
-------------
Change:
 * Introduces incompatibility in SRG.setPoints(): seeds are set as an image
   instead of a list of points.


Version 1.3.0
-------------

New:
 * Seeder Region Growing (SRG) plugin.
 
Improved:
 * Fast Median Filter plugin - added preview to options dialog.
 
Bug fixes:
 * BUG-2687379: Division by zero in entropy threshold progress reporting.
   Pointed by Gabriel Landini.
 * Correct potential numeric computation problem in Maximum Entropy Threshold
   pointed by Wilhelm Burger. EPSILON is made larger to avoid infinite result
   when dividing by values close to EPSILON.
 * BUG-2690849: Multiband Difference Edge and Multiband Gradient Edge plugins 
   were missing.

Other:
 * Project code moved to Java 1.6.
 * API for display of multi color overlays (OverlayCanvas)
 * I/O Utilities API (IOUtils)


Version 1.2.1
-------------

Improvements:
 * Maximum Entropy Threshold plugin now supports 16 bit images.
 * Several improvements to SRG algorithm (available through API).

Bug fixes:
 * BUG-2037287: VectorProcessor: iterators bugs. Results in corrections
   to plugins that used VectorProcessor, like k-means and vector edge
   detector.


Version 1.2
-----------

Improvements:
* sRGB <-> XYZ <-> CIE L\*a\*b\* conversion modified to use approach presented
  at http://www.brucelindbloom.com/.
* Improvements to maximum entropy threshold plugins.

Bug fixes:
* BUG-1503298: MetaImage Reader did not support some of MetaImage tags.
* BUG-1812587: FastMedian Exception for even kernel size.
* BUG-1881444: k-means macro always showed animations.


Version 1.1.1
-------------

Bug fixes:
* BUG-1594780: Correct reading of COLOR_SCALARS: use GRAY8 when nValue==1, use 
  RGB when nValue==3.
 
Improvements:
* Fast Median Filter Plugin: add support for RGB images.


Version 1.1
------------

Bug fixes:
* Correct spelling of PeronaMalikAnisotropicDiffusion* classes.

Improvements:
* VectorProcessor improvements.

* ProgressReporter - Change method names for access and update of current progress.

* Remove dependency on L2FProf library, use DialogUtils instead.

* Speedup computation of maximum entropy multi threshold by caching interval
  entropy computation (about 30x speedup for 3-threshold).

* VTKEncoder/Decoder - Add support for reading/writing RGB images in VTK
  format. Contributed by keesh on 2006-10-22.


New:
* DialogUtil: utilities for simplifying creation of property editor dialogs.

* AbstractAnisotropicDiffusion*: Abstract base classes to simplify creation of
  anisotropic diffusion filters and plugins.

* SRAD - Spackle Reducing Anisotropic Diffusion

* RGB to YCbCr color space conversion.

* IJDebug utility for logging only when IJ.debugMode is on.


Version 1.0
-----------

* Jar file renamed from 'ij-plugins-toolkit.jar' to 'ij-plugins_toolkit.jar',
  So it can be recognized bu ImageJ plugin discovery (presence of '_' in file 
  name needed.

* New source code layout. 'Plugins' and 'Toolkit' subdirectories depreciated, all
  source moved to root 'src' directory. Plugins distributed in a single JAR:
  ''ij-plugins_toolkit.jar' (RFE-1208248).

* 'Toolkit/src' code moved to 'src'.

* Image Quilting plugin moved to main source directory ('src').

* 3D IO plugins moved to main source directory ('src').  


Changes - 3D Toolkit
--------------------

### Version *

* VTK Filters moved to module ij-VTK


### Version 0.3.x

* VTK Filters - Optimize loading of dynamic VTK libraries.


### Version 0.3 - New features + some bug fix

New & improved:
   * Set of plugins for direct access to native VTK 3D filters: anisotropic
     diffusion, Laplacian, median, erosion, dilation.
   * Connected threshold growing - improved GUI, support for 16 bit
     images
   * 3D Toolkit plugins - better error detection
   * Number of related API extensions
   * ij.jar updated to 1.29x
   * Cleaner build scripts.
   
Bug fixes:
   * Corrected problems with voxel calibration in VTK and MetaImage I/O. 


### Version 0.2.x - bug fix release

* ij.jar (ImageJ v.1.28u binaries) added to source distribution. This way
    no additional libraries are needed to build from source.

### Version 0.2.1 - bug fix release

* VTK Writer: Removed bug introduced in v.0.2 - VTK was unable to read
    images generated by vtkEncoder - there was no space separating a tag
    and its value in image header.
    
* VTK Reader was not aware of tags CELL_DATA and COLOR_SCALARS.

### Version 0.2

New plugins added:
  
* VTK Reader 

* MetaImage Reader 

* MetaImage Writer
