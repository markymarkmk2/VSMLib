package de.dimm.vsm.Utilities;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class DefaultSSLSocketFactory extends SSLSocketFactory
{

    private SSLSocketFactory factory;

    public DefaultSSLSocketFactory()
    {
        try
        {

//            System.setProperty("javax.net.debug","ssl");

            SSLContext sslcontext = SSLContext.getInstance("TLS", "SunJSSE");


            sslcontext.init(null, new TrustManager[]
                    {
                        new DefaultTrustManager()
                    }, null);

            factory = sslcontext.getSocketFactory();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public Socket createSocket() throws IOException
    {
        return factory.createSocket();
    }

    public static SocketFactory getDefault()
    {
        return new DefaultSSLSocketFactory();
    }

    @Override
    public Socket createSocket( Socket socket, String s, int i, boolean flag )
            throws IOException
    {
        return factory.createSocket(socket, s, i, flag);
    }

    @Override
    public Socket createSocket( InetAddress inaddr, int i,
            InetAddress inaddr1, int j ) throws IOException
    {
        return factory.createSocket(inaddr, i, inaddr1, j);
    }

    @Override
    public Socket createSocket( InetAddress inaddr, int i )
            throws IOException
    {
        return factory.createSocket(inaddr, i);
    }

    @Override
    public Socket createSocket( String s, int i, InetAddress inaddr, int j )
            throws IOException
    {
        return factory.createSocket(s, i, inaddr, j);
    }

    @Override
    public Socket createSocket( String s, int i ) throws IOException
    {
        return factory.createSocket(s, i);
    }

    @Override
    public String[] getDefaultCipherSuites()
    {
        return factory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites()
    {
        return factory.getSupportedCipherSuites();
    }
}

