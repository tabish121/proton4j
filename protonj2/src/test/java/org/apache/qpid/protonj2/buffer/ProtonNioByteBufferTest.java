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
package org.apache.qpid.protonj2.buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

/**
 * Test class for the NIO ByteBuffer wrapper class
 */
public class ProtonNioByteBufferTest extends ProtonAbstractBufferTest {

    //----- Test NIO buffer implementation specifics

    @Test
    public void testUnwrapAllocatedBuffer() {
        ProtonBuffer buffer = allocateBuffer(13, 13);

        ByteBuffer unwrapped = (ByteBuffer) buffer.unwrap();

        assertEquals(13, unwrapped.capacity());
        assertEquals(0, unwrapped.position());
        assertEquals(13, unwrapped.limit());
    }

    @Test
    public void testUnwrapWrappedArray() {
        ProtonBuffer buffer = wrapBuffer(new byte[13]);

        ByteBuffer unwrapped = (ByteBuffer) buffer.unwrap();

        assertEquals(13, unwrapped.capacity());
        assertEquals(0, unwrapped.position());
        assertEquals(13, unwrapped.limit());
    }

    @Test
    public void testUnwrapWrappedByteBuffer() {
        ProtonBuffer buffer = new ProtonNioByteBuffer(ByteBuffer.allocate(13));

        ByteBuffer unwrapped = (ByteBuffer) buffer.unwrap();

        assertEquals(13, unwrapped.capacity());
        assertEquals(0, unwrapped.position());
        assertEquals(13, unwrapped.limit());
    }

    @Test
    public void testUnwrapWrappedByteBufferWithWriteIndex() {
        ProtonBuffer buffer = new ProtonNioByteBuffer(ByteBuffer.allocate(13), 13);

        ByteBuffer unwrapped = (ByteBuffer) buffer.unwrap();

        assertEquals(13, unwrapped.capacity());
        assertEquals(0, unwrapped.position());
        assertEquals(13, unwrapped.limit());
    }

    @Override
    @Test
    public void testCapacityEnforceMaxCapacity() {
        ProtonBuffer buffer = allocateBuffer(13, 13);
        assertEquals(13, buffer.maxCapacity());
        assertEquals(13, buffer.capacity());
        assertThrows(UnsupportedOperationException.class, () -> buffer.capacity(14));
    }

    @Override
    @Test
    public void testCapacityNegative() {
        ProtonBuffer buffer = allocateBuffer(13, 13);
        assertEquals(13, buffer.maxCapacity());
        assertEquals(13, buffer.capacity());
        assertThrows(IllegalArgumentException.class, () -> buffer.capacity(-1));
    }

    //----- Implement generic create methods from abstract test base

    @Override
    protected boolean canBufferCapacityBeChanged() {
        return false;
    }

    @Override
    protected boolean canAllocateDirectBackedBuffers() {
        return true;
    }

    @Override
    protected ProtonBuffer allocateBuffer(int initialCapacity) {
        return new ProtonNioByteBuffer(ByteBuffer.allocate(initialCapacity), 0);
    }

    @Override
    protected ProtonBuffer allocateDirectBuffer(int initialCapacity) {
        return new ProtonNioByteBuffer(ByteBuffer.allocateDirect(initialCapacity), 0);
    }

    @Override
    protected ProtonBuffer allocateBuffer(int initialCapacity, int maxCapacity) {
        if (initialCapacity != maxCapacity) {
            throw new UnsupportedOperationException("NIO buffer wrappers cannot grow");
        }

        return new ProtonNioByteBuffer(ByteBuffer.allocate(initialCapacity), 0);
    }

    @Override
    protected ProtonBuffer allocateDirectBuffer(int initialCapacity, int maxCapacity) {
        if (initialCapacity != maxCapacity) {
            throw new UnsupportedOperationException("NIO buffer wrappers cannot grow");
        }

        return new ProtonNioByteBuffer(ByteBuffer.allocateDirect(initialCapacity), 0);
    }

    @Override
    protected ProtonBuffer wrapBuffer(byte[] array) {
        return new ProtonNioByteBuffer(ByteBuffer.wrap(array));
    }
}
