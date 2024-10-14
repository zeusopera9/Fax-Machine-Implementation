package BitVectorConversion;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageToBitVector {

    // Method to load the image from a given file path
    public static BufferedImage loadImage(String imagePath) {
        BufferedImage image = null;
        try {
            // Read the image file into a BufferedImage
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            System.out.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
        }
        return image;
    }

    // Function to convert image to bit vector

    public static List<Integer> imageToBitVector(String imagePath) {
        List<Integer> bitVector = new ArrayList<>();

        try {
            BufferedImage image = ImageIO.read(new File(imagePath));

            // Loop through each pixel
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);

                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    // Convert each color channel to bits and add to the bit vector
                    for (int i = 7; i >= 0; i--) {
                        bitVector.add((red >> i) & 1);
                    }
                    for (int i = 7; i >= 0; i--) {
                        bitVector.add((green >> i) & 1);
                    }
                    for (int i = 7; i >= 0; i--) {
                        bitVector.add((blue >> i) & 1);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return bitVector;
    }

    public static void main(String[] args) {
        String imagePath = "C://Users//zaida//Desktop//flower.jpg";

        // Convert image to bit vector
        List<Integer> bitVector = imageToBitVector(imagePath);

        // Print the bit vector
        System.out.println("Bit Vector Size: " + bitVector.size());
        System.out.println("Bit Vector: " + bitVector);
    }
}
