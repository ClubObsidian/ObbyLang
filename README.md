<div align="center">
<h1>ObbyLang</h1>


[![License](https://img.shields.io/badge/license-GPL-blue)](https://github.com/ClubObsidian/ObbyLang/blob/master/LICENSE)

Fourth generation of ObbyLang for Minecraft platforms and Discord
</div>

## Description

ObbyLang is a project to run Javascript scripts, it provides support for a number of modules to make content development quicker right out of the box. ObbyLang was written as an answer to a scripting language with similar capabilities to Skript such as: being able to be hot reloaded, unloaded and loaded at will as well as being extendable while also being in a more formal programming format.

ObbyLang supports the following tools:
* SQL databases
  * With connection pooling through HikariCP
* Redis
* InfluxDB
  * With object pooling through influx4j
* MongoDB

Platforms:
* Bukkit
* BungeeCord
* Discord
* Velocity

## Performance

ObbyLang is pretty fast as it does not interpret Javascript code all the time. ObbyLang takes advantage of nashorn's CompiledScript functionality to interpret code once and then compile it. These compiled scripts then provide values to listeners, commands etc which are quick to execute since they get compiled down into native Java lamdas, which can be confirmed by running a code profiler such as Spark or VisualVM.

## Building

You'll need Java 11 or above and then run `gradlew shadowJar`

## Contributing

//TODO - Contributor documents

//TODO - Issue templates