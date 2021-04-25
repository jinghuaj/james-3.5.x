/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.james.modules.objectstorage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.Test;

class ObjectStorageProviderTest {

    @Test
    void fromShouldThrowWhenUnkownObjectStorage() {
        assertThatThrownBy(() -> ObjectStorageProvider.from("unknown"))
            .isInstanceOf(ConfigurationException.class)
            .hasMessage("Unknown object storage provider: unknown");
    }

    @Test
    void fromShouldMatchSwift() throws ConfigurationException {
        assertThat(ObjectStorageProvider.from("swift"))
            .isEqualTo(ObjectStorageProvider.SWIFT);
    }

    @Test
    void fromShouldMatchAwsS3() throws ConfigurationException {
        assertThat(ObjectStorageProvider.from("aws-s3"))
            .isEqualTo(ObjectStorageProvider.AWSS3);
    }
}