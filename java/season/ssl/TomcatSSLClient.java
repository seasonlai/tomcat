package season.ssl;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

/**
 * Created by Administrator on 2018/10/30.
 */
public class TomcatSSLClient {

    private static final String SSL_TYPE = "SSL";
    private static final String KS_TYPE = "JKS";
    private static final String X509 = "SunX509";
    private SSLSocket sslSocket;

    public TomcatSSLClient(String targetHost, int port) throws Exception {
        SSLContext sslContext = createSSLContext();
        SSLSocketFactory sslcntFactory = sslContext.getSocketFactory();
        sslSocket = (SSLSocket) sslcntFactory.createSocket(targetHost, port);
        //下面又干嘛？
        String[] supported = sslSocket.getEnabledCipherSuites();
        sslSocket.setEnabledCipherSuites(supported);
    }

    private SSLContext createSSLContext() throws Exception {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(X509);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(X509);
        //------//
        String clientKeyStoreFile = "client.jks";
        String cntPassphrase = "client";
        char[] cntPassword = cntPassphrase.toCharArray();
        KeyStore clientKeyStore = KeyStore.getInstance(KS_TYPE);
        clientKeyStore.load(new FileInputStream(clientKeyStoreFile), cntPassword);
        //------//
        String serverKeyStoreFile = "tomcat.jks";
        String svrPassphrase = "tomcat";
        char[] svrPassword = svrPassphrase.toCharArray();
        KeyStore serverKeyStore = KeyStore.getInstance(KS_TYPE);
        serverKeyStore.load(new FileInputStream(serverKeyStoreFile), svrPassword);
        //-------//
        kmf.init(clientKeyStore, cntPassword);
        tmf.init(serverKeyStore);
        SSLContext sslContext = SSLContext.getInstance(SSL_TYPE);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }

    public String sayToSvr(String sayMsg) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(sslSocket.getOutputStream());
        writer.println(sayMsg);
        writer.flush();
        return reader.readLine();
    }

    public static void main(String... args) throws Exception {
        TomcatSSLClient client = new TomcatSSLClient("127.0.0.1", 443);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String sayMsg, svrRespMsg;
        while ((sayMsg = reader.readLine()) != null) {
            svrRespMsg = client.sayToSvr(sayMsg);
            if (svrRespMsg != null && !svrRespMsg.trim().equals("")) {
                System.out.println("服务器通过SSL协议响应：" + svrRespMsg);
            }
        }
    }
}
