@ui @regression
Feature: Drawing and editing a graph on the canvas
  As a student
  I want to build my own graph with the mouse
  So that I can try the algorithms on examples from class

  Background:
    Given the visualizer is ready

  @smoke
  Scenario: A graph drawn by hand can be searched
    Given I have drawn a directed graph with nodes "A, B, C"
    When I connect "A" to "B"
    And I connect "B" to "C"
    And I mark "C" as a goal
    Then the graph has 3 nodes and 2 edges
    When I run "bfs" with depth 5
    Then the path is "A>B>C" with cost 2

  Scenario: The first node asks for the graph type and a name
    When I click the empty canvas
    Then I am asked to choose the graph type
    When I choose the undirected graph type
    Then I am asked to name the node
    When I name the node "Start"
    Then the graph has 1 nodes and 0 edges
    And the graph type indicator shows "Undirected"

  Scenario: An edge in an undirected graph works in both directions
    Given I have drawn a undirected graph with nodes "A, B"
    When I connect "A" to "B"
    Then the graph has 2 nodes and 2 edges

  Scenario: Deleting a node also removes its edges
    Given I have drawn a directed graph with nodes "A, B, C"
    And I connect "A" to "B"
    And I connect "B" to "C"
    When I delete node "B"
    Then the graph has 2 nodes and 0 edges

  Scenario: Cancelling the name dialog does not create a node
    When I click the empty canvas
    And I choose the directed graph type
    And I cancel naming the node
    Then the graph has 0 nodes and 0 edges

  @negative @known-bug
  Scenario: A duplicate node name is replaced by a default name
    # Bug: the app compares the new name with internal numeric ids instead of
    # the visible names, so a second node called "A" is accepted silently.
    Given I have drawn a directed graph with nodes "A"
    When I add a node named "A"
    Then an alert says "Node \"A\" already exists"
    And the graph has 2 nodes and 0 edges
