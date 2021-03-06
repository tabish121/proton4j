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
package org.apache.qpid.protonj2.codec.decoders.messaging;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.qpid.protonj2.buffer.ProtonBuffer;
import org.apache.qpid.protonj2.codec.DecodeException;
import org.apache.qpid.protonj2.codec.Decoder;
import org.apache.qpid.protonj2.codec.DecoderState;
import org.apache.qpid.protonj2.codec.StreamDecoder;
import org.apache.qpid.protonj2.codec.StreamDecoderState;
import org.apache.qpid.protonj2.codec.StreamTypeDecoder;
import org.apache.qpid.protonj2.codec.TypeDecoder;
import org.apache.qpid.protonj2.codec.decoders.AbstractDescribedTypeDecoder;
import org.apache.qpid.protonj2.codec.decoders.primitives.MapTypeDecoder;
import org.apache.qpid.protonj2.codec.decoders.primitives.NullTypeDecoder;
import org.apache.qpid.protonj2.types.Symbol;
import org.apache.qpid.protonj2.types.UnsignedLong;
import org.apache.qpid.protonj2.types.messaging.ApplicationProperties;

/**
 * Decoder of AMQP ApplicationProperties types from a byte stream
 */
public final class ApplicationPropertiesTypeDecoder extends AbstractDescribedTypeDecoder<ApplicationProperties> {

    @Override
    public Class<ApplicationProperties> getTypeClass() {
        return ApplicationProperties.class;
    }

    @Override
    public UnsignedLong getDescriptorCode() {
        return ApplicationProperties.DESCRIPTOR_CODE;
    }

    @Override
    public Symbol getDescriptorSymbol() {
        return ApplicationProperties.DESCRIPTOR_SYMBOL;
    }

    @Override
    public ApplicationProperties readValue(ProtonBuffer buffer, DecoderState state) throws DecodeException {
        TypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(buffer, state);

        if (decoder instanceof NullTypeDecoder) {
            return new ApplicationProperties(null);
        }

        checkIsExpectedType(MapTypeDecoder.class, decoder);

        return new ApplicationProperties(readMap(buffer, state, (MapTypeDecoder) decoder));
    }

    @Override
    public ApplicationProperties[] readArrayElements(ProtonBuffer buffer, DecoderState state, int count) throws DecodeException {
        TypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(buffer, state);

        ApplicationProperties[] result = new ApplicationProperties[count];

        if (decoder instanceof NullTypeDecoder) {
            for (int i = 0; i < count; ++i) {
                result[i] = new ApplicationProperties(null);
            }
            return result;
        }

        checkIsExpectedType(MapTypeDecoder.class, decoder);

        MapTypeDecoder mapDecoder = (MapTypeDecoder) decoder;

        for (int i = 0; i < count; ++i) {
            result[i] = new ApplicationProperties(readMap(buffer, state, mapDecoder));
        }

        return result;
    }

    @Override
    public void skipValue(ProtonBuffer buffer, DecoderState state) throws DecodeException {
        TypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(buffer, state);

        if (!(decoder instanceof NullTypeDecoder)) {
            checkIsExpectedType(MapTypeDecoder.class, decoder);
            decoder.skipValue(buffer, state);
        }
    }

    private Map<String, Object> readMap(ProtonBuffer buffer, DecoderState state, MapTypeDecoder mapDecoder) throws DecodeException {
        int size = mapDecoder.readSize(buffer);
        int count = mapDecoder.readCount(buffer);

        if (count > buffer.getReadableBytes()) {
            throw new DecodeException(String.format(
                    "Map encoded size %d is specified to be greater than the amount " +
                    "of data available (%d)", size, buffer.getReadableBytes()));
        }

        Decoder decoder = state.getDecoder();

        // Count include both key and value so we must include that in the loop
        Map<String, Object> map = new LinkedHashMap<>(count);
        for (int i = 0; i < count / 2; i++) {
            String key = decoder.readString(buffer, state);
            Object value = decoder.readObject(buffer, state);

            map.put(key, value);
        }

        return map;
    }

    @Override
    public ApplicationProperties readValue(InputStream stream, StreamDecoderState state) throws DecodeException {
        StreamTypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(stream, state);

        if (decoder instanceof NullTypeDecoder) {
            return new ApplicationProperties(null);
        }

        checkIsExpectedType(MapTypeDecoder.class, decoder);

        return new ApplicationProperties(readMap(stream, state, (MapTypeDecoder) decoder));
    }

    @Override
    public ApplicationProperties[] readArrayElements(InputStream stream, StreamDecoderState state, int count) throws DecodeException {
        StreamTypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(stream, state);

        ApplicationProperties[] result = new ApplicationProperties[count];

        if (decoder instanceof NullTypeDecoder) {
            for (int i = 0; i < count; ++i) {
                result[i] = new ApplicationProperties(null);
            }
            return result;
        }

        checkIsExpectedType(MapTypeDecoder.class, decoder);

        MapTypeDecoder mapDecoder = (MapTypeDecoder) decoder;

        for (int i = 0; i < count; ++i) {
            result[i] = new ApplicationProperties(readMap(stream, state, mapDecoder));
        }

        return result;
    }

    @Override
    public void skipValue(InputStream stream, StreamDecoderState state) throws DecodeException {
        StreamTypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(stream, state);

        if (!(decoder instanceof NullTypeDecoder)) {
            checkIsExpectedType(MapTypeDecoder.class, decoder);
            decoder.skipValue(stream, state);
        }
    }

    private Map<String, Object> readMap(InputStream stream, StreamDecoderState state, MapTypeDecoder mapDecoder) throws DecodeException {
        @SuppressWarnings("unused")
        int size = mapDecoder.readSize(stream);
        int count = mapDecoder.readCount(stream);

        StreamDecoder decoder = state.getDecoder();

        // Count include both key and value so we must include that in the loop
        Map<String, Object> map = new LinkedHashMap<>(count);
        for (int i = 0; i < count / 2; i++) {
            String key = decoder.readString(stream, state);
            Object value = decoder.readObject(stream, state);

            map.put(key, value);
        }

        return map;
    }
}
