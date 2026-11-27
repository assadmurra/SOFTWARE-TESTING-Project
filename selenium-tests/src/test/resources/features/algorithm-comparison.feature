@algorithm @regression
Feature: Comparing algorithms on the same graph
  As a student
  I want to compare two algorithms on one graph
  So that I can see the trade-offs between them

  The "tree" graph has a cheap route S>B>F>G (cost 5) and a costlier
  route S>A>D>G (cost 8) whose first node looks closer to the goal.

  Background:
    Given the visualizer is ready
    And graph "tree" is loaded

  Scenario: A* explores fewer nodes than UCS but finds the same route
    When I run "ucs" with depth 5
    And I remember the result
    And I clear the result
    And I run "astar" with depth 5
    Then the path is "S>B>F>G" with cost 5
    And fewer nodes are explored than before

  Scenario: Greedy best-first is not optimal
    When I run "astar" with depth 5
    And I remember the result
    And I clear the result
    And I run "greedy" with depth 5
    Then the path is "S>A>D>G" with cost 8
    And the path cost is higher than before
