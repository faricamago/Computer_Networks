import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SWriting implements Runnable
{
    private String inName;
    private final int size_of_buffer;
    private Socket socket;

    public SWriting(String inName,int bufferSize,Socket socket)
    {
        this.inName = inName;
        this.size_of_buffer = bufferSize;
        this.socket = socket;
    }

    @Override
    public void run()
    {
        try {
            int numBytes = 0;

            // Creating input and output streams
            InputStream inStream = new BufferedInputStream(new FileInputStream(inName),size_of_buffer);
            OutputStream outStream = new BufferedOutputStream(socket.getOutputStream(),size_of_buffer);
            
            byte[] buffer = new byte[size_of_buffer];
    
            // Loop runs until EOF is reached
            while((numBytes = inStream.read(buffer))!=-1)
            {
                outStream.write(buffer, 0, numBytes);
                System.out.println("W "+numBytes);
                outStream.flush();
            }

            // Closing input stream
            inStream.close();

            //keeping socket open while closing its output stream 
            socket.shutdownOutput();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}