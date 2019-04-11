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
package org.apache.qpid.proton4j.amqp.driver.codec.transport;

import java.util.List;
import java.util.Map;

import org.apache.qpid.proton4j.amqp.Symbol;
import org.apache.qpid.proton4j.amqp.UnsignedLong;
import org.apache.qpid.proton4j.amqp.driver.codec.ListDescribedType;

public class ErrorCondition extends ListDescribedType {

    public static final Symbol DESCRIPTOR_SYMBOL = Symbol.valueOf("amqp:error:list");
    public static final UnsignedLong DESCRIPTOR_CODE = UnsignedLong.valueOf(0x000000000000001dL);

    /**
     * Enumeration which maps to fields in the ErrorCondition Performative
     */
    public enum Field {
        CONDITION,
        DESCRIPTION,
        INFO
    }

    public ErrorCondition() {
        super(Field.values().length);
    }

    @SuppressWarnings("unchecked")
    public ErrorCondition(Object described) {
        super(Field.values().length, (List<Object>) described);
    }

    public ErrorCondition(List<Object> described) {
        super(Field.values().length, described);
    }

    @Override
    public Symbol getDescriptor() {
        return DESCRIPTOR_SYMBOL;
    }

    public ErrorCondition setCondition(Symbol o) {
        getList().set(Field.CONDITION.ordinal(), o);
        return this;
    }

    public Symbol getCondition() {
        return (Symbol) getList().get(Field.CONDITION.ordinal());
    }

    public ErrorCondition setDescription(String o) {
        getList().set(Field.DESCRIPTION.ordinal(), o);
        return this;
    }

    public String getDescription() {
        return (String) getList().get(Field.DESCRIPTION.ordinal());
    }

    public ErrorCondition setInfo(Map<Object, Object> o) {
        getList().set(Field.INFO.ordinal(), o);
        return this;
    }

    @SuppressWarnings("unchecked")
    public Map<Object, Object> getInfo() {
        return (Map<Object, Object>) getList().get(Field.INFO.ordinal());
    }
}