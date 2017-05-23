# Smart Carpooling Demo

This documentation aims to explain how the planning experiments in the *smart mobility* domain can be run.

1. [Installation](#installation)
	1. [Planning Environment](#planning-environment)
	1. [Python Libraries](#python-libraries)
1. [Usage](#usage)
	1. [Running a simple example](#run-default-example)
	1. [Running the Collective Adaptation Engine](#run-cae)
1. [Credits](#credits)
1. [References](#references)

## <a name="installation"></a>Installation

### <a name="planning-environment"></a>Planning Environment

To run the experiments for this domain, you have to download/clone the [Temporal Planning](https://github.com/aig-upf/temporal-planning) repository:

```
git clone https://github.com/aig-upf/temporal-planning
```

Follow the [instructions](https://github.com/aig-upf/temporal-planning/blob/master/README.md) to compile the contents of this repository.

It is important that you have all folders (`universal-pddl-parser`, `temporal-planning`, `smart-carpool-demo`, `VAL`) in the same path.

### <a name="python-libs-installation"></a>Python Libraries

The installation of the following Python libraries is required to execute the tools contained in this repository:

* [Flask](http://flask.pocoo.org/docs/0.12/quickstart/)
* [Flask-CORS](https://flask-cors.readthedocs.io/en/latest/)
* [geopy](https://pypi.python.org/pypi/geopy)
* [parse](https://pypi.python.org/pypi/parse)
* [queuelib](https://pypi.python.org/pypi/queuelib)

You can install all of them at once using the following command:
```
pip install flask flask-cors geopy parse queuelib
```

## <a name="usage"></a>Usage

### <a name="run-default-example"></a>Running a simple example

Mobility problems are specified using the JSON format. These problems are later converted into PDDL problems that can be solved by a temporal planner. A mobility problem written as JSON specifies the following fields:

* `map_path` - the path to the input OpenStreetMap.
* `map_boundaries` - object containing defining a rectangular area to analyse inside the map:
	* `min_latitude` - minimum latitude.
	* `max_latitude` - maximum latitude.
	* `min_longitude` - minimum longitude.
	* `max_longitude` - maximum longitude.
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


To test if everything has been correctly installed, you can run a simple example using a configuration file placed in `parser/config`. You just have to open the `smart-carpooling-demo` folder and run the following command:

```
./parser/parser.py --plan --json --visualize  parser/config/config.json
```

By running the previous command, 

### <a name="run-cae"></a>Running the Collective Adaptation Engine

## <a name="credits"></a>Credits

The code for creating and solving smart mobility problems has been written by:

* Antonio Bucchiarone (Fondazione Bruno Kessler).
* Anders Jonsson (Universitat Pompeu Fabra).
* Daniel Furelos Blanco (Universitat Pompeu Fabra).

## <a name="references"></a>References
