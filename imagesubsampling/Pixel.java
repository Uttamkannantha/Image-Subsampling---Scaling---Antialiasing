package imagesubsampling;

/**
 * Class to hold all values related to pixel
 */
public class Pixel {
    int r;
    int g;
    int b;
    int y;
    int u;
    int v;
    int typeIntRGB;

    public Pixel() {
    }

    /**
     * Constructor to assign the values to class variables
     * @param r red
     * @param g green
     * @param b blue
     * @param y luma or brightness
     * @param u blue projection
     * @param v red projection
     * @param typeIntRGB Integer value to represent rgb values
     */
    public Pixel(int r, int g, int b, int y, int u, int v, int typeIntRGB) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.y = y;
        this.u = u;
        this.v = v;
        this.typeIntRGB = typeIntRGB;
    }
}
