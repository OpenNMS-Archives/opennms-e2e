Feature: FMDataSource
  Allow Helm users to filter alarms using the FM Data Source.

  Scenario: By default, the FM data-source will show all alarms
    Given a user has a configured FM data-source
    And a set of stock alarms are present
    When the user creates a new table panel using the FM data-source
    Then the table should contain all alarms

#Feature: Ticketing
#  Helm users can trigger ticket creation and updates by
#  interacting the Alarm Table Panel.
#
#  Scenario: Actions are disable when no ticketing integration is enabled
#    Given a non-admin user has one or more alarms listed in a table panel
#    And no ticketing system integration is currently configured
#    When a user accesses the list of possible actions on a alarm
#    Then the "Open Ticket" action must not be present
#    And the "Details" action must be present
#
#  Scenario: Create ticket
#    Given a non-admin user has one or more alarms listed in a table panel
#    And a ticketing system integration has been setup
#    When the user clicks on create ticket
#    Then the state should be changed to CREATED or CREATE_PENDING
#
