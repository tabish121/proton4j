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

public class Detach extends ListDescribedType {

    public static final Symbol DESCRIPTOR_SYMBOL = Symbol.valueOf("amqp:detach:list");
    public static final UnsignedLong DESCRIPTOR_CODE = UnsignedLong.valueOf(0x0000000000000016L);

    /**
     * Enumeration which maps to fields in the Detach Performative
     */
    public enum Field {
        HANDLE,
        CLOSED,
        ERROR
    }

    public Detach() {
        super(Field.values().length);
    }

    @SuppressWarnings("unchecked")
    public Detach(Object described) {
        super(Field.values().length, (List<Object>) described);
    }

    public Detach(List<Object> described) {
        super(Field.values().length, described);
    }

    @Override
    public UnsignedLong getDescriptor() {
        return DESCRIPTOR_CODE;
    }

    public Detach setHandle(Object o) {
        getList().set(Field.HANDLE.ordinal(), o);
        return this;
    }

    public Object getHandle() {
        return getList().get(Field.HANDLE.ordinal());
    }

    public Detach setClosed(Object o) {
        getList().set(Field.CLOSED.ordinal(), o);
        return this;
    }

    public Object getClosed() {
        return getList().get(Field.CLOSED.ordinal());
    }

    public Detach setError(Object o) {
        getList().set(Field.ERROR.ordinal(), o);
        return this;
    }

    public Object getError() {
        return getList().get(Field.ERROR.ordinal());
    }
}