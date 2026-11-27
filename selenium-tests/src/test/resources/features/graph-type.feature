@ui @regression
Feature: Directed and undirected graphs
  As a student
  I want to switch a graph between directed and undirected
  So that I can see how edge direction changes reachability

  The "reverse-edge" graph only has the edge G -> S, so the goal G cannot
  be reached from S while the graph is directed.

  Background:
    Given the visualizer is ready
    And graph "reverse-edge" is loaded

  Scenario: Switching to undirected makes a reverse edge usable
    When I run "bfs" with depth 5
    Then no route is reported
    When I clear the result
    And I switch the graph type to undirected
    And I confirm the graph type change
    Then the graph type indicator shows "Undirected"
    When I run "bfs" with depth 5
    Then the path is "S>G" with cost 2

  @negative
  Scenario: Cancelling the change keeps the graph directed
    When I switch the graph type to undirected
    And I cancel the graph type change
    Then the graph type indicator shows "Directed"
    And the graph type selector shows "directed"
    When I run "bfs" with depth 5
    Then no route is reported

  @known-bug
  Scenario: The edge count includes the edges added by switching to undirected
    # Bug: the reverse edge is added, but the statistics panel is not refreshed.
    When I switch the graph type to undirected
    And I confirm the graph type change
    Then the graph has 2 nodes and 2 edges
