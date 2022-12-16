/* NAME: FARICA MAGO
 * UCID: 30111924
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.FileInputStream;

public class SendingThread implements Runnable
{
    private int initial_sequence_number;
    private File fileCheck;
    private DatagramSocket clientSocket;
    private int timeout;
    private int windowSize;
    public static TimeManager tm = new TimeManager(GoBackFtp.clientSocket, GoBackFtp.timeout);

    public SendingThread(int initial_sequence_number,File fileCheck, DatagramSocket clientSocket,int timeout,int windowSize)
    {
        this.initial_sequence_number = initial_sequence_number;
        this.fileCheck =  fileCheck;
        this.clientSocket = clientSocket;
        this.timeout = timeout;
        this.windowSize = windowSize;
    }

    @Override
    public void run()
    {
        try {
            //initialising sequence number
			int seqNum = initial_sequence_number;
			//initialising buffer size
			int size_of_buffer = FtpSegment.MAX_PAYLOAD_SIZE;
			// creating a buffer
			byte[] buffer = new byte[size_of_buffer];
			// input stream to read data from the file
			//FileInputStream input = new FileInputStream(fileCheck);
			InputStream input = new BufferedInputStream(new FileInputStream(fileCheck),FtpSegment.MAX_PAYLOAD_SIZE);
			// bytes read
			int byte_read_out = input.read(buffer);
			//timer = new Timer();
			while((byte_read_out!=-1))
        	{
				// creating a segment
				FtpSegment segment = new FtpSegment(seqNum, buffer,byte_read_out);
				//creating a packet
				DatagramPacket packet = FtpSegment.makePacket(segment, GoBackFtp.iAdd, GoBackFtp.UDP_port_number_for_server);
                // Waiting while transmission queue is full
                while(GoBackFtp.segment_queue.size() == windowSize)
                {
                    //sequence_numbers_queue = go.getSeq_num_queue();
                    //packets_queue = go.getpacket_queue();
                }

				//sending the packet
				clientSocket.send(packet);
				System.out.println("send		"+seqNum);
                //adding packet to the transmission queue
                GoBackFtp.segment_queue.add(segment);
                //setting up the timer
                if(GoBackFtp.segment_queue.peek().getSeqNum()==seqNum)
                {
                    tm.startTimer();
                }
				byte_read_out = input.read(buffer);
                seqNum++;				
        	}
            input.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}