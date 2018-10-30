package season.ssl.nio;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.util.Iterator;

/**
 * Created by Administrator on 2018/10/30.
 */
public class NioSSLServer {


    private static final String SSL_TYPE = "SSL";
    private static final String KS_TYPE = "JKS";
    private static final String X509 = "SunX509";
    private static final int port = 443;
    //------//
    private SSLEngine sslEngine;
    private Selector selector;
    private SSLContext sslContext;
    //网络传输的内容
    private ByteBuffer netInData;
    //解密后的内容
    private ByteBuffer appInData;
    //加密后输出的内容
    private ByteBuffer netOutData;
    //
    private ByteBuffer appOutData;

    public void run() throws Exception {
        createSSLContext();
        createSSLEngine();
        createBuff();
        createServerSocket();

        while (true) {
            selector.select(1000);
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey k = iterator.next();
                iterator.remove();
                handleRequest(k);
            }
        }
    }

    private void handleRequest(SelectionKey key) throws IOException {
            if (key.isAcceptable()) {
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel channel = ssc.accept();
                channel.configureBlocking(false);
                doHandShake(channel);
            } else if (key.isReadable()) {
                if (sslEngine.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    netInData.clear();
                    appInData.clear();
                    sc.read(netInData);
                    netInData.flip();
                    SSLEngineResult sslEngineResult = sslEngine.unwrap(netInData, appInData);
                    doTask();
                    if (sslEngineResult.getStatus() == SSLEngineResult.Status.OK) {
                        appInData.flip();
                        System.out.println(new String(appInData.array()));
                    }
                    sc.register(selector, SelectionKey.OP_WRITE);
                }
            } else if (key.isWritable()) {
                SocketChannel sc = (SocketChannel) key.channel();
                netOutData.clear();
                SSLEngineResult engineResult = sslEngine.wrap(appOutData, netOutData);
                doTask();
                netOutData.flip();
                while (netOutData.hasRemaining())
                    sc.write(netOutData);
                sc.register(selector, SelectionKey.OP_READ);
        }
    }

    private void doHandShake(SocketChannel sc) throws IOException {
        boolean handShakeDone = false;
        sslEngine.beginHandshake();
        SSLEngineResult.HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
        while (!handShakeDone) {
            switch (hsStatus) {
                case FINISHED:
                    System.out.println("FINISHED---");
                    break;
                case NEED_TASK:
                    System.out.println("NEED_TASK---");
                    hsStatus = doTask();
                    break;
                case NEED_UNWRAP:
                    System.out.println("NEED_UNWRAP---");
                    netInData.clear();
                    sc.read(netInData);
                    netInData.flip();
                    do {
                        sslEngine.unwrap(netInData, appInData);
                        hsStatus = doTask();
                    } while (hsStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP && netInData.remaining() > 0);
                    netInData.clear();
                    break;
                case NEED_WRAP:
                    System.out.println("NEED_WRAP---");
                    sslEngine.wrap(appOutData, netOutData);
                    hsStatus = doTask();
                    netOutData.flip();
                    sc.write(netOutData);
                    netOutData.clear();
                    break;
                case NOT_HANDSHAKING:
                    System.out.println("NOT_HANDSHAKING---");
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);
                    handShakeDone = true;
                    break;
            }
        }
    }

    private SSLEngineResult.HandshakeStatus doTask() {
        Runnable task;
        while ((task = sslEngine.getDelegatedTask()) != null) {
            new Thread(task).start();
        }
        return sslEngine.getHandshakeStatus();
    }

    private void createSSLContext() throws Exception {
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
        sslContext = SSLContext.getInstance(SSL_TYPE);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    }

    private void createSSLEngine() {
        sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false);
    }

    private void createBuff() {
        SSLSession session = sslEngine.getSession();
        appInData = ByteBuffer.allocate(session.getApplicationBufferSize());
        appOutData = ByteBuffer.wrap("hello\n".getBytes());
        netInData = ByteBuffer.allocate(session.getPacketBufferSize());
        netOutData = ByteBuffer.allocate(session.getPacketBufferSize());
    }

    private void createServerSocket() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        selector = Selector.open();
        ServerSocket serverSocket = serverChannel.socket();
        serverSocket.bind(new InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public static void main(String... args) throws Exception {
        new NioSSLServer().run();
    }
}
