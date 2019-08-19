import java.net.InetSocketAddress
import java.net.Socket

object AwaitPort {

  def apply(port: Int, retries: Int = 40): Unit =
    try {
      val socket = new Socket()
      socket.connect(new InetSocketAddress("localhost", port))
      socket.close()
    } catch {
      case _: Exception =>
        if (retries > 0) {
          Thread.sleep(250)
          apply(port, retries - 1)
        } else {
          throw new Exception(s"expected port $port to be open")
        }
    }
}
