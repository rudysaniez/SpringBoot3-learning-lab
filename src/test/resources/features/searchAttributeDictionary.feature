Feature: Search attribute dictionary

  Scenario: I would like to search an attribute dictionary
    Given I search this attribute dictionary with code equals to "code01"
    When I call a get in reactive api with the code searched
    Then I have a HTTP status equals 200 but the content is empty

  Scenario: I would like to bulk several attributes dictionary
    Given I prepare several attributes dictionary will be launched
    When I call a post bulk in reactive api with several attributes dictionary
    Then I have a HTTP status equals 200 and the content is a list of bulk result