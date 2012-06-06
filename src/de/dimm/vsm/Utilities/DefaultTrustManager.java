
package de.dimm.vsm.Utilities;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;


public class DefaultTrustManager implements TrustManager,X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] cert, String authType) 
    {
        System.out.println("C: trusting " + authType);
	// everything is trusted
    }

    @Override
    public void checkServerTrusted(X509Certificate[] cert, String authType)
    {
        System.out.println("S: trusting " + authType);
	// everything is trusted
    }

    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
	return new X509Certificate[0];
    }

        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs)
        {
            return true;
        }

        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs)
        {
            return true;
        }


}