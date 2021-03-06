/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import com.caucho.hessian.client.AbstractHessianConnection;
import com.caucho.hessian.client.AbstractHessianConnectionFactory;
import com.caucho.hessian.client.HessianConnection;
import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.io.Deserializer;
import com.caucho.hessian.io.HessianProtocolException;
import com.caucho.hessian.io.InputStreamDeserializer;
import com.caucho.hessian.io.Serializer;
import com.caucho.hessian.io.SerializerFactory;
import de.dimm.vsm.hessian.InetAddressDeserializer;
import de.dimm.vsm.hessian.InetAddressSerializer;
import de.dimm.vsm.net.interfaces.SocketOwner;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.util.HashMap;



class HessianSocketConnection extends AbstractHessianConnection
{
    private URL _url;
    private Socket _conn;


    private int _statusCode;
    private String _statusMessage;
    int bsize = 32*1024;



    HashMap<String,String> headerMap;
    HessianSocketConnection(URL url, Socket conn)
    {
        _url = url;
        _conn = conn;
        headerMap = new HashMap<String, String>();
    }

    /**
    * Adds a HTTP header.
    */
    @Override
    public void addHeader(String key, String value)
    {
        headerMap.put(key, value);
    }

    /**
    * Returns the output stream for the request.
    */
    @Override
    public OutputStream getOutputStream()
    throws IOException
    {
        return new BufferedOutputStream( _conn.getOutputStream(), bsize);
    }

    /**
    * Sends the request
    */

    @Override
    public void sendRequest() throws IOException
    {

    }




    /**
    * Returns the status code.
    */
    @Override
    public int getStatusCode()
    {
        return _statusCode;
    }

    /**
    * Returns the status string.
    */
    @Override
    public String getStatusMessage()
    {
        return _statusMessage;
    }

    /**
    * Returns the InputStream to the result
    */
    @Override
    public InputStream getInputStream()
    throws IOException
    {
        return new BufferedInputStream(_conn.getInputStream(), bsize);

    //    return _conn.getInputStream();
    }

    /**
    * Close/free the connection
    */
    @Override
    public void close()
    {

    }

    /**
    * Disconnect the connection
    */
    @Override
    public void destroy()
    {
      try
      {

          _conn.close();

          if (_conn instanceof HessianNoCloseSocket)
          {
              HessianNoCloseSocket hncs = (HessianNoCloseSocket)_conn;
              hncs.realClose();
          }
      }
      catch (IOException iOException)
      {
      }
    //      _conn.getHttpConnectionManager().releaseConnection(_conn.);

    }
}







class HessianSocketConnectionFactory extends AbstractHessianConnectionFactory
{
      int maxConnections = 10;
      int maxConnectionsPerHost = 10;
      SocketOwner socketOwner;
      int connTimeout;
      int txTimeout;

    public HessianSocketConnectionFactory(SocketOwner owner, int timeout, int txTimeout)
    {
        this.socketOwner = owner;
        this.connTimeout = timeout;
        this.txTimeout = txTimeout;
    }

    public void setConnTimeout( int connTimeout ) {
        this.connTimeout = connTimeout;
    }

    public void setTxTimeout( int txTimeout ) throws SocketException {
        this.txTimeout = txTimeout;
        HessianNoCloseSocket sock = socketOwner.getSocket();
        if (sock.isConnected() && !sock.isClosed())
            sock.setSoTimeout(txTimeout);        
    }
    

    


    @Override
    public HessianConnection open( URL url ) throws IOException
    {
        SocketAddress addr = new InetSocketAddress(url.getHost(), url.getPort());
        HessianNoCloseSocket sock = socketOwner.getSocket();
        
        if (!sock.isConnected() || sock.isClosed())
        {
            sock.close();
            sock = new HessianNoCloseSocket(connTimeout, txTimeout);
            sock.setTcpNoDelay(true);
            sock.setSendBufferSize(512*1024);
            sock.setReceiveBufferSize(512*1024);
            sock.setReuseAddress(true);

            sock.connect(addr);

            socketOwner.setSocket(sock);
        }
        

        HessianSocketConnection conn = new HessianSocketConnection( url, sock );        
        return conn;
    }

}


