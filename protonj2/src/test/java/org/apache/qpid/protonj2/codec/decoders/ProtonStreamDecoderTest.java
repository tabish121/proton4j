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
package org.apache.qpid.protonj2.codec.decoders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.apache.qpid.protonj2.buffer.ProtonBuffer;
import org.apache.qpid.protonj2.buffer.ProtonBufferInputStream;
import org.apache.qpid.protonj2.buffer.ProtonByteBufferAllocator;
import org.apache.qpid.protonj2.codec.CodecTestSupport;
import org.apache.qpid.protonj2.codec.DecodeEOFException;
import org.apache.qpid.protonj2.codec.DecodeException;
import org.apache.qpid.protonj2.codec.EncodingCodes;
import org.junit.jupiter.api.Test;

public class ProtonStreamDecoderTest extends CodecTestSupport {

    @Test
    public void testReadNullFromReadObjectForNullEncodng() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();
        InputStream stream = new ProtonBufferInputStream(buffer);

        buffer.writeByte(EncodingCodes.NULL);
        buffer.writeByte(EncodingCodes.NULL);

        assertNull(streamDecoder.readObject(stream, streamDecoderState));
        assertNull(streamDecoder.readObject(stream, streamDecoderState, UUID.class));
    }

    @Test
    public void testTryReadFromEmptyStream() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();
        InputStream stream = new ProtonBufferInputStream(buffer);

        try {
            streamDecoder.readObject(stream, streamDecoderState);
            fail("Should fail on read of object from empty stream");
        } catch (DecodeEOFException dex) {}
    }

    @Test
    public void testErrorOnReadOfUnknownEncoding() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();
        InputStream stream = new ProtonBufferInputStream(buffer);

        buffer.writeByte(255);

        assertNull(streamDecoder.peekNextTypeDecoder(stream, streamDecoderState));

        try {
            streamDecoder.readObject(stream, streamDecoderState);
            fail("Should throw if no type streamDecoder exists for given type");
        } catch (DecodeException ioe) {}
    }

    @Test
    public void testReadFromNullEncodingCode() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();
        InputStream stream = new ProtonBufferInputStream(buffer);

        final UUID value = UUID.randomUUID();

        buffer.writeByte(EncodingCodes.UUID);
        buffer.writeLong(value.getMostSignificantBits());
        buffer.writeLong(value.getLeastSignificantBits());

        try {
            streamDecoder.readObject(stream, streamDecoderState, String.class);
            fail("Should not allow for conversion to String type");
        } catch (ClassCastException cce) {
        }
    }

    @Test
    public void testReadMultipleFromNullEncoding() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();
        InputStream stream = new ProtonBufferInputStream(buffer);

        buffer.writeByte(EncodingCodes.NULL);

        assertNull(streamDecoder.readMultiple(stream, streamDecoderState, UUID.class));
    }

    @Test
    public void testReadMultipleFromSingleEncoding() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();
        InputStream stream = new ProtonBufferInputStream(buffer);

        final UUID value = UUID.randomUUID();

        buffer.writeByte(EncodingCodes.UUID);
        buffer.writeLong(value.getMostSignificantBits());
        buffer.writeLong(value.getLeastSignificantBits());

        UUID[] result = streamDecoder.readMultiple(stream, streamDecoderState, UUID.class);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(value, result[0]);
    }

    @Test
    public void testReadMultipleRequestsWrongTypeForArray() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();
        InputStream stream = new ProtonBufferInputStream(buffer);

        final UUID value = UUID.randomUUID();

        buffer.writeByte(EncodingCodes.UUID);
        buffer.writeLong(value.getMostSignificantBits());
        buffer.writeLong(value.getLeastSignificantBits());

        try {
            streamDecoder.readMultiple(stream, streamDecoderState, String.class);
            fail("Should not be able to convert to wrong resulting array type");
        } catch (ClassCastException cce) {}
    }

    @Test
    public void testReadMultipleRequestsWrongTypeForArrayEncoding() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();
        InputStream stream = new ProtonBufferInputStream(buffer);

        final UUID[] value = new UUID[] { UUID.randomUUID(), UUID.randomUUID() };

        encoder.writeArray(buffer, encoderState, value);

        try {
            streamDecoder.readMultiple(stream, streamDecoderState, String.class);
            fail("Should not be able to convert to wrong resulting array type");
        } catch (ClassCastException cce) {}
    }
}
