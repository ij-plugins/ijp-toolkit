import java.io.*;

import net.sourceforge.jiu.codecs.PNMCodec;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGBIntegerImage;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.OperationFailedException;

/** This class is a simple driver for my image quilting implementation.
 *  It parses your parameters, loads up the input image, runs the
 *  algorithm, and saves the resulting image.
 */
public class Driver {

    /** Run this to see the list of required and optional parameters.
     */
    public static void main(String args[]) throws Exception {

	if( args.length < 8 ) {
	    System.out.println("usage: java Driver inputFile outputFile outputWidth outputHeight patchSize overlapsize allowHorizontalPaths pathCostWeight");
	    System.out.println("A value of -1 for any of the last 4 arguments"
			       +" will be replaced with a default value.");
	    System.out.println("This currently only knows how to handle "
			       +"8 bit color PPM format images.")
	    return;
	}

	System.out.println("Started at "+new java.util.Date());

	// parse the arguments
	String inname = args[0];
	String outname = args[1];
	int outputwidth = Integer.parseInt(args[2]);
	int outputheight = Integer.parseInt(args[3]);
	int patchsize = ImageQuilter.DEFAULT_PATCH_SIZE;
	try {
	    int tmp = Integer.parseInt(args[4]);
	    if( tmp >= 0 ) {
		patchsize = tmp;
	    }
	}
	catch( NumberFormatException nfex ) {
	}
	int overlapsize = ImageQuilter.DEFAULT_OVERLAP_SIZE;
	try {
	    int tmp = Integer.parseInt(args[5]);
	    if( tmp >= 0 ) {
		overlapsize = tmp;
	    }
	}
	catch( NumberFormatException nfex ) {
	}
	boolean allowHPaths = false;
	if( args[6] != null && args[6].length() > 0 && !"-1".equals(args[6]) ){
	    if( args[6].charAt(0) == 't' || args[6].charAt(0) == 'T'
		|| args[6].charAt(0) == '1' ) {
		allowHPaths = true;
	    }
	}
	double pathCostWeight = 0.0;
	try {
	    double tmp = Double.parseDouble(args[7]);
	    if( tmp >= 0 ) {
		pathCostWeight = tmp;
	    }
	}
	catch( NumberFormatException nfex ) {
	}


	// load the input image
	PNMCodec codec = new PNMCodec();
	codec.setInputStream(new FileInputStream(inname));
	codec.process();
	RGBIntegerImage input = (RGBIntegerImage) codec.getImage();
	codec.close();
	System.out.println("Input image size: "+input.getWidth()
			   +"x"+input.getHeight());

	// run the synthesis algorithm
	ImageQuilter synther = new ImageQuilter(input, patchsize, overlapsize,
						allowHPaths, pathCostWeight);
	RGBIntegerImage output = synther.synthesize(outputwidth, outputheight);

	// save the output image
	codec = new PNMCodec();
	codec.setImage(output);
	codec.setOutputStream(new FileOutputStream(outname));
	codec.process();
	codec.close();

	System.out.println("Ended at "+new java.util.Date());

    }

}
