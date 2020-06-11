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
package org.apache.qpid.proton4j.engine.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.engine.EngineHandler;
import org.apache.qpid.proton4j.engine.EngineHandlerContext;
import org.apache.qpid.proton4j.engine.Frame;
import org.apache.qpid.proton4j.engine.HeaderFrame;
import org.apache.qpid.proton4j.engine.ProtocolFrame;
import org.apache.qpid.proton4j.engine.ProtocolFramePool;
import org.apache.qpid.proton4j.engine.SaslFrame;
import org.apache.qpid.proton4j.types.security.SaslPerformative;
import org.apache.qpid.proton4j.types.transport.AMQPHeader;
import org.apache.qpid.proton4j.types.transport.Performative;

public class FrameRecordingTransportHandler implements EngineHandler {

    private List<Frame<?>> framesRead = new ArrayList<>();
    private List<Frame<?>> framesWritten = new ArrayList<>();

    private final ProtocolFramePool framePool = new ProtocolFramePool();

    public FrameRecordingTransportHandler() {
    }

    public List<Frame<?>> getFramesWritten() {
        return framesWritten;
    }

    public List<Frame<?>> getFramesRead() {
        return framesRead;
    }

    @Override
    public void handleRead(EngineHandlerContext context, HeaderFrame header) {
        framesRead.add(header);
        context.fireRead(header);
    }

    @Override
    public void handleRead(EngineHandlerContext context, SaslFrame frame) {
        framesRead.add(frame);
        context.fireRead(frame);
    }

    @Override
    public void handleRead(EngineHandlerContext context, ProtocolFrame frame) {
        framesRead.add(frame);
        context.fireRead(frame);
    }

    @Override
    public void handleWrite(EngineHandlerContext context, AMQPHeader header) {
        framesWritten.add(new HeaderFrame(header));
        context.fireWrite(header);
    }

    @Override
    public void handleWrite(EngineHandlerContext context, Performative performative, int channel, ProtonBuffer payload, Runnable payloadToLarge) {
        framesWritten.add(framePool.take(performative, channel, 1, payload));
        context.fireWrite(performative, channel, payload, payloadToLarge);
    }

    @Override
    public void handleWrite(EngineHandlerContext context, SaslPerformative performative) {
        framesWritten.add(new SaslFrame(performative, 1, null));
        context.fireWrite(performative);
    }
}
