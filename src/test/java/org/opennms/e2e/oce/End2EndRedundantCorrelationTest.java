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

package org.opennms.e2e.oce;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.awaitility.core.ConditionTimeoutException;
import org.junit.Test;
import org.opennms.e2e.grafana.Grafana44SeleniumDriver;
import org.opennms.e2e.stacks.OpenNMSHelmOCEStack;
import org.opennms.gizmo.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class End2EndRedundantCorrelationTest extends End2EndCorrelationTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(End2EndStandaloneCorrelationTest.class);
    private String activeOCEAlias;

    public End2EndRedundantCorrelationTest() {
        super(true);
    }

    @Test
    public void canCorrelateAlarmsAfterFailure() throws IOException, InterruptedException {
        setup();
        waitForActiveOCE();
        shutdownKarafOnInstance(activeOCEAlias);

        LOG.info("Triggering alarms for correlation via API...");
        openNMSRestClient.triggerAlarmsForCorrelation();

        // OCE Should now correlate them, we need to wait here for the situation alarm to show up
        LOG.info("Waiting for a situation to be received by OpenNMS...");
        openNMSRestClient.waitForOutstandingSituation();
        LOG.info("Situation received, verifying via Helm...");

        // Login, navigate to dashboard, view alarm in table, verify the related alarms
        verifyGenericSituation(new Grafana44SeleniumDriver(e2e.getDriver(), stack.getHelmUrl()));
        cleanup();
    }

    private String getActiveOCEAlias() {
        for (String oceAlias : OpenNMSHelmOCEStack.redundanctOCEs) {
            try (final SshClient sshClient = new SshClient(stack.getOCESSHAddress(oceAlias), "admin", "admin")) {
                PrintStream pipe = sshClient.openShell();
                pipe.println("processor:current-role");
                pipe.println("logout");

                // Wait for karaf to process the commands
                await().atMost(10, SECONDS).until(sshClient.isShellClosedCallable());

                String[] shellOutput = sshClient.getStdout().split("\n");

                // This check could probably be more precise
                if (Arrays.stream(shellOutput).anyMatch(row -> row.contains("ACTIVE"))) {
                    return oceAlias;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void waitForActiveOCE() {
        LOG.info("Waiting for an active OCE instance...");
        await()
                .atMost(2, TimeUnit.MINUTES)
                .pollInterval(10, TimeUnit.SECONDS)
                .until(() -> {
                    String activeResult = getActiveOCEAlias();

                    if (activeResult != null) {
                        activeOCEAlias = activeResult;

                        return true;
                    }

                    return false;
                });
        LOG.info("OCE {} is now active", activeOCEAlias);
    }

    private void shutdownKarafOnInstance(String alias) {
        LOG.info("Shutting down Karaf on {}", alias);

        try (final SshClient sshClient = new SshClient(stack.getOCESSHAddress(alias), "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("system:shutdown -f");

            // Wait for karaf to process the commands
            await().atMost(10, SECONDS).until(sshClient.isShellClosedCallable());
            stack.
                    // Make sure the karaf instance is finished shutting down
                            waitForOCEToTerminateByAlias(alias);
        } catch (ConditionTimeoutException timeOut) {
            throw timeOut;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
