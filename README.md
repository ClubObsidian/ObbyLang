<div align="center">
<h1>ObbyLang</h1>


[![License](https://img.shields.io/badge/license-GPL-blue)](https://github.com/ClubObsidian/ObbyLang/blob/master/LICENSE)

Fourth generation of ObbyLang for Minecraft and Discord
</div>

## Description

ObbyLang is a project to run es5 and [some es6](#Javascript-Compatability) Javascript in Minecraft and on other platforms, it provides support for a number of modules to make content development quicker right out of the box. ObbyLang was written to be a scripting language with similar capabilities to Skript such as: being able to be hot reloaded, unloaded and loaded at will as well as being extendable but with the ability to write code in a formal programming language.

## Platforms

ObbyLang supports the following platforms:
* Bukkit
* BungeeCord
* Discord
* Velocity

## Documentation

There is work in progress documentation for ObbyLang [here](https://clubobsidian.gitbook.io/obbylang/). You will also want to be familar with nashorn which you can find documentation for [here](https://wiki.openjdk.java.net/display/Nashorn/Nashorn+extensions).

## Javascript Compatability

ObbyLang runs off of nashorn so it supports es5 with some es6 support. The es6 syntax that ObbyLang supports is listed below, the quote is from the [nashorn Github](https://github.com/openjdk/nashorn).

```
It also implements many new features introduced in ECMAScript 6 including template strings; let, const, and block scope; iterators and for..of loops; Map, Set, WeakMap, and WeakSet data types; symbols; and binary and octal literals.
```


## Tooling

ObbyLang supports the following tools:
* SQL databases
  * With connection pooling through HikariCP
* Redis
* InfluxDB
  * With object pooling through influx4j
* MongoDB

## Performance

ObbyLang is pretty fast as it does not interpret Javascript code every time code is ran. ObbyLang takes advantage of nashorn's CompiledScript functionality to interpret code once and then compile it. These compiled scripts then provide values to listeners, commands etc which are quick to execute since they get compiled down into native Java lamdas, which can be confirmed by running a code profiler such as Spark or VisualVM.



## Building

You'll need Java 11 or above and then run `gradlew shadowJar`

## Contributing

//TODO - Contributor documents

//TODO - Issue templates