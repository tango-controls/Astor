# Project Astor

Maven Java project

Astor is a graphical Tango control system administration tool.

* Goal

    * The first goal is to know at a quick glance, if everything is OK in a control system, and otherwise to be able to diagnose a problem and solve it.
    * The second goal is to configure the control system and its components.
    * The third goal is to have long term analysis on components (logs, statistics, usage,?.)

* Principle

    * On each host to be controlled, a device server (called Starter) takes care of all device servers running (or supposed to) on this computer.
    * The controlled server list is read from the TANGO database.
    * A graphical client (called Astor) is connected to all Starter servers and is able to:
    * Display the control system status and component status using coloured icons.
    * Execute actions on components (start, stop, test, configure, display information, ?.
    * Execute diagnostics on components.
    * Execute global analysis on a large number of crates or database.

## Cloning

```
git clone git@github.com:tango-controls/Astor
```

## Documentation 

[ ![Download](https://api.bintray.com/packages/tango-controls/maven/Astor/images/download.svg) ](https://bintray.com/tango-controls/maven/Astor/_latestVersion)
[![Docs](https://img.shields.io/badge/Latest-Docs-orange.svg)](https://tango-controls.readthedocs.io/en/latest/tools-and-extensions/built-in/astor/index.html)


## Building and Installation

### Dependencies

#### Project Dependencies 

* DBBench.jar
* Jive.jar
* JTango.jar
* ATKCore.jar
* ATKWidget.jar
* atkpanel.jar
  

#### Toolchain Dependencies 

* javac 7 or higher
* maven
  

### Build


Instructions on building the project.

```
cd Astor
mvn package
```

