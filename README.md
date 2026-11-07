# VehicleMesh

VehicleMesh is an early Java prototype for exploring cooperation between vehicles in a shared transport network. The long-term goal is to build a system for developing and testing vehicle networks for both passenger transport and parcel logistics and distribution.

The platform is intended to support experiments with proprietary and publicly available optimization solutions, using different objective functions. Examples include reducing total energy consumption across the entire fleet, reducing travel times for selected passenger classes, and balancing passenger priorities with delivery deadlines and fleet efficiency.

**Status: work in progress.** The repository contains a runnable console demo with nearest-idle-car dispatch, movement, pickups, arrivals, and fleet distance and step-based energy statistics. The Python server, Android app, parcel model, mesh transfers, and interchangeable optimization algorithms are planned work; see [ROADMAP.MD](ROADMAP.MD).

## Project vision

The idea is to treat vehicles as a cooperative network rather than optimize each vehicle independently. A vehicle may serve passengers or parcels, coordinate with other vehicles, and potentially transfer a passenger or shipment when that improves the selected objective.

The intended experimentation workflow is to define a scenario, select an optimization algorithm and its objective function, run the scenario, and compare the resulting service quality and resource consumption. Algorithms should eventually be usable either as implementations loaded into the platform or as external services accessed through a defined API contract.

Possible objectives include:

- Minimize fleet-wide energy consumption, including the effect of allocation and routing decisions across vehicles.
- Reduce waiting and arrival times for selected passenger classes, with explicit priorities and constraints.
- Improve parcel distribution, delivery times, and vehicle utilization.
- Compare trade-offs between energy, travel time, service fairness, capacity, and delivery requirements.

These are design goals. Realistic energy models, parcel handling, objective-function configuration, and comparative metrics are not implemented yet.

## What the current code does

### World and entities

`StreetGrid` creates a two-dimensional world with randomly positioned stations and passengers. Its coordinate space is currently 10,000 by 10,000 units; these are abstract coordinates, not a road map or geographic routing system.

Each `Station` owns a pool of `Car` objects. Cars and passengers derive from `MovingObject`, while stations derive from `StaticObject`. `Position` stores coordinates, and `MathOperation.distance()` calculates Euclidean distance.

The demo creates 3 stations, 6 cars, and 12 passengers. Cars are distributed across stations without dropping any of the requested fleet, and each car has its own mutable position. A scenario seed makes the initial positions and destinations repeatable.

### Console trip simulation

`ConsoleDemo` runs a small, single-threaded simulation over the existing cars and passengers:

1. All passengers request a trip at tick zero, with a generated destination.
2. Requests are considered in order and assigned to the nearest idle car by Euclidean distance. Requests wait if every car is busy.
3. Assigned cars move up to 600 coordinate units per tick toward pickup, then carry their passenger to the destination without overshooting it.
4. After arrival, a car becomes available for another request on the next tick.
5. The console reports assignments, pickups, arrivals, positions every five ticks, and final fleet statistics.

The demo carries one passenger per car at a time and keeps its trip state in `ConsoleDemo`. It does not use the unfinished legacy command queue, `CarState`, or `CallTaxiServer` dispatch path. Trips follow straight lines with no road network, traffic, pooling, or transfers. Distance units and ticks are abstract; distance is not an energy-consumption estimate.

The final summary includes completed and unfinished trips, total fleet distance, distance traveled empty and with passengers, total movement steps and energy, and average wait from tick zero to pickup among passengers actually picked up. The run ends when everyone arrives or after 200 ticks.

The demo uses a simple energy convention: **one moving-car step costs 1 energy unit**. A step is one car advancing during a tick, including a shorter final movement to pickup or arrival. Two cars moving in the same tick consume 2 units in total. Movement to pickup counts as empty travel, movement with a passenger counts as occupied travel, and stationary cars or zero-distance transitions consume no movement energy. This is an abstract counter, not a physical vehicle energy model.

### Vehicle commands and dispatch

`Car` has a blocking command queue and handlers for two commands:

- `GO_TO`: calculates a normalized direction toward a destination; it does not yet move the vehicle.
- `LOAD_PASSENGER`: stores a passenger in the vehicle and updates its state. Capacity and passenger-transfer handling still need completion.

`Car.init()` contains the command-processing loop, but the demo does not start it for each car.

`CallTaxiServer.handleRequest()` sketches a centralized dispatch policy: choose the nearest registered car and queue commands to approach and load the passenger. Despite its name, it is an in-process Java class, not a running HTTP server. Its car registry is not initialized by the demo, it does not filter cars by available seats, and the requested destination is not yet used to schedule the trip.

### Cooperation between vehicles

The intended protocol allows nearby vehicles to exchange information and potentially transfer passengers so that urgent passengers can reach their destinations sooner. This transfer concept is referred to in the code as *car hopping*.

The current code defines vehicle states and a proximity check with a communication radius of 10 coordinate units. `StreetGrid.worldCycle()` is structured to submit `Talk` tasks for nearby vehicles using a pool of 10 threads, with a three-second interval between communication cycles.

`StreetGrid.registerObject()` now registers cars in the world's active vehicle list. However, `Talk.run()` is still empty, and the console demo uses its own finite loop. It does not exchange messages or transfer passengers. There is no real wireless or mesh networking implementation yet.

### Movement

`MovingObject` provides trajectory calculations, and `MovementEngine` defines an interface for movement implementations. `LCGMovement` contains a separate random-movement implementation, but it is not connected to the demo.

