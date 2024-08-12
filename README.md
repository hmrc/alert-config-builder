
# alert-config-builder

`alert-config-builder` is a Scala utility which, given an alert specification for a number of services, generates and emits JSON alert configuration documents for those services, suitable for indexing in Elasticsearch.

The artifact produced by this project is used in the `alert-config` project. The 2 repositories are kept separate due to the fact that the `alert-config` project is user editable yet we don't want to make the functionality exposed here editable.

# Dependencies

**Java** : 11\
**Scala** : 2.13.*\
**SBT** : 1.9.7\
**app-config** : Depends on the **app-config** Git repository for the environment for which the alert config is being generated e.g. **app-config-qa**.\
For local testing, you will need an empty **app-config** folder outside the **alert-config-builder** i.e. `mkdir -p ../app-config`

# Parameters

`app-config-path` - A Java system property which identifies the location of the app-config repository to use in the generation of the alert-config. This can be either a relative or absolute path. If not provided this will default to `../app-config`.

# How to contribute and test

Make sure all dependencies are satisfied
Run `sbt clean test` to confirm main branch is functional
Amend the code
Make sure old and your new tests are passing: `sbt clean test`
To see the outcome:
- Create a new version by building your branch using [alert-config-builder jenkins job](https://build.tax.service.gov.uk/job/PlatOps/job/Libraries/job/alert-config-builder/)
- Update the [alert-config dependencies](https://github.com/hmrc/alert-config/blob/0951f5a361b5d04ddb28ec96589ff609e99ff428/project/AppDependencies.scala#L6) to use your new version
- Update **alert-config** code to make sure it is going to use your new code in **alert-config-builder**
- Run `sbt clean test run` in **alert-config** folder to generate configuration and investigate the result in the target folder

## Scalafmt
This repository uses [Scalafmt](https://scalameta.org/scalafmt/), a code formatter for Scala.
The formatting rules configured for this repository are defined within [.scalafmt.conf](.scalafmt.conf).

Formatting is not integrated with build pipelines.
We encourage contributors to make sure their work is well formatted using the following before committing changes:

 ```
 # check
 sbt scalafmtCheckAll

 # Apply
 sbt scalafmtAll
 ```

[Visit the official Scalafmt documentation to view a complete list of tasks which can be run.](https://scalameta.org/scalafmt/docs/installation.html#task-keys)

## Local testing

If you want to build a local version of alert-config-builder and test it without doing a build of alert-config,
you can do a local build:


1. Do an `sbt publishLocal` to publish a snapshot (https://www.scala-sbt.org/1.x/docs/Publishing.html)
2. Configure your local `alert-config` project's `AppDepedencies` to reference your snapshot:
```scala
  val compile: Seq[ModuleID] = Seq(
      // "uk.gov.hmrc" %% "alert-config-builder" % "1.112.0"       // comment out the main stream version of alert-config-builder
      "uk.gov.hmrc" %% "alert-config-builder" % "1.113.0-SNAPSHOT" // your specific snapshot number will vary
  )
```

_Note that this is just for local testing - please do not try to commit this change!_

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
