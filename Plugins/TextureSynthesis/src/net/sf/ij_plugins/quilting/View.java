import net.sourceforge.jiu.data.RGBIntegerImage;


/** This class is provides a view of an image. The view
 *  is a small part of the larger image. This provides
 *  a way to pass around parts of an image without actually
 *  copying the pixels. This does not ensure that
 *  the whole view is inside the image, so be careful.
 */
public class View {

    protected RGBIntegerImage image;
    protected int xoffset, yoffset;

    /** array/channel index of the red component */
    public static final int RED = RGBIntegerImage.INDEX_RED;
    /** array/channel index of the green component */
    public static final int GREEN = RGBIntegerImage.INDEX_GREEN;
    /** array/channel index of the blue component */
    public static final int BLUE = RGBIntegerImage.INDEX_BLUE;

    // this is used in the constructor, see the comment there
    private static boolean invalid_indices = true;

    /** This creates a new view of the given image.
     *  @param image This is the image to view.
     *  @param x This is the x coord of the upper left corner of the view
     *           in image's pixel coordinates.
     *  @param y This is the x coord of the upper left corner of the view
     *           in image's pixel coordinates.
     */
    public View(RGBIntegerImage image, int x, int y) {

	this.image = image;
	setCorner(x,y);

	// I'm making assumptions about the RGBImage constants
	//  so I threw this in to validate those assumptions
	if( invalid_indices ) {
	    invalid_indices = RED < 0 || GREEN < 0 || BLUE < 0;
	    invalid_indices |= RED > 2 || GREEN > 2 || BLUE > 2;
	    invalid_indices |= RED == BLUE || RED == GREEN || BLUE == GREEN;
	    if( invalid_indices ) {
		throw new RuntimeException("ERROR: "+getClass()
					   +" is broken due to changes in"
					   +" the JIU's RGBImage class");
	    }
	}
    }

    /** This moves the view to the specified position. */
    public void setCorner(int x, int y) {
	this.xoffset = x;
	this.yoffset = y;
    }

    /** returns the image x coordinate of the upper left corner of the view */
    public int getCornerX() {
	return xoffset;
    }

    /** returns the image y coordinate of the upper left corner of the view */
    public int getCornerY() {
	return yoffset;
    }

    /** returns true iff getCornerX() == 0 */
    public boolean isAtLeftEdge() {
	return xoffset == 0;
    }

    /** returns true iff getCornerY() == 0 */
    public boolean isAtTopEdge() {
	return yoffset == 0;
    }

    /** This fetches the RGB values from the given view coordinates. */
    public int[] getSample(int x, int y) {
	x = imageX(x);
	y = imageY(y);
	int out[] = new int[3];
	out[RED] = image.getSample(RED, x, y);
	out[GREEN] = image.getSample(GREEN, x, y);
	out[BLUE] = image.getSample(BLUE, x, y);
	return out;
    }

    /** Get the sample from the (x,y) view coordinate from the given channel.
     */
    public int getSample(int channel, int x, int y) {
	x = imageX(x);
	y = imageY(y);
	return image.getSample(channel, x, y);
    }

    /** This sets the RGB sample at the given view coordinates. */
    public void putSample(int x, int y, int[] newvals) {
	x = imageX(x);
	y = imageY(y);
	image.putSample(RED, x, y, newvals[RED]);
	image.putSample(GREEN, x, y, newvals[GREEN]);
	image.putSample(BLUE, x, y, newvals[BLUE]);
    }

    /** Set the sample at the (x,y) view coordinate for the given channel.
     */
    public void putSample(int channel, int x, int y, int newval) {
	x = imageX(x);
	y = imageY(y);
	image.putSample(channel, x, y, newval);
    }

    /** This returns the image that this is a view of. */
    public RGBIntegerImage getImage() {
	return image;
    }

    /* This converts view coordinates into image coordinates. */
    protected int imageX(int x) {
	return x + xoffset;
    }

    /* This converts view coordinates into image coordinates. */
    protected int imageY(int y) {
	return y + yoffset;
    }

}
