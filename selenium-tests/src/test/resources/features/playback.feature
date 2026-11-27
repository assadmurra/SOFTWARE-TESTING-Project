@ui @regression
Feature: Controlling the search animation
  As a student
  I want to pause, stop and step through the animation
  So that I can follow each decision the algorithm makes

  At the slowest speed the animation advances one step per second,
  which leaves time to use the controls while it runs.

  Background:
    Given the visualizer is ready
    And graph "weighted" is loaded

  @smoke
  Scenario: Pausing freezes the animation and resuming finishes it
    When I start "bfs" at the slowest speed
    And I pause the animation
    Then the pause button offers "Resume"
    And the animation does not advance for 3 seconds
    When I resume the animation
    Then the animation finishes
    And the path is "S>A>G" with cost 16

  Scenario: Stopping ends the animation without a result
    When I start "bfs" at the slowest speed
    And I stop the animation
    Then the animation controls are:
      | Start        | enabled  |
      | Pause        | disabled |
      | Stop         | disabled |
      | Step forward | disabled |
      | Step back    | disabled |
    And the result is ready for another search

  Scenario: Stepping forward reaches the final result
    When I start "ucs" at the slowest speed
    And I pause the animation
    And I step forward until the search finishes
    Then the path is "S>B>C>G" with cost 3

  Scenario: Stepping back from the final state hides the path again
    When I start "ucs" at the slowest speed
    And I pause the animation
    And I step forward until the search finishes
    And I step back once
    Then no path is shown

  Scenario: The space key starts the search
    When I press the "Space" key
    Then the animation finishes
    And the path is "S>A>G" with cost 16
