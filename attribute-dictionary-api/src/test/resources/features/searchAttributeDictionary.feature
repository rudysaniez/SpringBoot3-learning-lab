Feature: Search attribute dictionary

  Scenario: I would like to search an attribute dictionary
    Given I prepare the search in empty database with code equals to "code01"
    When I call a get in reactive api with the code searched in empty database
    Then I have a HTTP status equals 200 but the content is empty

  Scenario: I would like to search an attribute dictionary in database filled
    Given I prepare the search in database filled with code equals to "code01"
    When I call a get in reactive api with the code searched in database filled
    Then I have a HTTP status equals 200 and the content is not empty

  Scenario: I would like to delete all attributes in database filled
    Given A database filled
    When I call a delete all in reactive api in database filled
    Then I have a HTTP status equals to 200 and 5 attributes has been deleted

  Scenario: I would like to bulk several attributes dictionary
    Given I prepare several attributes dictionary will be launched
    When I call a post bulk in reactive api with several attributes dictionary
    Then I have a HTTP status equals 200 and the content is a list of bulk result