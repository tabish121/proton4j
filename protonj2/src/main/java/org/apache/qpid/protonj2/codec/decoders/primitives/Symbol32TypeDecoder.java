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
package org.apache.qpid.protonj2.codec.decoders.primitives;

import java.io.InputStream;

import org.apache.qpid.protonj2.buffer.ProtonBuffer;
import org.apache.qpid.protonj2.codec.DecodeException;
import org.apache.qpid.protonj2.codec.EncodingCodes;
import org.apache.qpid.protonj2.codec.decoders.ProtonStreamUtils;

/**
 * Decoder of AMQP Symbol values from a byte stream.
 */
public final class Symbol32TypeDecoder extends AbstractSymbolTypeDecoder {

    @Override
    public int getTypeCode() {
        return EncodingCodes.SYM32 & 0xff;
    }

    @Override
    protected int readSize(ProtonBuffer buffer) throws DecodeException {
        return buffer.readInt();
    }

    @Override
    protected int readSize(InputStream stream) throws DecodeException {
        return ProtonStreamUtils.readInt(stream);
    }
}
