package imagesubsampling;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Transforms the image according to the image transform pipeline given in the question
 */
public class TransformImage {
    //Frames to display the image
    JFrame frame;
    JLabel lbIm1;
    BufferedImage imgOne;
    int width = 1920; //Default width of the image
    int height = 1080; // Default height of the image

    /**
     * Reads the image from the given path and stores r,g,b and y,u,v values of each pixel of the image
     * @param width width of the image
     * @param height height of the image
     * @param imgPath relative path of the image
     * @return Image class with all the required values
     */
    public Image readImageRGB(int width, int height, String imgPath){
        Image image = new Image(height,width); //Initialize the image with given height and width

            int frameLength = width*height*3;
            File file = new File(imgPath);

            try(RandomAccessFile raf = new RandomAccessFile(file, "r")){//Try with resources to catch exception
            raf.seek(0);

            long len = frameLength;
            byte[] bytes = new byte[(int) len];

            raf.read(bytes);
            int ind = 0;

            //Read the image from height to width
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    //integer value to be used in bufferedImage
                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);

                    //r,g,b to y,u,v conversion
                    int[] yuv = convertRGBtoYUV(r & 0xff, g & 0xff, b & 0xff);

                    //Creating the new pixel object for every pixel
                    Pixel pixel = new Pixel(r & 0xff, g & 0xff, b & 0xff, yuv[0], yuv[1], yuv[2], pix);

                    image.pixels[y][x] = pixel;
                   // img.setRGB(x,y,pix);  //not sure if needed
                    ind++;
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return image;
     }

    /**
     * Sub samples the image and re-assigns the value to the lost values by calculating the average of previous and next valid pixels
     * @param ySampling rate of sampling of y
     * @param uSampling rate of sampling of u
     * @param vSampling rate of sampling of v
     * @param input Input image
     * @return Image after adjusting up the values for y, u and v
     */
     public Image subSample(int ySampling, int uSampling, int vSampling, Image input){
        Image output = new Image(1080,1920);

         for(int i = 0; i < 1080; i++) {

             for(int j = 0; j < 1920; j++) {
                 Pixel oldPixel = input.pixels[i][j];
                 Pixel newPixel = new Pixel();

                 if(j % ySampling == 0){ // if the value does not require adjusting
                    newPixel.y = oldPixel.y;
                 }
                 else{ // if the value requires up sampling
                     int nextPosition = j - j%ySampling + ySampling; // calculating the next valid pixel
                     if(nextPosition < 1920){
                         //Calculate the average of previous pixel and next valid pixel and assign to current pixel
                         newPixel.y = (input.pixels[i][j-1].y + input.pixels[i][nextPosition].y)/2;
                     }
                     else{
                         newPixel.y = input.pixels[i][j-1].y;
                     }
                 }

                 if(j % uSampling== 0){
                     newPixel.u = oldPixel.u;

                 }
                 else{// if the value requires up sampling
                     int nextPosition = j - j%uSampling + uSampling;// calculating the next valid pixel
                     if(nextPosition < 1920){
                         //Calculate the average of previous pixel and next valid pixel and assign to current pixel
                         newPixel.u = (input.pixels[i][j-1].u + input.pixels[i][nextPosition].u)/2;
                     }
                     else{
                         newPixel.u = input.pixels[i][j-1].u;
                     }
                 }
                 if(j % vSampling == 0){
                     //Calculate the average of previous pixel and next valid pixel and assign to current pixel
                     newPixel.v = oldPixel.v;

                 }
                 else{// if the value requires up sampling
                     int nextPosition = j - j%vSampling + vSampling;// calculating the next valid pixel
                     if(nextPosition < 1920){
                         newPixel.v = (input.pixels[i][j-1].v + input.pixels[i][nextPosition].v)/2;
                     }
                     else{
                         newPixel.v = input.pixels[i][j-1].v;
                     }
                 }
                 //Calculate the rgb value for the yuv calculated
                 int [] newRGB = convertYUVtoRGB(newPixel.y, newPixel.u, newPixel.v);
                 newPixel.r = newRGB[0];
                 newPixel.g = newRGB[1];
                 newPixel.b = newRGB[2];

                 //Calculating integer rgb value
                 newPixel.typeIntRGB = 0xff000000 | ((newPixel.r & 0xff) << 16) | ((newPixel.g & 0xff) << 8) | (newPixel.b & 0xff);
                 output.pixels[i][j] = newPixel;
             }

         }
         return output;
     }

    /**
     * Converts r,g and b value to y,u, v
     * @param r r value to be converted
     * @param g g value to be converted
     * @param b b value to be converted
     * @return returns the array with coverted values
     */
    public int[] convertRGBtoYUV(int r, int g, int b) { //lot of changes done
        int[] yuv = new int[3];
        yuv[0] = (int) (r * 0.299 + g * 0.587 + b * 0.114);
        yuv[1] = (int) (r * 0.596 + g * -0.274 + b * -0.322);
        yuv[2] = (int) (r * 0.211 + g * -0.523 + b * 0.312);
        return yuv;
    }

