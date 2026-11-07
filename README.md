# VehicleMesh

VehicleMesh is an early Java prototype for exploring cooperation between vehicles in a shared transport network. The long-term goal is to build a system for developing and testing vehicle networks for both passenger transport and parcel logistics and distribution.

The platform is intended to support experiments with proprietary and publicly available optimization solutions, using different objective functions. Examples include reducing total energy consumption across the entire fleet, reducing travel times for selected passenger classes, and balancing passenger priorities with delivery deadlines and fleet efficiency.

**Status: work in progress.** The repository currently contains simulation scaffolding, not a complete transport service or a working optimization platform. The Python server, Android app, parcel model, and interchangeable optimization algorithms are planned work; see [ROADMAP.MD](ROADMAP.MD).

## Project vision

The idea is to treat vehicles as a cooperative network rather than optimize each vehicle independently. A vehicle may serve passengers or parcels, coordinate with other vehicles, and potentially transfer a passenger or shipment when that improves the selected objective.

The intended experimentation workflow is to define a scenario, select an optimization algorithm and its objective function, run the scenario, and compare the resulting service quality and resource consumption. Algorithms should eventually be usable either as implementations loaded into the platform or as external services accessed through a defined API contract.

Possible objectives include:

- Minimize fleet-wide energy consumption, including the effect of allocation and routing decisions across vehicles.
- Reduce waiting and arrival times for selected passenger classes, with explicit priorities and constraints.
- Improve parcel distribution, delivery times, and vehicle utilization.
- Compare trade-offs between energy, travel time, service fairness, capacity, and delivery requirements.

These are design goals. Energy models, parcel handling, objective-function configuration, and comparative metrics are not implemented yet.

## What the current code does

### World and entities

`StreetGrid` creates a two-dimensional world with randomly positioned stations and passengers. Its coordinate space is currently 10,000 by 10,000 units; these are abstract coordinates, not a road map or geographic routing system.

Each `Station` owns a pool of `Car` objects. Cars and passengers derive from `MovingObject`, while stations derive from `StaticObject`. `Position` stores coordinates, and `MathOperation.distance()` calculates Euclidean distance.

The demo requests 10 stations, 1,000 cars, and 6,000 passengers. The current station allocation logic can leave part of the requested car budget unallocated, so the requested car count is not a guaranteed final fleet size.

### Vehicle commands and dispatch

`Car` has a blocking command queue and handlers for two commands:

- `GO_TO`: calculates a normalized direction toward a destination; it does not yet move the vehicle.
- `LOAD_PASSENGER`: stores a passenger in the vehicle and updates its state. Capacity and passenger-transfer handling still need completion.

`Car.init()` contains the command-processing loop, but the demo does not start it for each car.

`CallTaxiServer.handleRequest()` sketches a centralized dispatch policy: choose the nearest registered car and queue commands to approach and load the passenger. Despite its name, it is an in-process Java class, not a running HTTP server. Its car registry is not initialized by the demo, it does not filter cars by available seats, and the requested destination is not yet used to schedule the trip.

### Cooperation between vehicles

The intended protocol allows nearby vehicles to exchange information and potentially transfer passengers so that urgent passengers can reach their destinations sooner. This transfer concept is referred to in the code as *car hopping*.

The current code defines vehicle states and a proximity check with a communication radius of 10 coordinate units. `StreetGrid.worldCycle()` is structured to submit `Talk` tasks for nearby vehicles using a pool of 10 threads, with a three-second interval between communication cycles.

However, `Talk.run()` is empty, and the demo does not populate the world's active vehicle list. As a result, the current demo does not actually exchange messages or transfer passengers. There is no real wireless or mesh networking implementation yet.

### Movement

`MovingObject` provides trajectory calculations, and `MovementEngine` defines an interface for movement implementations. `LCGMovement` contains a separate random-movement implementation, but it is not connected to the demo.

`StreetGrid.registerObject()` and `Car.move()` are empty. The world therefore initializes entities but does not advance their positions or complete trips.

## Requirements

- A Java Development Kit (JDK), with both `java` and `javac` available on `PATH`. JDK 8 can compile the project's existing Java 7 source/target configuration.
- Maven 3, if using the Maven build instructions below. Maven is optional for the direct demo launch.

The project has no declared third-party Java dependencies. It has no Maven wrapper, Python dependency setup, database, or environment-variable configuration.

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

The console prints the VehicleMesh banner, `Creating the world...`, and messages describing station creation and assigned car counts. Positions and allocations vary between runs because initialization is random.

After initialization, the process remains in the world loop. With the current incomplete implementation, no vehicle movement, passenger service, or optimization results follow. Stop the demo with **Ctrl+C**; there is no automatic termination condition or graphical interface.

### Change the scenario

Edit the following line in `test/TestWorld.java`, then recompile and restart:

```java
StreetGrid world = new StreetGrid(10, 1000, 6000);
```

The arguments are the requested numbers of stations, cars, and passengers, respectively. There are no command-line scenario options yet. Constructor inputs are not validated; some combinations can produce an invalid random-number bound during car allocation, so arbitrary small scenarios may fail at startup.

## Repository structure

| Path | Purpose |
| --- | --- |
| `test/TestWorld.java` | Console demo entry point and initial scenario parameters. |
| `src/main/java/simulation/` | World initialization and communication-cycle scaffold. |
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

