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
package org.apache.qpid.protonj2.test.driver.actions;

import java.util.Map;

import org.apache.qpid.protonj2.test.driver.AMQPTestDriver;
import org.apache.qpid.protonj2.test.driver.codec.primitives.Symbol;
import org.apache.qpid.protonj2.test.driver.codec.transport.Close;
import org.apache.qpid.protonj2.test.driver.codec.transport.ErrorCondition;
import org.apache.qpid.protonj2.test.driver.codec.util.TypeMapper;

/**
 * AMQP Close injection action which can be added to a driver for write at a specific time or
 * following on from some other action in the test script.
 */
public class CloseInjectAction extends AbstractPerformativeInjectAction<Close> {

    private final Close close = new Close();

    public CloseInjectAction(AMQPTestDriver driver) {
        super(driver);
    }

    @Override
    public Close getPerformative() {
        return close;
    }

    public CloseInjectAction withErrorCondition(ErrorCondition error) {
        close.setError(error);
        return this;
    }

    public CloseInjectAction withErrorCondition(String condition, String description) {
        close.setError(new ErrorCondition(Symbol.valueOf(condition), description));
        return this;
    }

    public CloseInjectAction withErrorCondition(Symbol condition, String description) {
        close.setError(new ErrorCondition(condition, description));
        return this;
    }

    public CloseInjectAction withErrorCondition(String condition, String description, Map<String, Object> info) {
        close.setError(new ErrorCondition(Symbol.valueOf(condition), description, TypeMapper.toSymbolKeyedMap(info)));
        return this;
    }

    public CloseInjectAction withErrorCondition(Symbol condition, String description, Map<Symbol, Object> info) {
        close.setError(new ErrorCondition(condition, description, info));
        return this;
    }

    @Override
    protected void beforeActionPerformed(AMQPTestDriver driver) {
        // We fill in a channel using the next available channel id if one isn't set, then
        // report the outbound begin to the session so it can track this new session.
        if (onChannel() == CHANNEL_UNSET) {
            onChannel(0);
        }
    }
}
