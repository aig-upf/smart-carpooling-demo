# Smart Carpooling Demo

This documentation aims to explain how the planning experiments in the *smart mobility* domain can be run.

1. [Installation](#installation)
	1. [Planning Environment](#planning-environment)
  1. [Python Libraries](#python-libraries)
1. [Usage](#usage)
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

You can install all of them at once using the following command:
```
pip install flask flask-cors geopy
```

## <a name="usage"></a>Usage

## <a name="credits"></a>Credits

The code for creating and solving smart mobility problems has been written by:

* Antonio Bucchiarone (Fondazione Bruno Kessler).
* Anders Jonsson (Universitat Pompeu Fabra).
* Daniel Furelos Blanco (Universitat Pompeu Fabra).

## <a name="references"></a>References
