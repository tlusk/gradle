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

package org.gradle.integtests.tooling.r213

import org.gradle.integtests.tooling.fixture.CompositeToolingApiSpecification
import org.gradle.tooling.BuildException
import org.gradle.tooling.model.eclipse.EclipseProject

/**
 * Builds a composite with a single project.
 */
class SingleProjectCompositeBuildCrossVersionSpec extends CompositeToolingApiSpecification {
    def "can create composite of a single multi-project build"() {
        given:
        def singleBuild = populate("single-build") {
            buildFile << """
                allprojects {
                    apply plugin: 'java'
                    group = 'group'
                    version = '1.0'
                }
"""
            settingsFile << """
                rootProject.name = '${rootProjectName}'
                include 'a', 'b', 'c'
"""
        }
        when:
        def models = withCompositeConnection(singleBuild) { connection ->
            unwrap(connection.getModels(EclipseProject))
        }
        then:
        models.size() == 4
        rootProjects(models).size() == 1
        containsProjects(models, [':', ':a', ':b', ':c'])
    }

    def "can create composite of a single single-project build"() {
        given:
        def singleBuild = populate("single-build") {
            buildFile << """
                apply plugin: 'java'
                group = 'group'
                version = '1.0'
"""
            settingsFile << """
                rootProject.name = '${rootProjectName}'
"""
        }
        when:
        def models = withCompositeConnection(singleBuild) { connection ->
            unwrap(connection.getModels(EclipseProject))
        }
        then:
        models.size() == 1
        rootProjects(models).size() == 1
        containsProjects(models, [':'])
    }

    def "sees changes to composite build when projects are added"() {
        given:
        def singleBuild = populate("single-build") {
            buildFile << """
                allprojects {
                    apply plugin: 'java'
                    group = 'group'
                    version = '1.0'
                }
"""
            settingsFile << """
                rootProject.name = '${rootProjectName}'
"""
        }
        def composite = createComposite(singleBuild)

        when:
        def firstRetrieval = unwrap(composite.getModels(EclipseProject))

        then:
        firstRetrieval.size() == 1
        rootProjects(firstRetrieval).size() == 1
        containsProjects(firstRetrieval, [':'])

        when:
        // make project a multi-project build
        populate("single-build") {
            settingsFile << """
                include 'a'
"""
        }
        and:
        def secondRetrieval = unwrap(composite.getModels(EclipseProject))

        then:
        secondRetrieval.size() == 2
        rootProjects(secondRetrieval).size() == 1
        containsProjects(secondRetrieval, [':', ':a'])

        when:
        // adding more projects to multi-project build
        populate("single-build") {
            settingsFile << "include 'b', 'c'"
        }
        and:
        def thirdRetrieval = unwrap(composite.getModels(EclipseProject))

        then:
        thirdRetrieval.size() == 4
        rootProjects(thirdRetrieval).size() == 1
        containsProjects(thirdRetrieval, [':', ':a', ':b', ':c'])

        when:
        // remove the existing project
        singleBuild.deleteDir()

        and:
        def fourthRetrieval = unwrap(composite.getModels(EclipseProject))

        then:
        def e = thrown(BuildException)
        def causes = getCausalChain(e)
        causes.any {
            it.message.contains("Could not fetch model of type 'EclipseProject'")
        }
        causes.any {
            it.message.contains("single-build' does not exist")
        }

        cleanup:
        composite?.close()
    }

    Iterable<EclipseProject> rootProjects(Iterable<EclipseProject> projects) {
        projects.findAll { it.parent == null }
    }

    void containsProjects(models, projects) {
        def projectsFoundByPath = models.collect { it.gradleProject.path }
        assert projectsFoundByPath.containsAll(projects)
    }
}
