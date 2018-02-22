*** Settings ***
Documentation     IFS-2637 Manage interview panel link on competition dashboard - Internal
...
...               IFS-2633 Manage interview panel dashboard - Internal
...
...               IFS-2778 Invite Assessor to Interview Panel: Find and Invite Tabs
...
...               IFS-2779 Invite Assessor to Interview Panel: Review and Send Invite
Suite Setup       Custom Suite Setup
Suite Teardown    The user closes the browser
Force Tags        CompAdmin  Assessor
Resource          ../../resources/defaultResources.robot
Resource          ../07__Assessor/Assessor_Commons.robot

*** Test Cases ***
User navigates to the Manage interview panel
    [Documentation]  IFS-2633 IFS-2637
    [Tags]
    Given the Interview Panel is activated in the db
    When the user clicks the button/link   link=${CLOSED_COMPETITION_NAME}
    Then the user clicks the button/link   jQuery=a:contains("Manage interview panel")
    And the user sees the Interview panel page and the Interview links

There are no Assessors in Invite tab before sending the invites
    [Documentation]  IFS-2779
    [Tags]
    Given the user clicks the button/link  link=Invite assessors
    Then the user clicks the button/link   link=Invite
    And the user should see the element    jQuery=tr:contains("There are no assessors to be invited to this interview panel.")

CompAdmin can add an assessor to invite list
    [Documentation]  IFS-2778
    Given the user clicks the button/link  link=Find
    Then the competition admin invites assessors to the competition

Cancel sending invite returns to the invite tab
    [Documentation]  IFS-2779
    [Tags]
    [Setup]  the user clicks the button/link  link=Invite
    Given the user clicks the button/link     link=Review and send invites
    And the user should see the element       jQuery=h2:contains("Recipients") ~ p:contains("${assessor_ben}")
    When the user clicks the button/link      link=Cancel
    Then the user should see the element      jQuery=td:contains("${assessor_ben}")

Assessors receives the invite to interview panel
    [Documentation]  IFS-2779
    [Tags]
    Given the user clicks the button/link      link=Invite
    When the user clicks the button/link       link=Review and send invites
    Then the user should see the element       jQuery=h2:contains("Recipients") ~ p:contains("${assessor_ben}")
    And the user should see the element        jQuery=label:contains("Subject") ~ input[value="Invitation to Innovate UK interview panel for '${CLOSED_COMPETITION_NAME}'"]
    And the user enters text to a text field   css=.editor   Additional message
    When the user clicks the button/link       css=button[type="submit"]   #Send invite
    Then the user should see the element       link=Find
    And the user reads his email               ${assessor_ben_email}   Invitation to Innovate UK interview panel for '${CLOSED_COMPETITION_NAME}'   We are inviting you to the interview panel
    And the user reads his email               ${assessor_joel_email}   Invitation to Innovate UK interview panel for '${CLOSED_COMPETITION_NAME}'   We are inviting you to the interview panel

*** Keywords ***
Custom Suite Setup
    The user logs-in in new browser  &{Comp_admin1_credentials}

the Interview Panel is activated in the db
    Connect to Database    @{database}
    Execute sql string     UPDATE `${database_name}`.`competition` SET `has_interview_stage`=1 WHERE `id`='${CLOSED_COMPETITION}';

the user sees the Interview panel page and the Interview links
    And the user should see the element    jQuery=h1:contains("Manage interview panel")
    And the user should see the element    jQuery=a:contains("Assign applications")[aria-disabled="true"]
    And the user should see the element    jQuery=a:contains("Allocate applications to assessors")[aria-disabled="true"]
    #TODO The above keyword will need to be removed/updated once the Interview links are active IFS-2783