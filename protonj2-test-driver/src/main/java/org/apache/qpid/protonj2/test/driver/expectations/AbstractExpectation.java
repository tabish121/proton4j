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
package org.apache.qpid.protonj2.test.driver.expectations;

import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.qpid.protonj2.test.driver.AMQPTestDriver;
import org.apache.qpid.protonj2.test.driver.ScriptedExpectation;
import org.apache.qpid.protonj2.test.driver.codec.ListDescribedType;
import org.apache.qpid.protonj2.test.driver.codec.security.SaslChallenge;
import org.apache.qpid.protonj2.test.driver.codec.security.SaslInit;
import org.apache.qpid.protonj2.test.driver.codec.security.SaslMechanisms;
import org.apache.qpid.protonj2.test.driver.codec.security.SaslOutcome;
import org.apache.qpid.protonj2.test.driver.codec.security.SaslResponse;
import org.apache.qpid.protonj2.test.driver.codec.transport.AMQPHeader;
import org.apache.qpid.protonj2.test.driver.codec.transport.Attach;
import org.apache.qpid.protonj2.test.driver.codec.transport.Begin;
import org.apache.qpid.protonj2.test.driver.codec.transport.Close;
import org.apache.qpid.protonj2.test.driver.codec.transport.Detach;
import org.apache.qpid.protonj2.test.driver.codec.transport.Disposition;
import org.apache.qpid.protonj2.test.driver.codec.transport.End;
import org.apache.qpid.protonj2.test.driver.codec.transport.Flow;
import org.apache.qpid.protonj2.test.driver.codec.transport.HeartBeat;
import org.apache.qpid.protonj2.test.driver.codec.transport.Open;
import org.apache.qpid.protonj2.test.driver.codec.transport.Transfer;
import org.apache.qpid.protonj2.test.driver.exceptions.UnexpectedPerformativeError;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;

/**
 * Abstract base for expectations that need to handle matchers against fields in the
 * value being expected.
 *
 * @param <T> The type being validated
 */
public abstract class AbstractExpectation<T extends ListDescribedType> implements ScriptedExpectation {

    private static final Logger LOG = LoggerFactory.getLogger(AMQPTestDriver.class);

    public static int ANY_CHANNEL = -1;

    protected int expectedChannel = ANY_CHANNEL;
    protected final AMQPTestDriver driver;
    private boolean optional;

    public AbstractExpectation(AMQPTestDriver driver) {
        this.driver = driver;
    }

    //----- Configure base expectations

    public AbstractExpectation<T> onChannel(int channel) {
        this.expectedChannel = channel;
        return this;
    }

    /**
     * @return true if this element represents an optional part of the script.
     */
    @Override
    public boolean isOptional() {
        return optional;
    }

    /**
     * Marks this expectation as optional which can be useful when a frames arrival may or may not
     * occur based on some other timing in the test.
     *
     * @return if the frame expectation is optional and its absence shouldn't fail the test.
     */
    public AbstractExpectation<T> optional() {
        optional = true;
        return this;
    }

    //------ Abstract classes use these methods to control validation

    /**
     * Verifies the fields of the performative against any matchers registered.
     *
     * @param performative
     *      the performative received which will be validated against the configured matchers
     *
     * @throws AssertionError if a registered matcher assertion is not met.
     */
    protected final void verifyPerformative(T performative) throws AssertionError {
        LOG.debug("About to check the fields of the performative." +
                  "\n  Received:" + performative + "\n  Expectations: " + getExpectationMatcher());

        assertThat("Performative does not match expectation", performative, getExpectationMatcher());
    }

    protected final void verifyPayload(ByteBuf payload) {
        if (getPayloadMatcher() != null) {
            assertThat("Paylod does not match expectation", payload, getPayloadMatcher());
        } else if (payload != null) {
            throw new AssertionError("Performative should not have been sent with a paylod: ");
        }
    }

