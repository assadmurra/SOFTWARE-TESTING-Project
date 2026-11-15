Feature: Search algorithms on a weighted graph
  Background:
    Given the visualizer is ready
    And graph "weighted" is loaded

  @smoke
  Scenario: BFS chooses the fewest edges
    When I run "bfs" with depth 5
    Then the path is "S>A>G" with cost 16

  Scenario: UCS chooses the cheapest route
    When I run "ucs" with depth 5
    Then the path is "S>B>C>G" with cost 3

  Scenario: A star uses a consistent admissible heuristic
    When I run "astar" with depth 5
    Then the path is "S>B>C>G" with cost 3

  Scenario: DFS explores leftmost neighbors first
    When I run "dfs" with depth 5
    Then the path is "S>A>G" with cost 16
    And the traversal is "S>A>G"

  Scenario: Greedy follows the smallest heuristic
    When I run "greedy" with depth 5
    Then the path is "S>A>G" with cost 16

  Scenario: Depth limit one cannot reach the goal
    When I run "dls" with depth 1
    Then no route is reported

  Scenario: Iterative deepening reaches the goal at depth two
    When I run "ids" with depth 2
    Then the path is "S>A>G" with cost 16
