# Modelicus - Aspect Oriented Rule Language for Modelica

[Modelica](https://www.modelica.org/) is a modeling language for
physical systems. This tool can be used to enforce modeling rules.

## Dependencies

Modelicus is written in Java and thus requires a *JDK >= 1.7*. It uses
[SWI Prolog](http://www.swi-prolog.org/)(*swipl >= 7.0*) as its
decision procedure. [ANTLR](http://www.antlr.org/) is used as a parser
generator. [Apache Ant](https://ant.apache.org/) is the build system.

## Installation

Unless your SWI Prolog installation also placed the Java bindings into
*/usr/lib64/swipl-jpl/*, you'll need to adjust the path in <build.xml>.

Then, simply run

```
ant jar
```

To create the package `build/modelicus.jar`.
