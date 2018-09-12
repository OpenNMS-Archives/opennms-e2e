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

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opennms.e2e.core.EndToEndTestRule;
import org.opennms.e2e.grafana.Grafana44SeleniumDriver;
import org.opennms.e2e.grafana.GrafanaRestClient;
import org.opennms.e2e.opennms.OpenNMSRestClient;
import org.opennms.e2e.stacks.OpenNMSHelmStack;
import org.opennms.gizmo.docker.GizmoDockerRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore("Untested subset of the end2end topology")
public class ManualCorrelationTest extends CorrelationTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(ManualCorrelationTest.class);
    final OpenNMSHelmStack stack = new OpenNMSHelmStack();
    @Rule
    public final EndToEndTestRule e2e = EndToEndTestRule.builder()
            .withGizmoRule(GizmoDockerRule.builder()
                    .withStack(stack)
                    .build())
            .withWebDriverType(EndToEndTestRule.WebDriverType.LOCAL_CHROME)
            .build();
    @Rule
    public TestName name = new TestName();

    @Test
    public void canStartStack() throws InterruptedException {
        Grafana44SeleniumDriver grafanaDriver = new Grafana44SeleniumDriver(e2e.getDriver(), stack.getHelmUrl());
        grafanaDriver.home();
    }

    @Test
    public void canViewRelatedAlarms() throws InterruptedException, IOException {
        GrafanaRestClient grafanaRestClient = new GrafanaRestClient(stack.getHelmUrl());
        OpenNMSRestClient openNMSRestClient = new OpenNMSRestClient(stack.getOpenNMSUrl());

        try {
            setupHelm(grafanaRestClient);

            // Trigger a situation alarm on OpenNMS
            openNMSRestClient.triggerGenericSituation();

            // Login, navigate to dashboard, view alarm in table, verify the related alarms
            verifyGenericSituation(new Grafana44SeleniumDriver(e2e.getDriver(), stack.getHelmUrl()));
        } finally {
            // Cleanup
            openNMSRestClient.clearAllAlarms();
            cleanupHelm(grafanaRestClient);
        }
    }
}
