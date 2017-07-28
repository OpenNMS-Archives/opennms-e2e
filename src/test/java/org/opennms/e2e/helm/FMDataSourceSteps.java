/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.e2e.helm;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.opennms.e2e.core.EndToEndTestRule;
import org.opennms.e2e.grafana.Grafana44SeleniumDriver;
import org.opennms.e2e.grafana.model.Dashboard;
import org.opennms.e2e.grafana.model.DataSource;
import org.opennms.e2e.opennms.OpenNMSRestClient;
import org.opennms.e2e.opennms.model.Event;
import org.opennms.e2e.stacks.OpenNMSHelmStack;
import org.opennms.gizmo.docker.GizmoDockerRule;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

public class FMDataSourceSteps {

    private final OpenNMSHelmStack stack = new OpenNMSHelmStack();

    private EndToEndTestRule e2e = EndToEndTestRule.builder()
            .withGizmoRule(GizmoDockerRule.builder()
                    .withStack(stack)
                    //.skipTearDown(true)
                    //.useExistingStacks(true)
                    .build())
            .withWebDriverType(EndToEndTestRule.WebDriverType.SAUCELABS)
            //.withWebDriverType(EndToEndTestRule.WebDriverType.LOCAL_CHROME)
            .build();

    private UUID id;
    private Grafana44SeleniumDriver grafanaDriver;

    private DataSource fmDs;
    private Dashboard fmDash;

    @Before
    public void setUp(Scenario scenario) throws Exception {
        e2e.setUp(scenario);

        id = UUID.randomUUID();
        grafanaDriver = new Grafana44SeleniumDriver(e2e.getDriver(), stack.getHelmUrl());
    }

    @After
    public void tearDown(Scenario scenario) throws Exception {
        e2e.tearDown(scenario);
    }

    @Given("a user has a configured FM data-source")
    public void setupDataSource() throws Exception {
        fmDs = new DataSource();
        // Use a unique name, allowing multiple tests to be ran in parallel
        fmDs.setName("helm-fs-ds-" + id);
        fmDs.setType("opennms-helm-fm-ds");
        fmDs.setAccess("proxy");
        // Access OpenNMS via the linked container
        fmDs.setUrl("http://opennms:8980/opennms");
        fmDs.setUser(OpenNMSHelmStack.OPENNMS_ADMIN_USER);
        fmDs.setPassword(OpenNMSHelmStack.OPENNMS_ADMIN_PASSWORD);
        fmDs.setBasicAuth(true);

        grafanaDriver.home()
                .enableHelmApplication()
                .ensureDataSourceIsPresent(fmDs)
                .configureExistingDataSource(fmDs.getName())
                // Workaround for user and password via REST API
                .setDataSourceBasicAuth(fmDs.getUser(), fmDs.getPassword())
                .saveDataSource()
                .verifyDataSourceIsWorking();
    }

    @And("^a set of stock alarms are present$")
    public void aSetOfStockAlarmsArePresent() {
        final OpenNMSRestClient nmsRestClient = stack.getOpenNMSRestClient();

        // Send some trigger
        Event e = new Event();
        e.setUei("uei.opennms.org/alarms/trigger");
        e.setSeverity("CRITICAL");
        nmsRestClient.sendEvent(e);

        // Send another trigger
        e = new Event();
        e.setUei("uei.opennms.org/nodes/dataCollectionFailed");
        e.setSeverity("CRITICAL");
        nmsRestClient.sendEvent(e);

        // Make sure we have one or more alarms present
        await().atMost(30, SECONDS).until(nmsRestClient::getAlarms, hasSize(greaterThanOrEqualTo(2)));
    }

    @When("^the user creates a new table panel using the FM data-source")
    public void createNewDashboardAndPanel() throws InterruptedException {
        fmDash = new Dashboard();
        // Use a unique name, allowing multiple tests to be ran in parallel
        fmDash.setTitle("helm-dash-" + id);
        fmDash.setTimezone("browser");
        fmDash.setSchemaVersion(14);

        grafanaDriver.home()
                .ensureDashboardIsPresent(fmDash)
                .dashboard(fmDash.getTitle())
                .addRow()
                .addPanel("Alarm Table")
                .editPanel("Panel Title")
                .setPanelDataSource(fmDs.getName())
                .backToDashboard()
                .saveDashboard();
    }

    @Then("^the table should contain all alarms$")
    public void theTableShouldContainAllAlarms() {
        List<Map<String, String>> alarmTable = grafanaDriver.getTable();
        assertThat(alarmTable, hasSize(greaterThanOrEqualTo(1)));
    }
}
