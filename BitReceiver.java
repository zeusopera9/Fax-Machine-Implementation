import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BitReceiver {

    private static final int PORT = 12345;
    private static final int FRAME_SIZE = 256;

    // To check if received frame has valid parity (error detection)
    public static boolean verifyParity(byte[] frame) {
        int parityBit = frame[frame.length - 1];
        int count = 0;

        // Count the number of 1's in the frame (excluding parity bit)
        for(int i = 0; i < frame.length - 1; i++) {
            if(frame[i] == 1) {
                count++;
            }
        }

        // Check if parity matches
        return (count % 2 == parityBit);
    }

    // Function to reconstruct Bit Vector from frames
    public static List<Integer> reconstructBitVector(List<byte[]> frames) {
        List<Integer> bitVector = new ArrayList<>();

        for(byte[] frame : frames) {
            for(int i = 0; i < FRAME_SIZE; i++) {
                bitVector.add((int) frame[i]);
            }
        }

        return bitVector;
    }

    // Function to convert Bit Vector back to image and save it
    public static void bitVectorToImage(List<Integer> bitVector, int width, int height, String outputFilePath) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int bitIndex = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = 0, g = 0, b = 0;

                if (bitIndex + 24 <= bitVector.size()) {
                    // Extract 8 bits for each color channel
                    r = extractByteFromBitVector(bitVector, bitIndex);
                    g = extractByteFromBitVector(bitVector, bitIndex + 8);
                    b = extractByteFromBitVector(bitVector, bitIndex + 16);
                }
                bitIndex += 24;

                // Set RGB values for the pixel
                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgb);
            }
        }

        // Save the image to the specified output file path
        try {
            ImageIO.write(image, "png", new File(outputFilePath));
            System.out.println("Image saved to: " + outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Function to extract a byte from a list of bits (8 bits at a time)
    private static int extractByteFromBitVector(List<Integer> bitVector, int startIndex) {
        int value = 0;
        for(int i = 0; i < 8; i++) {
            value = (value << 1) | bitVector.get(startIndex + i);
        }
        return value;
    }

    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is waiting for connection ...");

            try(Socket socket = serverSocket.accept()) {
                System.out.println("Client connected");

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                // Receive the total number of frames from the client
                int totalFrames = in.readInt();
                System.out.println("Expecting " + totalFrames + " frames.");

                byte[] frame = new byte[FRAME_SIZE + 1]; // Frame + Parity Bit
                List<byte[]> frames = new ArrayList<>();

                while(frames.size() < totalFrames) {
                    // Receive a frame of bits
                    in.readFully(frame);

                    // Verify Parity
                    if(verifyParity(frame)) {
                        // Add valid frame to the list of frames
                        frames.add(frame.clone());  // Store the frame
                        // Send Acknowledgment (ACK)
                        out.writeUTF("ACK");
                        System.out.println("Received valid frame, sent ACK.");
                    } else {
                        // Send negative acknowledgement (NACK)
                        out.writeUTF("NACK");
                        System.out.println("Received invalid frame, sent NACK");
                    }
                }

                // Once all frames are received, reconstruct the bit vector
                List<Integer> bitVector = reconstructBitVector(frames);

                // Define image dimensions (these should match the original image)
                // For Flower
//                int width = 1886;
//                int height = 2829;

                // For cat
                int width = 3000;
                int height = 4206;
                String outputFilePath = "received_image.png";

                // Convert bit vector back to image and save
                bitVectorToImage(bitVector, width, height, outputFilePath);
            }
        } catch(IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
