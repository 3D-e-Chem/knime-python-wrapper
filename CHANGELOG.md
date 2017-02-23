# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).
The file is formatted as described on http://keepachangelog.com/.

## [Unreleased]

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
