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
package org.apache.qpid.proton4j.amqp.driver.actions;

import java.util.Map;

import org.apache.qpid.proton4j.amqp.Binary;
import org.apache.qpid.proton4j.amqp.Symbol;
import org.apache.qpid.proton4j.amqp.UnsignedLong;
import org.apache.qpid.proton4j.amqp.messaging.Source;
import org.apache.qpid.proton4j.amqp.messaging.Target;
import org.apache.qpid.proton4j.amqp.transport.Attach;
import org.apache.qpid.proton4j.amqp.transport.DeliveryState;
import org.apache.qpid.proton4j.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton4j.amqp.transport.Role;
import org.apache.qpid.proton4j.amqp.transport.SenderSettleMode;

/**
 * AMQP Attach injection action which can be added to a driver for write at a specific time or
 * following on from some other action in the test script.
 */
public class AttachInjectAction extends AbstractPerformativeInjectAction<Attach> {

    private final Attach attach;

    public AttachInjectAction(Attach attach) {
        this.attach = attach;
    }

    @Override
    public Attach getPerformative() {
        return attach;
    }

    public AttachInjectAction withName(String name) {
        attach.setName(name);
        return this;
    }

    public AttachInjectAction withHandle(long handle) {
        attach.setHandle(handle);
        return this;
    }

    public AttachInjectAction withRole(Role role) {
        attach.setRole(role);
        return this;
    }

    public AttachInjectAction withSndSettleMode(SenderSettleMode sndSettleMode) {
        attach.setSndSettleMode(sndSettleMode);
        return this;
    }

    public AttachInjectAction withRcvSettleMode(ReceiverSettleMode rcvSettleMode) {
        attach.setRcvSettleMode(rcvSettleMode);
        return this;
    }

    public AttachInjectAction withSource(Source source) {
        attach.setSource(source);
        return this;
    }

    public AttachInjectAction withTarget(Target target) {
        attach.setTarget(target);
        return this;
    }

    public AttachInjectAction withUnsettled(Map<Binary, DeliveryState> unsettled) {
        attach.setUnsettled(unsettled);
        return this;
    }

    public AttachInjectAction withIncompleteUnsettled(boolean incomplete) {
        attach.setIncompleteUnsettled(incomplete);
        return this;
    }

    public AttachInjectAction withInitialDeliveryCount(long initialDeliveryCount) {
        attach.setInitialDeliveryCount(initialDeliveryCount);
        return this;
    }

    public AttachInjectAction withMaxMessageSize(UnsignedLong maxMessageSize) {
        attach.setMaxMessageSize(maxMessageSize);
        return this;
    }

    public AttachInjectAction withOfferedCapabilities(Symbol... offeredCapabilities) {
        attach.setOfferedCapabilities(offeredCapabilities);
        return this;
    }

    public AttachInjectAction withDesiredCapabilities(Symbol... desiredCapabilities) {
        attach.setDesiredCapabilities(desiredCapabilities);
        return this;
    }

    public AttachInjectAction withProperties(Map<Symbol, Object> properties) {
        attach.setProperties(properties);
        return this;
    }
}