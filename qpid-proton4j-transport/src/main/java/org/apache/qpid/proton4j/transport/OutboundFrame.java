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
package org.apache.qpid.proton4j.transport;

import java.util.function.Consumer;

import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.transport.exceptions.TransportException;

/**
 * A Frame that is composed of a body and a payload that is meant to be encoded and
 * transmitted to the remote.
 */
public abstract class OutboundFrame<V> {

    private final byte type;

    private V body;
    private int channel;
    private ProtonBuffer payload;
    private int maxFrameSize;
    private Consumer<V> frameSizeLimitExceededHandler;

    protected OutboundFrame(byte type) {
        this.type = type;
    }

    void initialize(V body, int channel, ProtonBuffer payload, int maxFrameSize, Consumer<V> frameSizeLimitExceededHandler) {
        this.body = body;
        this.channel = channel;
        this.payload = payload;
        this.maxFrameSize = maxFrameSize;
        this.frameSizeLimitExceededHandler = frameSizeLimitExceededHandler;
    }

    public V getBody() {
        return body;
    }

    public int getChannel() {
        return channel;
    }

    public byte getType() {
        return type;
    }

    public ProtonBuffer getPayload() {
        return payload;
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    // TODO - The idea is to either redo the encode using the most that we can fit into the frame
    //        or throw to indicate we can't do anything because there was no handler to notice.
    public void onFrameSizeLimitExceeded() throws TransportException {
        if (frameSizeLimitExceededHandler != null) {
            frameSizeLimitExceededHandler.accept(getBody());
        } else {
            throw new TransportException("Could not encode frame because encoded form exceeds configured max frame size");
        }
    }
}