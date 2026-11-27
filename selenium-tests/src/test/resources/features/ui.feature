@ui @regression
Feature: General controls
  As a user
  I want the controls around the canvas to respond correctly
  So that the visualizer is comfortable to use

  Background:
    Given the visualizer is ready

  Scenario: Clearing the result allows another algorithm to run
    Given graph "weighted" is loaded
    When I run "bfs" with depth 5
    And I clear the result
    Then the result is ready for another search
    When I run "ucs" with depth 5
    Then the path is "S>B>C>G" with cost 3

  Scenario: The depth control appears only for depth-limited search
    When I select "dls"
    Then the depth control is visible
    When I select "bfs"
    Then the depth control is hidden

  Scenario Outline: The info panel describes the selected algorithm
    When I select "<value>"
    Then the algorithm info shows "<title>"

    Examples:
      | value         | title                     |
      | ucs           | Uniform Cost Search (UCS) |
      | astar         | A* Search                 |
      | bidirectional | Bidirectional Search      |

  Scenario: Keyboard shortcuts select building tools
    When I press the "E" key
    Then the "Add Edge" tool is active
    When I press the "G" key
    Then the "Set Goal" tool is active

  @smoke
  Scenario: The theme toggle changes the appearance
    When I toggle the theme
    Then the theme changes
