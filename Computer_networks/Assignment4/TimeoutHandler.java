import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.TimerTask;

class TimeoutHandler extends TimerTask
{
    DatagramSocket clientSocket;
    DatagramPacket packet;
    int seqNum;
    
    // define the constructor
    public TimeoutHandler(DatagramSocket clientSocket, DatagramPacket packet, int seqNum)
    {
        this.clientSocket = clientSocket;
        this.packet = packet;
        this.seqNum = seqNum;
    }
    // process re-transmission of the pending segment
    @Override
    public void run()
    {
        try {
            System.out.println("timeout");
            clientSocket.send(packet);
            System.out.println("retx		"+seqNum);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
