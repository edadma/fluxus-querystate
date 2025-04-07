package io.github.edadma.fluxus.querystate.examples

import io.github.edadma.fluxus._
import io.github.edadma.fluxus.querystate.QueryState
import io.github.edadma.fluxus.querystate.useQueryParam
import org.scalajs.dom

@main def run(): Unit = {
  // Initialize with default values
  QueryState.init(
    defaults = Map(
      "tab"    -> "home",
      "filter" -> "",
      "sort"   -> "newest",
    ),
  )

  // Render app to DOM
  render(TestApp, "app")
}

// Main App component
def TestApp: FluxusNode = {
  // Use query parameters in component
  val (activeTab, setActiveTab, _) = useQueryParam("tab", "home")

  useQueryParam("filter", "")
  useQueryParam("sort", "newest")

  div(
    cls := "container mx-auto p-4",

    // Header - Changed to primary color from DaisyUI
    div(
      cls := "bg-primary text-primary-content p-4 mb-4 rounded shadow-lg",
      h1(cls := "text-2xl font-bold", "QueryState Demo"),
    ),

    // Navigation tabs - Using DaisyUI tabs
    div(
      cls := "tabs tabs-boxed mb-4",
      button(
        cls     := s"tab ${if (activeTab == "home") "tab-active" else ""}",
        onClick := (() => setActiveTab("home")),
        "Home",
      ),
      button(
        cls     := s"tab ${if (activeTab == "products") "tab-active" else ""}",
        onClick := (() => setActiveTab("products")),
        "Products",
      ),
      button(
        cls     := s"tab ${if (activeTab == "about") "tab-active" else ""}",
        onClick := (() => setActiveTab("about")),
        "About",
      ),
    ),

    // Current URL display - Using DaisyUI card with neutral background
    div(
      cls := "card bg-base-200 p-2 mb-4 shadow-sm",
      div(
        cls := "card-body p-3",
        p("Current URL:"),
        code(cls := "text-sm bg-base-300 p-1 rounded", dom.window.location.href),
      ),
    ),

    // Tab content - Using DaisyUI card
    div(
      cls := "card bg-base-100 shadow p-4 rounded",
      div(
        cls := "card-body",
        activeTab match {
          case "products" => ProductsTab <> ()
          case "about"    => AboutTab <> ()
          case _          => HomeTab <> ()
        },
      ),
    ),

    // Reset button - Using DaisyUI button
    div(
      cls := "mt-4",
      button(
        cls     := "btn btn-error",
        onClick := (() => QueryState.reset()),
        "Reset URL State",
      ),
    ),

    // Footer
    div(
      cls := "mt-8 pt-4 border-t text-center text-base-content opacity-70",
      p("Try navigating with the browser's back/forward buttons"),
    ),
  )
}

// Home tab content
val HomeTab = () => {
  div(
    h2(cls := "text-xl font-bold mb-4", "Welcome to QueryState Demo"),
    p(
      "This demo shows how to synchronize application state with URL query parameters.",
      br(),
      "Try clicking the tabs above and notice how the URL changes.",
    ),
    div(
      cls := "alert alert-info mt-4",
      h3(cls := "font-bold", "Features:"),
      ul(
        cls := "list-disc pl-5",
        li("URL reflects application state"),
        li("Browser back/forward navigation works"),
        li("Shareable URLs with current state"),
        li("No full page reloads"),
      ),
    ),
  )
}

// Products tab content
val ProductsTab = () => {
  // Use query parameters for filter and sort
  val (filter, setFilter, _) = useQueryParam("filter", "")
  val (sort, setSort, _)     = useQueryParam("sort", "newest")

  // Product data
  val products = Seq(
    ("1", "Laptop", "Electronics", 999.99),
    ("2", "Desk Chair", "Furniture", 199.50),
    ("3", "Coffee Maker", "Kitchen", 49.99),
    ("4", "Headphones", "Electronics", 149.99),
    ("5", "Bookshelf", "Furniture", 89.99),
  )

  // Apply filter
  val filteredProducts = if (filter.isEmpty) {
    products
  } else {
    products.filter(_._3.toLowerCase == filter.toLowerCase)
  }

  // Apply sorting
  val sortedProducts = sort match {
    case "price-low"  => filteredProducts.sortBy(_._4)
    case "price-high" => filteredProducts.sortBy(-_._4)
    case _            => filteredProducts // "newest" - keep default order
  }

  div(
    h2(cls := "text-xl font-bold mb-4", "Products"),

    // Filter and sort controls - Using DaisyUI form elements
    div(
      cls := "grid grid-cols-1 md:grid-cols-2 gap-4 mb-4",
      div(
        label(cls := "label", span(cls := "label-text", "Filter by Category")),
        select(
          cls   := "select select-bordered w-full",
          value := filter,
          onChange := ((e: dom.Event) =>
            setFilter(e.target.asInstanceOf[dom.html.Select].value)
          ),
          option(value := "", "All Categories"),
          option(value := "electronics", "Electronics"),
          option(value := "furniture", "Furniture"),
          option(value := "kitchen", "Kitchen"),
        ),
      ),
      div(
        label(cls := "label", span(cls := "label-text", "Sort by")),
        select(
          cls   := "select select-bordered w-full",
          value := sort,
          onChange := ((e: dom.Event) =>
            setSort(e.target.asInstanceOf[dom.html.Select].value)
          ),
          option(value := "newest", "Newest"),
          option(value := "price-low", "Price: Low to High"),
          option(value := "price-high", "Price: High to Low"),
        ),
      ),
    ),

    // Product list - Using DaisyUI cards
    div(
      cls := "grid grid-cols-1 gap-4",
      sortedProducts.map { case (id, name, category, price) =>
        div(
          key := id,
          cls := "card bg-base-200 shadow-sm",
          div(
            cls := "card-body p-4",
            div(cls := "card-title", name),
            div(cls := "text-sm opacity-70", s"Category: $category"),
            div(cls := "mt-2 text-accent font-bold", f"$$${price}%.2f"),
          ),
        )
      },
    ),

    // Empty state - Using DaisyUI alert
    if (sortedProducts.isEmpty) {
      div(
        cls := "alert alert-warning my-4",
        div(
          "No products match your filter criteria",
        ),
      )
    } else null,
  )
}

// About tab content
val AboutTab = () => {
  div(
    h2(cls := "text-xl font-bold mb-4", "About QueryState"),
    p(cls  := "mb-4", "QueryState is a lightweight library for Fluxus applications that:"),
    ul(
      cls := "list-disc pl-5 mb-4",
      li("Synchronizes application state with URL query parameters"),
      li("Maintains state across page refreshes"),
      li("Enables shareable URLs with preserved state"),
      li("Integrates with browser history for back/forward navigation"),
      li("Provides a simple hook-based API for components"),
    ),
    div(
      cls := "mockup-code bg-base-300 p-4 rounded",
      pre(
        cls := "overflow-auto",
        code(
          "// Initialize with defaults\n" +
            "QueryState.init(\n" +
            "  defaults = Map(\n" +
            "    \"tab\" -> \"home\",\n" +
            "    \"filter\" -> \"\"\n" +
            "  )\n" +
            ")\n\n" +
            "// In a component\n" +
            "val (filter, setFilter) = useQueryParam(\"filter\", \"\")\n\n" +
            "// Update parameter\n" +
            "setFilter(\"new-value\")",
        ),
      ),
    ),
  )
}
