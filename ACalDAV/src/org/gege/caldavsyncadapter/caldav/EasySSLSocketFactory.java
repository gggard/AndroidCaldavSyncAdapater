/**
 * Copyright (c) 2012-2013, Gerald Garcia
 *
 * This file is part of Andoid Caldav Sync Adapter Free.
 *
 * Andoid Caldav Sync Adapter Free is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or at your option any later version.
 *
 * Andoid Caldav Sync Adapter Free is distributed in the hope that 
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Andoid Caldav Sync Adapter Free.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.gege.caldavsyncadapter.caldav;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public final class EasySSLSocketFactory implements
        LayeredSocketFactory {

    private static final String TAG = "TrustAllSSLSocketFactory";

    private static final EasySSLSocketFactory DEFAULT_FACTORY = new EasySSLSocketFactory();

    private SSLContext sslcontext;

    private javax.net.ssl.SSLSocketFactory socketfactory;

    private EasySSLSocketFactory() {
        super();
        TrustManager[] tm = new TrustManager[]{new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
                // do nothing
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                    String authType) throws CertificateException {
                // do nothing
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

        }};
        try {
            this.sslcontext = SSLContext.getInstance(SSLSocketFactory.TLS);
            this.sslcontext.init(null, tm, new SecureRandom());
            this.socketfactory = this.sslcontext.getSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG,
                    "Faild to instantiate TrustAllSSLSocketFactory!", e);
        } catch (KeyManagementException e) {
            Log.e(TAG,
                    "Failed to instantiate TrustAllSSLSocketFactory!", e);
        }
    }

    public static EasySSLSocketFactory getSocketFactory
            () {
        return DEFAULT_FACTORY;
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port,
            boolean autoClose) throws IOException, UnknownHostException {
        SSLSocket sslSocket = (SSLSocket) this.socketfactory.createSocket(
                socket, host, port, autoClose);
        return sslSocket;
    }

    @Override
    public Socket connectSocket(Socket sock, String host, int port,
            InetAddress localAddress, int localPort, HttpParams params)
            throws IOException, UnknownHostException, ConnectTimeoutException {
        if (host == null) {
            throw new IllegalArgumentException(
                    "Target host may not be null.");
        }
        if (params == null) {
            throw new IllegalArgumentException(
                    "Parameters may not be null.");
        }

        SSLSocket sslsock = (SSLSocket) ((sock != null) ? sock
                : createSocket());

        if ((localAddress != null) || (localPort > 0)) {

            // we need to bind explicitly
            if (localPort < 0) {
                localPort = 0; // indicates "any"
            }

            InetSocketAddress isa = new InetSocketAddress(localAddress,
                    localPort);
            sslsock.bind(isa);
        }

        int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
        int soTimeout = HttpConnectionParams.getSoTimeout(params);

        InetSocketAddress remoteAddress;
        remoteAddress = new InetSocketAddress(host, port);

        sslsock.connect(remoteAddress, connTimeout);

        sslsock.setSoTimeout(soTimeout);

        return sslsock;
    }

    @Override
    public Socket createSocket() throws IOException {
        // the cast makes sure that the factory is working as expected
        return (SSLSocket) this.socketfactory.createSocket();
    }

    @Override
    public boolean isSecure(Socket sock) throws IllegalArgumentException {
        return true;
    }

}