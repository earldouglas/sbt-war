name := "test"

version := "0.1.0-SNAPSHOT"

webappSettings

postProcess := {
  webappDir =>
    import java.io.File
    import com.yahoo.platform.yui.compressor.YUICompressor
    val src  = new File(webappDir, "script.js")
    val dest = new File(webappDir, "script-min.js")
    YUICompressor.main(Array(src.getPath, "-o", dest.getPath))
}
