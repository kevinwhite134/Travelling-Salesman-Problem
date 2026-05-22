# TSP Visualiser

A JavaFX application for exploring approaches to the Travelling Salesperson Problem (TSP). The app displays the same set of points in two panels so an exact brute-force solution can be compared with an angle-based heuristic.

## Features

- Generate random point sets with 3 to 20 points.
- Manually place and remove points on the canvas.
- Compare brute-force and heuristic tour lengths.
- Show runtime, operation count, and Big-O metadata for each solver.
- Visualise centre-of-mass rays, COM-centred circles, average/RMS radius circles, angle labels, hull points, and hull paths.
- Optionally enable a spike-reorder pass for the heuristic.

The brute-force solver is only run for 11 points or fewer because its runtime grows factorially.

## Requirements

- Java 17 or newer
- Maven 3.8 or newer

JavaFX is provided through Maven dependencies, so no separate JavaFX SDK setup is required for normal Maven runs.

## Run

From the project root:

```powershell
mvn javafx:run
```

## Build

```powershell
mvn package
```

## Project Structure

```text
src/main/java/org/example/
  Main.java                         JavaFX application entry point
  controller/TSPController.java     UI wiring, controls, point generation, and solver orchestration
  model/PointNode.java              Point data model
  model/TSPBruteForce.java          Exact TSP solver for small point counts
  model/TSPHeuristic.java           Angle-based heuristic solver with optional spike reorder
  model/HullOrderChecker.java       Convex hull ordering checks and hull helpers
  view/TSPCanvas.java               Canvas rendering and mouse interaction
```

## Usage

1. Use the point slider to choose how many random points to generate.
2. Click **Regenerate** to create a new random set.
3. Click **Solve** to re-run the solvers for the current points.
4. Enable **Manual placement** to add points with left click and remove nearby points with right click.
5. Use the overlay checkboxes to inspect centre-of-mass geometry, hull behaviour, and angle order.

For point sets above 11 points, the brute-force panel is disabled and only the heuristic result is shown.
