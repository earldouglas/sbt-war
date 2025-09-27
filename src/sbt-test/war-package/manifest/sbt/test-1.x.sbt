val warFile = taskKey[File]("warFile")

warFile := sbt.Keys.`package`.value

val extraPackageOptions =
  taskKey[PackageOption]("extraPackageOptions")

extraPackageOptions :=
  Package.ManifestAttributes(
    java.util.jar.Attributes.Name.SEALED -> "true"
  )
