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
package org.apache.qpid.proton4j.codec.decoders.security;

import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.codec.DecodeException;
import org.apache.qpid.proton4j.codec.DecoderState;
import org.apache.qpid.proton4j.codec.TypeDecoder;
import org.apache.qpid.proton4j.codec.decoders.AbstractDescribedTypeDecoder;
import org.apache.qpid.proton4j.codec.decoders.primitives.ListTypeDecoder;
import org.apache.qpid.proton4j.types.Symbol;
import org.apache.qpid.proton4j.types.UnsignedLong;
import org.apache.qpid.proton4j.types.security.SaslInit;

/**
 * Decoder of AMQP SaslInit type values from a byte stream.
 */
public final class SaslInitTypeDecoder extends AbstractDescribedTypeDecoder<SaslInit> {

    private static final int MIN_SASL_INIT_LIST_ENTRIES = 0;
    private static final int MAX_SASL_INIT_LIST_ENTRIES = 3;

    @Override
    public UnsignedLong getDescriptorCode() {
        return SaslInit.DESCRIPTOR_CODE;
    }

    @Override
    public Symbol getDescriptorSymbol() {
        return SaslInit.DESCRIPTOR_SYMBOL;
    }

    @Override
    public Class<SaslInit> getTypeClass() {
        return SaslInit.class;
    }

    @Override
    public SaslInit readValue(ProtonBuffer buffer, DecoderState state) throws DecodeException {
        TypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(buffer, state);

        checkIsExpectedType(ListTypeDecoder.class, decoder);

        return readProperties(buffer, state, (ListTypeDecoder) decoder);
    }

    @Override
    public void skipValue(ProtonBuffer buffer, DecoderState state) throws DecodeException {
        TypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(buffer, state);

        checkIsExpectedType(ListTypeDecoder.class, decoder);

        decoder.skipValue(buffer, state);
    }

    @Override
    public SaslInit[] readArrayElements(ProtonBuffer buffer, DecoderState state, int count) throws DecodeException {
        TypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(buffer, state);

        checkIsExpectedType(ListTypeDecoder.class, decoder);

        SaslInit[] result = new SaslInit[count];
        for (int i = 0; i < count; ++i) {
            result[i] = readProperties(buffer, state, (ListTypeDecoder) decoder);
        }

        return result;
    }

    private SaslInit readProperties(ProtonBuffer buffer, DecoderState state, ListTypeDecoder listDecoder) throws DecodeException {
        SaslInit init = new SaslInit();

        @SuppressWarnings("unused")
        int size = listDecoder.readSize(buffer);
        int count = listDecoder.readCount(buffer);

        // Don't decode anything if things already look wrong.
        if (count < MIN_SASL_INIT_LIST_ENTRIES) {
            throw new DecodeException("Not enougn entries in SaslInit list encoding: " + count);
        }

        if (count > MAX_SASL_INIT_LIST_ENTRIES) {
            throw new DecodeException("To many entries in SaslInit list encoding: " + count);
        }

        for (int index = 0; index < count; ++index) {
            switch (index) {
                case 0:
                    init.setMechanism(state.getDecoder().readSymbol(buffer, state));
                    break;
                case 1:
                    init.setInitialResponse(state.getDecoder().readBinaryAsBuffer(buffer, state));
                    break;
                case 2:
                    init.setHostname(state.getDecoder().readString(buffer, state));
                    break;
                default:
                    throw new DecodeException("To many entries in Properties encoding");
            }
        }

        return init;
    }
}
