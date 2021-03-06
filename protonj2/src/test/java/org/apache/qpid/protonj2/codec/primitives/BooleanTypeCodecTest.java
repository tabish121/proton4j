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
package org.apache.qpid.protonj2.codec.primitives;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.apache.qpid.protonj2.buffer.ProtonBuffer;
import org.apache.qpid.protonj2.buffer.ProtonByteBufferAllocator;
import org.apache.qpid.protonj2.codec.CodecTestSupport;
import org.apache.qpid.protonj2.codec.DecodeException;
import org.apache.qpid.protonj2.codec.EncodingCodes;
import org.apache.qpid.protonj2.codec.TypeDecoder;
import org.junit.jupiter.api.Test;

/**
 * Test the BooleanTypeDecoder for correctness
 */
public class BooleanTypeCodecTest extends CodecTestSupport {

    @Test
    public void testDecoderThrowsWhenAskedToReadWrongTypeAsBoolean() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        buffer.writeByte(EncodingCodes.UINT);
        buffer.writeByte(EncodingCodes.UINT);

        try {
            decoder.readBoolean(buffer, decoderState);
            fail("Should not allow read of integer type as boolean");
        } catch (DecodeException e) {}

        try {
            decoder.readBoolean(buffer, decoderState, false);
            fail("Should not allow read of integer type as boolean");
        } catch (DecodeException e) {}
    }

    @Test
    public void testDecodeBooleanEncodedBytes() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        buffer.writeByte(EncodingCodes.BOOLEAN_TRUE);
        buffer.writeByte(EncodingCodes.BOOLEAN);
        buffer.writeByte(0);
        buffer.writeByte(EncodingCodes.BOOLEAN_FALSE);
        buffer.writeByte(EncodingCodes.BOOLEAN);
        buffer.writeByte(1);

        boolean result1 = decoder.readBoolean(buffer, decoderState);
        boolean result2 = decoder.readBoolean(buffer, decoderState);
        boolean result3 = decoder.readBoolean(buffer, decoderState);
        boolean result4 = decoder.readBoolean(buffer, decoderState);

        assertTrue(result1);
        assertFalse(result2);
        assertFalse(result3);
        assertTrue(result4);
    }

    @Test
    public void testPeekNextTypeDecoder() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        buffer.writeByte(EncodingCodes.BOOLEAN_TRUE);
        buffer.writeByte(EncodingCodes.BOOLEAN);
        buffer.writeByte(0);
        buffer.writeByte(EncodingCodes.BOOLEAN_FALSE);
        buffer.writeByte(EncodingCodes.BOOLEAN);
        buffer.writeByte(1);

        assertEquals(Boolean.class, decoder.peekNextTypeDecoder(buffer, decoderState).getTypeClass());
        assertTrue(decoder.readBoolean(buffer, decoderState));
        assertEquals(Boolean.class, decoder.peekNextTypeDecoder(buffer, decoderState).getTypeClass());
        assertFalse(decoder.readBoolean(buffer, decoderState));
        assertEquals(Boolean.class, decoder.peekNextTypeDecoder(buffer, decoderState).getTypeClass());
        assertFalse(decoder.readBoolean(buffer, decoderState));
        assertEquals(Boolean.class, decoder.peekNextTypeDecoder(buffer, decoderState).getTypeClass());
        assertTrue(decoder.readBoolean(buffer, decoderState));
    }

    @Test
    public void testDecodeBooleanEncodedBytesAsPrimtives() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        buffer.writeByte(EncodingCodes.BOOLEAN_TRUE);
        buffer.writeByte(EncodingCodes.BOOLEAN);
        buffer.writeByte(0);
        buffer.writeByte(EncodingCodes.BOOLEAN_FALSE);
        buffer.writeByte(EncodingCodes.BOOLEAN);
        buffer.writeByte(1);

        boolean result1 = decoder.readBoolean(buffer, decoderState, false);
        boolean result2 = decoder.readBoolean(buffer, decoderState, true);
        boolean result3 = decoder.readBoolean(buffer, decoderState, true);
        boolean result4 = decoder.readBoolean(buffer, decoderState, false);

        assertTrue(result1);
        assertFalse(result2);
        assertFalse(result3);
        assertTrue(result4);
    }

    @Test
    public void testDecodeBooleanTrue() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        encoder.writeBoolean(buffer, encoderState, true);

        Object result = decoder.readObject(buffer, decoderState);
        assertTrue(result instanceof Boolean);
        assertTrue(((Boolean) result).booleanValue());

        encoder.writeBoolean(buffer, encoderState, true);

        Boolean booleanResult = decoder.readBoolean(buffer, decoderState);
        assertTrue(booleanResult.booleanValue());
        assertEquals(Boolean.TRUE, booleanResult);
    }

    @Test
    public void testDecodeBooleanFalse() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        encoder.writeBoolean(buffer, encoderState, false);

        Object result = decoder.readObject(buffer, decoderState);
        assertTrue(result instanceof Boolean);
        assertFalse(((Boolean) result).booleanValue());
    }

    @Test
    public void testDecodeBooleanFromNullEncoding() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        encoder.writeBoolean(buffer, encoderState, true);
        encoder.writeNull(buffer, encoderState);

        boolean result = decoder.readBoolean(buffer, decoderState);
        assertTrue(result);
        assertNull(decoder.readBoolean(buffer, decoderState));
    }

    @Test
    public void testDecodeBooleanAsPrimitiveWithDefault() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        encoder.writeBoolean(buffer, encoderState, true);
        encoder.writeNull(buffer, encoderState);

        boolean result = decoder.readBoolean(buffer, decoderState, false);
        assertTrue(result);
        result = decoder.readBoolean(buffer, decoderState, false);
        assertFalse(result);
    }

    @Test
    public void testDecodeBooleanFailsForNonBooleanType() throws Exception {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        encoder.writeLong(buffer, encoderState, 1l);

        try {
            decoder.readBoolean(buffer, decoderState);
            fail("Should not read long as boolean value.");
        } catch (DecodeException ioex) {}
    }

    @Test
    public void testDecodeSmallSeriesOfBooleans() throws IOException {
        doTestDecodeBooleanSeries(SMALL_SIZE);
    }

    @Test
    public void testDecodeLargeSeriesOfBooleans() throws IOException {
        doTestDecodeBooleanSeries(LARGE_SIZE);
    }

    private void doTestDecodeBooleanSeries(int size) throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        for (int i = 0; i < size; ++i) {
            encoder.writeBoolean(buffer, encoderState, i % 2 == 0);
        }

        for (int i = 0; i < size; ++i) {
            final Object result = decoder.readObject(buffer, decoderState);

            assertNotNull(result);
            assertTrue(result instanceof Boolean);

            Boolean boolValue = (Boolean) result;
            assertEquals(i % 2 == 0, boolValue.booleanValue());
        }
    }

    @Test
    public void testArrayOfBooleanObjects() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        final int size = 10;

        Boolean[] source = new Boolean[size];
        for (int i = 0; i < size; ++i) {
            source[i] = i % 2 == 0;
        }

        encoder.writeArray(buffer, encoderState, source);

        Object result = decoder.readObject(buffer, decoderState);
        assertNotNull(result);
        assertTrue(result.getClass().isArray());
        assertTrue(result.getClass().getComponentType().isPrimitive());

        boolean[] array = (boolean[]) result;
        assertEquals(size, array.length);

        for (int i = 0; i < size; ++i) {
            assertEquals(source[i], array[i]);
        }
    }

    @Test
    public void testZeroSizedArrayOfBooleanObjects() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        Boolean[] source = new Boolean[0];

        encoder.writeArray(buffer, encoderState, source);

        Object result = decoder.readObject(buffer, decoderState);
        assertNotNull(result);
        assertTrue(result.getClass().isArray());
        assertTrue(result.getClass().getComponentType().isPrimitive());

        boolean[] array = (boolean[]) result;
        assertEquals(source.length, array.length);
    }

    @Test
    public void testDecodeSmallBooleanArray() throws IOException {
        doTestDecodeBooleanArrayType(SMALL_ARRAY_SIZE);
    }

    @Test
    public void testDecodeLargeBooleanArray() throws IOException {
        doTestDecodeBooleanArrayType(LARGE_ARRAY_SIZE);
    }

    private void doTestDecodeBooleanArrayType(int size) throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        boolean[] source = new boolean[size];
        for (int i = 0; i < size; ++i) {
            source[i] = i % 2 == 0;
        }

        encoder.writeArray(buffer, encoderState, source);

        Object result = decoder.readObject(buffer, decoderState);
        assertNotNull(result);
        assertTrue(result.getClass().isArray());
        assertTrue(result.getClass().getComponentType().isPrimitive());

        boolean[] array = (boolean[]) result;
        assertEquals(size, array.length);

        for (int i = 0; i < size; ++i) {
            assertEquals(source[i], array[i]);
        }
    }

    @Test
    public void testArrayOfPrimitiveBooleanObjects() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        final int size = 10;

        boolean[] source = new boolean[size];
        for (int i = 0; i < size; ++i) {
            source[i] = i % 2 == 0;
        }

        encoder.writeArray(buffer, encoderState, source);

        Object result = decoder.readObject(buffer, decoderState);
        assertNotNull(result);
        assertTrue(result.getClass().isArray());
        assertTrue(result.getClass().getComponentType().isPrimitive());

        boolean[] array = (boolean[]) result;
        assertEquals(size, array.length);

        for (int i = 0; i < size; ++i) {
            assertEquals(source[i], array[i]);
        }
    }

    @Test
    public void testZeroSizedArrayOfPrimitiveBooleanObjects() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        boolean[] source = new boolean[0];

        encoder.writeArray(buffer, encoderState, source);

        Object result = decoder.readObject(buffer, decoderState);
        assertNotNull(result);
        assertTrue(result.getClass().isArray());
        assertTrue(result.getClass().getComponentType().isPrimitive());

        boolean[] array = (boolean[]) result;
        assertEquals(source.length, array.length);
    }

    @Test
    public void testArrayOfArraysOfPrimitiveBooleanObjects() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        final int size = 10;

        boolean[][] source = new boolean[2][size];
        for (int i = 0; i < size; ++i) {
            source[0][i] = i % 2 == 0;
            source[1][i] = i % 2 == 0;
        }

        encoder.writeArray(buffer, encoderState, source);

        Object result = decoder.readObject(buffer, decoderState);
        assertNotNull(result);
        assertTrue(result.getClass().isArray());

        Object[] resultArray = (Object[]) result;

        assertNotNull(resultArray);
        assertEquals(2, resultArray.length);

        assertTrue(resultArray[0].getClass().isArray());
        assertTrue(resultArray[1].getClass().isArray());

        for (int i = 0; i < resultArray.length; ++i) {
            boolean[] nested = (boolean[]) resultArray[i];
            assertEquals(source[i].length, nested.length);
            assertArrayEquals(source[i], nested);
        }
    }

    @Test
    public void testReadAllBooleanTypeEncodings() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        buffer.writeByte(EncodingCodes.BOOLEAN_TRUE);
        buffer.writeByte(EncodingCodes.BOOLEAN_FALSE);
        buffer.writeByte(EncodingCodes.BOOLEAN);
        buffer.writeByte(1);
        buffer.writeByte(EncodingCodes.BOOLEAN);
        buffer.writeByte(0);

        TypeDecoder<?> typeDecoder = decoder.readNextTypeDecoder(buffer, decoderState);
        assertEquals(Boolean.class, typeDecoder.getTypeClass());
        assertTrue((Boolean) typeDecoder.readValue(buffer, decoderState));
        typeDecoder = decoder.readNextTypeDecoder(buffer, decoderState);
        assertEquals(Boolean.class, typeDecoder.getTypeClass());
        assertFalse((Boolean) typeDecoder.readValue(buffer, decoderState));
        typeDecoder = decoder.readNextTypeDecoder(buffer, decoderState);
        assertEquals(Boolean.class, typeDecoder.getTypeClass());
        assertTrue((Boolean) typeDecoder.readValue(buffer, decoderState));
        typeDecoder = decoder.readNextTypeDecoder(buffer, decoderState);
        assertEquals(Boolean.class, typeDecoder.getTypeClass());
        assertFalse((Boolean) typeDecoder.readValue(buffer, decoderState));
    }

    @Test
    public void testSkipValueFullBooleanTypeEncodings() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        for (int i = 0; i < 10; ++i) {
            buffer.writeByte(EncodingCodes.BOOLEAN);
            buffer.writeByte(1);
            buffer.writeByte(EncodingCodes.BOOLEAN);
            buffer.writeByte(0);
        }

        encoder.writeObject(buffer, encoderState, false);

        for (int i = 0; i < 10; ++i) {
            TypeDecoder<?> typeDecoder = decoder.readNextTypeDecoder(buffer, decoderState);
            assertEquals(Boolean.class, typeDecoder.getTypeClass());
            typeDecoder.skipValue(buffer, decoderState);
            typeDecoder = decoder.readNextTypeDecoder(buffer, decoderState);
            assertEquals(Boolean.class, typeDecoder.getTypeClass());
            typeDecoder.skipValue(buffer, decoderState);
        }

        final Object result = decoder.readObject(buffer, decoderState);

        assertNotNull(result);
        assertTrue(result instanceof Boolean);

        Boolean value = (Boolean) result;
        assertEquals(false, value);
    }

    @Test
    public void testSkipValue() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        for (int i = 0; i < 10; ++i) {
            encoder.writeBoolean(buffer, encoderState, Boolean.TRUE);
            encoder.writeBoolean(buffer, encoderState, false);
        }

        encoder.writeObject(buffer, encoderState, false);

        for (int i = 0; i < 10; ++i) {
            TypeDecoder<?> typeDecoder = decoder.readNextTypeDecoder(buffer, decoderState);
            assertEquals(Boolean.class, typeDecoder.getTypeClass());
            typeDecoder.skipValue(buffer, decoderState);
            typeDecoder = decoder.readNextTypeDecoder(buffer, decoderState);
            assertEquals(Boolean.class, typeDecoder.getTypeClass());
            typeDecoder.skipValue(buffer, decoderState);
        }

        final Object result = decoder.readObject(buffer, decoderState);

        assertNotNull(result);
        assertTrue(result instanceof Boolean);

        Boolean value = (Boolean) result;
        assertEquals(false, value);
    }
}
