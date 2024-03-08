/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
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
package org.apache.logging.log4j.jctools;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
import org.apache.logging.log4j.spi.recycler.RecyclerFactoryProvider;
import org.apache.logging.log4j.spi.recycler.RecyclerFactoryRegistry;
import org.junit.jupiter.api.Test;

class JCToolsRecyclerFactoryProviderTest {

    @Test
    void verify_is_the_first() {
        final List<Class<?>> providerClasses = RecyclerFactoryRegistry.getRecyclerFactoryProviders().stream()
                .sorted(Comparator.comparing(RecyclerFactoryProvider::getOrder))
                .<Class<?>>map(RecyclerFactoryProvider::getClass)
                .toList();
        assertThat(providerClasses).startsWith(JCToolsRecyclerFactoryProvider.class);
    }
}