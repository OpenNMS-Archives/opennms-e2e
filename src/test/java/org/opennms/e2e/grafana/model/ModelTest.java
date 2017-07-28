package org.opennms.e2e.grafana.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ModelTest {

    @Test
    public void canUnmarshalDatasources() throws IOException {
        final String dataSourceJson = "{\n" +
                "\t\"id\": 4,\n" +
                "\t\"orgId\": 1,\n" +
                "\t\"name\": \"minion-dev (FM)\",\n" +
                "\t\"type\": \"opennms-helm-fm-ds\",\n" +
                "\t\"typeLogoUrl\": \"public/img/icn-datasource.svg\",\n" +
                "\t\"access\": \"proxy\",\n" +
                "\t\"url\": \"https://nms.opennms.org:8443/opennms\",\n" +
                "\t\"password\": \"p\",\n" +
                "\t\"user\": \"u\",\n" +
                "\t\"database\": \"\",\n" +
                "\t\"basicAuth\": true,\n" +
                "\t\"isDefault\": false,\n" +
                "\t\"jsonData\": null\n" +
                "}";

        DataSource expectedDs = new DataSource();
        expectedDs.setId(4);
        expectedDs.setOrgId(1);
        expectedDs.setName("minion-dev (FM)");
        expectedDs.setType("opennms-helm-fm-ds");
        expectedDs.setAccess("proxy");
        expectedDs.setUrl("https://nms.opennms.org:8443/opennms");
        expectedDs.setPassword("p");
        expectedDs.setUser("u");
        expectedDs.setDatabase("");
        expectedDs.setBasicAuth(true);
        expectedDs.setDefault(false);

        DataSource actualDs = new ObjectMapper()
                .readerFor(DataSource.class)
                .readValue(dataSourceJson);

        assertThat(expectedDs, equalTo(actualDs));
    }

    @Test
    public void canUnmarshalDashboard() throws IOException {
        final String dashboardJson = "{\n" +
                "\t\"meta\": {\n" +
                "\t\t\"type\": \"db\",\n" +
                "\t\t\"canSave\": true,\n" +
                "\t\t\"canEdit\": true,\n" +
                "\t\t\"canStar\": true,\n" +
                "\t\t\"slug\": \"helm\",\n" +
                "\t\t\"expires\": \"0001-01-01T00:00:00Z\",\n" +
                "\t\t\"created\": \"2017-07-26T17:28:11-04:00\",\n" +
                "\t\t\"updated\": \"2017-07-26T17:29:24-04:00\",\n" +
                "\t\t\"updatedBy\": \"admin\",\n" +
                "\t\t\"createdBy\": \"admin\",\n" +
                "\t\t\"version\": 2\n" +
                "\t},\n" +
                "\t\"dashboard\": {\n" +
                "\t\t\"annotations\": {\n" +
                "\t\t\t\"list\": []\n" +
                "\t\t},\n" +
                "\t\t\"editMode\": false,\n" +
                "\t\t\"editable\": true,\n" +
                "\t\t\"gnetId\": null,\n" +
                "\t\t\"graphTooltip\": 0,\n" +
                "\t\t\"hideControls\": false,\n" +
                "\t\t\"id\": 9,\n" +
                "\t\t\"links\": [],\n" +
                "\t\t\"rows\": [{\n" +
                "\t\t\t\"collapse\": false,\n" +
                "\t\t\t\"height\": \"250px\",\n" +
                "\t\t\t\"panels\": [{\n" +
                "\t\t\t\t\"columns\": [],\n" +
                "\t\t\t\t\"datasource\": \"my helm fm ds\",\n" +
                "\t\t\t\t\"fontSize\": \"100%\",\n" +
                "\t\t\t\t\"id\": 1,\n" +
                "\t\t\t\t\"pageSize\": null,\n" +
                "\t\t\t\t\"scroll\": true,\n" +
                "\t\t\t\t\"showHeader\": true,\n" +
                "\t\t\t\t\"sort\": {\n" +
                "\t\t\t\t\t\"col\": 0,\n" +
                "\t\t\t\t\t\"desc\": true\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"span\": 12,\n" +
                "\t\t\t\t\"styles\": [{\n" +
                "\t\t\t\t\t\"alias\": \"Time\",\n" +
                "\t\t\t\t\t\"dateFormat\": \"YYYY-MM-DD HH:mm:ss\",\n" +
                "\t\t\t\t\t\"pattern\": \"Time\",\n" +
                "\t\t\t\t\t\"type\": \"date\"\n" +
                "\t\t\t\t}, {\n" +
                "\t\t\t\t\t\"alias\": \"\",\n" +
                "\t\t\t\t\t\"colorMode\": null,\n" +
                "\t\t\t\t\t\"colors\": [\"rgba(245, 54, 54, 0.9)\", \"rgba(237, 129, 40, 0.89)\", \"rgba(50, 172, 45, 0.97)\"],\n" +
                "\t\t\t\t\t\"decimals\": 2,\n" +
                "\t\t\t\t\t\"pattern\": \"/.*/\",\n" +
                "\t\t\t\t\t\"thresholds\": [],\n" +
                "\t\t\t\t\t\"type\": \"number\",\n" +
                "\t\t\t\t\t\"unit\": \"short\"\n" +
                "\t\t\t\t}],\n" +
                "\t\t\t\t\"targets\": [{\n" +
                "\t\t\t\t\t\"filter\": {\n" +
                "\t\t\t\t\t\t\"clauses\": [],\n" +
                "\t\t\t\t\t\t\"limit\": 1000\n" +
                "\t\t\t\t\t},\n" +
                "\t\t\t\t\t\"refId\": \"A\"\n" +
                "\t\t\t\t}],\n" +
                "\t\t\t\t\"title\": \"Panel Title\",\n" +
                "\t\t\t\t\"transform\": \"table\",\n" +
                "\t\t\t\t\"type\": \"opennms-helm-alarm-table-panel\"\n" +
                "\t\t\t}],\n" +
                "\t\t\t\"repeat\": null,\n" +
                "\t\t\t\"repeatIteration\": null,\n" +
                "\t\t\t\"repeatRowId\": null,\n" +
                "\t\t\t\"showTitle\": false,\n" +
                "\t\t\t\"title\": \"Dashboard Row\",\n" +
                "\t\t\t\"titleSize\": \"h6\"\n" +
                "\t\t}],\n" +
                "\t\t\"schemaVersion\": 14,\n" +
                "\t\t\"style\": \"dark\",\n" +
                "\t\t\"tags\": [],\n" +
                "\t\t\"templating\": {\n" +
                "\t\t\t\"list\": []\n" +
                "\t\t},\n" +
                "\t\t\"time\": {\n" +
                "\t\t\t\"from\": \"now-6h\",\n" +
                "\t\t\t\"to\": \"now\"\n" +
                "\t\t},\n" +
                "\t\t\"timepicker\": {\n" +
                "\t\t\t\"refresh_intervals\": [\"5s\", \"10s\", \"30s\", \"1m\", \"5m\", \"15m\", \"30m\", \"1h\", \"2h\", \"1d\"],\n" +
                "\t\t\t\"time_options\": [\"5m\", \"15m\", \"1h\", \"6h\", \"12h\", \"24h\", \"2d\", \"7d\", \"30d\"]\n" +
                "\t\t},\n" +
                "\t\t\"timezone\": \"browser\",\n" +
                "\t\t\"title\": \"helm\",\n" +
                "\t\t\"version\": 2\n" +
                "\t}\n" +
                "}";

        Dashboard expectedDash = new Dashboard();
        expectedDash.setId(9);
        expectedDash.setSchemaVersion(14);
        expectedDash.setStyle("dark");
        expectedDash.setTitle("helm");
        expectedDash.setVersion(2);
        expectedDash.setTimezone("browser");

        Row row = new Row();
        expectedDash.getRows().add(row);

        Panel panel = new Panel();
        panel.setDatasource("my helm fm ds");
        panel.setId(1);
        panel.setSpan(12);
        panel.setTitle("Panel Title");
        panel.setType("opennms-helm-alarm-table-panel");
        row.getPanels().add(panel);

        DashboardWithMetadata actualDashWithMeta = new ObjectMapper()
                .readerFor(DashboardWithMetadata.class)
                .readValue(dashboardJson);

        Dashboard actualDash = actualDashWithMeta.getDashboard();

        assertThat(expectedDash, equalTo(actualDash));
    }
}
