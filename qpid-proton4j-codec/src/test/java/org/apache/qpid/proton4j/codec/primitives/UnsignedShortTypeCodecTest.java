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
package org.apache.qpid.proton4j.codec.primitives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.qpid.proton4j.amqp.UnsignedShort;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.buffer.ProtonByteBufferAllocator;
import org.apache.qpid.proton4j.codec.CodecTestSupport;
import org.junit.Test;

public class UnsignedShortTypeCodecTest extends CodecTestSupport {

    @Test
    public void testEncodeDecodeUnsignedShort() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        encoder.writeUnsignedShort(buffer, encoderState, UnsignedShort.valueOf((byte) 64));

        Object result = decoder.readObject(buffer, decoderState);
        assertTrue(result instanceof UnsignedShort);
        assertEquals(64, ((UnsignedShort) result).byteValue());
    }

    @Test
    public void testEncodeDecodeUnsignedShortAbove32k() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        encoder.writeUnsignedShort(buffer, encoderState, UnsignedShort.valueOf((short) 33565));

        Object result = decoder.readObject(buffer, decoderState);
        assertTrue(result instanceof UnsignedShort);
        assertTrue(((UnsignedShort) result).shortValue() < 0);
        assertEquals(33565, ((UnsignedShort) result).intValue());
    }


    @Test
    public void testEncodeDecodeUnsignedShortFromInt() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        encoder.writeUnsignedShort(buffer, encoderState, 33565);

        Object result = decoder.readObject(buffer, decoderState);
        assertTrue(result instanceof UnsignedShort);
        assertTrue(((UnsignedShort) result).shortValue() < 0);
        assertEquals(33565, ((UnsignedShort) result).intValue());

        try {
            encoder.writeUnsignedShort(buffer, encoderState, 65536);
            fail("Should not be able to write illegal out of range value");
        } catch (IllegalArgumentException iae) {
        }
    }

    @Test
    public void testEncodeDecodeShort() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        encoder.writeUnsignedShort(buffer, encoderState, (short) 64);

        Object result = decoder.readObject(buffer, decoderState);
        assertTrue(result instanceof UnsignedShort);
        assertEquals(64, ((UnsignedShort) result).shortValue());
    }

    @Test
    public void testDecodeSmallSeriesOfUnsignedShorts() throws IOException {
        doTestDecodeUnsignedShortSeries(SMALL_SIZE);
    }

    @Test
    public void testDecodeLargeSeriesOfUnsignedShorts() throws IOException {
        doTestDecodeUnsignedShortSeries(LARGE_SIZE);
    }

    private void doTestDecodeUnsignedShortSeries(int size) throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        for (int i = 0; i < size; ++i) {
            encoder.writeUnsignedShort(buffer, encoderState, (byte)(i % 255));
        }

        for (int i = 0; i < size; ++i) {
            final UnsignedShort result = decoder.readUnsignedShort(buffer, decoderState);

            assertNotNull(result);
            assertEquals((byte)(i % 255), result.byteValue());
        }
    }

    @Test
    public void testArrayOfUnsignedShortObjects() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        final int size = 10;

        UnsignedShort[] source = new UnsignedShort[size];
        for (int i = 0; i < size; ++i) {
            source[i] = UnsignedShort.valueOf((byte) (i % 255));
        }

        encoder.writeArray(buffer, encoderState, source);

        Object result = decoder.readObject(buffer, decoderState);
        assertNotNull(result);
        assertTrue(result.getClass().isArray());
        assertFalse(result.getClass().getComponentType().isPrimitive());

        UnsignedShort[] array = (UnsignedShort[]) result;
        assertEquals(size, array.length);

        for (int i = 0; i < size; ++i) {
            assertEquals(source[i], array[i]);
        }
    }

    @Test
    public void testZeroSizedArrayOfUnsignedShortObjects() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        UnsignedShort[] source = new UnsignedShort[0];

        encoder.writeArray(buffer, encoderState, source);

        Object result = decoder.readObject(buffer, decoderState);
        assertNotNull(result);
        assertTrue(result.getClass().isArray());
        assertFalse(result.getClass().getComponentType().isPrimitive());

        UnsignedShort[] array = (UnsignedShort[]) result;
        assertEquals(source.length, array.length);
    }
}