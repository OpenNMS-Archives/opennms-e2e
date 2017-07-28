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

package org.opennms.e2e.grafana.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class DashboardWithMetadata {
    @JsonProperty("meta")
    private DashboardMetadata metadata;
    private Dashboard dashboard;

    public DashboardMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(DashboardMetadata metadata) {
        this.metadata = metadata;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DashboardWithMetadata that = (DashboardWithMetadata) o;
        return Objects.equals(metadata, that.metadata) &&
                Objects.equals(dashboard, that.dashboard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, dashboard);
    }

    @Override
    public String toString() {
        return "DashboardWithMetadata{" +
                "metadata=" + metadata +
                ", dashboard=" + dashboard +
                '}';
    }
}
