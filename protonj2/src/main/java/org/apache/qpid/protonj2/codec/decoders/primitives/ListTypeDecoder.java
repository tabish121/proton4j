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
package org.apache.qpid.protonj2.codec.decoders.primitives;

import java.io.InputStream;
import java.util.List;

import org.apache.qpid.protonj2.buffer.ProtonBuffer;
import org.apache.qpid.protonj2.codec.DecodeException;
import org.apache.qpid.protonj2.codec.decoders.PrimitiveTypeDecoder;

/**
 * Base class for List type decoders.
 */
@SuppressWarnings("rawtypes")
public interface ListTypeDecoder extends PrimitiveTypeDecoder<List> {

    int readSize(ProtonBuffer buffer) throws DecodeException;

    int readCount(ProtonBuffer buffer) throws DecodeException;

    int readSize(InputStream stream) throws DecodeException;

    int readCount(InputStream stream) throws DecodeException;

    @Override
    default Class<List> getTypeClass() {
        return List.class;
    }
}