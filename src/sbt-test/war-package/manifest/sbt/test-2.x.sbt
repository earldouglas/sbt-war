val warFile = taskKey[File]("warFile")

warFile :=
  fileConverter.value
    .toPath(sbt.Keys.`package`.value)
    .toFile()

val extraPackageOptions =
  taskKey[PackageOption]("extraPackageOptions")

extraPackageOptions :=
  Package.ManifestAttributes(
    java.util.jar.Attributes.Name.SEALED.toString() -> "true",
  )
