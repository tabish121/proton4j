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
import org.apache.qpid.proton4j.amqp.driver.actions.BeginInjectAction;
import org.apache.qpid.proton4j.amqp.driver.actions.CloseInjectAction;
import org.apache.qpid.proton4j.amqp.transport.Close;
import org.apache.qpid.proton4j.amqp.transport.ErrorCondition;
import org.apache.qpid.proton4j.buffer.ProtonBuffer;
import org.hamcrest.Matcher;

/**
 * Scripted expectation for the AMQP Close performative
 */
public class CloseExpectation extends AbstractExpectation<Close> {

    /**
     * Enumeration which maps to fields in the Close Performative
     */
    public enum Field {
        ERROR
    }

    private CloseInjectAction response;

    public CloseExpectation(AMQPTestDriver driver) {
        super(driver);
    }

    public CloseInjectAction respond() {
        response = new CloseInjectAction(new Close());
        driver.addScriptedElement(response);
        return response;
    }

    //----- Handle the performative and configure response is told to respond

    @Override
    public void handleClose(Close close, ProtonBuffer payload, int channel, AMQPTestDriver context) {
        super.handleClose(close, payload, channel, context);

        if (response == null) {
            return;
        }

        // Input was validated now populate response with auto values where not configured
        // to say otherwise by the test.
        if (response.onChannel() == BeginInjectAction.CHANNEL_UNSET) {
            response.onChannel(channel);
        }
    }

    //----- Type specific with methods that perform simple equals checks

    public CloseExpectation withError(ErrorCondition error) {
        return withError(equalTo(error));
    }

    //----- Matcher based with methods for more complex validation

    public CloseExpectation withError(Matcher<?> m) {
        getMatchers().put(Field.ERROR, m);
        return this;
    }

    @Override
    protected Object getFieldValue(Close close, Enum<?> performativeField) {
        Object result = null;

        if (performativeField == Field.ERROR) {
            result = close.getError();
        } else {
            throw new AssertionError("Request for unknown field in type Close");
        }

        return result;
    }

    @Override
    protected Enum<?> getFieldEnum(int fieldIndex) {
        return Field.values()[fieldIndex];
    }

    @Override
    protected Class<Close> getExpectedTypeClass() {
        return Close.class;
    }
}