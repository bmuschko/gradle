/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.api.tasks.diagnostics.internal;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import org.gradle.api.Task;
import org.gradle.api.internal.DirectedGraph;
import org.gradle.api.internal.GraphAggregator;
import org.gradle.util.GUtil;

import java.util.*;

public class SingleProjectTaskReportModel implements TaskReportModel {
    private final SetMultimap<String, TaskDetails> groups = TreeMultimap.create(new Comparator<String>() {
        public int compare(String string1, String string2) {
            return string1.compareToIgnoreCase(string2);
        }
    }, new Comparator<TaskDetails>() {
        public int compare(TaskDetails task1, TaskDetails task2) {
            return task1.getPath().compareTo(task2.getPath());
        }
    });
    private final TaskDetailsFactory factory;

    public SingleProjectTaskReportModel(TaskDetailsFactory factory) {
        this.factory = factory;
    }

    public void build(final Collection<? extends Task> tasks) {
        Set<Task> topLevelTasks = new LinkedHashSet<Task>();
        for (final Task task : tasks) {
            if (GUtil.isTrue(task.getGroup())) {
                topLevelTasks.add(task);
            }
        }
        GraphAggregator<Task> aggregator = new GraphAggregator<Task>(new DirectedGraph<Task, Object>() {
            public void getNodeValues(Task node, Collection<Object> values, Collection<Task> connectedNodes) {
                for (Task dep : node.getTaskDependencies().getDependencies(node)) {
                    if (tasks.contains(dep)) {
                        connectedNodes.add(dep);
                    }
                }
            }
        });

        GraphAggregator.Result<Task> result = aggregator.group(topLevelTasks, tasks);
        for (Task task : result.getTopLevelNodes()) {
            Set<Task> nodesForThisTask = new TreeSet<Task>(result.getNodes(task));
            Set<TaskDetails> children = new LinkedHashSet<TaskDetails>();
            Set<String> dependencies = new TreeSet<String>();
            for (Task node : nodesForThisTask) {
                if (node != task) {
                    children.add(new TaskDetailsImpl(node, factory.create(node), Collections.<TaskDetails>emptySet(),
                            Collections.<String>emptySet()));
                }
                for (Task dep : node.getTaskDependencies().getDependencies(node)) {
                    if (topLevelTasks.contains(dep) || !tasks.contains(dep)) {
                        dependencies.add(tasks.contains(dep) ? dep.getName() : dep.getPath());
                    }
                }
            }

            String group = topLevelTasks.contains(task) ? task.getGroup() : DEFAULT_GROUP;
            groups.put(group, new TaskDetailsImpl(task, factory.create(task), children, dependencies));
        }
    }

    public Set<String> getGroups() {
        return groups.keySet();
    }

    public Set<TaskDetails> getTasksForGroup(String group) {
        if (!groups.containsKey(group)) {
            throw new IllegalArgumentException(String.format("Unknown group '%s'", group));
        }
        return groups.get(group);
    }

    private static class TaskDetailsImpl implements TaskDetails {
        private final Task task;
        private final TaskDetails details;
        private final Set<TaskDetails> children;
        private final Set<String> dependencies;

        public TaskDetailsImpl(Task task, TaskDetails details, Set<TaskDetails> children, Set<String> dependencies) {
            this.task = task;
            this.details = details;
            this.children = children;
            this.dependencies = dependencies;
        }

        public String getPath() {
            return details.getPath();
        }

        public String getName() {
            return details.getName();
        }

        public String getDescription() {
            return details.getDescription();
        }

        @Override
        public String toString() {
            return task.toString();
        }

        public Task getTask() {
            return task;
        }

        public Set<String> getDependencies() {
            return dependencies;
        }

        public Set<TaskDetails> getChildren() {
            return children;
        }
    }
}
