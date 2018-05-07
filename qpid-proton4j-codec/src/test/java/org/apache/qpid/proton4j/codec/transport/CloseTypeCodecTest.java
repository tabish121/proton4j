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
package org.apache.qpid.proton4j.codec.transport;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.qpid.proton4j.amqp.Symbol;
import org.apache.qpid.proton4j.amqp.transport.Close;
import org.apache.qpid.proton4j.amqp.transport.ErrorCondition;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.apache.qpid.proton4j.buffer.ProtonByteBufferAllocator;
import org.apache.qpid.proton4j.codec.CodecTestSupport;
import org.junit.Test;

public class CloseTypeCodecTest extends CodecTestSupport {

    @Test
    public void testEncodeDecodeTypeWithNoError() throws Exception {
       ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();

       Close input = new Close();

       encoder.writeObject(buffer, encoderState, input);

       final Close result = (Close) decoder.readObject(buffer, decoderState);

       assertNull(result.getError());
    }

    @Test
    public void testEncodeDecodeTypeWithError() throws Exception {
       ProtonBuffer buffer = ProtonByteBufferAllocator.DEFAULT.allocate();
       ErrorCondition error = new ErrorCondition();
       error.setCondition(Symbol.valueOf("amqp-error"));

       Close input = new Close();

       input.setError(error);

       encoder.writeObject(buffer, encoderState, input);

       final Close result = (Close) decoder.readObject(buffer, decoderState);

       assertNotNull(result.getError());
       assertNotNull(result.getError().getCondition());
       assertNull(result.getError().getDescription());
    }
}