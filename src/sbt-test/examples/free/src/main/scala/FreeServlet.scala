import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class FreeServlet extends HttpServlet {

  implicit val responseMonad: Monad[Either[WebAppErr, ?]] =
    new Monad[Either[WebAppErr, ?]] {
      def pure[A](a: A): Either[WebAppErr, ?][A] = Right(a)
      def bind[A, B](
          fa: Either[WebAppErr, ?][A]
      )(f: A => Either[WebAppErr, ?][B]): Either[WebAppErr, ?][B] =
        fa match {
          case Left(e)  => Left(e)
          case Right(a) => f(a)
        }
    }

  val database = new Database

  def interpreter(
      req: HttpServletRequest
  ): WebAppOp ~> Either[WebAppErr, ?] =
    new ~>[WebAppOp, Either[WebAppErr, ?]] {
      def apply[A](c: WebAppOp[A]): Either[WebAppErr, ?][A] =
        try {
          c match {
            case Value(x)    => Right(x)
            case GetDatabase => Right(database)
            case RequestHeader(name) =>
              Option(req.getHeader(name))
                .map(Right(_))
                .getOrElse(Left(MissingHeader(name)))
            case RequestParam(name) =>
              Option(req.getParameter(name))
                .map(Right(_))
                .getOrElse(Left(MissingParameter(name)))
            case Error(e) => Left(e)
          }
        } catch {
          case t: Throwable =>
            Left(InternalServerError(t))
        }
    }

  override def doPost(
      req: HttpServletRequest,
      res: HttpServletResponse
  ): Unit = {
    WebAppOp.signIn.foldMap(interpreter(req)) match {
      case Right(sessionId) =>
        res.setStatus(201)
        res.setHeader("X-Session-ID", sessionId)
      case Left(e) =>
        res.setStatus(e.status)
        res.getWriter.write(e.message)
    }
  }

  override def doGet(
      req: HttpServletRequest,
      res: HttpServletResponse
  ): Unit = {
    WebAppOp.getSecret.foldMap(interpreter(req)) match {
      case Right(secret) =>
        res.setStatus(200)
        res.getWriter.write(secret)
      case Left(e) =>
        res.setStatus(e.status)
        res.getWriter.write(e.message)
    }
  }
}
