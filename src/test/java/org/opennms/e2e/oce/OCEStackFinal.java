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

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.opennms.e2e.core.EndToEndTestRule;
import org.opennms.e2e.grafana.Grafana44SeleniumDriver;
import org.opennms.gizmo.docker.GizmoDockerRule;
import org.opennms.gizmo.docker.stacks.EmptyDockerStack;

public class OCEStackFinal {

    private final OCEStack stack = new OCEStack();

    @Rule
    public EndToEndTestRule e2e = EndToEndTestRule.builder()
            .withGizmoRule(GizmoDockerRule.builder()
                    .withStack(stack)
                    .build())
            .withWebDriverType(EndToEndTestRule.WebDriverType.SAUCELABS)
            .build();

    @Test
    public void canCorrelateAlarms() {
        // No alarms/situations
        assertThat(getReductionKeysForOutstandingAlarms(), hasSize(0));

        // Trigger a situation
        triggerSituationFromTwoPortDowns();

        // Verify that there is a situation
        await().atMost(2, MINUTES).until(this::getReductionKeysForOutstandingAlarms, hasSize(3));
        assertThat(getReductionKeysForSituations(), hasSize(1));

        // Ensure that we can retrieve the reduction keys from the alarms
        final String reductionKeyForSituation = getReductionKeysForSituations().iterator().next();
        assertThat(getReductionKeysForAlarmsInSituationWith(reductionKeyForSituation), hasSize(2));
    }

    private void triggerSituationFromTwoPortDowns() {

        /*
        SwitchSimClient switchSimClient1 = new SwitchSimClient(stack.getSwitchSim(0));
        SwitchSimClient switchSimClient2 = new SwitchSimClient(stack.getSwitchSim(1));

        switchSimClient1.portDown(0);
        switchSimClient2.portDown(1);
        */
    }

    private List<String> getReductionKeysForOutstandingAlarms() {
        // TODO: Retrieve this from Grafana
        /*
        // We assume that the dashboard is already setup
                Grafana44SeleniumDriver grafanaDriver = new Grafana44SeleniumDriver(e2e.getDriver(), stack.getHelmUrl());
        grafanaDriver.home();
         */
        return Collections.emptyList();
    }


    private List<String> getReductionKeysForSituations() {
        // TODO: Retrieve this from Grafana
        /*
        // We assume that the dashboard is already setup
                Grafana44SeleniumDriver grafanaDriver = new Grafana44SeleniumDriver(e2e.getDriver(), stack.getHelmUrl());
        grafanaDriver.home();
         */
        return Collections.emptyList();
    }

    private List<String> getReductionKeysForAlarmsInSituationWith(String reductionKey) {
        // TODO: Retrieve this from Grafana
        /*
        // We assume that the dashboard is already setup
                Grafana44SeleniumDriver grafanaDriver = new Grafana44SeleniumDriver(e2e.getDriver(), stack.getHelmUrl());
        grafanaDriver.home();
         */
        return Collections.emptyList();
    }

    private static final class OCEStack extends EmptyDockerStack {

        public URL getHelmUrl() {
            return null;
        }
    }
}
