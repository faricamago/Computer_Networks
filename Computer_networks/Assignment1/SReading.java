/* NAME: FARICA MAGO
 * UCID: 30111924
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SReading implements Runnable
{
    private String outFileName;
    private final int size_of_buffer;
    private Socket socket;

    public SReading(String outName,int bufferSize,Socket socket)
    {
        this.outFileName = outName;
        this.size_of_buffer = bufferSize;
        this.socket = socket;
    }

    @Override
    public void run()
    {
        try {
            int numBytes = 0;
            // Creating input and output streams
            InputStream inStream = new BufferedInputStream(socket.getInputStream(),size_of_buffer);
            OutputStream outStream = new BufferedOutputStream(new FileOutputStream(outFileName),size_of_buffer);

            byte[] buffer = new byte[size_of_buffer];
    
            // Loop runs until EOF is reached
            while((numBytes = inStream.read(buffer))!=-1)
            {
                outStream.write(buffer, 0, numBytes);
                System.out.println("R "+numBytes);
                outStream.flush();
            }

            // closing input and output streams
            inStream.close();
            outStream.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}