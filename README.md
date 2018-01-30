Abstract Python wrapper KNIME node and helpers.
Used for development of KNIME nodes calling Python scripts.

[![Build Status](https://travis-ci.org/3D-e-Chem/knime-python-wrapper.svg?branch=master)](https://travis-ci.org/3D-e-Chem/knime-python-wrapper)
[![Build status](https://ci.appveyor.com/api/projects/status/y7u4n23sjo25pyg8/branch/master?svg=true)](https://ci.appveyor.com/project/3D-e-Chem/knime-python-wrapper/branch/master)
[![SonarCloud Gate](https://sonarcloud.io/api/badges/gate?key=nl.esciencecenter.e3dchem.python:nl.esciencecenter.e3dchem.python)](https://sonarcloud.io/dashboard?id=nl.esciencecenter.e3dchem.python:nl.esciencecenter.e3dchem.python)
[![SonarCloud Coverage](https://sonarcloud.io/api/badges/measure?key=nl.esciencecenter.e3dchem.python:nl.esciencecenter.e3dchem.python&metric=coverage)](https://sonarcloud.io/component_measures/domain/Coverage?id=nl.esciencecenter.e3dchem.python:nl.esciencecenter.e3dchem.python)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.321904.svg)](https://doi.org/10.5281/zenodo.321904)

The nodes in Scripting>Python folder of the node repository (nodes part of the `KNIME Python integration` plugin) the end-user needs to paste Python code in a text area in the node dialog.
Nodes derived from this repo will have a Python script included in their jar file and the dialog of the node will contain no source code text area.
The included Python script is not editable by the end-user, but can read options from dialog like the input column name.

# Usage

Requirements:

* KNIME SDK, https://www.knime.org, version 3.5 or higher

Instructions for KNIME node developers that want to call a Python script.
Several steps must be performed:

1. [Add update site](#1-add-update-site)
2. [Add dependency](#2-add-dependency)
3. [Implement](#3-implement)
4. [Write tests](#4-write-tests)

## Add update site

The releases of this repository are available in the `https://3d-e-chem.github.io/updates` update site.

Configure KNIME SDK by adding  the `https://3d-e-chem.github.io/updates` update site in Preferences > Install/Update > Available Software Sites.

To make use of in a Tycho based project add to the `<repositories>` tag of the `pom.xml` file the following:
```
<repository>
    <id>3d-e-chem</id>
    <layout>p2</layout>
    <url>https://3d-e-chem.github.io/updates</url>
</repository>
```

## Add dependency

To implement the node a dependency is needed for the plugin add tests.
To do this add `nl.esciencecenter.e3dchem.knime.python` as a required plugin to the `plugin/META-INF/MANIFEST.MF` and `tests/META-INF/MANIFEST.MF` file.

## Implement

### Config

Create your node config class extended from the `nl.esciencecenter.e3dchem.python.PythonWrapperNodeConfig` class.

Overwrite the constructor to add required Python modules with the `addRequiredModule("<module name>")` method.

PythonWrapperNodeConfig class are configured for a single input table called `input_table` and a single output table called `output_table`.
To change the the number and names of input and/or output tables, override the constructor.

### Dialog

In your nodes dialog the Python options panel should be added by adding the following to the dialog constructor
```java
pythonOptions = new PythonOptionsPanel<PredictMetabolitesConfig>();
addTab("Python options", pythonOptions);
```

To save the Python options to disk you must call the `pythonOptions.saveSettingsTo(config)` followed by `config.saveTo(settings)` in the `save*To()` method of the dialog.
To load the Python options from disk you must call the `config.loadFrom(settings)` followed by `pythonOptions.loadSettingsFrom(config)` in the `load*From()` methods of the dialog.

### Python script

Inside Python script the following variables are special:

* `options`, dictionary filled from Java with PythonWrapperNodeConfig.getOptionsValues() method, to read from
* `input_table`, Pandas Dataframe with input data table, to read from
* `output_table`, Pandas Dataframe with output data table, to assign with value
* `flow_variables`, dictionary of flow variables, to get or put key/value pairs
  * `flow_variables['warning_message']`, if key exists then value will be set as warning message of node

The Python script should be located in same directory as the model.

### Model

Create your node model class extended from the `nl.esciencecenter.e3dchem.python.PythonWrapperNodeModel` class.
Overwrite the `python_code_filename` fields in the constructor, this is the name of the Python script.

## Write tests

To run tests which execute the node a call to `PythonWrapperTestUtils.materializeKNIMEPythonUtils()` is required
this will add the KNIME-Python utility scripts to the Python path.

# Build

To build the plugin and run the tests run the following command:

```
mvn verify
```

An Eclipse update site will be made in `p2/target/repository` repository.
The update site can be used to perform a local installation.

# Development

Development of code in this repository.

Steps to get development environment setup:

1. Download KNIME SDK from https://www.knime.org/downloads/overview
2. Install/Extract/start KNIME SDK
3. Start SDK
4. Install m2e (Maven integration for Eclipse) + KNIME Python Integration

    1. Goto Window -> Preferences -> Install/Update -> Available Software Sites
    2. Make sure the following Software Sites (or a version of them) are present otherwise add them:

      * http://update.knime.org/analytics-platform/3.5
      * http://download.eclipse.org/eclipse/updates/4.6
      * http://download.eclipse.org/releases/neon

    3. Goto Help -> Check for updates
    4. Install any updates found & restart
    5. Goto Help > Install new software ...
    6. Select --All Available sites-- in work with pulldown
    7. Wait for list to be filled, `Pending...` should disappear
    8. Select the following items:

      * m2e - Maven integration for Eclipse (includes Incubating components)
      * KNIME Python Integration

    9. Install software & restart

5. Import this repo as an Existing Maven project

After the import the Maven plugin connections must be setup, a Discover m2e connections dialog will popup to install all requested connectors, after the installation restart eclipse.

# New release

1. Update versions in pom files with `mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=<version>-SNAPSHOT` command.
2. Commit and push changes
3. Create package with `mvn package`, will create update site in `p2/target/repository`
4. Append new release to an update site
  1. Make clone of an update site repo
  2. Append release to the update site with `mvn install -Dtarget.update.site=<path to update site>`
5. Commit and push changes in this repo and update site repo.
6. Make nodes available to 3D-e-Chem KNIME feature by following steps at https://github.com/3D-e-Chem/knime-node-collection#new-release
