import net.sourceforge.jiu.data.RGBIntegerImage;
import java.util.LinkedList;

/** This class has some helper methods for texture synthesis algorithms.
 */
public class SynthAide {

    /** This copies a rectangular region of pixels from one view to another.
     *  The region copied will start at (firstx,firsty) and extend to include
     *  (firstx+width-1, firsty+height-1). Coordinates are view coordinates.
     */
    public static void copy(View from, View to, int firstx, int firsty,
			    int width, int height) {
	int lastx = firstx+width-1;
	int lasty = firsty+height-1;
	for(int y = firsty; y <= lasty; y++) {
	    for(int x = firstx; x <= lastx; x++) {
		to.putSample( x, y, from.getSample(x,y) );
	    }
	}
    }

    /** This returns a square 2D normalized Gaussian filter of the given size.
     */
    public static double[][] gaussian(int length) {

	if( length % 2 == 0 )
	    length++;

	// this stddev puts makes a good spread for a given size
	double stddev = length / 4.9;

	// make a 1d gaussian kernel
	double oned[] = new double[length];
	for(int i = 0; i < length; i++) {
	    int x = i - length/2;
	    double exponent = x*x / (-2 * stddev * stddev);
	    oned[i] = Math.exp(exponent);
	}

	// make the 2d version based on the 1d
	double twod[][] = new double[length][length];
	double sum = 0.0;
	for(int i = 0; i < length; i++) {
	    for(int j = 0; j < length; j++) {
		twod[i][j] = oned[i] * oned[j];
		sum += twod[i][j];
	    }
	}

	// normalize
	for(int i = 0; i < length; i++) {
	    for(int j = 0; j < length; j++) {
		twod[i][j] /= sum;
	    }
	}

	return twod;
    }

    /** This searches the given array for all non-negative values less than
     *  or equal to a given threshold and returns the list
     *  of array indicies of matches. Negative values are assumed
     *  to be invalid and thus are ignored.
     *  
     *  @return This returns a list of TwoDLoc objects.
     */
    public static LinkedList lessThanEqual(double[][] vals, double threshold) {

	LinkedList list = new LinkedList();
	for(int r = 0; r < vals.length; r++) {
	    for(int c = 0; c < vals[r].length; c++) {
		if( vals[r][c] >= 0 && vals[r][c] <= threshold ) {
		    list.addFirst( new TwoDLoc(r,c) );
		}
	    }
	}
	return list;
    }

    /** This blends the pixel values at (x,y) from the two patches and puts
     *  the result in toPatch.
     *  @param frompart This gives the ration of the fromPatch value to
     *                  use (0 <= frompart <= 1). The rest of the value
     *                  comes from toPatch.
     */
    public static void blend(Patch fromPatch, Patch toPatch, int x, int y,
			     double frompart) {

	int[] tovals = toPatch.getSample(x,y);
	int[] fromvals = fromPatch.getSample(x,y);
	int[] newvals = new int[3];
	for(int i = 0; i < 3; i++) {
	    double sum = tovals[i]*(1-frompart) + fromvals[i]*frompart;
	    newvals[i] = (int) Math.round( sum );
	}
	toPatch.putSample(x,y,newvals);
    }


    /** This computes the sum (accross channels) of squared differences
     *  between the pixel values at the given coordinate in the given
     *  views.
     */
    public static int ssd(View view1, View view2, int x, int y) {

	int vals[] = view1.getSample(x, y);
	int vals2[] = view2.getSample(x, y);
	
	int diff = vals[0] - vals2[0];
	int sum = diff * diff;
	diff = vals[1] - vals2[1];
	sum += diff * diff;
	diff = vals[2] - vals2[2];
	sum += diff * diff;

	return sum;
    }


}
