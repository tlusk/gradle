/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.tooling.composite.internal
import org.gradle.tooling.composite.GradleConnection
import org.gradle.tooling.internal.consumer.CompositeConnectionParameters
import org.gradle.tooling.internal.consumer.async.AsyncConsumerActionExecutor
import org.gradle.tooling.model.eclipse.EclipseProject
import spock.lang.Specification

class DefaultGradleConnectionTest extends Specification {

    AsyncConsumerActionExecutor actionExecutor = Mock()
    CompositeConnectionParameters connectionParameters = Mock()
    GradleConnection connection = new DefaultGradleConnection(actionExecutor, connectionParameters)

    def "can get model builder"() {
        expect:
        connection.models(EclipseProject) instanceof ModelResultCompositeModelBuilder
    }

    def "close stops all underlying connections"() {
        when:
        connection.close()
        then:
        actionExecutor.stop()
    }

    def "errors propagate to caller when closing connection"() {
        given:
        actionExecutor.stop() >> { throw new RuntimeException() }
        when:
        connection.close()
        then:
        thrown(RuntimeException)
    }
}
