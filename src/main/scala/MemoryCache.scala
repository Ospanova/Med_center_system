import akka.http.caching.scaladsl.Cache
import akka.http.caching.scaladsl.CachingSettings
import akka.http.caching.LfuCache
import akka.http.scaladsl.server.RequestContext
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.directives.CachingDirectives._

// Use the request's URI as the cache's key
val keyerFunction: PartialFunction[RequestContext, Uri] = {
    case r: RequestContext ⇒ r.request.uri
}
val defaultCachingSettings = CachingSettings(system)
val lfuCacheSettings =
    defaultCachingSettings.lfuCacheSettings
      .withInitialCapacity(25)
      .withMaxCapacity(50)
      .withTimeToLive(20.seconds)
      .withTimeToIdle(10.seconds)
val cachingSettings =
    defaultCachingSettings.withLfuCacheSettings(lfuCacheSettings)
val lfuCache: Cache[Uri, RouteResult] = LfuCache(cachingSettings)

// Create the route
val route = cache(lfuCache, keyerFunction)(innerRoute)