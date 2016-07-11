Abstract Python wrapper KNIME node and helpers.
Used for development of KNIME nodes calling Python scripts.

[![Build Status](https://travis-ci.org/3D-e-Chem/knime-python-wrapper.svg?branch=master)](https://travis-ci.org/3D-e-Chem/knime-python-wrapper)

The nodes in Scripting>Python folder of the node repository (nodes part of the `KNIME Python integration` plugin) the end-user needs to paste Python code in a text area in the node dialog.
Nodes derived from this repo will have a Python script included in their jar file and the dialog of the node will contain no source code text area.
The included Python script is not editable by the end-user, but can read options from dialog like the input column name.

# Usage

Requirements:

* KNIME SDK, https://www.knime.org, version 3.1 or higher

Instructions for KNIME node developers that want to call a Python script.
Several steps must be performed:

[1. Add update site](#1-add-update-site)
[2. Add dependency](#2-add-dependency)
[3. Implement node](#3-implement-node)
[4. Write tests](#4-write-tests)

## 1. Add update site

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

## 2. Add dependency

To implement the node a dependency is needed for the plugin add tests.
To do this add `nl.esciencecenter.e3dchem.knime.python` as a required plugin to the `plugin/META-INF/MANIFEST.MF` and `tests/META-INF/MANIFEST.MF` file.

## 3. Implement node

Create your node config class extended from the `nl.esciencecenter.e3dchem.python.PythonWrapperNodeConfig` class.
Add fields you want to have in the node dialog and be available inside the Python script inside the `options` dictionary.

Create your node model class extended from the `nl.esciencecenter.e3dchem.python.PythonWrapperNodeModel` class.
Overwrite the `python_code_filename` and `required_python_packages` fields in the constructor.

## 4. Write tests

To run tests which execute the node it is needed to setup `KNIME Python integration` plugin.
This can be done by calling `PythonWrapperTestUtils.materializeKNIMEPythonUtils()` in `@BeforeClass` method of a test case.

# Build

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

    1. Goto Help > Install new software ...
    2. Make sure Update site is http://update.knime.org/analytics-platform/3.1 is in the pull down list otherwise add it
    3. Select --all sites-- in work with pulldown
    4. Select m2e (Maven integration for Eclipse)
    5. Select `KNIME Python Integration`
    6. Install software & restart

5. Import this repo as an Existing Maven project

During import the Tycho Eclipse providers must be installed.

# New release

1. Update versions in pom files with `mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=<version>` command.
2. Manually update version of "source" feature in `p2/category.xml` file.
3. Commit and push changes
3. Create package with `mvn package`, will create update site in `p2/target/repository`
4. Append new release to an update site
  1. Make clone of an update site repo
  2. Append release to the update site with `mvn install -Dtarget.update.site=<path to update site>`
5. Commit and push changes in this repo and update site repo.