/**
 *
 * @author Administrator
 */
public class RemoteCallFactory implements SocketOwner
{
     HessianProxyFactory hfactory;
     int port;
     boolean ssl;
     InetAddress adress;
     String context;
     boolean tcp;
     HessianNoCloseSocket sock;
     int connTimeout;
     int txTimeout;

    public RemoteCallFactory( InetAddress adress, int port, String context, boolean ssl, boolean tcp, int connTimeout, int txTimeout  ) throws MalformedURLException
    {
        this.port = port;
        this.ssl = ssl;
        this.adress = adress;
        this.context = context;
        this.tcp = tcp;
        this.connTimeout = connTimeout;
        this.txTimeout = txTimeout;
        hfactory = new HessianProxyFactory();
        
              

        //String urlName = (ssl)? "https://" : "http://" + adress.getHostAddress() + ":" + port + "/" + context;

        
        // Workaround for http://bugs.caucho.com/view.php?id=3634
        hfactory.setSerializerFactory(new SerializerFactory()
        {

            @Override
            protected Deserializer loadDeserializer( Class cl ) throws HessianProtocolException
            {
                if (InputStream.class.isAssignableFrom(cl)) {
                    return new InputStreamDeserializer();
                }
                else if (InetAddress.class.isAssignableFrom(cl)) {
                    return new InetAddressDeserializer();
                }
                else                {
                    return super.loadDeserializer(cl);
                }
            }

            @Override
            protected Serializer loadSerializer( Class cl ) throws HessianProtocolException {
                if (InetAddress.class.isAssignableFrom(cl)) {
                    return new InetAddressSerializer();
                }
                else {
                    return super.loadSerializer(cl);
                }
            }
        });

    }

    public void setConnectTimeout( int timeout )
    {
        this.connTimeout = timeout;
        if (hfactory.getConnectionFactory() instanceof HessianSocketConnectionFactory)
        {
            ((HessianSocketConnectionFactory)hfactory.getConnectionFactory()).setConnTimeout(timeout);
        }
    }
    public void setTxTimeout( int timeout ) throws SocketException
    {
        this.txTimeout = timeout;
        hfactory.setReadTimeout(timeout);
        if (hfactory.getConnectionFactory() instanceof HessianSocketConnectionFactory)
        {
            ((HessianSocketConnectionFactory)hfactory.getConnectionFactory()).setTxTimeout(timeout);
        }
    }

    public int getConnTimeout() {
        return connTimeout;
    }

    public int getTxTimeout() {
        return txTimeout;
    }
    


    public void resetSocket() throws IOException
    {
        if (sock != null)
        {
            sock.realClose();
            sock = new HessianNoCloseSocket(connTimeout, txTimeout);
            hfactory.setConnectionFactory( new HessianSocketConnectionFactory(this, connTimeout, txTimeout));
            sock.connect(new InetSocketAddress(adress, port));
        }
    }
    public void close() throws IOException
    {
        if (sock != null)
        {
            sock.realClose();
        }
    }

    public HessianNoCloseSocket getSock()
    {
        return sock;
    }

    public InetAddress getAdress()
    {
        return adress;
    }

    public int getPort()
    {
        return port;
    }
    



    public Object create(Class api)
    {        
        if (tcp)
        {
            sock = new HessianNoCloseSocket(connTimeout, txTimeout);
            hfactory.setConnectionFactory( new HessianSocketConnectionFactory(this, connTimeout, txTimeout));
        }
        
        try
        {
            Object o = null;
            String urlName = ((ssl)? "https://" : "http://") + adress.getHostAddress() + ":" + port + "/" + context;
            o =  hfactory.create(api, urlName);

            return o;
        }
        catch (Exception malformedURLException)
        {
            malformedURLException.printStackTrace();
        }
        return null; // CANNOT HAPPEN
    }

    @Override
    public HessianNoCloseSocket getSocket()
    {
        return sock;
    }

    @Override
    public void setSocket( HessianNoCloseSocket s )
    {
        sock = s;
    }

}
