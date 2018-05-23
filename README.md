# Smart Carpooling Demo

This documentation aims to explain how the planning experiments in the *smart mobility* domain can be run.

1. [Installation](#installation)
	1. [Planning Environment](#planning-environment)
	1. [Python Libraries](#python-libraries)
1. [Usage](#usage)
	1. [Running a simple example](#run-default-example)
	1. [Running the Collective Adaptation Engine](#run-cae)
	1. [Creating Smart Mobility Problems](#create-sm-problems)
	1. [Plan visualization and creation of issues](#visualization-and-issue-creation)
1. [Credits](#credits)
1. [References](#references)

## <a name="installation"></a>Installation

### <a name="planning-environment"></a>Planning Environment

To run the experiments for this domain, you have to download/clone the [Temporal Planning](https://github.com/aig-upf/temporal-planning) repository:

```
git clone https://github.com/aig-upf/temporal-planning.git
```

Follow the [instructions](https://github.com/aig-upf/temporal-planning/blob/master/README.md) to compile the contents of this repository.

It is important that you have all folders (`universal-pddl-parser`, `temporal-planning`, `smart-carpooling-demo`, `VAL`) in the same path.

### <a name="python-libs-installation"></a>Python Libraries

The installation of the following Python libraries is required to execute the tools contained in this repository:

* [Flask](http://flask.pocoo.org/docs/0.12/quickstart/)
* [Flask-CORS](https://flask-cors.readthedocs.io/en/latest/)
* [geopy](https://pypi.python.org/pypi/geopy)
* [openpyxl](http://openpyxl.readthedocs.io/en/default/)
* [parse](https://pypi.python.org/pypi/parse)
* [queuelib](https://pypi.python.org/pypi/queuelib)

You can install all of them at once using the following command:
```
pip install flask flask-cors geopy openpyxl parse queuelib
```

## <a name="usage"></a>Usage

### <a name="run-default-example"></a>Running a simple example

To test if everything has been correctly installed, you can follow this section. The file you will be running is called `solver.py` (inside the `solver` folder):

```
solver.py [-h] [--json] [--geojson] [--time TIME] [--memory MEMORY] config
```

where:

* `-h` - shows help.
* `--json` - whether the solution has to be converted into JSON format.
* `--geojson` - whether the solution has to be converted into GeoJSON format (used by a visualizer).
* `--time` - the amount of time (in seconds) during which the planner will run. Default: 3600s.
* `--memory` - the maximum amount of memory (in MiB) used by the planner. Default: 4096 MiB.
* `config` - the path to a configuration/problem file. The format of these files is explained [here](#create-sm-problems).

You can use the configuration file `config.json` placed in `solver/config`. You just have to open the `smart-carpooling-demo` folder and run the following command:

```
./solver/solver.py --json --geojson  solver/config/config.json
```

By running the previous command, you will see the following process:

1. The input map is parsed.
1. The planner starts and will stop as soon as a solution is found or one of the previous time/memory criterias is met.
1. If there is a solution, it will be converted into JSON format (see file `tmp_sas_plan.json`).
1. If there is a solution, it will be converted into GeoJSON format (see file `cooperative_tmp_sas_plan.geojson`). This file can be later used by a visualizer (explained later).

### <a name="run-cae"></a>Running the Collective Adaptation Engine

The Collective Adaptation Engine (CAE) code is contained inside the `collective-adaptation-engine` folder. It is written in Java and it requires version 8 to run. You can directly import this folder from Eclipse.

This module automatically calls all the previous modules described in the [previous section](#run-default-example). The class you need to run to is `CollectiveMobility.java`.
The execution consists of several runs. Each run follows this process:

1. An initial random state is created.
1. The state is sent to the planner to get a collective solution.
1. Once a solution is obtained, an adaptation issue is introduced.
1. The issue is resolved in two ways: selfishly and collectively.
1. Statistics are collected from both the selfish and collective plans.

Once all runs have been executed, the overall statistics are exported to two files: `dataEvaluationCollective.csv`
and `dataEvaluationSelfish.csv`. The meaning of their columns is the following:

* `id` - number of run.
* `dv1` - number of vehicles involved.
* `dv2` - number of passenger involved.
* `dv3` - execution time of the adaptation.
* `dv4` - average of meters done by vehicles.
* `dv5` - average of meters done by passengers walking.
* `dv6` - total number of used agents used including the car pool company.

### <a name="create-sm-problems"></a>Creating Smart Mobility Problems

Mobility problems are specified using the JSON format. These problems are later converted into PDDL problems that can be solved by a temporal planner. A mobility problem written as JSON specifies the following fields:

* `map_path` - the path to the input OpenStreetMap.
* `map_boundaries` - object containing defining a rectangular area to analyse inside the map:
	* `min_latitude` - minimum latitude.
	* `max_latitude` - maximum latitude.
	* `min_longitude` - minimum longitude.
	* `max_longitude` - maximum longitude.
* `solution_type` - specifies whether the solution must be `collective` (agents may interact) or `selfish` (agents cannot interact).
* `pedestrians` - list of the pedestrians/passengers in the problem. Each contains the following fields:
	* `id` - a unique identifier.
	* `init_pos` - OSM label of its initial position.
	* `target_pos` - OSM label of its target position.
	* `walk_range` - maximum distance it can walk away from its origin and target positions.
* `carpools` - list of carpools in the problem. Each contains the following fields:
	* `id` - a unique identifier.
	* `init_pos` - OSM label of its initial position.
	* `target_pos` - OSM label of its target position.
* `blocked_streets` - list of blocked streets in the problem. Each contains the following fields:
	* `init_pos` - OSM label of its initial position.
	* `target_pos` - OSM label of its target position.
* `blocked_frontiers` - list of blocked frontiers. Each frontier is either specifies the field `latitude` or the field `longitude`.
All the streets crossing that frontier become blocked in the planning problem.

You can find examples of these files inside the `solver/config` folder.

### <a name="visualization-and-issue-creation"></a>Plan visualization and creation of issues

The visualizer supports the following functionalities:

* Create carpooling scenarios.
* Solve carpooling scenarios.
* Add adaptation issues (blocked streets) by clicking on a street.
* Visualize the solutions step by step.
* Visualize the overall distance traversed by carpools and passengers.

The following list shows what each of the navigation buttons does:

* `Play` - each of step in the plan is automatically displayed one after the other. Each state is shown for one second.
* `Pause` - stops the automatic displaying of the plan.
* `Next` - shows the next state in the plan.
* `Restart` - resets the visualizer to the first state (blocked streets are kept).
* `Generate scenario` - generates a new carpooling scenario.
* `Send current state` - the current state of the scenario is sent to the solver to get an updated solution, e.g. to resolve new adaptation issues (blocked streets).

To use the different functionalities of the visualizer, you must run the `service.py` file of the `solver` folder:
```
./solver/server.py
```

This file opens uses the port `5000` to receive the calls from the visualizer.

![Image of the visualizer](doc/img/visualiser_full.png)

You can see a demo of the visualizer in the following [link](https://youtu.be/omWu3FpZNsI).

## <a name="credits"></a>Credits

The code for creating and solving smart mobility problems has been written by:

* Antonio Bucchiarone (Fondazione Bruno Kessler).
* Anders Jonsson (Universitat Pompeu Fabra).
* Daniel Furelos Blanco (Universitat Pompeu Fabra).

## <a name="references"></a>References

* <a name="ref-aamas2018-extended-abstract">Bucchiarone, A., Furelos-Blanco, D., Jonsson, A., Khandokar, F., and Mourshed, M. (2018).</a> _Collective Adaptation through Concurrent Planning: the Case of Sustainable Urban Mobility_. Proc. of the 17th International Conference on Autonomous Agents and Multiagent Systems
(AAMAS 2018).

* <a name="ref-aamas2018-demo-paper">Furelos-Blanco, D., Bucchiarone, A., and Jonsson, A. (2018).</a> _CARPooL: Collective Adaptation using concuRrent PLanning_. Proc. of the 17th International Conference on Autonomous Agents and Multiagent Systems
(AAMAS 2018).

