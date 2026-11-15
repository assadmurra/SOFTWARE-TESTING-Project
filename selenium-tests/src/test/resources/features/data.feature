Feature: External CSV search cases
  Scenario Outline: Validate a CSV expectation through Load Graph
    Given the visualizer is ready
    When I execute external CSV case "<id>"
    Then the result matches the external CSV expectation
    Examples:
      | id     |
      | bfs    |
      | ucs    |
      | astar  |
      | absent |
