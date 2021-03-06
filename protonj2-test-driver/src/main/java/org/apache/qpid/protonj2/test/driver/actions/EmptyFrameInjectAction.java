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

import org.apache.qpid.protonj2.test.driver.AMQPTestDriver;
import org.apache.qpid.protonj2.test.driver.ScriptedAction;

/**
 * AMQP Empty Frame injection action which can be added to a driver for write at a specific time or
 * following on from some other action in the test script.
 */
public class EmptyFrameInjectAction implements ScriptedAction {

    public static final int CHANNEL_UNSET = -1;

    private int channel = CHANNEL_UNSET;

    private final AMQPTestDriver driver;

    public EmptyFrameInjectAction(AMQPTestDriver driver) {
        this.driver = driver;
    }

    public int onChannel() {
        return this.channel;
    }

    @Override
    public EmptyFrameInjectAction perform(AMQPTestDriver driver) {
        driver.sendEmptyFrame(this.channel == CHANNEL_UNSET ? 0 : this.channel);
        return this;
    }

    @Override
    public EmptyFrameInjectAction now() {
        perform(driver);
        return this;
    }

    @Override
    public EmptyFrameInjectAction later(int delay) {
        driver.afterDelay(delay, this);
        return this;
    }

    @Override
    public EmptyFrameInjectAction queue() {
        driver.addScriptedElement(this);
        return this;
    }
}
