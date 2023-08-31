# Package Overview

- This package is a ballerina tool
- This tool will consists of static code scanners
- Scanners will output results generated after going through the AST of a source file
- The outputs of these scanners will be converged to generate a report that is of SARIF format
- SARIF reports will be utilized by code scanning platforms to showcase metrics of the analysis

## Getting started

- Run the following command to generate a SARIF report for a ballerina file

```cmd
bal shariff balFileName.bal
```
