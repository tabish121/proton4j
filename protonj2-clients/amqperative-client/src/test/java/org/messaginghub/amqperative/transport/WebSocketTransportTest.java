/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.messaginghub.amqperative.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.buffer.ProtonNettyByteBuffer;
import org.junit.Test;
import org.messaginghub.amqperative.SslOptions;
import org.messaginghub.amqperative.TransportOptions;
import org.messaginghub.amqperative.test.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete;

/**
 * Test the Netty based WebSocket Transport
 */
public class WebSocketTransportTest extends TcpTransportTest {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketTransportTest.class);

    @Override
    protected WebSocketTransport createTransport(String host, int port, TransportListener listener, TransportOptions options, SslOptions sslOptions) {
        WebSocketTransport transport = new WebSocketTransport(host, port, options, sslOptions);
        transport.setTransportListener(listener);
        return transport;
    }

    @Override
    protected TransportOptions createTransportOptions() {
        return new TransportOptions().useWebSockets(true);
    }

    @Override
    protected TransportOptions createServerTransportOptions() {
        return new TransportOptions().useWebSockets(true);
    }

    @Test(timeout = 60000)
    public void testConnectToServerUsingCorrectPath() throws Exception {
        final String WEBSOCKET_PATH = "/testpath";

        try (NettyEchoServer server = createEchoServer()) {
            server.setWebSocketPath(WEBSOCKET_PATH);
            server.start();

            final int port = server.getServerPort();

            Transport transport = createTransport(
                HOSTNAME, port, testListener, createTransportOptions().webSocketPath(WEBSOCKET_PATH), createSSLOptions());

            try {
                transport.connect(null);
                LOG.info("Connected to server:{}:{} as expected.", HOSTNAME, port);
            } catch (Exception e) {
                fail("Should have connected to the server at " + HOSTNAME + ":" + port + " but got exception: " + e);
            }

            assertTrue(transport.isConnected());
            assertEquals("Server host is incorrect", HOSTNAME, transport.getHost());
            assertEquals("Server port is incorrect", port, transport.getPort());

            transport.close();

            // Additional close should not fail or cause other problems.
            transport.close();
        }

        assertTrue(!transportClosed);  // Normal shutdown does not trigger the event.
        assertTrue(exceptions.isEmpty());
        assertTrue(data.isEmpty());
    }

    @Test(timeout = 60000)
    public void testConnectToServerUsingIncorrectPath() throws Exception {
        final String WEBSOCKET_PATH = "/testpath";

        try (NettyEchoServer server = createEchoServer()) {
            // No configured path means it won't match the requested one.
            server.start();

            final int port = server.getServerPort();

            server.close();

            Transport transport = createTransport(
                HOSTNAME, port, testListener, createTransportOptions().webSocketPath(WEBSOCKET_PATH), createSSLOptions());

            try {
                transport.connect(null);
                fail("Should have failed to connect to the server: " + HOSTNAME + ":" + port);
            } catch (Exception e) {
                LOG.info("Failed to connect to: {}:{} as expected.", HOSTNAME, port);
            }

            assertFalse(transport.isConnected());

            transport.close();
        }

        assertTrue(!transportClosed);  // Normal shutdown does not trigger the event.
        assertTrue(exceptions.isEmpty());
        assertTrue(data.isEmpty());
    }

    @Test(timeout = 60000)
    public void testConnectionsSendReceiveLargeDataWhenFrameSizeAllowsIt() throws Exception {
        final int FRAME_SIZE = 8192;

        ProtonBuffer sendBuffer = new ProtonNettyByteBuffer(Unpooled.buffer(FRAME_SIZE));
        for (int i = 0; i < FRAME_SIZE; ++i) {
            sendBuffer.writeByte('A');
        }

        try (NettyEchoServer server = createEchoServer()) {
            // Server should pass the data through without issue with this size
            server.setMaxFrameSize(FRAME_SIZE);
            server.start();

            final int port = server.getServerPort();

            List<Transport> transports = new ArrayList<Transport>();

            Transport transport = createTransport(
                HOSTNAME, port, testListener, createTransportOptions().webSocketMaxFrameSize(FRAME_SIZE), createSSLOptions());

            try {
                // The transport should allow for the size of data we sent.
                transport.connect(null);
                transports.add(transport);
                transport.writeAndFlush(sendBuffer.copy());
            } catch (Exception e) {
                fail("Should have connected to the server at " + HOSTNAME + ":" + port + " but got exception: " + e);
            }

            assertTrue(Wait.waitFor(new Wait.Condition() {
                @Override
                public boolean isSatisfied() throws Exception {
                    LOG.debug("Checking completion: read {} expecting {}", bytesRead.get(), FRAME_SIZE);
                    return bytesRead.get() == FRAME_SIZE || !transport.isConnected();
                }
            }, 10000, 50));

            assertTrue("Connection failed while receiving.", transport.isConnected());

            transport.close();
        }

        assertTrue(exceptions.isEmpty());
    }

    @Test(timeout = 60000)
    public void testConnectionReceivesFragmentedData() throws Exception {
        final int FRAME_SIZE = 5317;

        ProtonBuffer sendBuffer = new ProtonNettyByteBuffer(Unpooled.buffer(FRAME_SIZE));
        for (int i = 0; i < FRAME_SIZE; ++i) {
            sendBuffer.writeByte('A' + (i % 10));
        }

        try (NettyEchoServer server = createEchoServer()) {
            server.setMaxFrameSize(FRAME_SIZE);
            // Server should fragment the data as it goes through
            server.setFragmentWrites(true);
            server.start();

            final int port = server.getServerPort();

            List<Transport> transports = new ArrayList<Transport>();

            TransportOptions clientOptions = createTransportOptions();
            clientOptions.traceBytes(true);
            clientOptions.webSocketMaxFrameSize(FRAME_SIZE);

            NettyTransportListener wsListener = new NettyTransportListener(true);

            Transport transport = createTransport(HOSTNAME, port, wsListener, clientOptions, createSSLOptions());
            try {
                transport.connect(null);
                transports.add(transport);
                transport.writeAndFlush(sendBuffer.copy());
            } catch (Exception e) {
                fail("Should have connected to the server at " + HOSTNAME + ":" + port + " but got exception: " + e);
            }

            assertTrue(Wait.waitFor(new Wait.Condition() {
                @Override
                public boolean isSatisfied() throws Exception {
                    LOG.debug("Checking completion: read {} expecting {}", bytesRead.get(), FRAME_SIZE);
                    return bytesRead.get() == FRAME_SIZE || !transport.isConnected();
                }
            }, 10000, 50));

            assertTrue("Connection failed while receiving.", transport.isConnected());

            transport.close();

            assertEquals("Expected 2 data packets due to seperate websocket frames", 2, data.size());

            ByteBuf receivedBuffer = Unpooled.buffer(FRAME_SIZE);
            for(ByteBuf buf : data) {
               buf.readBytes(receivedBuffer, buf.readableBytes());
            }

            assertEquals("Unexpected data length", FRAME_SIZE, receivedBuffer.readableBytes());
            assertTrue("Unexpected data", ByteBufUtil.equals((ByteBuf) sendBuffer.unwrap(), 0, receivedBuffer, 0, FRAME_SIZE));
        } finally {
            for (ByteBuf buf : data) {
                buf.release();
            }
        }

        assertTrue(exceptions.isEmpty());
    }

    @Test(timeout = 60000)
    public void testConnectionsSendReceiveLargeDataFailsDueToMaxFrameSize() throws Exception {
        final int FRAME_SIZE = 1024;

        ProtonBuffer sendBuffer = new ProtonNettyByteBuffer(Unpooled.buffer(FRAME_SIZE));
        for (int i = 0; i < FRAME_SIZE; ++i) {
            sendBuffer.writeByte('A');
        }

        try (NettyEchoServer server = createEchoServer()) {
            // Server should pass the data through, client should choke on the incoming size.
            server.setMaxFrameSize(FRAME_SIZE);
            server.start();

            final int port = server.getServerPort();

            List<Transport> transports = new ArrayList<Transport>();

            Transport transport = createTransport(
                HOSTNAME, port, testListener, createTransportOptions().webSocketMaxFrameSize(FRAME_SIZE / 2), createSSLOptions());
            try {
                // Transport can't receive anything bigger so it should fail the connection
                // when data arrives that is larger than this value.
                transport.connect(null);
                transports.add(transport);
                transport.writeAndFlush(sendBuffer.copy());
            } catch (Exception e) {
                fail("Should have connected to the server at " + HOSTNAME + ":" + port + " but got exception: " + e);
            }

            assertTrue("Transport should have lost connection", Wait.waitFor(() -> !transport.isConnected()));
        }

        assertFalse(exceptions.isEmpty());
    }

    @Test(timeout = 60000)
    public void testTransportDetectsConnectionDropWhenServerEnforcesMaxFrameSize() throws Exception {
        final int FRAME_SIZE = 1024;

        ProtonBuffer sendBuffer = new ProtonNettyByteBuffer(Unpooled.buffer(FRAME_SIZE));
        for (int i = 0; i < FRAME_SIZE; ++i) {
            sendBuffer.writeByte('A');
        }

        try (NettyEchoServer server = createEchoServer()) {
            // Server won't accept the data as it's to large and will close the connection.
            server.setMaxFrameSize(FRAME_SIZE / 2);
            server.start();

            final int port = server.getServerPort();

            List<Transport> transports = new ArrayList<Transport>();

            final Transport transport = createTransport(
                HOSTNAME, port, testListener, createTransportOptions().webSocketMaxFrameSize(FRAME_SIZE), createSSLOptions());

            try {
                // Transport allows bigger frames in so that server is the one causing the failure.
                transport.connect(null);
                transports.add(transport);
                transport.writeAndFlush(sendBuffer.copy());
            } catch (Exception e) {
                fail("Should have connected to the server at " + HOSTNAME + ":" + port + " but got exception: " + e);
            }

            assertTrue("Transport should have lost connection", Wait.waitFor(new Wait.Condition() {
                @Override
                public boolean isSatisfied() throws Exception {
                    try {
                        transport.writeAndFlush(sendBuffer.copy());
                    } catch (IOException e) {
                        LOG.info("Transport send caught error:", e);
                        return true;
                    }

                    return false;
                }
            }, 10000, 10));

            transport.close();
        }
    }

    @Test(timeout = 60000)
    public void testConfiguredHttpHeadersArriveAtServer() throws Exception {
        try (NettyEchoServer server = createEchoServer()) {
            server.start();

            final int port = server.getServerPort();

            TransportOptions clientOptions = createTransportOptions();
            clientOptions.addWebSocketHeader("test-header1", "FOO");
            clientOptions.webSocketHeaders().put("test-header2", "BAR");

            Transport transport = createTransport(HOSTNAME, port, testListener, clientOptions, createSSLOptions());
            try {
                transport.connect(null);
                LOG.info("Connected to server:{}:{} as expected.", HOSTNAME, port);
            } catch (Exception e) {
                fail("Should have connected to the server at " + HOSTNAME + ":" + port + " but got exception: " + e);
            }

            assertTrue(transport.isConnected());
            assertEquals("Server host is incorrect", HOSTNAME, transport.getHost());
            assertEquals("Server port is incorrect", port, transport.getPort());

            assertTrue("HandshakeCompletion not set within given time", server.awaitHandshakeCompletion(2000));
            HandshakeComplete handshake = server.getHandshakeComplete();
            assertNotNull("completion should not be null", handshake);
            HttpHeaders requestHeaders = handshake.requestHeaders();

            assertTrue(requestHeaders.contains("test-header1"));
            assertTrue(requestHeaders.contains("test-header2"));

            assertEquals("FOO", requestHeaders.get("test-header1"));
            assertEquals("BAR", requestHeaders.get("test-header2"));

            transport.close();
        }

        assertTrue(!transportClosed);  // Normal shutdown does not trigger the event.
        assertTrue(exceptions.isEmpty());
        assertTrue(data.isEmpty());
    }

    private static final String BROKER_JKS_KEYSTORE = "src/test/resources/broker-jks.keystore";
    private static final String PASSWORD = "password";

    @Test(timeout = 30000)
    public void testNonSslWebSocketConnectionFailsToSslServer() throws Exception {
        SslOptions serverSslOptions = new SslOptions();
        serverSslOptions.keyStoreLocation(BROKER_JKS_KEYSTORE);
        serverSslOptions.keyStorePassword(PASSWORD);
        serverSslOptions.verifyHost(false);
        serverSslOptions.sslEnabled(true);

        try (NettyBlackHoleServer server = new NettyBlackHoleServer(createServerTransportOptions(), serverSslOptions)) {
            server.start();

            final int port = server.getServerPort();

            TransportOptions clientOptions = createTransportOptions();

            Transport transport = createTransport(HOSTNAME, port, testListener, clientOptions, createSSLOptions());
            try {
                transport.connect(null);
                fail("should not have connected");
            } catch (Exception e) {
                LOG.trace("Failed to connect with message: {}", e.getMessage());
            }
        }
    }

    @Test(timeout = 30000)
    public void testWebsocketConnectionToBlackHoleServerTimesOut() throws Exception {
        try (NettyBlackHoleServer server = new NettyBlackHoleServer(new TransportOptions(), new SslOptions().sslEnabled(false))) {
            server.start();

            final int port = server.getServerPort();

            TransportOptions clientOptions = createTransportOptions();
            clientOptions.connectTimeout(25);

            Transport transport = createTransport(HOSTNAME, port, testListener, clientOptions, createSSLOptions());
            try {
                transport.connect(null);
                fail("should not have connected");
            } catch (Exception e) {
                String message = e.getMessage();
                assertNotNull(message);
                assertTrue("Unexpected message: " + message, message.contains("WebSocket handshake timed out"));
            }
        }
    }
}
