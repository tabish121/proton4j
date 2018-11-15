/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.qpid.proton4j.codec.messaging;

import java.io.IOException;

import org.apache.qpid.proton4j.amqp.UnsignedInteger;
import org.apache.qpid.proton4j.amqp.transport.Flow;
import org.apache.qpid.proton4j.codec.CodecBenchmarkBase;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

public class FlowBenchmark extends CodecBenchmarkBase {

    private Flow flow;
    private Blackhole blackhole;

    @Setup
    public void init(Blackhole blackhole) {
        this.blackhole = blackhole;
        super.init();
        initFlow();
        encode();
    }

    private void initFlow() {
        flow = new Flow();
        flow.setNextIncomingId(1);
        flow.setIncomingWindow(2047);
        flow.setNextOutgoingId(1);
        flow.setOutgoingWindow(UnsignedInteger.MAX_VALUE.longValue());
        flow.setHandle(0);
        flow.setDeliveryCount(10);
        flow.setLinkCredit(1000);
    }

    @Benchmark
    public void encode() {
        buffer.clear();
        encoder.writeObject(buffer, encoderState, flow);
    }

    @Benchmark
    public void decode() throws IOException {
        buffer.setReadIndex(0);
        blackhole.consume(decoder.readObject(buffer, decoderState));
    }

    public static void main(String[] args) throws RunnerException {
        runBenchmark(FlowBenchmark.class);
    }
}
