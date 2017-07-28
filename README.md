# End-to-end tests for OpenNMS

## Overview

This repository contains end-to-end tests for the OpenNMS Platform and related components.

The tests and associated scenarios are written in [Gherkin](https://github.com/cucumber/cucumber/wiki/Gherkin) and driven by [Cucumber](https://cucumber.io/).

Services are hosted in [Docker](https://www.docker.com/) containers and orchestrated by [Gizmo](https://github.com/OpenNMS/gizmo).

We interact with the services using the available REST APIs, and browser based automation using [Selenium](http://www.seleniumhq.org/).
