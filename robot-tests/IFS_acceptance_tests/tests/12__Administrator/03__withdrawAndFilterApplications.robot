*** Settings ***
Documentation   IFS-2945 Withdraw a project from Project Setup
...
...             IFS-3473 Previous Applications Filter
Force Tags      Administrator
Resource        ../../resources/defaultResources.robot
Resource        ../10__Project_setup/PS_Common.robot

*** Variables ***
${externalProjectWithdrawnMessage}    This project has been withdrawn
${proj_electric_drive}  ${application_ids['Electric Drive']}

*** Test Cases ***
The IFS Admin withdraws a project from Project Setup
    [Documentation]  IFS-2945
    [Tags]  HappyPath
    [Setup]  The user logs-in in new browser               &{ifs_admin_user_credentials}
    Given the user navigates to the page                   ${server}/project-setup-management/competition/${NOT_EDITABLE_COMPETITION}/status/all
    And the user clicks the button/link                    jQuery=tr:contains("${INFORM_COMPETITION_NAME_2}") a:contains("Incomplete")
    When the user cancels then withdraws the project
    Then the user can see the previous application         ${INFORM_COMPETITION_NAME_2_NUMBER}  Withdrawn

The IFS Admin filters the applications
    [Documentation]  IFS-3473
    [Tags]  HappyPath
    [Setup]
    Given the user selects a filter for the applications    Withdrawn  filter
    Then the user can see the previous application          ${INFORM_COMPETITION_NAME_2_NUMBER}  Withdrawn
    When the user selects a filter for the applications     Unsuccessful  filter
    Then the user can see the previous application          ${proj_electric_drive}  Unsuccessful

The IFS Admin clears any filters applied
    [Documentation]  IFS-3473
    [Tags]  HappyPath
    [Setup]
    When the user clicks the button/link                         link=Clear all filters
    Then the user can see the previous application               ${INFORM_COMPETITION_NAME_2_NUMBER}  Withdrawn
    And the user can see the previous application                ${proj_electric_drive}  Unsuccessful

*** Keywords ***
The user cancels then withdraws the project
    the user clicks the button/link            link=Withdraw project
    the user clicks the button/link            jQuery=button:contains("Withdraw project") ~ button:contains("Cancel")  #Cancel the modal
    the user clicks the button/link            link=Withdraw project
    the user clicks the button/link            css=button[type="submit"]  #Withdraw the project on the modal

The user can see the previous application
    [Arguments]  ${filteredApplication}  ${applicationStatusInTable}
    the user should see the element            jQuery=td:contains("${filteredApplication}") ~ td:contains("${applicationStatusInTable}")

The user selects a filter for the applications
    [Arguments]  ${applicationStatusInDropDown}  ${filterID}
    Given the user selects the option from the drop-down menu    ${applicationStatusInDropDown}  id=${filterID}
    When the user clicks the button/link                         css=button[class="button"]  #Filter
