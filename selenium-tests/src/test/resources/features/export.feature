@ui @export @regression
Feature: Saving and exporting results
  As a student
  I want to save my graph and export the results
  So that I can include them in a report or continue later

  Each scenario downloads into its own folder and the files are opened and
  checked, not only the download itself.

  Background:
    Given the visualizer is ready
    And graph "weighted" is loaded

  @smoke
  Scenario: The JSON export contains the graph and the search result
    When I run "ucs" with depth 5
    And I export the graph as JSON
    Then a file "graph.json" is downloaded
    And the downloaded JSON has 5 nodes and the path "S>B>C>G" with cost 3

  Scenario: A saved graph can be loaded again
    When I save the graph
    Then a file "graph.json" is downloaded
    When I load the downloaded file
    Then the graph has 5 nodes and 5 edges
    When I run "ucs" with depth 5
    Then the path is "S>B>C>G" with cost 3

  Scenario: The CSV export contains the search metrics
    When I run "ucs" with depth 5
    And I export the graph as CSV
    Then a file "metrics.csv" is downloaded
    And the downloaded CSV contains:
      | Algorithm      | ucs  |
      | Success        | True |
      | Path Cost      | 3    |
      | Nodes Explored | 4    |
      | Node Count     | 5    |

  @negative
  Scenario: Exporting metrics before any search is refused
    When I try to export the metrics as CSV
    Then an alert says "Please run a search first"
    And no file is downloaded

  Scenario: The PNG export is a real image
    When I export the graph as PNG
    Then a file "ai-search-*.png" is downloaded
    And the downloaded file is a PNG image

  Scenario: The SVG export draws every node
    When I export the graph as SVG
    Then a file "graph.svg" is downloaded
    And the downloaded SVG draws 5 nodes
