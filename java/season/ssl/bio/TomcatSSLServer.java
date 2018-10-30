package season.ssl.bio;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

/**
 * Created by Administrator on 2018/10/30.
 */
public class TomcatSSLServer {


    private static final String SSL_TYPE = "SSL";
    private static final String KS_TYPE = "JKS";
    private static final String X509 = "SunX509";
    private static final int port = 443;
    private static TomcatSSLServer sslServer;
    private SSLServerSocket svrSocket;

    public static TomcatSSLServer newInstance() throws Exception {
        if (sslServer == null) {
            sslServer = new TomcatSSLServer();
        }
        return sslServer;
    }

    public void startService() {
        SSLSocket cntSocket = null;
        BufferedReader ioRead = null;
        PrintWriter ioWriter = null;
        String tmpMsg = null;
        while (true) {
            try {
                cntSocket = (SSLSocket) svrSocket.accept();
                ioRead = new BufferedReader(new InputStreamReader(cntSocket.getInputStream()));
                ioWriter = new PrintWriter(cntSocket.getOutputStream());
                while ((tmpMsg = ioRead.readLine()) != null) {
                    System.out.println("客户端通过SSL协议发送信息：" + tmpMsg);
                    tmpMsg = "欢迎通过SSL协议连接";
                    ioWriter.println(tmpMsg);
                    ioWriter.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cntSocket != null)
                        cntSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private TomcatSSLServer() throws Exception {
        SSLContext sslContext = createSSLContext();
        SSLServerSocketFactory serverFactory = sslContext.getServerSocketFactory();
        svrSocket = (SSLServerSocket) serverFactory.createServerSocket(port);
        //设置需要验证客户端
        svrSocket.setNeedClientAuth(true);
        //下面在干嘛？？
        String[] supported = svrSocket.getEnabledCipherSuites();
        svrSocket.setEnabledCipherSuites(supported);
    }

    private SSLContext createSSLContext() throws Exception {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(X509);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(X509);
        String serverKeyStoreFile = "tomcat.jks";
        String svrPassphrase = "tomcat";
        char[] svrPassword = svrPassphrase.toCharArray();
        KeyStore serverKeyStore = KeyStore.getInstance(KS_TYPE);
        serverKeyStore.load(new FileInputStream(serverKeyStoreFile), svrPassword);
        kmf.init(serverKeyStore, svrPassword);
        //-------//
        String clientKeyStoreFile = "client.jks";
        String cntPassphrase = "client";
        char[] cntPassword = cntPassphrase.toCharArray();
        KeyStore clientKeyStore = KeyStore.getInstance(KS_TYPE);
        clientKeyStore.load(new FileInputStream(clientKeyStoreFile), cntPassword);
        tmf.init(clientKeyStore);
        //------//
        SSLContext sslContext = SSLContext.getInstance(SSL_TYPE);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }

    public static void main(String... args) throws Exception {
        TomcatSSLServer.newInstance().startService();
    }
}
