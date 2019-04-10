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
package org.apache.qpid.proton4j.amqp.driver.codec.types;

import java.util.List;

import org.apache.qpid.proton4j.amqp.Symbol;
import org.apache.qpid.proton4j.amqp.UnsignedLong;
import org.apache.qpid.proton4j.amqp.driver.codec.ListDescribedType;

public class SaslInit extends ListDescribedType {

    public static final Symbol DESCRIPTOR_SYMBOL = Symbol.valueOf("amqp:sasl-init:list");
    public static final UnsignedLong DESCRIPTOR_CODE = UnsignedLong.valueOf(0x0000000000000041L);

    /**
     * Enumeration which maps to fields in the SaslInit Performative
     */
    public enum Field {
        MECHANISM,
        INITIAL_RESPONSE,
        HOSTNAME
    }

    public SaslInit() {
        super(Field.values().length);
    }

    @SuppressWarnings("unchecked")
    public SaslInit(Object described) {
        super(Field.values().length, (List<Object>) described);
    }

    public SaslInit(List<Object> described) {
        super(Field.values().length, described);
    }

    @Override
    public Symbol getDescriptor() {
        return DESCRIPTOR_SYMBOL;
    }

    public SaslInit setMechanism(Object o) {
        getList().set(Field.MECHANISM.ordinal(), o);
        return this;
    }

    public Object getMechanism() {
        return getList().get(Field.MECHANISM.ordinal());
    }

    public SaslInit setInitialResponse(Object o) {
        getList().set(Field.INITIAL_RESPONSE.ordinal(), o);
        return this;
    }

    public Object getInitialResponse() {
        return getList().get(Field.INITIAL_RESPONSE.ordinal());
    }

    public SaslInit setHostname(Object o) {
        getList().set(Field.HOSTNAME.ordinal(), o);
        return this;
    }

    public Object getHostname() {
        return getList().get(Field.HOSTNAME.ordinal());
    }
}