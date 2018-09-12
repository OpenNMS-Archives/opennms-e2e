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

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.e2e.core.EndToEndTestRule;
import org.opennms.e2e.grafana.Grafana44SeleniumDriver;
import org.opennms.e2e.grafana.GrafanaRestClient;
import org.opennms.e2e.grafana.model.Dashboard;
import org.opennms.e2e.opennms.OpenNMSRestClient;
import org.opennms.e2e.opennms.model.Event;
import org.opennms.e2e.stacks.OpenNMSHelmStack;
import org.opennms.gizmo.docker.GizmoDockerRule;

public class OCEStep2Test {

    private final OpenNMSHelmStack stack = new OpenNMSHelmStack();

    @Rule
    public EndToEndTestRule e2e = EndToEndTestRule.builder()
            .withGizmoRule(GizmoDockerRule.builder()
                    .withStack(stack)
                    .build())
            .withWebDriverType(EndToEndTestRule.WebDriverType.LOCAL_CHROME)
            .build();

    @Test
    public void canViewAlarmInAlarmTable() throws InterruptedException {
        /*
        // Using REST to Grafana:
        // Enable Helm plugin
        // Create FM datasource
        // Create dashboard with alarm table
        GrafanaRestClient grafanaRestClient = new GrafanaRestClient(stack.getHelmUrl());
        grafanaRestClient.enablePlugin("helm");
        grafanaRestClient.createFMDatasource(stack.getOpenNMSUrl());
        Dashboard dashboard = new Dashboard();
        grafanaRestClient.addDashboard(dashboard);

        // Using REST to OpenNMS:
        // Trigger alarm in OpenNMS
        OpenNMSRestClient openNMSRestClient = stack.getOpenNMSRestClient();
        Event e = new Event();
        e.setUei("uei.opennms.org/alarms/trigger");
        openNMSRestClient.sendEvent(e);
        await().atMost(1, MINUTES).until(openNMSRestClient::getAlarms, hasSize(greaterThanOrEqualTo(1)));
        */

        // Using Selenium to Grafana:
        // Login, navigate to dashboard, view alarm in table
        Grafana44SeleniumDriver grafanaDriver = new Grafana44SeleniumDriver(e2e.getDriver(), stack.getHelmUrl());
        List<Map<String, String>> alarms = grafanaDriver.home().dashboard("alarms").getTable();
        assertThat(alarms, hasSize(greaterThanOrEqualTo(1)));
    }
}
