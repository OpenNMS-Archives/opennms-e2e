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
import java.util.Collections;

import org.opennms.e2e.grafana.Grafana44SeleniumDriver;
import org.opennms.e2e.grafana.GrafanaRestClient;

import javafx.util.Pair;

public class CorrelationTestBase {
    protected static final String pluginName = "opennms-helm-app";
    protected static final String dataSourceName = "OpenNMS-Fault-Management";
    protected static final String dashboardName = "Helm-Dashboard";
    protected static final String genericAlarmTitle = "Alarm: Generic Trigger";

    protected void setupHelm(GrafanaRestClient grafanaRestClient) throws IOException {
        // Enable Helm plugin
        grafanaRestClient.setPluginStatus(pluginName, true);

        // Create FM datasource
        grafanaRestClient.addFMDataSource(dataSourceName);

        // Create dashboard with alarm table
        grafanaRestClient.addFMDasboard(dashboardName, dataSourceName);
    }

    protected void cleanupHelm(GrafanaRestClient grafanaRestClient) {
        grafanaRestClient.deleteDashboard(dashboardName);
        grafanaRestClient.deleteDataSource(dataSourceName);
        grafanaRestClient.setPluginStatus(pluginName, false);
    }

    protected void verifyGenericSituation(Grafana44SeleniumDriver grafanaDriver) throws InterruptedException {
        grafanaDriver
                .home()
                .dashboard(dashboardName)
                .verifyAnAlarmIsPresent()
                .verifyRelatedAlarmLabels(Collections.singletonList(new Pair<>(genericAlarmTitle, 3)));
    }
}
