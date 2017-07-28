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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Dashboard {
    private Integer id;
    private List<String> links = new ArrayList<>();
    private List<Row> rows = new ArrayList<>();
    private int schemaVersion;
    private String style;
    private List<String> tags = new ArrayList<>();
    private String timezone;
    private String title;
    private int version;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }


    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dashboard dashboard = (Dashboard) o;
        return id == dashboard.id &&
                schemaVersion == dashboard.schemaVersion &&
                version == dashboard.version &&
                Objects.equals(links, dashboard.links) &&
                Objects.equals(rows, dashboard.rows) &&
                Objects.equals(style, dashboard.style) &&
                Objects.equals(tags, dashboard.tags) &&
                Objects.equals(timezone, dashboard.timezone) &&
                Objects.equals(title, dashboard.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, links, rows, schemaVersion, style, tags, timezone, title, version);
    }

    @Override
    public String toString() {
        return "Dashboard{" +
                "id=" + id +
                ", links=" + links +
                ", rows=" + rows +
                ", schemaVersion=" + schemaVersion +
                ", style='" + style + '\'' +
                ", tags=" + tags +
                ", timezone='" + timezone + '\'' +
                ", title='" + title + '\'' +
                ", version=" + version +
                '}';
    }

}
