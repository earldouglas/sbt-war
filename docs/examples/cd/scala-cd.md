% Continuous deployment for Scala
% James Earl Douglas
% June 07, 2014

Here we outline the steps to take to enable continuous deployment for Scala
Web projects that are based on [xsbt-web-plugin](https://github.com/earldouglas/xsbt-web-plugin/).

This builds on [continuous integration for scala](scala-ci.html),
adding automated *.war* file deployment to Heroku.

## Heroku

First, create a new [Heroku](http://heroku.com/) app.  In the steps below, we'll
call the app *scala-sd*.

## Scala Web project

*build.sbt:*

```scala
name := "scala-cd"
```

## Continuous integration

Follow steps outlined in [continuous integration for
scala](/posts/scala-ci.html).

## Automate deployment to Heroku

Heroku supports deployment of *.war* files to Heroku using the Heroku Toolbelt
and its *heroku-deploy* plugin.  We can use this from sbt with the
[sbt-heroku-deploy](https://github.com/earldouglas/sbt-heroku-deploy/) plugin.

*project/plugins.sbt:*

```scala
addSbtPlugin("com.earldouglas" % "sbt-heroku-deploy" % "0.1.0")
```

*build.sbt:*

```scala
enablePlugins(JettyPlugin, HerokuDeploy)

herokuAppName := "scala-cd"
```

Next, we need to provide Travis CI with our Heroku deployment credentials, so let's
encrypt them and add them to *.travis.yml*.  See the related
[Travis CI docs](http://docs.travis-ci.com/user/deployment/heroku/)
for more information.

Run the following from your project directory:

```bash
travis encrypt HEROKU_API_KEY=`heroku auth:token` --add
```

This adds an encrypted version of your Heroku API Key to *.travis.yml*:

```yaml
env:
  global:
    secure: [a long, ecnrypted string]
```

Finally, we configure Travis CI to run the deployment task for successful
builds.  Remove the old `script` line from *.travis.yml*, and replace
it with the following.

*.travis.yml:*

```yaml
script:
- sbt coveralls package
after_success:
  if ([ "$TRAVIS_BRANCH" == "master" ] || [ ! -z "$TRAVIS_TAG" ]) &&
      [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
    sbt herokuDeploy;
  fi
```

Now when we push changs to GitHub, Travis CI picks them up, and runs a
build via `sbt coveralls package`.  If the changes were pushed to the
master branch, Travis CI pushes the packaged *.war* file to Heroku.
