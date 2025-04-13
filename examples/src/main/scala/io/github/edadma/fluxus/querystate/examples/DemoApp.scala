package io.github.edadma.fluxus.querystate.examples

import io.github.edadma.fluxus._
import io.github.edadma.fluxus.querystate.useQueryParams
import org.scalajs.dom

@main def run(): Unit = {
  // Render app to DOM without any initialization needed
  render(DemoApp, "app")
}

// Sample product data
val products = Seq(
  ("p1", "Laptop Pro", "Electronics", 1299.99, 4.5),
  ("p2", "Ergonomic Chair", "Furniture", 249.50, 4.2),
  ("p3", "Smart Coffee Maker", "Kitchen", 89.99, 3.9),
  ("p4", "Noise-Cancelling Headphones", "Electronics", 199.99, 4.8),
  ("p5", "Modern Bookshelf", "Furniture", 129.99, 4.0),
  ("p6", "Wireless Charger", "Electronics", 39.99, 3.7),
  ("p7", "Standing Desk", "Furniture", 349.50, 4.3),
  ("p8", "Blender", "Kitchen", 79.99, 4.1),
  ("p9", "Smart Speaker", "Electronics", 129.99, 4.4),
  ("p10", "Kitchen Island", "Furniture", 399.99, 4.6),
  ("p11", "Toaster Oven", "Kitchen", 59.99, 3.8),
  ("p12", "Tablet", "Electronics", 349.99, 4.7),
)

