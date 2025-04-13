package io.github.edadma.fluxus.querystate

import io.github.edadma.fluxus._
import org.scalajs.dom
import org.scalajs.dom.window
import scala.scalajs.js.URIUtils

/** A hook to use URL query parameters as component state.
  *
  * @param params
  *   Sequence of (key, defaultValue) pairs. Use null for parameters without a default.
  * @param useHash
  *   Whether to use hash-based URL parameters
  * @return
  *   Sequence of (current value, setter function, updater function) tuples in the same order as input params
  */
def useQueryState(
    params: Seq[(String, String)],
    useHash: Boolean = false,
): Seq[(String, String => Unit, (String => String) => Unit)] = {
  // This will force component re-renders when URL changes
  val (_, _, setForceUpdate) = useState(0)

  // Get the current URL parameters
  val urlParams = parseQueryParams(useHash)

  // Apply defaults for missing parameters, preserving URL values exactly as they are
  val currentValues = params.map { case (key, defaultValue) =>
    // If parameter exists in URL, use it exactly as is
    if (urlParams.contains(key)) {
      key -> urlParams(key)
    } else {
      // If not in URL, use default (empty for null)
      val effectiveDefault = if (defaultValue == null) "" else defaultValue
      key -> effectiveDefault
    }
  }.toMap

  // Set up listener for URL changes (browser navigation)
  useEffect(
    () => {
      // Only update URL if non-null defaults need to be applied
      val needsUrlUpdate = params.exists { case (key, defaultValue) =>
        defaultValue != null &&
        defaultValue.nonEmpty &&
        !urlParams.contains(key)
      }

      if (needsUrlUpdate) {
        // Only include non-empty values in URL
        val urlValues = currentValues.filter(_._2.nonEmpty)
        updateQueryParams(urlValues, useHash)
      }

      // Set up browser navigation event listener
      val handler = (_: dom.Event) => {
        // Force component to re-render with new URL params
        setForceUpdate(_ => System.currentTimeMillis().toInt)
      }

      window.addEventListener("popstate", handler)

      // Clean up listener on unmount
      () => window.removeEventListener("popstate", handler)
    },
    Seq(), // Only run on mount/unmount
  )

  // Create parameter tuples for each param
  params.map { case (key, _) =>
    val currentValue = currentValues.getOrElse(key, "")

    // Setter function - updates the URL directly
    val setValue = (newValue: String) => {
      // Get current URL params
      val current = parseQueryParams(useHash)

      // Update with new value
      val updated = if (newValue.isEmpty) {
        current - key
      } else {
        current + (key -> newValue)
      }

      // Update URL
      updateQueryParams(updated, useHash)

      // Force component to re-render with new URL params
      setForceUpdate(_ => System.currentTimeMillis().toInt)
    }

    // Updater function - updates based on current value
    val updateValue = (updateFn: String => String) => {
      val current           = parseQueryParams(useHash)
      val currentParamValue = current.getOrElse(key, "")
      val newValue          = updateFn(currentParamValue)

      // Update with new value
      val updated = if (newValue.isEmpty) {
        current - key
      } else {
        current + (key -> newValue)
      }

      // Update URL
      updateQueryParams(updated, useHash)

      // Force component to re-render with new URL params
      setForceUpdate(_ => System.currentTimeMillis().toInt)
    }

    (currentValue, setValue, updateValue)
  }
}

// Helper functions
private def parseQueryParams(useHash: Boolean): Map[String, String] = {
  val queryString = if (useHash) {
    window.location.hash.stripPrefix("#")
  } else {
    window.location.search.stripPrefix("?")
  }

  if (queryString.isEmpty) Map.empty
  else {
    queryString.split("&").map { param =>
      val parts = param.split("=")
      if (parts.length > 1) parts(0) -> URIUtils.decodeURIComponent(parts(1))
      else parts(0)                  -> ""
    }.toMap
  }
}

private def updateQueryParams(params: Map[String, String], useHash: Boolean): Unit = {
  val filteredParams = params.filter(_._2.nonEmpty)
  val queryString = filteredParams.map { case (key, value) =>
    s"$key=${URIUtils.encodeURIComponent(value)}"
  }.mkString("&")

  val url = if (useHash) {
    if (queryString.isEmpty) window.location.pathname else s"${window.location.pathname}#$queryString"
  } else {
    if (queryString.isEmpty) window.location.pathname else s"${window.location.pathname}?$queryString"
  }

  // Update browser URL
  window.history.pushState(null, "", url)
}
