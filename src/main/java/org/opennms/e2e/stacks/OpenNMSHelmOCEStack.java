/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.e2e.stacks;

import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.awaitility.core.ConditionTimeoutException;
import org.opennms.gizmo.docker.GizmoDockerStacker;
import org.opennms.gizmo.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;

public class OpenNMSHelmOCEStack extends OpenNMSHelmStack {
    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSHelmOCEStack.class);
    public static final String OCE = "OCE";
    public static final List<String> redundanctOCEs = Arrays.asList("OCE1", "OCE2");
    private final boolean redundant;
    private int instanceNum = 1;

    private OpenNMSHelmOCEStack(boolean redundant) {
        this.redundant = redundant;
    }

    public static OpenNMSHelmOCEStack withStandaloneOCE() {
        return new OpenNMSHelmOCEStack(false);
    }

    public static OpenNMSHelmOCEStack withRedundantOCE() {
        return new OpenNMSHelmOCEStack(true);
    }

    @Override
    public Map<String, Function<GizmoDockerStacker, ContainerConfig>> getContainersByAlias() {
        ImmutableMap.Builder<String, Function<GizmoDockerStacker, ContainerConfig>> containers =
                new ImmutableMap.Builder<>();
        containers.putAll(super.getContainersByAlias());

        if (redundant) {
            redundanctOCEs.forEach(oceAlias -> containers.put(oceAlias, this::oceConfig));
        } else {
            containers.put(OCE, this::oceConfig);
        }

        return containers.build();
    }

    @Override
    public List<Consumer<GizmoDockerStacker>> getWaitingRules() {
        ImmutableList.Builder<Consumer<GizmoDockerStacker>> waitingRules = new ImmutableList.Builder<>();
        waitingRules.addAll(super.getWaitingRules());

        if (redundant) {
            redundanctOCEs.forEach(oceAlias -> waitingRules.add(stacker -> waitForOCEByAlias(oceAlias, stacker)));
        } else {
            waitingRules.add(stacker -> waitForOCEByAlias(OCE, stacker));
        }

        return waitingRules.build();
    }

    private ContainerConfig oceConfig(GizmoDockerStacker stacker) {
        String featuresToBoot = "KARAF_FEATURES=oce-datasource-opennms,oce-engine-cluster,oce-driver-main,";

        Path sentinelOverlayPath = setupOverlay("sentinel-overlay");

        if (redundant) {
            featuresToBoot += "sentinel-coordination-api,sentinel-coordination-common," +
                    "sentinel-coordination-zookeeper,oce-processor-redundant";
            insertApplicationId(sentinelOverlayPath);
        } else {
            featuresToBoot += "oce-processor-standalone";
        }

        return ContainerConfig.builder()
                .image("opennms/sentinel:oce")
                .exposedPorts("8301/tcp")
                .env("KARAF_REPOS=mvn:org.opennms.oce/oce-karaf-features/1.0.0-SNAPSHOT/xml", featuresToBoot,
                        "KARAF_DEBUG_LOGGING=org.opennms.oce")
                .hostConfig(HostConfig.builder()
                        .publishAllPorts(true)
                        .autoRemove(true)
                        .binds(sentinelOverlayPath + ":/opt/sentinel-overlay")
                        .links(String.format("%s:kafka", stacker.getContainerInfo(KafkaStack.KAFKA).name()))
                        .build())
                .cmd("-f")
                .build();
    }

    private void insertApplicationId(Path overlayDir) {
        String applicationIdProperty = "\napplication.id = oce-datasource-instance-" + instanceNum++;

        try {
            Files.write(Paths.get(overlayDir.toString(), "etc", "org.opennms.oce.datasource.opennms.kafka" +
                    ".streams.cfg"), applicationIdProperty.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void waitForOCEToTerminateByAlias(String alias) {
        LOG.info("Waiting for {} to terminate...", alias);

        try (final SshClient sshClient = new SshClient(stacker.getServiceAddress(alias, 8301),
                "admin", "admin")) {
            await()
                    .atMost(1, TimeUnit.MINUTES)
                    .pollInterval(5, TimeUnit.SECONDS)
                    .until(() -> {
                        try {
                            sshClient.openShell().println("logout");
                        } catch (Exception e) {
                            return true;
                        }

                        return false;
                    });
        } catch (ConditionTimeoutException timedOut) {
            throw timedOut;
        } catch (Exception ignore) {
        }

        LOG.info("{} has terminated", alias);
    }

    private static void waitForOCEByAlias(String alias, GizmoDockerStacker stacker) {
        LOG.info("Waiting for {}...", alias);

        try (final SshClient sshClient = new SshClient(stacker.getServiceAddress(alias, 8301),
                "admin", "admin")) {
            await()
                    .atMost(1, TimeUnit.MINUTES)
                    .pollInterval(5, TimeUnit.SECONDS)
                    .ignoreExceptions()
                    .until(() -> {
                        sshClient.openShell().println("logout");

                        await()
                                .atMost(10, TimeUnit.SECONDS)
                                .until(sshClient.isShellClosedCallable());

                        return true;
                    });
        } catch (ConditionTimeoutException timedOut) {
            throw timedOut;
        } catch (Exception ignore) {
        }

        LOG.info("{} is ready", alias);
    }
}