    protected final void verifyChannel(int channel) {
        if (expectedChannel != ANY_CHANNEL && expectedChannel != channel) {
            throw new AssertionError("Expected send on channel + " + expectedChannel + ": but was on channel:" + channel);
        }
    }

    protected abstract Matcher<ListDescribedType> getExpectationMatcher();

    protected abstract Class<T> getExpectedTypeClass();

    protected Matcher<ByteBuf> getPayloadMatcher() {
        return null;
    }

    //----- Base implementation of the handle methods to describe when we get wrong type.

    @Override
    public void handleOpen(Open open, ByteBuf payload, int channel, AMQPTestDriver context) {
        doVerification(open, payload, channel, context);
    }

    @Override
    public void handleBegin(Begin begin, ByteBuf payload, int channel, AMQPTestDriver context) {
        doVerification(begin, payload, channel, context);
    }

    @Override
    public void handleAttach(Attach attach, ByteBuf payload, int channel, AMQPTestDriver context) {
        doVerification(attach, payload, channel, context);
    }

    @Override
    public void handleFlow(Flow flow, ByteBuf payload, int channel, AMQPTestDriver context) {
        doVerification(flow, payload, channel, context);
    }

    @Override
    public void handleTransfer(Transfer transfer, ByteBuf payload, int channel,AMQPTestDriver context) {
        doVerification(transfer, payload, channel, context);
    }

    @Override
    public void handleDisposition(Disposition disposition, ByteBuf payload, int channel, AMQPTestDriver context) {
        doVerification(disposition, payload, channel, context);
    }

    @Override
    public void handleDetach(Detach detach, ByteBuf payload, int channel, AMQPTestDriver context) {
        doVerification(detach, payload, channel, context);
    }

    @Override
    public void handleEnd(End end, ByteBuf payload, int channel, AMQPTestDriver context) {
        doVerification(end, payload, channel, context);
    }

    @Override
    public void handleClose(Close close, ByteBuf payload, int channel, AMQPTestDriver context) {
        doVerification(close, payload, channel, context);
    }

    @Override
    public void handleHeartBeat(HeartBeat thump, ByteBuf payload, int channel, AMQPTestDriver context) {
        doVerification(thump, payload, channel, context);
    }

    @Override
    public void handleMechanisms(SaslMechanisms saslMechanisms, AMQPTestDriver context) {
        doVerification(saslMechanisms, null, 0, context);
    }

    @Override
    public void handleInit(SaslInit saslInit, AMQPTestDriver context) {
        doVerification(saslInit, null, 0, context);
    }

    @Override
    public void handleChallenge(SaslChallenge saslChallenge, AMQPTestDriver context) {
        doVerification(saslChallenge, null, 0, context);
    }

    @Override
    public void handleResponse(SaslResponse saslResponse, AMQPTestDriver context) {
        doVerification(saslResponse, null, 0, context);
    }

    @Override
    public void handleOutcome(SaslOutcome saslOutcome, AMQPTestDriver context) {
        doVerification(saslOutcome, null, 0, context);
    }

    @Override
    public void handleAMQPHeader(AMQPHeader header, AMQPTestDriver context) {
        doVerification(header, null, 0, context);
    }

    @Override
    public void handleSASLHeader(AMQPHeader header, AMQPTestDriver context) {
        doVerification(header, null, 0, context);
    }

    //----- Internal implementation

    private void doVerification(Object performative, ByteBuf payload, int channel, AMQPTestDriver driver) {
        if (getExpectedTypeClass().equals(performative.getClass())) {
            verifyPayload(payload);
            verifyChannel(channel);
            verifyPerformative(getExpectedTypeClass().cast(performative));
        } else {
            reportTypeExpectationError(performative, getExpectedTypeClass());
        }
    }

    private void reportTypeExpectationError(Object received, Class<T> expected) {
        throw new UnexpectedPerformativeError("Expeceted type: " + expected + " but received value: " + received);
    }
}
