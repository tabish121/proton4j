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
package org.apache.qpid.proton4j.engine.impl;

import org.apache.qpid.proton4j.amqp.transport.Attach;
import org.apache.qpid.proton4j.amqp.transport.Flow;
import org.apache.qpid.proton4j.amqp.transport.Role;
import org.apache.qpid.proton4j.engine.LinkCreditState;
import org.apache.qpid.proton4j.engine.Session;

/**
 * Base for link credit state classes.
 */
public abstract class ProtonLinkCreditState implements LinkCreditState {

    protected final ProtonSessionWindow sessionWindow;

    protected long remoteDeliveryCount;
    protected long remoteLinkCredit;

    /**
     * @param sessionWindow
     *    The credit window of the parent {@link Session}
     */
    public ProtonLinkCreditState(ProtonSessionWindow sessionWindow) {
        this.sessionWindow = sessionWindow;
    }

    @Override
    public int getCredit() {
        return 0;
    }

    /**
     * Creates a snapshot of the current credit state, a subclass should implement this
     * method and provide an appropriately populated snapshot of the current state.
     *
     * @return a snapshot of the current credit state.
     */
    abstract ProtonLinkCreditState snapshot();

    /**
     * Initialize link state on an outbound Attach for this link
     *
     * @param attach
     *      the {@link Attach} performative that will be sent.
     *
     * @return the attach object for chaining
     */
    Attach configureOutbound(Attach attach) {
        if (attach.getRole() == Role.SENDER) {
            attach.setInitialDeliveryCount(0);
        }
        return attach;
    }

    /**
     * Perform any needed initialization for link credit based on the initial Attach
     * sent from the remote
     *
     * @param attach
     *
     *
     * @return the attach for chaining.
     */
    Attach processInboud(Attach attach) {
        remoteDeliveryCount = attach.getInitialDeliveryCount();
        return attach;
    }

    /**
     * Handle incoming {@link Flow} performatives and update link credit accordingly.
     *
     * @param flow
     *      The {@link Flow} instance to be processed.
     *
     * @return the passed object for chaining.
     */
    Flow handleFlow(Flow flow) {
        remoteDeliveryCount = flow.getDeliveryCount();
        remoteLinkCredit = flow.getLinkCredit();
        return flow;
    }
}