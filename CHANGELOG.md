# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).
The file is formatted as described on http://keepachangelog.com/.

## [Unreleased]

### Fixed

* Support KNIME 4.3

## [2.0.3] - 2019-07-02

### Changed

- In tests call `PythonWrapperTestUtils.init()` instead of `PythonWrapperTestUtils.materializeKNIMEPythonUtils()`.

### Fixed

- Force usage of python3 in PATH during tests ((#8)[https://github.com/3D-e-Chem/knime-python-wrapper/issues/8])

## [2.0.2] - 2019-06-27

### Changed

- Compatible with KNIME 4 (#6)[https://github.com/3D-e-Chem/knime-python-wrapper/issues/6]

## [2.0.1] - 2018-02-07

### Changed

* Replaced PythonKernel from org.knime.python to org.knime.python2 (#4)

### Fixed

* Give proper exception when Python file is missing (#1)

## [1.1.0] - 2017-02-23

### Added

* Set node warning message from Python
* Allow changing of number and names of input and output tables,
  default is 1 input table called `input_table` in Python
  and 1 output table called `output_table` in Python.
* Test coverage
* More tests
* Codacy integration

### Fixed

* Give proper exception when Python file is missing (#1)

## [1.0.0] - 2016-07-11

### Added

* Abstract PythonWrapperNode classes
* Test utility to run tests which call PythonKernel execute
