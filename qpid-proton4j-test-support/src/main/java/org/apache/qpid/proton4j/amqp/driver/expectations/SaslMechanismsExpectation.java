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
package org.apache.qpid.proton4j.amqp.driver.expectations;

import static org.hamcrest.CoreMatchers.equalTo;

import org.apache.qpid.proton4j.amqp.driver.AMQPTestDriver;
import org.apache.qpid.proton4j.amqp.driver.codec.ListDescribedType;
import org.apache.qpid.proton4j.amqp.driver.codec.security.SaslMechanisms;
import org.apache.qpid.proton4j.amqp.driver.matchers.security.SaslMechanismsMatcher;
import org.apache.qpid.proton4j.types.Symbol;
import org.hamcrest.Matcher;

/**
 * Scripted expectation for the AMQP SaslMechanisms performative
 */
public class SaslMechanismsExpectation extends AbstractExpectation<SaslMechanisms> {

    private final SaslMechanismsMatcher matcher = new SaslMechanismsMatcher();

    public SaslMechanismsExpectation(AMQPTestDriver driver) {
        super(driver);
    }

    //----- Type specific with methods that perform simple equals checks

    public SaslMechanismsExpectation withSaslServerMechanisms(String... mechanisms) {
        Symbol[] expected = null;

        if (mechanisms != null) {
            expected = new Symbol[mechanisms.length];
            for (int i = 0; i < mechanisms.length; ++i) {
                expected[i] = Symbol.valueOf(mechanisms[i]);
            }
        }

        return withSaslServerMechanisms(equalTo(expected));
    }

    public SaslMechanismsExpectation withSaslServerMechanisms(Symbol... mechanisms) {
        return withSaslServerMechanisms(equalTo(mechanisms));
    }

    //----- Matcher based with methods for more complex validation

    public SaslMechanismsExpectation withSaslServerMechanisms(Matcher<?> m) {
        matcher.addFieldMatcher(SaslMechanisms.Field.SASL_SERVER_MECHANISMS, m);
        return this;
    }

    @Override
    protected Matcher<ListDescribedType> getExpectationMatcher() {
        return matcher;
    }

    @Override
    protected Object getFieldValue(SaslMechanisms saslMechanisms, Enum<?> performativeField) {
        return saslMechanisms.getFieldValue(performativeField.ordinal());
    }

    @Override
    protected Enum<?> getFieldEnum(int fieldIndex) {
        return SaslMechanisms.Field.values()[fieldIndex];
    }

    @Override
    protected Class<SaslMechanisms> getExpectedTypeClass() {
        return SaslMechanisms.class;
    }
}
