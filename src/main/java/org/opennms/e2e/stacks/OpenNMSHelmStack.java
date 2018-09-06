/*
 * Copyright 2016, The OpenNMS Group
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opennms.e2e.stacks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import org.opennms.e2e.grafana.GrafanaRestClient;
import org.opennms.e2e.opennms.OpenNMSRestClient;
import org.opennms.gizmo.docker.GizmoDockerStack;
import org.opennms.gizmo.docker.GizmoDockerStacker;
import org.opennms.gizmo.docker.stacks.EmptyDockerStack;
import org.opennms.gizmo.utils.HttpUtils;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.notNullValue;

public class OpenNMSHelmStack extends EmptyDockerStack {

    private static String OPENNMS = "OPENNMS";
    private static String HELM = "HELM";

    private GizmoDockerStacker stacker;

    public static final String OPENNMS_ADMIN_USER = "admin";
    public static final String OPENNMS_ADMIN_PASSWORD = "admin";

    @Override
    public Map<String, Function<GizmoDockerStacker, ContainerConfig>> getContainersByAlias() {
        return ImmutableMap.of(OPENNMS, (stacker) -> ContainerConfig.builder()
                    .image("opennms/horizon-core-web:20.0.1-1")
                    .exposedPorts("8980/tcp")
                    .env("POSTGRES_HOST=db",
                            "POSTGRES_PORT=5432",
                            "POSTGRES_USER=" + PostgreSQLStack.USERNAME,
                            "POSTGRES_PASSWORD=" + PostgreSQLStack.PASSWORD,
                            "OPENNMS_DBNAME=opennms",
                            "OPENNMS_DBUSER=opennms",
                            "OPENNMS_DBPASS=opennms")
                    .hostConfig(HostConfig.builder()
                            .publishAllPorts(true)
                            .autoRemove(true)
                            .links(String.format("%s:db", stacker.getContainerInfo(PostgreSQLStack.POSTGRES).name()))
                            .build())
                    .cmd("-s")
                    .build(),
                HELM, (stacker) -> ContainerConfig.builder()
                        .image("opennms/helm:bleeding")
                        .exposedPorts("3000/tcp")
                        .hostConfig(HostConfig.builder()
                                .publishAllPorts(true)
                                .autoRemove(true)
                                .links(String.format("%s:opennms", stacker.getContainerInfo(OPENNMS).name()))
                                .build())
                        .build()
        );
    }

    @Override
    public List<GizmoDockerStack> getDependencies() {
        return Collections.singletonList(new PostgreSQLStack());
    }

    @Override
    public void beforeStack(GizmoDockerStacker stacker) {
        this.stacker = stacker;
    }

    @Override
    public List<Consumer<GizmoDockerStacker>> getWaitingRules() {
        return Lists.newArrayList((stacker) -> {
            final OpenNMSRestClient nmsRestClient = getOpenNMSRestClient();
            await().atMost(5, MINUTES)
                    .pollInterval(5, SECONDS).pollDelay(0, SECONDS)
                    .ignoreExceptions()
                    .until(nmsRestClient::getDisplayVersion, notNullValue());
        }, (stacker) -> {
            final GrafanaRestClient grafanaRestClient = getGrafanaRestClient();
            await().atMost(2, MINUTES)
                    .pollInterval(5, SECONDS).pollDelay(0, SECONDS)
                    .ignoreExceptions()
                    .until(grafanaRestClient::getDataSources, hasSize(greaterThanOrEqualTo(0)));
        });
    }

    public URL getOpenNMSUrl() {
        InetSocketAddress addr = stacker.getServiceAddress(OpenNMSHelmStack.OPENNMS, 8980);
        try {
            return new URL(String.format("http://%s:%d/opennms", addr.getHostString(), addr.getPort()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public URL getHelmUrl() {
        return HttpUtils.toHttpUrl(stacker.getServiceAddress(OpenNMSHelmStack.HELM, 3000));
    }

    public GrafanaRestClient getGrafanaRestClient() {
        return new GrafanaRestClient(getHelmUrl());
    }

    public OpenNMSRestClient getOpenNMSRestClient() {
        return new OpenNMSRestClient(getOpenNMSUrl());
    }
}
