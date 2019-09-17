//
// Example of using Seeded Region Growing plugin from a macro
//

minThreshold = 45;
maxThreshold = 140;

// Load input image - blobs
run("Blobs (25K)");
imSrc = getTitle();

// Start preparing seeds for region growing
run("Duplicate...", " ");
im1 = getTitle();

// Create seeds for blobs
setThreshold(maxThreshold , 255);
run("Create Selection");
run("Set...", "value=128");
run("Make Inverse");
run("Set...", "value=0");

// Create seeds for background
selectWindow(imSrc);
run("Duplicate...", " ");
im2 = getTitle();
setThreshold(0, minThreshold );
run("Create Selection");

selectWindow(im1);
run("Restore Selection");
run("Set...", "value=250");

resetThreshold();
run("Select None");

// Run region growing from seeds
run("Seeded Region Growing ...", "image=" + imSrc + " seeds=" + im1 + " stack=[Current slice only]");

// Extract results 
setThreshold(0, 129);
run("Create Selection");
roiManager("Add");

// Cleanup
close(im1);
close(im2);
