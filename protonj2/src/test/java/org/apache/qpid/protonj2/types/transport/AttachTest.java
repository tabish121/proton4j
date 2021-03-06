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
package org.apache.qpid.protonj2.types.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.qpid.protonj2.types.UnsignedInteger;
import org.apache.qpid.protonj2.types.messaging.Target;
import org.apache.qpid.protonj2.types.transactions.Coordinator;
import org.junit.jupiter.api.Test;

public class AttachTest {

    @Test
    public void testGetPerformativeType() {
        assertEquals(Performative.PerformativeType.ATTACH, new Attach().getPerformativeType());
    }

    @Test
    public void testToStringOnFreshInstance() {
        assertNotNull(new Attach().toString());
    }

    @Test
    public void testInitialState() {
        Attach attach = new Attach();

        assertEquals(0, attach.getElementCount());
        assertTrue(attach.isEmpty());
        assertFalse(attach.hasDesiredCapabilites());
        assertFalse(attach.hasHandle());
        assertFalse(attach.hasIncompleteUnsettled());
        assertFalse(attach.hasInitialDeliveryCount());
        assertFalse(attach.hasMaxMessageSize());
        assertFalse(attach.hasName());
        assertFalse(attach.hasOfferedCapabilites());
        assertFalse(attach.hasProperties());
        assertFalse(attach.hasReceiverSettleMode());
        assertFalse(attach.hasRole());
        assertFalse(attach.hasSenderSettleMode());
        assertFalse(attach.hasSource());
        assertFalse(attach.hasTarget());
    }

    @Test
    public void testIsEmpty() {
        Attach attach = new Attach();

        assertEquals(0, attach.getElementCount());
        assertTrue(attach.isEmpty());
        assertFalse(attach.hasHandle());

        attach.setHandle(0);

        assertTrue(attach.getElementCount() > 0);
        assertFalse(attach.isEmpty());
        assertTrue(attach.hasHandle());

        attach.setHandle(1);

        assertTrue(attach.getElementCount() > 0);
        assertFalse(attach.isEmpty());
        assertTrue(attach.hasHandle());
    }

    @Test
    public void testSetNameRefusesNull() {
        try {
            new Attach().setName(null);
            fail("Link name is mandatory");
        } catch (NullPointerException npe) {
        }
    }

    @Test
    public void testSetRoleRefusesNull() {
        try {
            new Attach().setRole(null);
            fail("Link role is mandatory");
        } catch (NullPointerException npe) {
        }
    }

    @Test
    public void testHandleRangeChecks() {
        Attach attach = new Attach();
        try {
            attach.setHandle(-1);
            fail("Cannot set negative long handle value");
        } catch (IllegalArgumentException iae) {}

        try {
            attach.setHandle(UnsignedInteger.MAX_VALUE.longValue() + 1);
            fail("Cannot set long handle value bigger than uint max");
        } catch (IllegalArgumentException iae) {}
    }

    @Test
    public void testDeliveryCountRangeChecks() {
        Attach attach = new Attach();
        try {
            attach.setInitialDeliveryCount(-1);
            fail("Cannot set negative long delivery count value");
        } catch (IllegalArgumentException iae) {}

        try {
            attach.setInitialDeliveryCount(UnsignedInteger.MAX_VALUE.longValue() + 1);
            fail("Cannot set long delivery count value bigger than uint max");
        } catch (IllegalArgumentException iae) {}
    }

    @Test
    public void testHasTargetOrCoordinator() {
        Attach attach = new Attach();

        assertFalse(attach.hasCoordinator());
        assertFalse(attach.hasTarget());
        assertFalse(attach.hasTargetOrCoordinator());

        attach.setTarget(new Target());

        assertFalse(attach.hasCoordinator());
        assertTrue(attach.hasTarget());
        assertTrue(attach.hasTargetOrCoordinator());

        attach.setTarget(new Coordinator());

        assertTrue(attach.hasCoordinator());
        assertFalse(attach.hasTarget());
        assertTrue(attach.hasTargetOrCoordinator());

        attach.setTarget((Target) null);

        assertFalse(attach.hasCoordinator());
        assertFalse(attach.hasTarget());
        assertFalse(attach.hasTargetOrCoordinator());

        attach.setCoordinator(new Coordinator());

        assertTrue(attach.hasCoordinator());
        assertFalse(attach.hasTarget());
        assertTrue(attach.hasTargetOrCoordinator());
    }

    @Test
    public void testCopyAttachWithTarget() {
        Attach original = new Attach();

        original.setTarget(new Target());

        Attach copy = original.copy();

        assertNotNull(copy.getTarget());
        assertEquals(original.<Target>getTarget(), copy.<Target>getTarget());
    }

    @Test
    public void testCopyAttachWithCoordinator() {
        Attach original = new Attach();

        original.setCoordinator(new Coordinator());

        Attach copy = original.copy();

        assertNotNull(copy.getTarget());
        assertEquals(original.<Coordinator>getTarget(), copy.<Coordinator>getTarget(), "Should be equal");

        Coordinator coordinator = copy.getTarget();

        assertNotNull(coordinator);
        assertEquals(original.getTarget(), coordinator);
    }

    @Test
    public void testCopyFromNew() {
        Attach original = new Attach();
        Attach copy = original.copy();

        assertTrue(original.isEmpty());
        assertTrue(copy.isEmpty());

        assertEquals(0, original.getElementCount());
        assertEquals(0, copy.getElementCount());
    }
}
