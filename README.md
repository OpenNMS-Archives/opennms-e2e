# End-to-end tests for OpenNMS [![CircleCI](https://circleci.com/gh/OpenNMS/opennms-e2e.svg?style=svg)](https://circleci.com/gh/OpenNMS/opennms-e2e)

## Overview

This repository contains end-to-end tests for the OpenNMS Platform and related components.

Services are hosted in [Docker](https://www.docker.com/) containers and orchestrated by [Gizmo](https://github.com/OpenNMS/gizmo).

We interact with the services using the available REST APIs, and browser based automation using [Selenium](http://www.seleniumhq.org/).
