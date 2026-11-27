@ui @regression
Feature: Example graphs menu
  As a new user
  I want ready-made example graphs
  So that I can try the algorithms without drawing anything

  Background:
    Given the visualizer is ready

  Scenario Outline: The "<example>" example loads and can be searched
    When I open the "<example>" example
    And I confirm clearing the graph
    Then the graph has <nodes> nodes and <edges> edges
    When I run "<algorithm>" with depth 5
    Then the path is "<path>" with cost <cost>

    @smoke
    Examples: smallest example
      | example | nodes | edges | algorithm | path      | cost |
      | simple  | 5     | 4     | bfs       | 0>1>2>3>4 | 4    |

    Examples: other examples
      | example  | nodes | edges | algorithm | path      | cost |
      | tree     | 7     | 6     | bfs       | 0>2>6     | 2    |
      | grid     | 9     | 12    | bfs       | 0>3>6>7>8 | 4    |
      | weighted | 6     | 8     | ucs       | 0>1>3>4>5 | 7    |

  @negative @known-bug
  Scenario: Cancelling the confirmation keeps the current graph
    # Bug: the app ignores "Cancel" and loads the example on top of the current graph.
    Given graph "weighted" is loaded
    When I open the "simple" example
    And I cancel clearing the graph
    Then the graph has 5 nodes and 5 edges
    When I run "ucs" with depth 5
    Then the path is "S>B>C>G" with cost 3
