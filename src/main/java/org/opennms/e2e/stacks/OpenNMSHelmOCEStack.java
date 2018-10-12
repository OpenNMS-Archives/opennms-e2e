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
import java.io.PrintStream;
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private ContainerConfig oceConfig(GizmoDockerStacker stacker) {
        Path sentinelOverlayPath = setupOverlay("sentinel-overlay");
        Path deployPath = sentinelOverlayPath.resolve("deploy");
        Path standaloneFeatures = deployPath.resolve("features-standalone.xml");
        Path redundantFeatures = deployPath.resolve("features-redundant.xml");

        if (redundant) {
            standaloneFeatures.toFile().delete();
            redundantFeatures.toFile().renameTo(deployPath.resolve("features.xml").toFile());

            try {
                insertApplicationId(sentinelOverlayPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            redundantFeatures.toFile().delete();
            standaloneFeatures.toFile().renameTo(deployPath.resolve("features.xml").toFile());
        }

        return ContainerConfig.builder()
                .image("opennms/sentinel:oce")
                .exposedPorts("8301/tcp")
                .env("KARAF_DEBUG_LOGGING=org.opennms.oce")
                .hostConfig(HostConfig.builder()
                        .publishAllPorts(true)
                        .autoRemove(true)
                        .binds(sentinelOverlayPath + ":/opt/sentinel-overlay")
                        .links(String.format("%s:kafka", stacker.getContainerInfo(KafkaStack.KAFKA).name()))
                        .build())
                .cmd("-f")
                .build();
    }

    private void insertApplicationId(Path overlayDir) throws IOException {
        String applicationIdProperty = "\napplication.id = oce-datasource-instance-" + instanceNum++;
        Files.write(Paths.get(overlayDir.toString(), "etc", "org.opennms.oce.datasource.opennms.kafka" +
                ".streams.cfg"), applicationIdProperty.getBytes(), StandardOpenOption.APPEND);
    }

    public void waitForOCEToTerminateByAlias(String alias) throws Exception {
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
        }

        LOG.info("{} has terminated", alias);
    }

    private static void waitForOCEByAlias(String alias, GizmoDockerStacker stacker) {
        LOG.info("Waiting for {}...", alias);

        try (final SshClient sshClient = new SshClient(stacker.getServiceAddress(alias, 8301),
                "admin", "admin")) {
            // Wait until the karaf shell is available for login
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

            Thread.sleep(15000);
            
            // TODO: Hack
            await()
                    .atMost(10, TimeUnit.MINUTES)
                    .pollInterval(5, TimeUnit.SECONDS)
                    .ignoreExceptions()
                    .until(() -> {
                        PrintStream pipe = sshClient.openShell();
                        pipe.println("bundle:list -s");
                        Thread.sleep(1000);

//                            await()
//                                    .atMost(10, TimeUnit.SECONDS)
//                                    .until(sshClient.isShellClosedCallable());

                        String[] output = sshClient.getStdout().split("\n");

                        if(Arrays.stream(output)
                                .anyMatch(string -> string.contains("org.opennms.oce.driver") &&
                                        string.contains("Active"))) {
                            return true;
                        } else {
                            pipe.println("shell:exec touch deploy/features.xml");
                            LOG.info("Features were not started, touching features.xml");

                            return false;
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LOG.info("{} is ready", alias);
    }
}
