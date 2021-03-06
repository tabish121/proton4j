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
package org.apache.qpid.protonj2.engine;

import org.apache.qpid.protonj2.buffer.ProtonBuffer;
import org.apache.qpid.protonj2.types.security.SaslPerformative;
import org.apache.qpid.protonj2.types.security.SaslPerformative.SaslPerformativeHandler;

/**
 * Frame object containing a SASL performative
 */
public class SASLEnvelope extends PerformativeEnvelope<SaslPerformative>{

    public static final byte SASL_FRAME_TYPE = (byte) 1;

    public SASLEnvelope(SaslPerformative performative) {
        this(performative, null);
    }

    public SASLEnvelope(SaslPerformative performative, ProtonBuffer payload) {
        super(SASL_FRAME_TYPE);

        initialize(performative, 0, payload);
    }

    public <E> void invoke(SaslPerformativeHandler<E> handler, E context) {
        getBody().invoke(handler, context);
    }
}
