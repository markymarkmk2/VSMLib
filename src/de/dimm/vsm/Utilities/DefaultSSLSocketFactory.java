package de.dimm.vsm.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class DefaultSSLSocketFactory extends SSLSocketFactory
{
    private static char[] password;
    private static String keyStore;
    private SSLSocketFactory factory;
       
    public DefaultSSLSocketFactory()
    {
        SSLContext sslContext;
        try
        {
//            System.setProperty("javax.net.debug","ssl");
            sslContext = SSLContext.getInstance("TLS", "SunJSSE");

            /*
             * Allocate and initialize a KeyStore object.
             */
            //KeyStore ks = KeyToolHelper.load_keystore(/*syskeystore*/false);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());  
            InputStream keystoreStream;
            if (new File(keyStore).exists())
            {
                keystoreStream = new FileInputStream(new File(keyStore));
            }
            else
            {
                keystoreStream = DefaultSSLSocketFactory.class.getClassLoader().getResourceAsStream(keyStore); // note, not getSYSTEMResourceAsStream                  
            }
            ks.load(keystoreStream, password);              
            keystoreStream.close();

            /*
             * Allocate and initialize a KeyManagerFactory.
             */
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);
            /*
             * Allocate and initialize a TrustManagerFactory.
             */
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);


            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            
            factory = sslContext.getSocketFactory();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static void setKeyStore( String keyStore )
    {
        DefaultSSLSocketFactory.keyStore = keyStore;
    }

    public static void setPassword( char[] password )
    {
        DefaultSSLSocketFactory.password = password;
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

