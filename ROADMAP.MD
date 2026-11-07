# VehicleMesh roadmap

This roadmap describes intended development, not features already available. The current implementation is an early Java simulation prototype; see [README.md](README.md) for its behavior and limitations.

The goal is a platform for developing vehicle networks for passenger transport and parcel logistics and distribution, and for testing proprietary or publicly available optimization solutions with different objective functions.

## 1. Reimplement the server in Python

- [ ] Reimplement and complete the current Java dispatch/server logic in Python.
- [ ] Define the server's responsibilities for vehicle registration, passenger requests, fleet state, and dispatch decisions.
- [ ] Expose an API for passenger clients and optimization integrations.
- [ ] Define how the Python server exchanges state and decisions with the simulation, keeping the migration scope explicit rather than assuming that all Java simulation code must be rewritten immediately.
- [ ] Complete the request lifecycle: request creation, vehicle assignment, pickup, travel, and arrival.

## 2. Add an Android app for passenger testing

- [ ] Build an Android app for testing passenger interactions with the system.
- [ ] Allow test passengers to provide pickup and destination information and request a trip.
- [ ] Display request status and vehicle assignment, and support testing passenger priorities and transfer preferences.
- [ ] Connect the app to the Python server through its defined API.
- [ ] Use test scenarios to validate the passenger experience and the complete request lifecycle.

## 3. Support selectable optimization algorithms

- [ ] Develop a system for registering and selecting the optimization algorithm used for a scenario or experiment.
- [ ] Support algorithms loaded into the platform as local implementations or plugins.
- [ ] Support algorithms hosted externally and invoked through an API with a specific, documented interface.
- [ ] Define a common contract for local and remote algorithms: scenario and fleet state, passenger and parcel requests, capacities, priorities, constraints, objective configuration, and proposed allocation or routing decisions.
- [ ] Define validation, errors, time limits, and fallback behavior for invalid or unavailable algorithms.
- [ ] Allow experiments with both proprietary and publicly available implementations, without assuming that external algorithms must disclose their internal code.
- [ ] Make objective functions configurable, including global fleet energy consumption, travel times for selected passenger classes, parcel delivery performance, and combinations of these objectives.

## Supporting simulation and evaluation work

- [x] Add a finite, seeded console demo with nearest-idle-car assignment, movement, pickups, arrivals, and basic fleet distance and waiting-time statistics.
- [x] Track total fleet movement steps and energy with a cost of 1 energy unit per moving-car step, including empty travel.
- [ ] Complete general-purpose movement scheduling, legacy command execution, and inter-vehicle communication.
- [x] Allocate and register the full requested fleet, with independent positions for cars at the same station.
- [ ] Complete vehicle capacity handling and passenger pooling in the general simulation.
- [ ] Model parcel pickup, transport, transfer, and delivery alongside passenger transport.
- [ ] Extend the abstract step-based energy counter with a realistic vehicle energy-consumption model so that global energy objectives can be evaluated.
- [ ] Add reproducible scenarios and comparable results for different algorithms and objective functions.
- [ ] Measure waiting and travel times by passenger class, energy consumption, delivery performance, vehicle utilization, and the effects of transfers.

There are no committed dates for these milestones. The API contract, plugin format, and implementation technologies beyond the planned Python server and Android app remain to be defined.
