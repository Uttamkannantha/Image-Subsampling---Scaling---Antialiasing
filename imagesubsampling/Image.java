package imagesubsampling;

import java.awt.image.BufferedImage;

public class Image {
    Pixel[][] pixels;
    int height;
    int width;

    /**
     * Constructor to assigning the height and weight
     * @param height Height of the image
     * @param width Width of the image
     */
    Image(int height, int width){
        this.height = height;
        this.width = width;
        pixels = new Pixel[height][width];
    }


    /**
     * Creates the Buffered image required to print the image
     * @return Return the buffered image, required to print image in java
     */
    public BufferedImage getBufferedImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                try {
                    image.setRGB(i, j, pixels[j][i].typeIntRGB);
                } catch (Exception e) {
                    System.out.println("i: " + i + " j: " + j);
                }
            }
        }
        return image;
    }
}
