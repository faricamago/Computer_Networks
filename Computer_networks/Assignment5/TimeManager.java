import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;

class TimeManager
{
    private DatagramSocket clientSocket;
    private int timeout;
    private Timer timer = new Timer();
    private TimerTask timertask = new TimeoutHandler(GoBackFtp.clientSocket);
     
    
    // define the constructor
    public TimeManager(DatagramSocket clientSocket,int timeout)
    {
        this.clientSocket = clientSocket;
        this.timeout = timeout; 
    }
    
    public synchronized void startTimer()
    {
		timer.scheduleAtFixedRate(timertask,this.timeout,this.timeout);
    }

    public synchronized void stopTimer()
    {
        timertask.cancel();
        timer.cancel();
    }
}
