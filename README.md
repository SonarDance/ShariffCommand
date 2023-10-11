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

- Create Generic Issue reports for SonarCloud & SonarQube analysis - done
- Create SonarQube SLang AST from Ballerina source files
- Generate SARIF report

## Usage (Local)

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
bal shariff balFile.bal <command>
```

- --command--:

```
  --generate-generic-report
  --generate-Slang-ast
  --generate-sarif-report
```

## Usage (Remote - https://dev-central.ballerina.io/)

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

4. Go to [ballerina-dev-central](dev-central.ballerina.io) and create an account

5. Follow the steps provided [here](https://ballerina.io/learn/publish-packages-to-ballerina-central/)

6. Find the settings.toml file in the user directory

```
📦<USER_HOME>/.ballerina/
 ┣ 📂.config
 ┣ 📂repositories
 ┣ 📜ballerina-version
 ┣ 📜command-notice
 ┣ 📜installer-version
 ┗ 📜**settings.toml**
```

7. Update the Settings toml file with the credentials

```settings.toml
[central]
accesstoken="TOKEN_FROM_CENTRAL"
```

8. Configure the Ballerina.toml file in the tool-shariff project directory as follows

```Ballerina.toml
[package]
org = "DEV_CENTRAL_ORGANIZATION_NAME"
name = "tool_shariff"
version = "0.1.0"
distribution = "2201.7.2"
```

9. Set the ballerina dev sentral environment variable to true

```cmd
set BALLERINA_DEV_CENTRAL=true
```

10. Create a bala file

```cmd
bal pack
```

11. Push the bala file to dev central

```cmd
bal push
```

12. pull to ballerina tool from dev central and use it
