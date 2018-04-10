package bootstrap.liftweb

import net.liftweb.http.Html5Properties
import net.liftweb.http.LiftRules
import net.liftweb.http.Req
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.SiteMap

class Boot {

  def boot {

    LiftRules.addToPackages("code")

    def sitemap(): SiteMap = SiteMap(
      Menu.i("Home") / "index"
    )

    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

  }

}