    /**
     * Converts y,u and v value to respective r,g, v value
     * @param y y value to be converted
     * @param u u value to be converted
     * @param v v value to be converted
     * @return returns the array with converted values
     */
    public int[] convertYUVtoRGB(int y, int u, int v) { //lot of changes done
        int[] rgb = new int[3];
        rgb[0] = (int) (y  + u * 0.956 + u * 0.621);
        rgb[1] = (int) (y  + u * -0.272 + v * -0.647);
        rgb[2] = (int) (y  + u * -1.106 + v * 1.703);

        //Clamping the value to 255 if it is above 255 and clamping it to 0 if its below 0
        rgb[0] = Math.max(rgb[0], 0);
        rgb[0] = Math.min(rgb[0], 255);
        rgb[1] = Math.max(rgb[1], 0);
        rgb[1] = Math.min(rgb[1], 255);
        rgb[2] = Math.max(rgb[2], 0);
        rgb[2] = Math.min(rgb[2], 255);
        return rgb;
    }

    /**
     * Scales the image to required scale and also antialias the image according to the image
     * @param input Input image to be scaled
     * @param scaleHeightFactor height scale factor
     * @param scaleWidthFactor wiedth scale factor
     * @param alias 0 for no anti-aliasing and 1 for aliasing
     * @return returns transformed image according to input
     */
    Image scaleAndAntiAlias(Image input, float scaleHeightFactor, float scaleWidthFactor, int alias){

        int scaledHeight =  (int)(height * scaleHeightFactor);
        int scaledWidth = (int)(width * scaleWidthFactor);
        Image outputImage = new Image(scaledHeight,scaledWidth);

        for(int i = 0;i< scaledHeight;i++){
            for(int j =0;j< scaledWidth;j++){

                int remappedRow = (int) (i/scaleHeightFactor);
                int remappedCol = (int) (j/scaleWidthFactor);

                Pixel newPixel =  new Pixel();
                if(alias == 1){ // if aliasing is required
                    int count = 0;
                    for(int x =-1;x<=1;x++){//traverse neighbouring pixels and sum it (averaged later)
                       for(int y = -1;y<=1;y++){
                           if(remappedCol + y >= 0 && remappedCol + y < width && remappedRow + x >= 0 && remappedRow + x < height){
                               newPixel.r = newPixel.r + input.pixels[remappedRow+x][remappedCol+y].r;
                               newPixel.g = newPixel.g + input.pixels[remappedRow+x][remappedCol+y].g;
                               newPixel.b = newPixel.b + input.pixels[remappedRow+x][remappedCol+y].b;
                               count++;
                           }

                       }
                   }
                    //Divide the sum calculated above by count to get average
                    newPixel.r = newPixel.r/count;
                    newPixel.g = newPixel.g/count;
                    newPixel.b = newPixel.b/count;
                    newPixel.typeIntRGB = 0xff000000 | ((newPixel.r & 0xff) << 16) | ((newPixel.g & 0xff) << 8) | (newPixel.b & 0xff);
                }
                else{// if aliasing is not required
                    newPixel.r = input.pixels[remappedRow][remappedCol].r;
                    newPixel.g = input.pixels[remappedRow][remappedCol].g;
                    newPixel.b = input.pixels[remappedRow][remappedCol].b;
                    newPixel.typeIntRGB = input.pixels[remappedRow][remappedCol].typeIntRGB;
                }
                outputImage.pixels[i][j] = newPixel; // Assign the new pixel to output image
            }
        }
        return outputImage;
    }

    /**
     * Displays the image in Jframe
     * @param image Image to be displayed
     */
    public void displayImage(Image image){
        //Gets the buffered image required to display the image
        imgOne = image.getBufferedImage();
        frame = new JFrame();
        GridBagLayout gLayout = new GridBagLayout();
        frame.getContentPane().setLayout(gLayout);

        lbIm1 = new JLabel(new ImageIcon(imgOne));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(lbIm1, c);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args){

        String inputFilename = args[0];
        int y = Integer.parseInt(args[1]);
        int u = Integer.parseInt(args[2]);
        int v = Integer.parseInt(args[3]);
        float scaleWidth = Float.parseFloat(args[4]);
        float scaleHeight = Float.parseFloat(args[5]);
        int alias = Integer.parseInt(args[6]);

        TransformImage transformImage = new TransformImage();
        Image readImage =  transformImage.readImageRGB(1920,1080,inputFilename);
        Image subSampledImage = transformImage.subSample(y,u,v,readImage);
        Image finalImage = transformImage.scaleAndAntiAlias(subSampledImage, scaleHeight, scaleWidth,alias);
        transformImage.displayImage(readImage);
        transformImage.displayImage(finalImage);
    }
}
