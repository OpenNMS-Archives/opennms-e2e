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

package org.opennms.e2e.opennms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opennms.e2e.opennms.model.Alarm;
import org.opennms.e2e.opennms.model.AlarmList;
import org.opennms.e2e.opennms.model.Event;

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
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OpenNMSRestClient {

    private static final String DEFAULT_USERNAME = "admin";

    private static final String DEFAULT_PASSWORD = "admin";

    private final URL url;

    private final String authorizationHeader;

    public OpenNMSRestClient(InetSocketAddress addr) throws MalformedURLException {
        this(addr, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    public OpenNMSRestClient(URL url) {
        this(url, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    public OpenNMSRestClient(InetSocketAddress addr, String username, String password) throws MalformedURLException {
        this(new URL(String.format("http://%s:%d/opennms", addr.getHostString(), addr.getPort())), username, password);
    }

    public OpenNMSRestClient(URL url, String username, String password) {
        this.url = url;
        authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public String getDisplayVersion() {
        final WebTarget target = getTarget().path("info");
        final String json = getBuilder(target).get(String.class);

        final ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode actualObj = mapper.readTree(json);
            return actualObj.get("displayVersion").asText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendEvent(Event event) {
        final WebTarget target = getTarget().path("events");
        final Response response = getBuilder(target).post(Entity.json(event));
        if (!Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new IllegalArgumentException(String.format("Send event failed with %d: %s", response.getStatus(), response));
        }
    }

    public List<Alarm> getAlarms() {
        final AlarmList alarmList = getBuilder(getTarget().path("alarms"))
                .accept(MediaType.APPLICATION_JSON)
                .get(AlarmList.class);
        return alarmList.getAlarms();
    }

    private WebTarget getTarget() {
        final Client client = ClientBuilder.newClient();
        return client.target(url.toString()).path("rest");
    }

    private WebTarget getTargetV2() {
        final Client client = ClientBuilder.newClient();
        return client.target(url.toString()).path("api").path("v2");
    }

    private Invocation.Builder getBuilder(final WebTarget target) {
        return target.request().header("Authorization", authorizationHeader);
    }
}
