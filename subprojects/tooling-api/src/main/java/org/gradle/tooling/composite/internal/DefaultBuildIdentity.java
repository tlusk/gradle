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

package org.gradle.tooling.composite.internal;

import org.gradle.tooling.composite.BuildIdentity;

import java.io.File;

public class DefaultBuildIdentity implements BuildIdentity {
    private final File rootDir;

    public DefaultBuildIdentity(File rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public String toString() {
        return String.format("build=%s", rootDir.getAbsolutePath());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultBuildIdentity that = (DefaultBuildIdentity) o;

        return rootDir.equals(that.rootDir);

    }

    @Override
    public int hashCode() {
        return rootDir.hashCode();
    }
}
