// Customize behaviour of MetaImage Reader

// Running it through a macro can avoid opening options dialog
path = File.openDialog("Select MetaImage File");
run("MetaImage Reader ...", "open="+path);

// Adjust brightness of the opened images. `Use` stack histogram.
run("Enhance Contrast...", "saturated=0.1 use");