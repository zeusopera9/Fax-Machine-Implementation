import BitVectorConversion.ImageToBitVector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class BitSender {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;
    private static final int FRAME_SIZE = 256;

    // Function calculate Parity
    public static byte calculateParity(byte[] frame) {
        int count = 0;
        for(byte bit : frame) {
            if(bit == 1) {
                count++;
            }
        }

        return (byte) (count % 2);
    }

    // Function to split bit vector into frames
    public static byte[][] splitIntoFrames(List<Integer> bitVector) {
        int numFrames = (int) Math.ceil(bitVector.size() / (double) FRAME_SIZE);
        byte[][] frames = new byte[numFrames][FRAME_SIZE + 1];

        for(int i = 0; i < numFrames; i++) {
            for(int j = 0; j < FRAME_SIZE; j++) {
                int index = i * FRAME_SIZE + j;
                if(index < bitVector.size()) {
                    frames[i][j] = bitVector.get(index).byteValue();
                } else {
                    frames[i][j] = 0; // Padding the extra bits in frame
                }
            }
            // Calculate and add Parity Bit
            frames[i][FRAME_SIZE] = calculateParity(frames[i]);
        }

        return frames;
    }

    public static void main(String[] args) {
        // Actual bitVector obtained from image
        List<Integer> bitVector = ImageToBitVector.imageToBitVector("C://Users//zaida//Desktop//dog.jpeg");

        try(Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream((socket.getInputStream()));

            // Split the Bit Vector into frames
            byte[][] frames = splitIntoFrames(bitVector);

            // First send the total number of frames to the server
            out.writeInt(frames.length);
            System.out.println("Sent total number of frames: " + frames.length);

            for(byte[] frame : frames) {
                // Send a frame
                out.write(frame);
                System.out.println("Sent Frame");

                // Waiting for Acknowledgement
                String response = in.readUTF();
                if(response.equals("ACK")) {
                    System.out.println("Received ACK, moving to the next frame");
                } else {
                    System.out.println("Received NACK, resending frame");
                    out.write(frame); // Resend the frame
                }
            }
        } catch(IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
