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
package org.apache.qpid.proton4j.codec.decoders.transactions;

import java.io.IOException;

import org.apache.qpid.proton4j.amqp.Symbol;
import org.apache.qpid.proton4j.amqp.UnsignedLong;
import org.apache.qpid.proton4j.amqp.transactions.Declared;
import org.apache.qpid.proton4j.codec.DecoderState;
import org.apache.qpid.proton4j.codec.DescribedTypeDecoder;
import org.apache.qpid.proton4j.codec.TypeDecoder;
import org.apache.qpid.proton4j.codec.decoders.primitives.ListTypeDecoder;
import org.apache.qpid.proton4j.codec.decoders.primitives.ListTypeDecoder.ListEntryHandler;

import io.netty.buffer.ByteBuf;

/**
 * Decoder of AMQP Declared types from a byte stream.
 */
public class DeclaredTypeDecoder implements DescribedTypeDecoder<Declared>, ListEntryHandler<Declared> {

    @Override
    public Class<Declared> getTypeClass() {
        return Declared.class;
    }

    @Override
    public UnsignedLong getDescriptorCode() {
        return Declared.DESCRIPTOR_CODE;
    }

    @Override
    public Symbol getDescriptorSymbol() {
        return Declared.DESCRIPTOR_SYMBOL;
    }

    @Override
    public Declared readValue(ByteBuf buffer, DecoderState state) throws IOException {

        TypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(buffer, state);

        if (!(decoder instanceof ListTypeDecoder)) {
            throw new IOException("Expected List type indicator but got decoder for type: " + decoder.getTypeClass().getName());
        }

        ListTypeDecoder listDecoder = (ListTypeDecoder) decoder;
        Declared declared = new Declared();

        listDecoder.readValue(buffer, state, this, declared);

        return declared;
    }

    @Override
    public void onListEntry(int index, Declared declared, ByteBuf buffer, DecoderState state) throws IOException {
        switch (index) {
            case 0:
                declared.setTxnId(state.getDecoder().readBinary(buffer, state));
                break;
            default:
                throw new IllegalStateException("To many entries in Declared encoding");
        }
    }

    @Override
    public void skipValue(ByteBuf buffer, DecoderState state) throws IOException {
        TypeDecoder<?> decoder = state.getDecoder().readNextTypeDecoder(buffer, state);

        if (!(decoder instanceof ListTypeDecoder)) {
            throw new IOException("Expected List type indicator but got decoder for type: " + decoder.getTypeClass().getName());
        }

        decoder.skipValue(buffer, state);
    }
}