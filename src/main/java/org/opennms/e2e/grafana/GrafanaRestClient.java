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

package org.opennms.e2e.grafana;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import okhttp3.Credentials;
import org.opennms.e2e.grafana.model.Dashboard;
import org.opennms.e2e.grafana.model.DashboardCreate;
import org.opennms.e2e.grafana.model.DashboardWithMetadata;
import org.opennms.e2e.grafana.model.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GrafanaRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(GrafanaRestClient.class);

    public static final String DEFAULT_USERNAME = "admin";

    public static final String DEFAULT_PASSWORD = "admin";

    private final URL url;
    private final String username;
    private final String password;

    public GrafanaRestClient(InetSocketAddress addr) throws MalformedURLException {
        this(new URL(String.format("http://%s:%d/", addr.getHostString(), addr.getPort())));
    }

    public GrafanaRestClient(URL url) {
        this(url, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    public GrafanaRestClient(URL url, String username, String password) {
        this.url = Objects.requireNonNull(url);
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
    }

    private WebTarget getTarget() {
        final Client client = ClientBuilder.newClient();
        return client.target(url.toString());
    }

    private Invocation.Builder getBuilder(final WebTarget target) {
        Invocation.Builder builder = target.request();
        builder = builder.header("Authorization", Credentials.basic(username, password));
        return builder;
    }

    public Optional<DataSource> getDataSourceByName(String name) {
        try {
            final String json = doGet(getTarget().path("api").path("datasources")
                    .path("name").path(Objects.requireNonNull(name)));
            return Optional.of(getDataSourceFromJson(json));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    private String doGet(WebTarget target) {
        LOG.info("Making request to target: {}", target.getUri());
        return getBuilder(target).accept(MediaType.APPLICATION_JSON).get(String.class);
    }

    public List<DataSource> getDataSources() {
        final String json = doGet(getTarget().path("api").path("datasources"));
        return getDataSourcesFromJson(json);
    }

    public void addDataSource(DataSource datasource) {
        final WebTarget target = getTarget().path("api").path("datasources");
        final Response response = getBuilder(target).post(Entity.json(datasource));
        if (!Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new IllegalArgumentException(String.format("Add failed with %d: %s", response.getStatus(), response));
        }
    }

    public void deleteDataSource(int id) {
        final WebTarget target = getTarget().path("api").path("datasources").path(Integer.toString(id));
        getBuilder(target).delete();
    }

    public Optional<DashboardWithMetadata> getDashboardByName(String name) {
        try {
            final String json = doGet(getTarget().path("api").path("dashboards")
                    .path(Objects.requireNonNull(name)));
            return Optional.of(getDashboardFromJson(json));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    public void deleteDashboard(int id) {
        final WebTarget target = getTarget().path("api").path("dashboards").path(Integer.toString(id));
        getBuilder(target).delete();
    }

    public void addDashboard(Dashboard dash) {
        DashboardCreate create = new DashboardCreate();
        create.setDashboard(dash);
        create.setOverwrite(true);

        final WebTarget target = getTarget().path("api").path("dashboards").path("db");
        final Response response = getBuilder(target).post(Entity.json(create));
        if (!Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new IllegalArgumentException(String.format("Add failed with %d: %s", response.getStatus(), response));
        }
    }

    private DataSource getDataSourceFromJson(String json) {
        try {
            return new ObjectMapper()
                    .readerFor(DataSource.class)
                    .readValue(json);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private List<DataSource> getDataSourcesFromJson(String json) {
        try {
            return Arrays.asList(new ObjectMapper()
                    .readerFor(DataSource[].class)
                    .readValue(json));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private DashboardWithMetadata getDashboardFromJson(String json) {
        try {
            return new ObjectMapper()
                    .readerFor(DashboardWithMetadata.class)
                    .readValue(json);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
