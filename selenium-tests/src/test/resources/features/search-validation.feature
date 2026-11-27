@ui @negative @regression
Feature: Search is refused when the graph is incomplete
  As a user
  I want a clear message when my graph cannot be searched
  So that I know what to fix instead of seeing a wrong or empty result

  Background:
    Given the visualizer is ready

  @smoke
  Scenario: Starting a search with no graph is refused
    When I start the search
    Then an alert says "Please add nodes to the graph first"
    And the search was not started

  Scenario: Starting a search without a goal is refused
    Given I have drawn a directed graph with nodes "A, B"
    And I connect "A" to "B"
    When I start the search
    Then an alert says "Please set a goal node"
    And the search was not started

  Scenario: Starting a search after removing the source is refused
    Given I have drawn a directed graph with nodes "A, B"
    And I connect "A" to "B"
    And I mark "B" as a goal
    When I toggle "A" as the source
    And I start the search
    Then an alert says "Please set a source node"
    And the search was not started

  Scenario Outline: <algorithm> needs heuristic values before it can start
    Given I have drawn a directed graph with nodes "A, B"
    And I connect "A" to "B"
    And I mark "B" as a goal
    When I run "<value>" with depth 5
    Then an alert says "<algorithm> requires heuristic values"
    And the search was not started

    Examples:
      | algorithm                | value  |
      | Greedy Best-First Search | greedy |
      | A* Search                | astar  |
