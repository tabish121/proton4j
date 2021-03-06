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
import org.apache.qpid.protonj2.test.driver.codec.primitives.Binary;
import org.apache.qpid.protonj2.test.driver.codec.security.SaslChallenge;

/**
 * AMQP SaslChallenge injection action which can be added to a driver for write at a specific time or
 * following on from some other action in the test script.
 */
public class SaslChallengeInjectAction extends AbstractSaslPerformativeInjectAction<SaslChallenge> {

    private final SaslChallenge saslChallenge = new SaslChallenge();

    public SaslChallengeInjectAction(AMQPTestDriver driver) {
        super(driver);
    }

    public SaslChallengeInjectAction withChallenge(byte[] challenge) {
        saslChallenge.setChallenge(new Binary(challenge));
        return this;
    }

    public SaslChallengeInjectAction withChallenge(Binary challenge) {
        saslChallenge.setChallenge(challenge);
        return this;
    }

    @Override
    public SaslChallenge getPerformative() {
        return saslChallenge;
    }
}
