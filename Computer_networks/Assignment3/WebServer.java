

/**
 * WebServer Class
 * 
 * Implements a multi-threaded web server
 * supporting non-persistent connections.
 * 
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;


public class WebServer extends Thread {
	
    private boolean shutdown = false;
	// global logger object, configures in the driver class
	private static final Logger logger = Logger.getLogger("WebServer");
	
	
    /**
     * Constructor to initialize the web server
     * 
     * @param port 	    The server port at which the web server listens > 1024
     * @param timeout 	The timeout value for detecting non-resposive clients, in milli-second units
     * 
     */

    private int port;
    private int timeout;
    private ExecutorService executorPool = Executors.newFixedThreadPool(8);
     
	public WebServer(int port, int timeout)
    {
        this.port = port;
        this.timeout = timeout;
    }

	
    /**
	 * Main web server method.
	 * The web server remains in listening mode 
	 * and accepts connection requests from clients 
	 * until the shutdown method is called.
	 *
     */
	public void run()
    {
        try
        {
            // Opening the server socket and listening for connections via port specified by the user
            ServerSocket serverSocket = new ServerSocket(port);

            // setting timeout to terminate the thread periodically
            serverSocket.setSoTimeout(100);

            while(!shutdown)
            {
                
                try{       
                    // accepting the connection due to the request from the client
                Socket socket = serverSocket.accept();
                //socket.setSoTimeout(this.timeout);
                InetAddress inet = socket.getInetAddress();
                String add = inet.toString();
                String final_inet = add.substring(1);
                System.out.println("A new client has connected on the port number: "+socket.getPort());
                System.out.println("The address of the client is: "+final_inet);
                //Creating thread
                executorPool.execute(new WorkerThread(this.timeout,socket));
                }
                catch(SocketTimeoutException e)
                {
                    
                }
            }

            // Shutdown executor pool
            executorPool.shutdown();

            // Using the code given in the assign3 sheet

            try
            {
                if(!executorPool.awaitTermination(5,TimeUnit.SECONDS))
                {
                    executorPool.shutdownNow();
                }
            }catch(InterruptedException e)
            {
                executorPool.shutdownNow();
            }
            // closing the server socket
            serverSocket.close();

        }catch(IOException e)
        {
            //do nothing for a clean code
        }
    }
	

    /**
     * Signals the web server to shutdown.
	 *
     */
	public void shutdown()
    {
        this.shutdown = true;
    }
	
}