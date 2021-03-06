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
package org.gradle.api.internal.artifacts;

import org.gradle.api.internal.DomainObjectContext;
import org.gradle.api.internal.artifacts.configurations.DependencyMetaDataProvider;
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.project.ServiceRegistry;

/**
 * Factory for various types related to dependency management.
 *
 * <p>The motivation for having this factory is to allow implementation
 * types, and more importantly their dependencies, to be loaded from a
 * different class loader. This helps to prevent version conflicts,
 * for example between Maven 2 and Maven 3 libraries.
 */
public interface DependencyManagementServices extends ServiceRegistry {
    DependencyResolutionServices create(FileResolver resolver, DependencyMetaDataProvider dependencyMetaDataProvider,
                                        ProjectFinder projectFinder, DomainObjectContext domainObjectContext);
}
