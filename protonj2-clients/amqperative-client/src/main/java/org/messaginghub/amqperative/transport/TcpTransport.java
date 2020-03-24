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

import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.buffer.ProtonBufferAllocator;
import org.apache.qpid.proton4j.buffer.ProtonNettyByteBuffer;
import org.apache.qpid.proton4j.buffer.ProtonNettyByteBufferAllocator;
import org.messaginghub.amqperative.SslOptions;
import org.messaginghub.amqperative.TransportOptions;
import org.messaginghub.amqperative.util.IOExceptionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * TCP based transport that uses Netty as the underlying IO layer.
 */
public class TcpTransport implements Transport {

    private static final Logger LOG = LoggerFactory.getLogger(TcpTransport.class);

    public static final int SHUTDOWN_TIMEOUT = 50;
    public static final int DEFAULT_MAX_FRAME_SIZE = 65535;

    protected Bootstrap bootstrap;
    protected EventLoopGroup group;
    protected Channel channel;
    protected TransportListener listener;
    protected ThreadFactory ioThreadfactory;
    protected int maxFrameSize = DEFAULT_MAX_FRAME_SIZE;
    protected final TransportOptions options;
    protected final SslOptions sslOptions;

    private final String host;
    private final int port;
    private final AtomicBoolean connected = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final CountDownLatch connectLatch = new CountDownLatch(1);
    private volatile IOException failureCause;

    /**
     * Create a new transport instance
     *
     * @param host
     *        the host name or IP address that this transport connects to.
     * @param port
     * 		  the port on the given host that this transport connects to.
     * @param options
     *        the transport options used to configure the socket connection.
     * @param sslOptions
     * 		  the SSL options to use if the options indicate SSL is enabled.
     */
    public TcpTransport(String host, int port, TransportOptions options, SslOptions sslOptions) {
        if (options == null) {
            throw new IllegalArgumentException("Transport Options cannot be null");
        }

        if (sslOptions == null) {
            throw new IllegalArgumentException("Transport SSL Options cannot be null");
        }

        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("Transport host value cannot be null");
        }

        if (port < 0) {
            throw new IllegalArgumentException("Transport port value must be a non-negative int value");
        }

