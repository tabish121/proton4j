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
package org.apache.qpid.protonj2.engine.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.protonj2.engine.Attachments;

/**
 * Proton implementation of an Attachments object.
 */
public class ProtonAttachments implements Attachments {

    private final Map<Object, Object> contextMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return contextMap == null ? null : (T) contextMap.get(key);
    }

    @Override
    public <T> T get(String key, Class<T> typeClass) {
        return typeClass.cast(contextMap.get(key));
    }

    @Override
    public ProtonAttachments set(String key, Object value) {
        contextMap.put(key, value);
        return this;
    }

    @Override
    public boolean containsKey(String key) {
        return contextMap.containsKey(key);
    }

    @Override
    public Attachments clear() {
        if (contextMap != null) {
            contextMap.clear();
        }

        return this;
    }
}
