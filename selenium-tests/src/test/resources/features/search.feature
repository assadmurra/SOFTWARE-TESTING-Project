@algorithm @regression
Feature: Search algorithms on a weighted graph
  As a student learning search algorithms
  I want each algorithm to return the path its strategy predicts
  So that the visualizer teaches the correct behaviour

  The "weighted" graph has a short but expensive route S>A>G (cost 16)
  and a long but cheap route S>B>C>G (cost 3).

  Background:
    Given the visualizer is ready
    And graph "weighted" is loaded

  @smoke
  Scenario: BFS chooses the route with the fewest edges
    When I run "bfs" with depth 5
    Then the path is "S>A>G" with cost 16

  @smoke
  Scenario: UCS chooses the cheapest route
    When I run "ucs" with depth 5
    Then the path is "S>B>C>G" with cost 3

  Scenario: A* with a consistent heuristic finds the cheapest route
    When I run "astar" with depth 5
    Then the path is "S>B>C>G" with cost 3

  Scenario: DFS explores the leftmost neighbour first
    When I run "dfs" with depth 5
    Then the path is "S>A>G" with cost 16
    And the traversal is "S>A>G"

  Scenario: Greedy best-first follows the smallest heuristic
    When I run "greedy" with depth 5
    Then the path is "S>A>G" with cost 16

  Scenario: Bidirectional search alternates between both frontiers
    When I run "bidirectional" with depth 5
    Then the path is "S>A>G" with cost 16
    And the traversal is "S>G>A"

  @negative
  Scenario: A depth limit of one cannot reach the goal
    When I run "dls" with depth 1
    Then no route is reported

  Scenario: Iterative deepening reaches the goal at depth two
    When I run "ids" with depth 2
    Then the path is "S>A>G" with cost 16