The console demo advances vehicle positions directly and updates an onboard passenger's position along with their car. The legacy `Car.move()` method and general-purpose movement scheduler remain unfinished.

## Requirements

- A Java Development Kit (JDK), with both `java` and `javac` available on `PATH`. JDK 8 can compile the project's existing Java 7 source/target configuration.
- Maven 3, if using the Maven build instructions below. Maven is optional for the direct demo launch.

The project has no declared third-party Java dependencies. It has no Maven wrapper, Python dependency setup, database, or environment-variable configuration.

### Windows: `java` or `javac` is not recognized

If the JDK was just installed, an existing terminal or its parent app may still have the previous `PATH`. In PowerShell, reload the saved Windows path into the current session:

```powershell
$env:Path = [Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' + [Environment]::GetEnvironmentVariable('Path', 'User')
java -version
javac -version
```

Then run the compilation and launch commands below. If either command is still unavailable, confirm that a full JDK is installed and its `bin` directory is on `PATH`. Restarting the entire terminal host app also picks up the updated Windows environment.

## Run the demo

Run all commands from the repository root.

### Option 1: compile and run directly

This option does not require Maven. First create the output directory.

On Windows PowerShell:

```powershell
New-Item -ItemType Directory -Force target/classes | Out-Null
```

On Linux or macOS:

```sh
mkdir -p target/classes
```

Then compile and run. `javac` compiles the demo and the source classes it references:

```sh
javac -d target/classes -sourcepath src/main/java test/TestWorld.java
java -cp target/classes TestWorld
```

The entry point is `test/TestWorld.java`, which is outside Maven's standard source directories and has no package declaration. It is a console demo with a `main()` method, not an automated unit test.

### Option 2: build the main sources with Maven, then launch the demo

```sh
mvn compile
javac -cp target/classes -d target/classes test/TestWorld.java
java -cp target/classes TestWorld
```

The `pom.xml` explicitly sets Java source and target to `1.7`. If your JDK rejects those legacy levels, use a JDK that supports them, update both compiler levels in `pom.xml` to a version supported by your JDK, or use the direct compilation option above, which does not set a legacy target. There is no configured executable JAR or Maven run goal for the demo.

### Expected behavior and stopping

The console prints the VehicleMesh banner, the seed, station creation, trip requests, and live progress. With the default seed, the end of the run looks like this:

```text
[tick 049] ARRIVAL P12 with Car04 at (2013, 65)
[tick 049] STATUS queued=0 active=0 completed=12/12

SUMMARY: completed=12/12, queued=0, active=0, ticks=49 (all trips completed)
Fleet distance: 115865.6 units; empty: 54773.4; with passengers: 61092.3
Fleet movement: 205 steps; empty: 98; with passengers: 107
Fleet energy: 205 energy units (1 per moving-car step)
Average wait until pickup: 16.2 ticks (12 picked up)
```

By default, ticks are separated by 150 milliseconds, so this scenario takes about seven seconds. The demo exits automatically. Press **Ctrl+C** to stop it early. There is no graphical interface.

### Change the scenario

Two optional command-line arguments control the delay between ticks and the scenario seed:

```sh
java -cp target/classes TestWorld 0
java -cp target/classes TestWorld 150 123
```

The first runs the default scenario without pauses. The second uses a 150 ms delay and seed `123`. The default seed is `42`; changing the delay does not change simulation results. The delay must be nonnegative.

To change the population, edit the following line in `test/TestWorld.java`, then recompile and restart:

```java
StreetGrid world = new StreetGrid(3, 6, 12, seed);
```

The first three arguments are the numbers of stations, cars, and passengers; the fourth is the initialization seed. Use at least one station, at least one car per station, and a nonnegative passenger count. The tick limit is set by `new ConsoleDemo(world, seed + 1).run(200, delayMs)` in the same file; larger scenarios may need a higher limit.

## Run behavioral checks

After compiling the demo, compile and run the dependency-free checks:

```sh
javac -cp target/classes -d target/classes test/simulation/ConsoleDemoTest.java
java -cp target/classes simulation.ConsoleDemoTest
```

These verify completed trips and passenger destinations, reproducibility, nearest-idle-car assignment, movement, distance and step-based energy accounting, independent car positions, and empty, zero-distance, and tick-limited scenarios. They are run explicitly, not by Maven's default test lifecycle.

## Repository structure

| Path | Purpose |
| --- | --- |
| `test/TestWorld.java` | Console demo entry point and initial scenario parameters. |
| `src/main/java/simulation/` | World initialization, finite console trip demo, and legacy communication-cycle scaffold. |
| `test/simulation/ConsoleDemoTest.java` | Dependency-free behavioral checks for the demo. |
| `src/main/java/transport/` | Vehicle model, states, passenger storage, and command processing. |
| `src/main/java/people/` | Passenger model. |
| `src/main/java/places/` | Positions and vehicle stations. |
| `src/main/java/infrastructure/` | Initial centralized dispatch logic. |
| `src/main/java/protocol/` | Commands, vehicle states, and the unfinished inter-vehicle protocol. |
| `src/main/java/model/` | Shared entity models and movement interface. |
| `src/main/java/implementation/` | Unconnected random-movement implementation. |
| `src/main/java/utility/` | Distance calculations. |
| `pom.xml` | Maven project and compiler configuration. |

## Next steps

See [ROADMAP.MD](ROADMAP.MD) for the planned Python server, Android passenger app, and optimization algorithm interface.

