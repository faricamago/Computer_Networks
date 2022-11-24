
/**
 * HttpClient Class
 * 
 * CPSC 441
 * Assignment 2
 * 
 */


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.*;

public class HttpClient {

	private static final Logger logger = Logger.getLogger("HttpClient"); // global logger

    /**
     * Default no-arg constructor
     */
	public HttpClient() {
		// nothing to do!
	}
	
    /**
     * Downloads the object specified by the parameter url.
	 *
     * @param url	URL of the object to be downloaded. It is a fully qualified URL.
     */
	public void get(String url)
    { 
        // Splitting the url to remove the part until //
        String[] removed_till_two_backslashes = url.split("//",2);
        String[] parts = removed_till_two_backslashes[1].split("/",2);
        String host,filepath,filename,port;
        int index_of_last_slash = parts[1].lastIndexOf("/");
        if(parts[0].contains(":"))
        {
            String [] hostport = parts[0].split(":",2);
            host = hostport[0];
            port = hostport[1];
            if(parts[1].isEmpty())
            {
                filepath = "index.html";
                filename = "index.html";
            }
            else
            {
                filepath = parts[1];
                filename = parts[1].substring(index_of_last_slash+1);
            }
        }
        else
        {
            host = parts[0];
            port = "80";
            if(parts[1].isEmpty())
            {
                filepath = "index.html";
                filename = "index.html";
            }
            else
            {
                filepath = parts[1];
                filename = parts[1].substring(index_of_last_slash+1);
            }
        }

        int port_number = Integer.parseInt(port);

        try{
        // making a TCP connection
        InetAddress iAddress = InetAddress.getByName(host);
        InetSocketAddress sAddress = new InetSocketAddress(iAddress,port_number);

        // creaking a TCP socket object 
        Socket sock = new Socket();

        // Connecting the socket to the server
        sock.connect(sAddress);
       
        // Formating HTTP GET Request to send it to the socket
        String req1 = "GET /";
        String req2 = " HTTP/1.1\r\nHost: ";
        String req3 = "\r\nConnection: close\r\n\r\n";        
        String final_request = req1 + filepath + req2 + host + req3;

        // creating and opening output stream object to write the HTTP GET request into the socket
        OutputStream s_out = new BufferedOutputStream(sock.getOutputStream());       
        s_out.write(final_request.getBytes("US-ASCII"));
        System.out.println(final_request);
        s_out.flush();

        // creating and opening input stream object to read the contents from the socket
        InputStream in = new BufferedInputStream(sock.getInputStream());

        // reading from the input stream socket byte by byte until the end of response header.
        // the response header is (\r\n\r\n) which is being checked by nested if else stements        

        int byte_read = 0;
        while((byte_read = in.read())!=-1){
            System.out.print((char)byte_read);
            if(((char)byte_read)  == '\r'){
                byte_read = in.read();
                System.out.print((char)byte_read);
                if(((char)byte_read) == '\n'){
                    byte_read = in.read();
                    System.out.print((char)byte_read);
                    if(((char)byte_read) == '\r'){
                        byte_read = in.read();
                        System.out.print((char)byte_read);
                        if(((char)byte_read) == '\n')
                        {
                            break;
                        }}}}}

        // writing to the output stream
        // creating and opening buffered output stream to write contents from the input stream to a new file on our local system
        OutputStream out = new BufferedOutputStream(new FileOutputStream(filename),4096);

        // reading from the input stream socket and writing the chunks to the output stream
        int byte_read_out = 0;
        byte[] buffer = new byte[4096];
        while((byte_read_out = in.read(buffer))!=-1)
        {
           out.write(buffer,0,byte_read_out);
           out.flush();
        }

        // closing the socket and the streams
        s_out.close();
        in.close();
        out.close();
        sock.close();

        // exception handling
    }catch(UnknownHostException e){
        e.printStackTrace();
    }catch(IOException e){
        e.printStackTrace();
    }
    }
}
