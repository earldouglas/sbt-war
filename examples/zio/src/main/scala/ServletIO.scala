object Request {

  import javax.servlet.ServletRequest
  import javax.servlet.ServletRequest
  import javax.servlet.ServletResponse
  import javax.servlet.http.HttpServletRequest
  import javax.servlet.http.HttpUpgradeHandler
  import scala.collection.JavaConverters._
  import zio.ZEnvironment
  import zio.ZIO

  def effect[A](
      k: HttpServletRequest => A
  ): ZIO[WithRequest, Throwable, A] =
    ZIO.environmentWithZIO(e => ZIO.attempt(k(e.get.request)))

  def effectO[A](
      k: HttpServletRequest => A
  ): ZIO[WithRequest, Throwable, Option[A]] =
    ZIO.environmentWithZIO(e => ZIO.attempt(Option(k(e.get.request))))

  def route[A, R <: WithRequest](
      k: PartialFunction[List[String], ZIO[R, Throwable, A]]
  ): ZIO[R, Throwable, A] =
    for {
      method <- getMethod
      context <- getServletPath
      uri <- getRequestURI
      a <- k(
        method :: uri
          .substring(context.length)
          .split("/")
          .drop(1)
          .toList
      )
    } yield a

  val headers: ZIO[WithRequest, Throwable, Map[String, List[String]]] =
    getHeaderNames() flatMap { namesO =>
      ZIO.collectAll {
        namesO
          .map(_.asScala.toList)
          .getOrElse(Nil)
          .map({ name =>
            getHeaders(name) map { valuesO =>
              (
                name,
                valuesO
                  .map(_.asScala.toList)
                  .getOrElse(Nil)
              )
            }
          })
      } map {
        _.toMap
      }
    }

  // ServletRequest methods:

  def getAsyncContext() =
    effect(_.getAsyncContext())

  def getAttribute(name: String) =
    effectO(_.getAttribute(name))

  def getAttributeNames() =
    effect(_.getAttributeNames())

  def getCharacterEncoding() =
    effectO(_.getCharacterEncoding())

  def getContentLength() =
    effect(_.getContentLength())

  def getContentLengthLong() =
    effect(_.getContentLengthLong())

  def getContentType() =
    effectO(_.getContentType())

  def getDispatcherType() =
    effect(_.getDispatcherType())

  def getInputStream() =
    effect(_.getInputStream())

  def getLocalAddr() =
    effect(_.getLocalAddr())

  def getLocalName() =
    effect(_.getLocalName())

  def getLocalPort() =
    effect(_.getLocalPort())

  def getLocale() =
    effect(_.getLocale())

  def getLocales() =
    effect(_.getLocales())

  def getParameter(name: String) =
    effectO(_.getParameter(name))

  def getParameterMap() =
    effect(_.getParameterMap())

  def getParameterNames() =
    effect(_.getParameterNames())

  def getParameterValues(name: String) =
    effectO(_.getParameterValues(name))

  def getProtocol() =
    effect(_.getProtocol())

  def getReader() =
    effect(_.getReader())

  def getRemoteAddr() =
    effect(_.getRemoteAddr())

  def getRemoteHost() =
    effect(_.getRemoteHost())

  def getRemotePort() =
    effect(_.getRemotePort())

  def getRequestDispatcher(path: String) =
    effectO(_.getRequestDispatcher(path))

  def getScheme() =
    effect(_.getScheme())

  def getServerName() =
    effect(_.getServerName())

  def getServerPort() =
    effect(_.getServerPort())

  def getServletContext() =
    effect(_.getServletContext())

  def isAsyncStarted() =
    effect(_.isAsyncStarted())

  def isAsyncSupported() =
    effect(_.isAsyncSupported())

  def isSecure() =
    effect(_.isSecure())

  def removeAttribute(name: String) =
    effect(_.removeAttribute(name))

  def setAttribute(name: String, o: Object) =
    effect(_.setAttribute(name, o))

  def setCharacterEncoding(env: String) =
    effect(_.setCharacterEncoding(env))

  def startAsync() =
    effect(_.startAsync())

  def startAsync(
      servletRequest: ServletRequest,
      servletResponse: ServletResponse
  ) =
    effect(_.startAsync(servletRequest, servletResponse))

  // HttpServletRequest methods:

  def authenticate
      : ZIO[WithRequest with WithResponse, Throwable, Boolean] =
    ZIO.environmentWithZIO { req: ZEnvironment[WithRequest] =>
      ZIO.environmentWithZIO { res: ZEnvironment[WithResponse] =>
        ZIO.attempt(req.get.request.authenticate(res.get.response))
      }
    }

  def changeSessionId() =
    effect(_.changeSessionId())

  def getAuthType() =
    effectO(_.getAuthType())

  def getContextPath() =
    effect(_.getContextPath())

  def getCookies() =
    effectO(_.getCookies())

  def getDateHeader(name: String) =
    effect(_.getDateHeader(name))

  def getHeader(name: String) =
    effectO(_.getHeader(name))

  def getHeaderNames() =
    effectO(_.getHeaderNames())

  def getHeaders(name: String) =
    effectO(_.getHeaders(name))

  def getIntHeader(name: String) =
    effect(_.getIntHeader(name))

  def getMethod() =
    effect(_.getMethod())

  def getPart(name: String) =
    effectO(_.getPart(name))

  def getParts() =
    effect(_.getParts())

  def getPathInfo() =
    effectO(_.getPathInfo())

  def getPathTranslated() =
    effectO(_.getPathTranslated())

  def getQueryString() =
    effectO(_.getQueryString())

  def getRemoteUser() =
    effectO(_.getRemoteUser())

  def getRequestURI() =
    effect(_.getRequestURI())

  def getRequestURL() =
    effect(_.getRequestURL())

  def getRequestedSessionId() =
    effectO(_.getRequestedSessionId())

  def getServletPath() =
    effect(_.getServletPath())

  def getSession() =
    effect(_.getSession())

  def getSession(create: Boolean) =
    effectO(_.getSession(create))

  def getUserPrincipal() =
    effectO(_.getUserPrincipal())

  def isRequestedSessionIdFromCookie() =
    effect(_.isRequestedSessionIdFromCookie())

  def isRequestedSessionIdFromURL() =
    effect(_.isRequestedSessionIdFromURL())

  def isRequestedSessionIdValid() =
    effect(_.isRequestedSessionIdValid())

  def isUserInRole(role: String) =
    effect(_.isUserInRole(role))

  def login(username: String, password: String) =
    effect(_.login(username, password))

  def logout() =
    effect(_.logout())

  def upgrade[T <: HttpUpgradeHandler](handlerClass: Class[T]) =
    effect(_.upgrade(handlerClass))
}

