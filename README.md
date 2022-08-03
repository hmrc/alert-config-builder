
# alert-config-builder

`alert-config-builder` is a Scala utility which, given an alert specification for a number of services, generates and emits JSON alert configuration documents for those services, suitable for indexing in Elasticsearch.

The artifact produced by this project is used in the `alert-config` project. The 2 repositories are kept separate due to the fact that the `alert-config` project is user editable yet we don't want to make the functionality exposed here editable.

# Dependencies

**Java** : 11\
**Scala** : 2.11.6\
**SBT** : 1.5.1\
**app-config** : Depends on the **app-config** Git repository for the environment for which the alert config is being generated e.g. **app-config-qa**.
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

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
