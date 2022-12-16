import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.TimerTask;

class TimeoutHandler extends TimerTask
{
    private DatagramSocket clientSocket;
     
    
    // define the constructor
    public TimeoutHandler(DatagramSocket clientSocket)
    {
        this.clientSocket = clientSocket;
    }
    // process re-transmission of the pending segment
    @Override
    public void run()
    {
        try {
            System.out.println("timeout");
            for(FtpSegment seg:GoBackFtp.segment_queue)
            {
                int next = seg.getSeqNum();
                DatagramPacket packet = FtpSegment.makePacket(seg, GoBackFtp.iAdd, GoBackFtp.UDP_port_number_for_server);
                clientSocket.send(packet);
                System.out.println("retx		"+next);
                //GoBackFtp.segment_queue.remove(seg);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
