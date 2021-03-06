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
package org.apache.qpid.protonj2.engine.util;

import org.apache.qpid.protonj2.engine.EngineHandler;
import org.apache.qpid.protonj2.engine.EngineHandlerContext;
import org.apache.qpid.protonj2.engine.HeaderEnvelope;
import org.apache.qpid.protonj2.engine.IncomingAMQPEnvelope;
import org.apache.qpid.protonj2.engine.OutgoingAMQPEnvelope;
import org.apache.qpid.protonj2.engine.SASLEnvelope;

/**
 * Drops all read frames in tests where no inbound frame handling is needed.
 */
public class FrameReadSinkTransportHandler implements EngineHandler {

    public FrameReadSinkTransportHandler() {
    }

    @Override
    public void handleRead(EngineHandlerContext context, HeaderEnvelope header) {
    }

    @Override
    public void handleRead(EngineHandlerContext context, SASLEnvelope frame) {
    }

    @Override
    public void handleRead(EngineHandlerContext context, IncomingAMQPEnvelope frame) {
    }

    @Override
    public void handleWrite(EngineHandlerContext context, HeaderEnvelope frame) {
        context.fireWrite(frame);
    }

    @Override
    public void handleWrite(EngineHandlerContext context, OutgoingAMQPEnvelope frame) {
        context.fireWrite(frame);
    }

    @Override
    public void handleWrite(EngineHandlerContext context, SASLEnvelope frame) {
        context.fireWrite(frame);
    }
}
