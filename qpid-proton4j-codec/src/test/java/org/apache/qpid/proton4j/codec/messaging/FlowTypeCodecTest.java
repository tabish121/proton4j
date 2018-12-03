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
package org.apache.qpid.proton4j.codec.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.qpid.proton4j.amqp.UnsignedInteger;
import org.apache.qpid.proton4j.amqp.transport.Flow;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.buffer.ProtonByteBufferAllocator;
import org.apache.qpid.proton4j.codec.CodecTestSupport;
import org.junit.Test;

public class FlowTypeCodecTest extends CodecTestSupport {

    @Test
    public void testDecodeFlow() throws IOException {
        doTestDecodeFlowSeries(1);
    }

    @Test
    public void testDecodeSmallSeriesOfFlows() throws IOException {
        doTestDecodeFlowSeries(SMALL_SIZE);
    }

    @Test
    public void testDecodeLargeSeriesOfFlows() throws IOException {
        doTestDecodeFlowSeries(LARGE_SIZE);
    }

    private void doTestDecodeFlowSeries(int size) throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        Flow flow = new Flow();
        flow.setNextIncomingId(1);
        flow.setIncomingWindow(2047);
        flow.setNextOutgoingId(1);
        flow.setOutgoingWindow(UnsignedInteger.MAX_VALUE.longValue());
        flow.setHandle(0);
        flow.setDeliveryCount(10);
        flow.setLinkCredit(1000);

        for (int i = 0; i < size; ++i) {
            encoder.writeObject(buffer, encoderState, flow);
        }

        for (int i = 0; i < size; ++i) {
            final Object result = decoder.readObject(buffer, decoderState);

            assertNotNull(result);
            assertTrue(result instanceof Flow);

            Flow decoded = (Flow) result;

            assertEquals(flow.getNextIncomingId(), decoded.getNextIncomingId());
            assertEquals(flow.getIncomingWindow(), decoded.getIncomingWindow());
            assertEquals(flow.getNextOutgoingId(), decoded.getNextOutgoingId());
            assertEquals(flow.getOutgoingWindow(), decoded.getOutgoingWindow());
            assertEquals(flow.getHandle(), decoded.getHandle());
            assertEquals(flow.getDeliveryCount(), decoded.getDeliveryCount());
            assertEquals(flow.getLinkCredit(), decoded.getLinkCredit());
        }
    }

    @Test
    public void testEncodeDecodeArrayOfDataSections() throws IOException {
        ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

        Flow flow = new Flow();
        flow.setNextIncomingId(1);
        flow.setIncomingWindow(2047);
        flow.setNextOutgoingId(1);
        flow.setOutgoingWindow(UnsignedInteger.MAX_VALUE.longValue());
        flow.setHandle(0);
        flow.setDeliveryCount(10);
        flow.setLinkCredit(1000);

        Flow[] flowArray = new Flow[3];

        flowArray[0] = flow;
        flowArray[1] = flow;
        flowArray[2] = flow;

        encoder.writeObject(buffer, encoderState, flowArray);

        final Object result = decoder.readObject(buffer, decoderState);

        assertTrue(result.getClass().isArray());
        assertEquals(Flow.class, result.getClass().getComponentType());

        Flow[] resultArray = (Flow[]) result;

        for (int i = 0; i < resultArray.length; ++i) {
            assertNotNull(resultArray[i]);
            assertTrue(resultArray[i] instanceof Flow);
            assertEquals(flowArray[i].getNextIncomingId(), resultArray[i].getNextIncomingId());
            assertEquals(flowArray[i].getIncomingWindow(), resultArray[i].getIncomingWindow());
            assertEquals(flowArray[i].getNextOutgoingId(), resultArray[i].getNextOutgoingId());
            assertEquals(flowArray[i].getOutgoingWindow(), resultArray[i].getOutgoingWindow());
            assertEquals(flowArray[i].getHandle(), resultArray[i].getHandle());
            assertEquals(flowArray[i].getDeliveryCount(), resultArray[i].getDeliveryCount());
            assertEquals(flowArray[i].getLinkCredit(), resultArray[i].getLinkCredit());
        }
    }
}