        this.sslOptions = sslOptions;
        this.options = options;
        this.host = host;
        this.port = port;
    }

    @Override
    public ScheduledExecutorService connect(final Runnable initRoutine) throws IOException {
        if (closed.get()) {
            throw new IllegalStateException("Transport has already been closed");
        }

        if (listener == null) {
            throw new IllegalStateException("A transport listener must be set before connection attempts.");
        }

        boolean useKQueue = KQueueSupport.isAvailable(options);
        boolean useEpoll = EpollSupport.isAvailable(options);

        if (useKQueue) {
            LOG.trace("Netty Transport using KQueue mode");
            group = KQueueSupport.createGroup(1, ioThreadfactory);
        } else if (useEpoll) {
            LOG.trace("Netty Transport using Epoll mode");
            group = EpollSupport.createGroup(1, ioThreadfactory);
        } else {
            LOG.trace("Netty Transport using NIO mode");
            group = new NioEventLoopGroup(1, ioThreadfactory);
        }

        bootstrap = new Bootstrap();
        bootstrap.group(group);
        if (useKQueue) {
            KQueueSupport.createChannel(bootstrap);
        } else if (useEpoll) {
            EpollSupport.createChannel(bootstrap);
        } else {
            bootstrap.channel(NioSocketChannel.class);
        }
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            public void initChannel(Channel connectedChannel) throws Exception {
                if (initRoutine != null) {
                    try {
                        initRoutine.run();
                    } catch (Throwable initError) {
                        LOG.warn("Error during initialization of channel from provided initialization routine");
                        connectionFailed(connectedChannel, IOExceptionSupport.create(initError));
                        throw initError;
                    }
                }
                configureChannel(connectedChannel);
            }
        });

        configureNetty(bootstrap, options);

        ChannelFuture future = bootstrap.connect(getHost(), getPort());
        future.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    handleException(future.channel(), IOExceptionSupport.create(future.cause()));
                }
            }
        });

        try {
            connectLatch.await();
        } catch (InterruptedException ex) {
            LOG.debug("Transport connection was interrupted.");
            Thread.interrupted();
            failureCause = IOExceptionSupport.create(ex);
        }

        if (failureCause != null) {
            // Close out any Netty resources now as they are no longer needed.
            if (channel != null) {
                channel.close().syncUninterruptibly();
                channel = null;
            }

            throw failureCause;
        } else {
            // Connected, allow any held async error to fire now and close the transport.
            channel.eventLoop().execute(() -> {
                if (failureCause != null) {
                    channel.pipeline().fireExceptionCaught(failureCause);
                }
            });
        }

        return group;
    }

    @Override
    public boolean isConnected() {
        return connected.get();
    }

    @Override
    public boolean isSecure() {
        return sslOptions.sslEnabled();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            connected.set(false);
            try {
                if (channel != null) {
                    channel.close().syncUninterruptibly();
                }
            } finally {
                if (group != null) {
                    Future<?> fut = group.shutdownGracefully(0, SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
                    if (!fut.awaitUninterruptibly(2 * SHUTDOWN_TIMEOUT)) {
                        LOG.trace("Channel group shutdown failed to complete in allotted time");
                    }
                }
            }
        }
    }

    @Override
    public ProtonBufferAllocator getBufferAllocator() {
        return new ProtonNettyByteBufferAllocator() {

            @Override
            public ProtonBuffer outputBuffer(int initialCapacity) {
                return new ProtonNettyByteBuffer(channel.alloc().ioBuffer(initialCapacity));
            }

            @Override
            public ProtonBuffer outputBuffer(int initialCapacity, int maximumCapacity) {
                return new ProtonNettyByteBuffer(channel.alloc().ioBuffer(initialCapacity, initialCapacity));
            }
        };
     }

    @Override
    public TcpTransport write(ProtonBuffer output) throws IOException {
        checkConnected(output);
        LOG.trace("Attempted write of: {} bytes", output);
        channel.write(toOutputBuffer(output), channel.voidPromise());
        return this;
    }

    @Override
    public TcpTransport writeAndFlush(ProtonBuffer output) throws IOException {
        checkConnected(output);
        LOG.trace("Attempted write and flush of: {} bytes", output);
        channel.writeAndFlush(toOutputBuffer(output), channel.voidPromise());
        return this;
    }

    @Override
    public TcpTransport flush() throws IOException {
        checkConnected();
        LOG.trace("Attempted flush of pending writes");
        channel.flush();
        return this;
    }

    @Override
    public TransportListener getTransportListener() {
        return listener;
    }

    TcpTransport setTransportListener(TransportListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public TransportOptions getTransportOptions() {
        return options.clone();
    }

    @Override
    public SslOptions getSslOptions() {
        return sslOptions.clone();
    }

    @Override
    public Principal getLocalPrincipal() {
        Principal result = null;

        if (isSecure()) {
            SslHandler sslHandler = channel.pipeline().get(SslHandler.class);
            result = sslHandler.engine().getSession().getLocalPrincipal();
        }

        return result;
    }

    TcpTransport setMaxFrameSize(int maxFrameSize) {
        if (connected.get()) {
            throw new IllegalStateException("Cannot change Max Frame Size while connected.");
        }

        this.maxFrameSize = maxFrameSize;
        return this;
    }

    @Override
    public ThreadFactory getThreadFactory() {
        return ioThreadfactory;
    }

    TcpTransport setThreadFactory(ThreadFactory factory) {
        if (isConnected() || channel != null) {
            throw new IllegalStateException("Cannot set IO ThreadFactory after Transport connect");
        }

        this.ioThreadfactory = factory;
        return this;
    }

    protected final ByteBuf toOutputBuffer(final ProtonBuffer output) throws IOException {
        final ByteBuf nettyBuf;

        if (output instanceof ProtonNettyByteBuffer) {
            nettyBuf = (ByteBuf) output.unwrap();
        } else {
            ProtonNettyByteBuffer wrapped = new ProtonNettyByteBuffer(channel.alloc().ioBuffer(output.getReadableBytes()));
            wrapped.writeBytes(output);
            nettyBuf = wrapped.unwrap();
        }

        return nettyBuf;
    }

    //----- Internal implementation details, can be overridden as needed -----//

    protected void addAdditionalHandlers(ChannelPipeline pipeline) {

    }

    protected ChannelInboundHandlerAdapter createChannelHandler() {
        return new NettyTcpTransportHandler();
    }

    //----- Event Handlers which can be overridden in subclasses -------------//

    protected void handleConnected(Channel channel) throws Exception {
        LOG.trace("Channel has become active! Channel is {}", channel);
        connectionEstablished(channel);
    }

    protected void handleChannelInactive(Channel channel) throws Exception {
        LOG.trace("Channel has gone inactive! Channel is {}", channel);
        if (connected.compareAndSet(true, false) && !closed.get()) {
            LOG.trace("Firing onTransportClosed listener");
            if (channel.eventLoop().inEventLoop()) {
                listener.onTransportClosed();
            } else {
                channel.eventLoop().execute(() -> {
                    listener.onTransportClosed();
                });
            }
        } else if (!closed.get()) {
            if (failureCause == null) {
                failureCause = new IOException("Connection failed");
            }
            connectionFailed(channel, failureCause);
        }
    }

    protected void handleException(Channel channel, Throwable cause) {
        LOG.trace("Exception on channel! Channel is {}", channel);
        if (connected.compareAndSet(true, false) && !closed.get()) {
            LOG.trace("Firing onTransportError listener");
            if (channel.eventLoop().inEventLoop()) {
                if (failureCause != null) {
                    listener.onTransportError(failureCause);
                } else {
                    listener.onTransportError(cause);
                }
            } else {
                channel.eventLoop().execute(() -> {
                    if (failureCause != null) {
                        listener.onTransportError(failureCause);
                    } else {
                        listener.onTransportError(cause);
                    }
                });
            }
        } else {
            // Hold the first failure for later dispatch if connect succeeds.
            // This will then trigger disconnect using the first error reported.
            if (failureCause == null) {
                LOG.trace("Holding error until connect succeeds: {}", cause.getMessage());
                failureCause = IOExceptionSupport.create(cause);
            }

            connectionFailed(channel, failureCause);
        }
    }

    //----- State change handlers and checks ---------------------------------//

    protected final void checkConnected() throws IOException {
        if (!connected.get() || !channel.isActive()) {
            throw new IOException("Cannot send to a non-connected transport.");
        }
    }

    private void checkConnected(ProtonBuffer output) throws IOException {
        if (!connected.get() || !channel.isActive()) {
            if (output instanceof ProtonNettyByteBuffer) {
                ReferenceCountUtil.release(output.unwrap());
            }
            throw new IOException("Cannot send to a non-connected transport.");
        }
    }

    /*
     * Called when the transport has successfully connected and is ready for use.
     */
    private void connectionEstablished(Channel connectedChannel) {
        channel = connectedChannel;
        connected.set(true);
        connectLatch.countDown();
    }

    /*
     * Called when the transport connection failed and an error should be returned.
     */
    private void connectionFailed(Channel failedChannel, IOException cause) {
        failureCause = cause;
        channel = failedChannel;
        connected.set(false);
        connectLatch.countDown();
    }

    private void configureNetty(Bootstrap bootstrap, TransportOptions options) {
        bootstrap.option(ChannelOption.TCP_NODELAY, options.tcpNoDelay());
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, options.connectTimeout());
        bootstrap.option(ChannelOption.SO_KEEPALIVE, options.tcpKeepAlive());
        bootstrap.option(ChannelOption.SO_LINGER, options.soLinger());

        if (options.sendBufferSize() != -1) {
            bootstrap.option(ChannelOption.SO_SNDBUF, options.sendBufferSize());
        }

        if (options.receiveBufferSize() != -1) {
            bootstrap.option(ChannelOption.SO_RCVBUF, options.receiveBufferSize());
            bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(options.receiveBufferSize()));
        }

        if (options.trafficClass() != -1) {
            bootstrap.option(ChannelOption.IP_TOS, options.trafficClass());
        }

        if (options.localAddress() != null || options.localPort() != 0) {
            if(options.localAddress() != null) {
                bootstrap.localAddress(options.localAddress(), options.localPort());
            } else {
                bootstrap.localAddress(options.localPort());
            }
        }
    }

    private void configureChannel(final Channel channel) throws Exception {
        if (isSecure()) {
            final SslHandler sslHandler;
            try {
                sslHandler = SslSupport.createSslHandler(channel.alloc(), getHost(), getPort(), sslOptions);
            } catch (Exception ex) {
                throw IOExceptionSupport.create(ex);
            }

            channel.pipeline().addLast("ssl", sslHandler);
        }

        if (options.traceBytes()) {
            channel.pipeline().addLast("logger", new LoggingHandler(getClass()));
        }

        addAdditionalHandlers(channel.pipeline());

        channel.pipeline().addLast(createChannelHandler());
    }

    //----- Default implementation of Netty handler --------------------------//

    protected abstract class NettyDefaultHandler<E> extends SimpleChannelInboundHandler<E> {

        @Override
        public void channelRegistered(ChannelHandlerContext context) throws Exception {
            channel = context.channel();
        }

        @Override
        public void channelActive(ChannelHandlerContext context) throws Exception {
            // In the Secure case we need to let the handshake complete before we
            // trigger the connected event.
            if (!isSecure()) {
                handleConnected(context.channel());
            } else {
                SslHandler sslHandler = context.pipeline().get(SslHandler.class);
                sslHandler.handshakeFuture().addListener(new GenericFutureListener<Future<Channel>>() {
                    @Override
                    public void operationComplete(Future<Channel> future) throws Exception {
                        if (future.isSuccess()) {
                            LOG.trace("SSL Handshake has completed: {}", channel);
                            handleConnected(channel);
                        } else {
                            LOG.trace("SSL Handshake has failed: {}", channel);
                            handleException(channel, future.cause());
                        }
                    }
                });
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext context) throws Exception {
            handleChannelInactive(context.channel());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
            handleException(context.channel(), cause);
        }
    }

    //----- Handle binary data over socket connections -----------------------//

    protected class NettyTcpTransportHandler extends NettyDefaultHandler<ByteBuf> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
            LOG.trace("New data read: {}", buffer);

            final ProtonNettyByteBuffer wrapped = new ProtonNettyByteBuffer(buffer);

            // Avoid all doubts to the contrary
            if (channel.eventLoop().inEventLoop()) {
                listener.onData(wrapped);
            } else {
                channel.eventLoop().execute(() -> {
                    listener.onData(wrapped);
                });
            }
        }
    }
}
