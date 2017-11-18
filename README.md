Tests [![Circle CI](https://circleci.com/gh/eluchsinger/WirelessSoundSystem/tree/master-gradle.svg?style=svg&circle-token=2042a347bcc464e27835253f754febbbb2eb08e6)](https://circleci.com/gh/eluchsinger/WirelessSoundSystem/tree/master-gradle)

# Wireless Sound System
This is the Wireless Sound System project.
The goal of this solution is to provide an easy and multi-platform method to stream music between a server and a client.


## Technical:
- Gradle is doing the automated build part.
- Logging is done using the Java Logger.
- Continuous Integration CircleCI
- Preferred IDE: IntelliJ Idea

## Running the client on Ubuntu / Raspbian
To make the application run on Ubuntu and Raspbian, you have to make sure that you are using the OracleJDK (Version 8.0, at least). If you are using the OpenJDK it might fail to run.

To check if you are running it on the correct JDK, you can copy the following code into your terminal:
```sh
java -version
```
There you can see the default Java VM used. If the name contains "OpenJDK", it is the wrong one.

To install the OracleJDK, it's easiest to use [WebUpd8](http://www.webupd8.org/2012/09/install-oracle-java-8-in-ubuntu-via-ppa.html), a repository for Ubuntu.



# Copyright
Copyright © 2015 Esteban Luchsinger
