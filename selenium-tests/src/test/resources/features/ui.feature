Feature: Graph loading and controls
  Background:
    Given the visualizer is ready

  Scenario: Loading a saved graph restores nodes and edges
    Given graph "weighted" is loaded
    Then the graph has 5 nodes and 5 edges
    When I run "ucs" with depth 5
    Then the path is "S>B>C>G" with cost 3

  Scenario: A disconnected goal has no route
    Given graph "disconnected" is loaded
    When I run "bfs" with depth 5
    Then no route is reported

  Scenario: Clear result permits another algorithm
    Given graph "weighted" is loaded
    When I run "bfs" with depth 5
    And I clear the result
    Then the result is ready for another search
    When I run "ucs" with depth 5
    Then the path is "S>B>C>G" with cost 3

  Scenario: Depth control follows algorithm selection
    When I select "dls"
    Then the depth control is visible
    When I select "bfs"
    Then the depth control is hidden

  Scenario: Theme toggle changes appearance
    When I toggle the theme
    Then the theme changes
