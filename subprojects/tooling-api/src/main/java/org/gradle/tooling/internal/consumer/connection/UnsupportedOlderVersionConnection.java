/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.tooling.internal.consumer.connection;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.UnsupportedVersionException;
import org.gradle.tooling.internal.adapter.ProtocolToModelAdapter;
import org.gradle.tooling.internal.build.VersionOnlyBuildEnvironment;
import org.gradle.tooling.internal.consumer.Distribution;
import org.gradle.tooling.internal.consumer.TestExecutionRequest;
import org.gradle.tooling.internal.consumer.parameters.ConsumerOperationParameters;
import org.gradle.tooling.internal.protocol.ConnectionVersion4;
import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.internal.Exceptions;

import java.util.Set;

/**
 * An adapter for unsupported connection using a {@code ConnectionVersion4} based provider.
 *
 * <p>Used for providers >= 1.0-milestone-3 and <= 1.0-milestone-7.</p>
 */
public class UnsupportedOlderVersionConnection implements ConsumerConnection {
    private final Distribution distribution;
    private final ProtocolToModelAdapter adapter;
    private final String version;

    public UnsupportedOlderVersionConnection(Distribution distribution, ConnectionVersion4 delegate, ProtocolToModelAdapter adapter) {
        this.distribution = distribution;
        this.adapter = adapter;
        version = delegate.getMetaData().getVersion();
    }

    public void stop() {
    }

    public String getDisplayName() {
        return distribution.getDisplayName();
    }

    public <T> T run(Class<T> type, ConsumerOperationParameters operationParameters) throws UnsupportedOperationException, IllegalStateException {
        if (type.equals(BuildEnvironment.class)) {
            return adapter.adapt(type, doGetBuildEnvironment());
        }
        throw fail();
    }

    private Object doGetBuildEnvironment() {
        return new VersionOnlyBuildEnvironment(version);
    }

    public <T> T run(BuildAction<T> action, ConsumerOperationParameters operationParameters) throws UnsupportedOperationException, IllegalStateException {
        return new UnsupportedActionRunner(version).run(action, operationParameters);
    }

    public void runTests(TestExecutionRequest testExecutionRequest, ConsumerOperationParameters operationParameters) {
        throw Exceptions.unsupportedFeature(operationParameters.getEntryPointName(), version, "2.6");
    }

    private UnsupportedVersionException fail() {
        return new UnsupportedVersionException(String.format("Support for Gradle version %s was removed in tooling API version 2.0. You should upgrade your Gradle build to use Gradle 1.0-milestone-8 or later.", version));
    }

    @Override
    public <T> Set<T> buildModels(Class<T> elementType, ConsumerOperationParameters operationParameters) throws UnsupportedOperationException, IllegalStateException {
        throw Exceptions.unsupportedFeature(operationParameters.getEntryPointName(), version, "2.13");
    }
}