// Main App component
def DemoApp: FluxusNode = {
  // Use all query parameters in a single hook call
  val Seq(
    (view, setView, _),
    (category, setCategory, _),
    (sort, setSort, _),
    (page, setPage, updatePage),
    (perPage, setPerPage, _),
  ) = useQueryParams(
    Seq(
      "view"     -> "grid",     // Default to grid view
      "category" -> null,       // No default category (show all)
      "sort"     -> "name-asc", // Default sort
      "page"     -> "1",        // Default to first page
      "perPage"  -> "6",        // Default items per page
    ),
    useHash = false, // Use standard query parameters
  )

  // Parse numeric parameters
  val currentPage  = page.toIntOption.getOrElse(1)
  val itemsPerPage = perPage.toIntOption.getOrElse(6)

  // Filter by category
  val filteredProducts = if (category == null || category.isEmpty) {
    products
  } else {
    products.filter(_._3.toLowerCase == category.toLowerCase)
  }

  // Apply sorting
  val sortedProducts = sort match {
    case "price-asc"   => filteredProducts.sortBy(_._4)
    case "price-desc"  => filteredProducts.sortBy(-_._4)
    case "rating-desc" => filteredProducts.sortBy(-_._5)
    case "name-desc"   => filteredProducts.sortBy(_._2).reverse
    case _             => filteredProducts.sortBy(_._2) // "name-asc" is default
  }

  // Apply pagination
  val totalProducts = sortedProducts.length
  val totalPages    = Math.ceil(totalProducts.toDouble / itemsPerPage).toInt
  val pagedProducts = sortedProducts.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage)

  div(
    cls := "container mx-auto p-4",

    // Header
    div(
      cls := "bg-primary text-primary-content p-4 mb-6 rounded shadow",
      h1(cls := "text-2xl font-bold", "QueryState Demo"),
      p("Current URL:"),
      code(cls := "text-sm bg-base-300 p-1 rounded text-base-content", dom.window.location.href),
    ),

    // Control panel
    div(
      cls := "grid grid-cols-1 md:grid-cols-4 gap-4 mb-6",

      // View toggle
      div(
        cls := "form-control",
        label(cls := "label", "View"),
        div(
          cls := "btn-group",
          button(
            cls     := s"btn ${if (view == "grid") "btn-active" else ""}",
            onClick := (() => setView("grid")),
            "Grid",
          ),
          button(
            cls     := s"btn ${if (view == "list") "btn-active" else ""}",
            onClick := (() => setView("list")),
            "List",
          ),
        ),
      ),

      // Category filter
      div(
        cls := "form-control",
        label(cls := "label", "Category"),
        select(
          cls   := "select select-bordered w-full",
          value := (if (category == null) "" else category),
          onChange := ((e: dom.Event) =>
            setCategory(e.target.asInstanceOf[dom.html.Select].value)
          ),
          option(value := "", "All Categories"),
          option(value := "electronics", "Electronics"),
          option(value := "furniture", "Furniture"),
          option(value := "kitchen", "Kitchen"),
        ),
      ),

      // Sort control
      div(
        cls := "form-control",
        label(cls := "label", "Sort By"),
        select(
          cls   := "select select-bordered w-full",
          value := sort,
          onChange := ((e: dom.Event) =>
            setSort(e.target.asInstanceOf[dom.html.Select].value)
          ),
          option(value := "name-asc", "Name (A-Z)"),
          option(value := "name-desc", "Name (Z-A)"),
          option(value := "price-asc", "Price (Low to High)"),
          option(value := "price-desc", "Price (High to Low)"),
          option(value := "rating-desc", "Top Rated"),
        ),
      ),

      // Items per page
      div(
        cls := "form-control",
        label(cls := "label", "Items Per Page"),
        select(
          cls   := "select select-bordered w-full",
          value := perPage,
          onChange := ((e: dom.Event) => {
            setPerPage(e.target.asInstanceOf[dom.html.Select].value)
            setPage("1") // Reset to first page when changing items per page
          }),
          option(value := "3", "3"),
          option(value := "6", "6"),
          option(value := "9", "9"),
          option(value := "12", "12"),
        ),
      ),
    ),

    // Results info
    div(
      cls := "mb-4",
      p(
        cls := "text-base-content/70",
        s"Showing ${if (totalProducts > 0) (currentPage - 1) * itemsPerPage + 1 else 0} - " +
          s"${Math.min(currentPage * itemsPerPage, totalProducts)} of $totalProducts products",
      ),
    ),

    // Product list
    div(
      cls := (if (view == "grid") "grid grid-cols-1 md:grid-cols-3 gap-4" else "space-y-4"),
      if (pagedProducts.isEmpty) {
        div(
          cls := "col-span-full alert alert-info",
          "No products match your filter criteria.",
        )
      } else {
        pagedProducts.map { case (id, name, category, price, rating) =>
          if (view == "grid") {
            // Grid view
            div(
              key := id,
              cls := "card bg-base-200 shadow-sm",
              div(
                cls := "card-body p-4",
                h3(cls  := "card-title", name),
                div(cls := "badge badge-neutral", category),
                div(
                  cls := "flex justify-between items-center mt-4",
                  div(cls := "text-accent font-bold", f"$$${price}%.2f"),
                  div(cls := "flex items-center", span(cls := "text-yellow-400 mr-1", "★"), span(f"$rating%.1f")),
                ),
              ),
            )
          } else {
            // List view
            div(
              key := id,
              cls := "flex items-center p-4 bg-base-200 rounded shadow-sm",
              div(
                cls := "flex-grow",
                h3(cls  := "font-bold", name),
                div(cls := "text-base-content/70", category),
              ),
              div(
                cls := "flex items-center space-x-4",
                div(cls := "flex items-center", span(cls := "text-yellow-400 mr-1", "★"), span(f"$rating%.1f")),
                div(cls := "text-accent font-bold", f"$$${price}%.2f"),
              ),
            )
          }
        }
      },
    ),

    // Pagination
    if (totalPages > 1) {
      div(
        cls := "flex justify-center mt-6 space-x-2",
        button(
          cls      := "btn btn-sm",
          disabled := currentPage <= 1,
          onClick  := (() => setPage((currentPage - 1).toString)),
          "Previous",
        ),

        // Page number buttons
        (1 to totalPages).map { p =>
          if (
            p == 1 ||
            p == totalPages ||
            (p >= currentPage - 1 && p <= currentPage + 1)
          ) {
            button(
              key     := p.toString,
              cls     := s"btn btn-sm ${if (p == currentPage) "btn-primary" else ""}",
              onClick := (() => setPage(p.toString)),
              p.toString,
            )
          } else if (p == currentPage - 2 || p == currentPage + 2) {
            button(
              key := s"ellipsis-$p",
              cls := "btn btn-sm btn-disabled",
              "...",
            )
          } else null
        },
        button(
          cls      := "btn btn-sm",
          disabled := currentPage >= totalPages,
          onClick  := (() => setPage((currentPage + 1).toString)),
          "Next",
        ),
      )
    } else null,

    // Information about the library
    div(
      cls := "mt-8 pt-4 border-t text-center text-base-content/70",
      p(
        "This demo showcases the enhanced QueryState library. Try:",
        br(),
        "• Using the filters and controls above",
        br(),
        "• Refreshing the page (state is preserved)",
        br(),
        "• Using browser back/forward buttons",
        br(),
        "• Sharing the URL (it contains all your selections)",
      ),
    ),
  )
}
