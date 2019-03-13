package local.test.endpoint

import javax.ws.rs.core.{MediaType, Response}
import javax.ws.rs.{GET, Path, Produces, QueryParam}

@Path("/hello")
class Hello {

  @GET
  @Produces(Array(MediaType.TEXT_PLAIN))
  def getMessage(@QueryParam("name") name: String): Response = {
    val greeting = if (name == null || name.isEmpty) "Nobody" else name
    Response.ok("Hallo " + greeting + "\n").build
  }
}
