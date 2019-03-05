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
package org.apache.qpid.proton4j.engine.test.basictypes;

import org.apache.qpid.proton4j.amqp.Symbol;

public class TerminusExpiryPolicy {

    public static final Symbol LINK_DETACH = Symbol.valueOf("link-detach");
    public static final Symbol SESSION_END = Symbol.valueOf("session-end");
    public static final Symbol CONNECTION_CLOSE = Symbol.valueOf("connection-close");
    public static final Symbol NEVER = Symbol.valueOf("never");

    private TerminusExpiryPolicy() {
        // No instances
    }
}