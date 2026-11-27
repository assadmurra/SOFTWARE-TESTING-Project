@ui @regression
Feature: Loading graphs from JSON files
  As a user
  I want to load a saved graph from a JSON file
  So that I do not have to draw it again, and bad files are rejected safely

  Background:
    Given the visualizer is ready

  @smoke
  Scenario: Loading a saved graph restores its nodes and edges
    Given graph "weighted" is loaded
    Then the graph has 5 nodes and 5 edges
    And the graph type indicator shows "Directed"
    When I run "ucs" with depth 5
    Then the path is "S>B>C>G" with cost 3

  Scenario: Loading an undirected graph file sets the graph type
    Given graph "undirected" is loaded
    Then the graph has 4 nodes and 8 edges
    And the graph type indicator shows "Undirected"

  @negative
  Scenario: A goal that cannot be reached has no route
    Given graph "disconnected" is loaded
    When I run "bfs" with depth 5
    Then no route is reported

  @negative
  Scenario: A malformed JSON file is rejected and the canvas stays empty
    Given graph "malformed" is loaded
    Then an alert says "Error loading graph"
    And the graph has 0 nodes and 0 edges

  @negative
  Scenario: A malformed JSON file does not replace the current graph
    Given graph "weighted" is loaded
    And graph "malformed" is loaded
    Then an alert says "Error loading graph"
    And the graph has 5 nodes and 5 edges
    When I run "ucs" with depth 5
    Then the path is "S>B>C>G" with cost 3

  @negative @known-bug
  Scenario: A JSON file without a graph section does not replace the current graph
    # Bug: the app clears its graph before it notices the missing "graph" key.
    # The statistics still show 5 nodes, but Start then reports an empty graph.
    Given graph "weighted" is loaded
    And graph "missing-graph-key" is loaded
    Then an alert says "Error loading graph"
    When I run "ucs" with depth 5
    Then the path is "S>B>C>G" with cost 3
