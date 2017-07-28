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

import org.opennms.e2e.grafana.model.Dashboard;
import org.opennms.e2e.grafana.model.DataSource;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class Grafana44SeleniumDriver {
    private final WebDriver driver;
    private final URL url;
    private final GrafanaRestClient restClient;

    public Grafana44SeleniumDriver(WebDriver driver, URL url) {
        this.driver = Objects.requireNonNull(driver);
        this.url = Objects.requireNonNull(url);
        restClient = new GrafanaRestClient(url);
    }

    public Grafana44SeleniumDriver ensureDataSourceIsPresent(DataSource ds) {
        Objects.requireNonNull(ds);
        // Delete existing datasource if present
        restClient.getDataSourceByName(ds.getName()).ifPresent(dataSource -> restClient.deleteDataSource(dataSource.getId()));
        // Add
        restClient.addDataSource(ds);
        return this;
    }

    public Grafana44SeleniumDriver ensureDashboardIsPresent(Dashboard dash) {
        Objects.requireNonNull(dash);
        // Delete existing datasource if present
        restClient.getDashboardByName(dash.getTitle()).ifPresent(meta -> restClient.deleteDashboard(meta.getDashboard().getId()));
        // Add
        restClient.addDashboard(dash);
        return this;
    }

    public Grafana44SeleniumDriver home() throws InterruptedException {
        driver.get(url.toString());

        try {
            driver.findElement(By.className("navbar-brand-btn-background"));
            // We're already logged in
            return this;
        } catch (NoSuchElementException nse) {
            // pass
        }

        // Login using the default credentials
        WebElement el = waitFor(By.name("username"));
        el.sendKeys(GrafanaRestClient.DEFAULT_USERNAME);

        el = driver.findElement(By.name("password"));
        el.sendKeys(GrafanaRestClient.DEFAULT_PASSWORD);
        el.sendKeys(Keys.ENTER);

        // Mash the log in button until we move forward
        for (int i = 0; i < 3; i++) {
            By by = By.xpath("//button[text()='Log in']");
            try {
                WebElement logInBtn = driver.findElement(by);
                final Actions builder = new Actions(driver);
                builder.moveToElement(logInBtn).click().perform();
            } catch (NoSuchElementException|StaleElementReferenceException e) {
                break;
            }
            Thread.sleep(500);
        }

        // No wait for the navbar to appear
        waitFor(By.className("navbar-brand-btn-background"));

        return this;
    }

    public Grafana44SeleniumDriver enableHelmApplication() {
        driver.get(url.toString() + "plugins/opennms-helm-app/edit");

        try {
            driver.findElement(By.xpath("//button[text()='Disable']"));
            return this;
        } catch (NoSuchElementException nse) {
            // pass
        }

        return this;
    }

    public Grafana44SeleniumDriver dashboard(String title) {
        driver.get(url.toString() + "dashboard/db/" + title);
        waitFor(By.partialLinkText(title));
        return this;
    }

    public Grafana44SeleniumDriver addRow() {
        moveToAndClick(By.xpath("//span[contains(text(),'ADD ROW')]"));
        return this;
    }

    public Grafana44SeleniumDriver addPanel(String type) {
        waitFor(By.xpath("//input[@placeholder='panel search filter']")).sendKeys(type);
        moveToAndClick(By.xpath("//div[@title='" + type + "']"));
        return this;
    }

    public Grafana44SeleniumDriver editPanel(String name) {
        moveToAndClick(By.xpath("//span[contains(text(),'Panel Title')]"));
        moveToAndClick(By.xpath("//a[text()='Edit']"));
        return this;
    }

    public Grafana44SeleniumDriver setPanelDataSource(String name) {
        moveToAndClick(By.xpath("//div[label/text()='Panel Data Source']/*/a"));
        WebElement el = waitFor(By.xpath("//div[label/text()='Panel Data Source']/*/input"));
        el.sendKeys(name);
        el.sendKeys(Keys.ENTER);
        return this;
    }

    public Grafana44SeleniumDriver saveDashboard() {
        moveToAndClick(By.className("fa-save"));
        // Confirm
        moveToAndClick(By.xpath("//button[text()='Save']"));
        return this;
    }

    public Grafana44SeleniumDriver backToDashboard() {
        moveToAndClick(By.xpath("//a[text()='Back to dashboard']"));
        return this;
    }

    public Grafana44SeleniumDriver configureExistingDataSource(String name) {
        moveToAndClick(By.className("navbar-brand-btn-background"));
        moveToAndClick(By.xpath("//span[text()='Data Sources']"));
        moveToAndClick(By.xpath("//div[contains(text(),'" + name + "')]"));
        return this;
    }

    public Grafana44SeleniumDriver setDataSourceBasicAuth(String user, String password) {
        waitFor(By.xpath("//input[@placeholder='user']")).sendKeys(user);
        driver.findElement(By.xpath("//input[@placeholder='password']")).sendKeys(password);
        return this;
    }

    public Grafana44SeleniumDriver saveDataSource() {
        moveToAndClick(By.xpath("//button[text()='Save & Test']"));
        return this;
    }

    public void verifyDataSourceIsWorking() {
        waitFor(By.xpath("//div[text()='Data source is working']"));
    }

    public void verifyDataSourceIsNotWorking() {
        waitFor(By.xpath("//div[text()='Unknown error']"));
        assertThat(driver.findElements(By.xpath("//div[text()='Data source is working']")), hasSize(0));
    }

    public WebElement waitFor(By by) {
        await().atMost(15, TimeUnit.SECONDS).until(() -> {
            try {
                driver.findElement(by);
                return true;
            } catch (NoSuchElementException nse) {
                return false;
            }
        });
        return driver.findElement(by);
    }

    public WebElement moveToAndClick(By by) {
        final WebElement el = waitFor(by);
        final Actions builder = new Actions(driver);
        builder.moveToElement(el).perform();
        builder.moveToElement(el).click().perform();
        return el;
    }

    public List<Map<String, String>> getTable() {
        WebElement table = driver.findElement(By.xpath("//table"));
        // Header
        WebElement header = table.findElement(By.tagName("thead"));
        WebElement headerRow = header.findElement(By.tagName("tr"));
        List<String> columnNames = headerRow.findElements(By.tagName("th")).stream()
                .map(WebElement::getText)
                .map(s -> s.replaceAll("\\s+",""))
                .collect(Collectors.toList());

        // Rows
        List<WebElement> allRows = table.findElements(By.tagName("tr"));
        List<Map<String, String>> parsedRows = new ArrayList<>(allRows.size());
        // Cells
        for (WebElement row : allRows) {
            Map<String, String> rowMap = new LinkedHashMap<>();
            List<WebElement> cells = row.findElements(By.tagName("td"));
            int i = 0;
            for (WebElement cell : cells) {
                rowMap.put(columnNames.get(i), cell.getText());
                i++;
            }
            parsedRows.add(rowMap);
        }
        return parsedRows;
    }
}