object Response {

  import java.util.Locale
  import javax.servlet.http.Cookie
  import javax.servlet.http.HttpServletResponse
  import zio.ZIO

  def effect[A](
      k: HttpServletResponse => A
  ): ZIO[WithResponse, Throwable, A] =
    ZIO.environmentWithZIO(e => ZIO.attempt(k(e.get.response)))

  def effectO[A](
      k: HttpServletResponse => A
  ): ZIO[WithResponse, Throwable, Option[A]] =
    ZIO.environmentWithZIO(e => ZIO.attempt(Option(k(e.get.response))))

  // ServletResponse methods:

  def flushBuffer() =
    effect(_.flushBuffer())

  def getBufferSize() =
    effect(_.getBufferSize())

  def getCharacterEncoding() =
    effect(_.getCharacterEncoding())

  def getContentType() =
    effectO(_.getContentType())

  def getLocale() =
    effect(_.getLocale())

  def getOutputStream() =
    effect(_.getOutputStream())

  def getWriter() =
    effect(_.getWriter())

  def isCommitted() =
    effect(_.isCommitted())

  def reset() =
    effect(_.reset())

  def resetBuffer() =
    effect(_.resetBuffer())

  def setBufferSize(size: Int) =
    effect(_.setBufferSize(size))

  def setCharacterEncoding(charset: String) =
    effect(_.setCharacterEncoding(charset))

  def setContentLength(len: Int) =
    effect(_.setContentLength(len))

  def setContentLengthLong(len: Long) =
    effect(_.setContentLengthLong(len))

  def setContentType(contentType: String) =
    effect(_.setContentType(contentType))

  def setLocale(loc: Locale) =
    effect(_.setLocale(loc))

  // HttpServletResponse methods:

  def addCookie(cookie: Cookie) =
    effect(_.addCookie(cookie))

  def addDateHeader(name: String, date: Long) =
    effect(_.addDateHeader(name, date))

  def addHeader(name: String, value: String) =
    effect(_.addHeader(name, value))

  def addIntHeader(name: String, value: Int) =
    effect(_.addIntHeader(name, value))

  def containsHeader(name: String) =
    effect(_.containsHeader(name))

  def encodeRedirectURL(url: String) =
    effect(_.encodeRedirectURL(url))

  def encodeURL(url: String) =
    effect(_.encodeURL(url))

  def getHeader(name: String) =
    effectO(_.getHeader(name))

  def getHeaderNames() =
    effect(_.getHeaderNames())

  def getHeaders(name: String) =
    effect(_.getHeaders(name))

  def getStatus() =
    effect(_.getStatus())

  def sendError(sc: Int) =
    effect(_.sendError(sc))

  def sendError(sc: Int, msg: String) =
    effect(_.sendError(sc, msg))

  def sendRedirect(location: String) =
    effect(_.sendRedirect(location))

  def setDateHeader(name: String, date: Long) =
    effect(_.setDateHeader(name, date))

  def setHeader(name: String, value: String) =
    effect(_.setHeader(name, value))

  def setIntHeader(name: String, value: Int) =
    effect(_.setIntHeader(name, value))

  def setStatus(sc: Int) =
    effect(_.setStatus(sc))
}
