Abstract Python wrapper KNIME node and helpers.
Used for development of KNIME nodes calling Python scripts.

[![Java CI with Maven](https://github.com/3D-e-Chem/knime-python-wrapper/actions/workflows/ci.yml/badge.svg)](https://github.com/3D-e-Chem/knime-python-wrapper/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=3D-e-Chem_knime-python-wrapper&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=3D-e-Chem_knime-python-wrapper)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=3D-e-Chem_knime-python-wrapper&metric=coverage)](https://sonarcloud.io/summary/new_code?id=3D-e-Chem_knime-python-wrapper)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.4537256.svg)](https://doi.org/10.5281/zenodo.4537256)

The nodes in Scripting>Python folder of the node repository (nodes part of the `KNIME Python integration` plugin) the end-user needs to paste Python code in a text area in the node dialog.
Nodes derived from this repo will have a Python script included in their jar file and the dialog of the node will contain no source code text area.
The included Python script is not editable by the end-user, but can read options from dialog like the input column name.

# Usage

Instructions for KNIME node developers that want to call a Python script.
Several steps must be performed:

- [Usage](#usage)
	- [Add update site](#add-update-site)
	- [Add dependency](#add-dependency)
	- [Implement](#implement)
		- [Config](#config)
		- [Dialog](#dialog)
		- [Python script](#python-script)
		- [Model](#model)
	- [Write tests](#write-tests)
- [Build](#build)
- [Development](#development)
- [New release](#new-release)

## Add update site

The releases of this repository are available in the `https://3d-e-chem.github.io/updates/5.1` update site.

Configure target platform by adding the `https://3d-e-chem.github.io/updates/5.1` update site with `Abstract Python wrapper KNIME node and helpers` software.

To make use of it in a [Tycho based project](https://github.com/3D-e-Chem/tycho-knime-node-archetype/), add to `targetplatform/KNIME-AP-5.1.target` file the following:

```
<location includeAllPlatforms="false" includeConfigurePhase="false" includeMode="planner" includeSource="true" type="InstallableUnit">
		<unit id="nl.esciencecenter.e3dchem.python.plugin" version="0.0.0"/>
		<repository location="https://3d-e-chem.github.io/updates/5.1"/>
</location>
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

To save the Python options to disk you must call the `pythonOptions.saveSettingsTo(config)` followed by `config.saveToInDialog(settings)` in the `save*To()` method of the dialog.
To load the Python options from disk you must call the `config.loadFromInDialog(settings)` followed by `pythonOptions.loadSettingsFrom(config)` in the `load*From()` methods of the dialog.

### Python script

Inside Python script the following variables are special:

- `options`, dictionary filled from Java with PythonWrapperNodeConfig.getOptionsValues() method, to read from
- `input_table`, Pandas Dataframe with input data table, to read from
- `output_table`, Pandas Dataframe with output data table, to assign with value
- `flow_variables`, dictionary of flow variables, to get or put key/value pairs
  - `flow_variables['warning_message']`, if key exists then value will be set as warning message of node

The Python script should be located in same directory as the model.

### Model

Create your node model class extended from the `nl.esciencecenter.e3dchem.python.PythonWrapperNodeModel` class.
Overwrite the `python_code_filename` fields in the constructor, this is the name of the Python script.

## Write tests

To run tests which execute the node a call to `PythonWrapperTestUtils.init()` is required
this will add the KNIME-Python utility scripts to the Python path and configure it to use the `python3` executable in current PATH.

# Build

To build the plugin and run the tests run the following command:

```
mvn verify
```

An Eclipse update site will be made in `p2/target/repository` repository.
The update site can be used to perform a local installation.

# Development

Steps to get development environment setup based on https://github.com/knime/knime-sdk-setup#sdk-setup:

1. Install Java 17
2. Install Eclipse for [RCP and RAP developers](hhttps://www.eclipse.org/downloads/packages/installe)
3. Configure Java 17 inside Eclipse Window > Preferences > Java > Installed JREs
4. Import this repo as an Existing Maven project
5. Activate target platform by going to Window > Preferences > Plug-in Development > Target Platform and check the `KNIME Analytics Platform (5.1) - nl.esciencecenter.e3dchem.python.targetplatform/KNIME-AP-5.1.target` target definition.
6. A KNIME Analytics Platform instance can be started by right clicking on the `targetplatform/KNIME\ Analytics\ Platform.launch` file and selecting `Run As → KNIME Analytics Platform`. The KNIME instance will contain the target platform together with all extensions defined in the workspace.

During import the Tycho Eclipse providers must be installed.

# New release

1. Update versions in pom files with `mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=<version>-SNAPSHOT` command.
2. Commit and push changes
3. Create package with `mvn package`, will create update site in `p2/target/repository`
4. Append new release to an update site
5. Make clone of an update site repo
6. Append release to the update site with `mvn install -Dtarget.update.site=<path to update site>`
7. Commit and push changes in this repo and update site repo.
8. Make nodes available to 3D-e-Chem KNIME feature by following steps at https://github.com/3D-e-Chem/knime-node-collection#new-release
9. Create a GitHub release
10. Update CITATION.cff file to reflect DOI which was generated by GitHub release on https://zenodo.org
