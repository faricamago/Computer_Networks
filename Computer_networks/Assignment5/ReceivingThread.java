import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceivingThread implements Runnable
{
    private DatagramSocket clientSocket;
    private int timeout;
    private int windowSize;
    //private DatagramPacket packet;
    private int ackNum;
    //private TimeManager tm = null;

    public ReceivingThread(int initial_sequence_number, DatagramSocket clientSocket,int timeout,int windowSize)
    {
        this.clientSocket = clientSocket;
        this.timeout = timeout;   
        this.windowSize = windowSize;  
        this.ackNum = initial_sequence_number;
    }

    @Override
    public void run()
    {
        try
        {
            int size_of_buffer = FtpSegment.MAX_PAYLOAD_SIZE;
			// creating a buffer
			byte[] buffer = new byte[size_of_buffer];
            DatagramPacket packet = new DatagramPacket(buffer,size_of_buffer);
            //while(!(go.getSeq_num_queue().size()==0) || (sendingThr.isAlive()==true))
            while((!(GoBackFtp.segment_queue.isEmpty())) || (GoBackFtp.sendThread.isAlive()==true))
            {
                clientSocket.receive(packet);
				FtpSegment segment2 = new FtpSegment(packet);
				//reading ack
				ackNum = segment2.getSeqNum();
                System.out.println("ack		"+ackNum);
                for(FtpSegment seg:GoBackFtp.segment_queue)
                {
                    int next = seg.getSeqNum();
                    if(next <= ackNum)
                    {
                        //Stop re-transmission timer
                        SendingThread.tm.stopTimer();
                        //Update the transmission queue
                        GoBackFtp.segment_queue.remove(seg);
                        if(!(GoBackFtp.segment_queue.isEmpty()))
                        {
                            // start re-transmission timer
                            SendingThread.tm = new TimeManager(clientSocket,timeout);
                            SendingThread.tm.startTimer();
                        }
                    }
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}