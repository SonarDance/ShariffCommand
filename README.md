# ShariffCommand

## About

- ShariffCommand is a ballerina tool
- This tool adds "bal shariff" command support for ballerina
- The goal of the tool is to be able to provide static code scanning functionality for ballerina lang

## How it works

1. Once "bal shariff" is executed, it determines if the source file is in a ballerina project
2. If it's a ballerina project, then this will build the project
3. Next the built jar file is extracted to retrieve the syntax tree of the source file
4. The syntax tree is traversed to produce the generic issues report for SonarCloud/SonarQube analysis
5. More features to be onboarded...

## Features

- Create Generic Issue reports for SonarCloud & SonarQube analysis
- More features to be onboarded...

## Usage

1. Run and build jar file

```cmd
gradlew clean build
```

2. Place the jar file in the tool-shariff folder

```
📦...
📦tool-shariff
 ┗ **📜ShariffCommand-1.0-SNAPSHOT.jar**
📜...
```

3. Navigate to the tool-shariff folder

```cmd
cd tool-shariff
```

4. Create a bala file

```cmd
bal pack
```

5. Push the bala file to local repository

```cmd
bal push --repository=local
```

6. Move the tool_shariff to the central.ballerina.io, bala folder

```
📦central.ballerina.io
 ┗ 📦bala
    ┗📦tharana_wanigaratne
      ┗**📦tool_shariff**
📦local
```

7. modify the .config folders following files

```
**📜bal-tools.toml**
**📜dist-2201.7.2.toml**
```

8. Include the tool details in them as follows

```bal-tools.toml
[[tool]]
id = "shariff"
org = "tharana_wanigaratne"
name = "tool_shariff"
```

```dist-2201.7.2.toml
[[tool]]
id = "shariff"
org = "tharana_wanigaratne"
name = "tool_shariff"
version = "0.1.0"
```

9. Check if the tool is added using the cmd

```cmd
bal tool list
```

10. Try out the tool

```cmd
bal shariff balFile.bal
```
