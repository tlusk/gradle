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

package org.gradle.tooling.internal.consumer;

import org.gradle.api.JavaVersion;
import org.gradle.internal.Factory;
import org.gradle.internal.concurrent.DefaultExecutorFactory;
import org.gradle.internal.concurrent.ExecutorFactory;
import org.gradle.internal.jvm.UnsupportedJavaRuntimeException;
import org.gradle.internal.service.DefaultServiceRegistry;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.composite.GradleBuild;
import org.gradle.tooling.composite.GradleConnection;
import org.gradle.tooling.composite.internal.DefaultGradleBuildBuilder;
import org.gradle.tooling.composite.internal.DefaultGradleConnectionBuilder;
import org.gradle.tooling.composite.internal.GradleConnectionFactory;
import org.gradle.tooling.internal.consumer.loader.CachingToolingImplementationLoader;
import org.gradle.tooling.internal.consumer.loader.DefaultToolingImplementationLoader;
import org.gradle.tooling.internal.consumer.loader.SynchronizedToolingImplementationLoader;
import org.gradle.tooling.internal.consumer.loader.ToolingImplementationLoader;

public class ConnectorServices {
    private static DefaultServiceRegistry singletonRegistry = new ConnectorServiceRegistry();

    public static DefaultGradleConnector createConnector() {
        assertJava6();
        return singletonRegistry.getFactory(DefaultGradleConnector.class).create();
    }

    public static GradleConnection.Builder createGradleConnectionBuilder() {
        assertJava6();
        return singletonRegistry.getFactory(GradleConnection.Builder.class).create();
    }

    public static GradleBuild.Builder createGradleBuildBuilder() {
        assertJava6();
        return singletonRegistry.getFactory(GradleBuild.Builder.class).create();
    }

    public static CancellationTokenSource createCancellationTokenSource() {
        assertJava6();
        return new DefaultCancellationTokenSource();
    }

    public static void close() {
        assertJava6();
        singletonRegistry.close();
    }

    /**
     * Resets the state of connector services. Meant to be used only for testing!
     */
    public static void reset() {
        singletonRegistry.close();
        singletonRegistry = new ConnectorServiceRegistry();
    }

    private static void assertJava6() {
        JavaVersion javaVersion = JavaVersion.current();
        if (!javaVersion.isJava6Compatible()) {
            throw UnsupportedJavaRuntimeException.usingUnsupportedVersion("Gradle Tooling API", JavaVersion.VERSION_1_6);
        }
    }

    private static class ConnectorServiceRegistry extends DefaultServiceRegistry {
        protected Factory<DefaultGradleConnector> createConnectorFactory(final ConnectionFactory connectionFactory, final DistributionFactory distributionFactory) {
            return new Factory<DefaultGradleConnector>() {
                public DefaultGradleConnector create() {
                    return new DefaultGradleConnector(connectionFactory, distributionFactory);
                }
            };
        }

        protected Factory<GradleConnection.Builder> createGradleConnectionBuilder(final GradleConnectionFactory gradleConnectionFactory, final DistributionFactory distributionFactory) {
            return new Factory<GradleConnection.Builder>() {
                public GradleConnection.Builder create() {
                    return new DefaultGradleConnectionBuilder(gradleConnectionFactory, distributionFactory);
                }
            };
        }

        protected Factory<GradleBuild.Builder> createGradleBuildBuilder() {
            return new Factory<GradleBuild.Builder>() {
                public GradleBuild.Builder create() {
                    return new DefaultGradleBuildBuilder();
                }
            };
        }

        protected ExecutorFactory createExecutorFactory() {
            return new DefaultExecutorFactory();
        }

        protected ExecutorServiceFactory createExecutorServiceFactory() {
            return new DefaultExecutorServiceFactory();
        }

        protected DistributionFactory createDistributionFactory(ExecutorServiceFactory executorFactory) {
            return new DistributionFactory(executorFactory);
        }

        protected ToolingImplementationLoader createToolingImplementationLoader() {
            return new SynchronizedToolingImplementationLoader(new CachingToolingImplementationLoader(new DefaultToolingImplementationLoader()));
        }

        protected LoggingProvider createLoggingProvider() {
            return new SynchronizedLogging();
        }

        protected ConnectionFactory createConnectionFactory(ToolingImplementationLoader toolingImplementationLoader, ExecutorFactory executorFactory, LoggingProvider loggingProvider) {
            return new ConnectionFactory(toolingImplementationLoader, executorFactory, loggingProvider);
        }

        protected GradleConnectionFactory createGradleConnectionFactory(ToolingImplementationLoader toolingImplementationLoader, ExecutorFactory executorFactory, LoggingProvider loggingProvider) {
            return new GradleConnectionFactory(toolingImplementationLoader, executorFactory, loggingProvider);
        }
    }
}
