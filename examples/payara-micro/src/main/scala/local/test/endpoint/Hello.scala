package local.test.endpoint

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("hello")
class Hello {

  @GET
  @Produces(Array(MediaType.TEXT_PLAIN))
  def getMessage(@QueryParam("name") name: String): Response = {
    val greeting = if (name == null || name.isEmpty) "Nobody" else name
    Response.ok("Hallo " + greeting + "\n").build
  }
}
