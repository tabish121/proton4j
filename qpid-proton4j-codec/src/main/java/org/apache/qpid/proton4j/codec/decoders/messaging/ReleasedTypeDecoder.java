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
package org.apache.qpid.proton4j.codec.decoders.messaging;

import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.codec.DecodeException;
import org.apache.qpid.proton4j.codec.DecoderState;
import org.apache.qpid.proton4j.codec.TypeDecoder;
import org.apache.qpid.proton4j.codec.decoders.AbstractDescribedTypeDecoder;
import org.apache.qpid.proton4j.codec.decoders.primitives.ListTypeDecoder;
import org.apache.qpid.proton4j.types.Symbol;
import org.apache.qpid.proton4j.types.UnsignedLong;
import org.apache.qpid.proton4j.types.messaging.Released;

/**
 * Decoder of AMQP Released type values from a byte stream.
 */
public final class ReleasedTypeDecoder extends AbstractDescribedTypeDecoder<Released> {

    @Override
    public Class<Released> getTypeClass() {
        return Released.class;
    }

    @Override
    public UnsignedLong getDescriptorCode() {
        return Released.DESCRIPTOR_CODE;
    }

    @Override
    public Symbol getDescriptorSymbol() {
        return Released.DESCRIPTOR_SYMBOL;
    }

    @Override
    public Released readValue(ProtonBuffer buffer, DecoderState state) throws DecodeException {
        TypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(buffer, state);

        checkIsExpectedType(ListTypeDecoder.class, decoder);

        decoder.skipValue(buffer, state);

        return Released.getInstance();
    }

    @Override
    public Released[] readArrayElements(ProtonBuffer buffer, DecoderState state, int count) throws DecodeException {
        TypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(buffer, state);

        checkIsExpectedType(ListTypeDecoder.class, decoder);

        Released[] result = new Released[count];
        for (int i = 0; i < count; ++i) {
            decoder.skipValue(buffer, state);
            result[i] = Released.getInstance();
        }

        return result;
    }

    @Override
    public void skipValue(ProtonBuffer buffer, DecoderState state) throws DecodeException {
        TypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(buffer, state);

        checkIsExpectedType(ListTypeDecoder.class, decoder);

        decoder.skipValue(buffer, state);
    }
}
