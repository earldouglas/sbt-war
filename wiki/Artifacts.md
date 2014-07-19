## *.war* file

To disable publishing of the *.war* file, add the setting:

```scala
packagedArtifacts <<= packagedArtifacts map { as => as.filter(_._1.`type` != "war") }
```

Note that `package` can still be used to create the *.war* file under the project *target/* directory.

### Massaging the *.war* file

```scala
warPostProcess in Compile <<= (target) map {
  (target) => { 
    () =>
      val webapp = target / "webapp"
      IO.delete(webapp / "test.html")
  }
}
```

## *.jar* file

To enable publishing of the project's *.jar* file, add the setting:

```scala
publishArtifact in (Compile, packageBin) := true
```