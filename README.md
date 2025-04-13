# fluxus-querystate

[![License: ISC](https://img.shields.io/badge/License-ISC-blue.svg)](https://opensource.org/license/isc-license-txt/)
![Scala Version](https://img.shields.io/badge/scala-3.6.4-blue.svg)
![ScalaJS Version](https://img.shields.io/badge/scalajs-1.18.2-blue.svg)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.edadma/fluxus-querystate_sjs1_3.svg?label=Maven%20Central)](https://central.sonatype.com/search?q=g%3Aio.github.edadma+a%3Afluxus-querystate_*)

A lightweight library for synchronizing [Fluxus](https://github.com/edadma/fluxus) application state with URL query parameters, enabling shareable URLs and browser history integration.

## Features

- 🔄 **Synchronize App State**: Keep application state in sync with URL query parameters
- 🔗 **Shareable URLs**: Generate URLs that preserve user's current view and filters
- 🧭 **Browser History Integration**: Support for browser back/forward navigation without page reloads
- 🪝 **Simple Hook API**: Familiar hook-based API for easy integration with Fluxus components
- 🔍 **Hash Mode Support**: Option to use hash-based URL parameters for compatibility with static hosting

## Installation

Add the dependency to your `build.sbt`:

```scala
libraryDependencies += "io.github.edadma" %%% "fluxus-querystate" % "0.0.2"
```

## Getting Started

### Basic Usage

```scala
import io.github.edadma.fluxus._
import io.github.edadma.fluxus.querystate.useQueryState

val YourComponent = (props: YourComponentProps) => {
  // Use query parameters in component
  val Seq(
    (activeTab, setActiveTab, _)
  ) = useQueryState(
    Seq(
      "tab" -> "home" // Default value
    ),
    useHash = false // Set to true to use hash-based URLs
  )
  
  div(
    // Navigation tabs
    div(
      button(
        cls := s"tab ${if (activeTab == "home") "active" else ""}",
        onClick := (() => setActiveTab("home")),
        "Home"
      ),
      button(
        cls := s"tab ${if (activeTab == "about") "active" else ""}",
        onClick := (() => setActiveTab("about")),
        "About"
      )
    ),
    
    // Content based on active tab
    div(
      activeTab match {
        case "about" => AboutTab <> ()
        case _ => HomeTab <> ()
      }
    )
  )
}
```

### API Reference

#### Hooks

```scala
// Use query parameters in a component - returns a sequence of (value, setValue, updateValue) tuples
val Seq(
  (value1, setValue1, updateValue1),
  (value2, setValue2, updateValue2),
  // ...more parameters
) = useQueryState(
  Seq(
    "key1" -> "defaultValue1", 
    "key2" -> "defaultValue2",
    // Use null for parameters without default values
    "key3" -> null
  ),
  useHash = false // Whether to use hash-based URLs
)
```

Each tuple contains:
- `value`: Current parameter value
- `setValue`: Function to set the parameter directly - `setValue(newValue)`
- `updateValue`: Function to update the parameter based on current value - `updateValue(currentValue => newValue)`

## Examples

### Filter and Sort

```scala
val ProductsTab = () => {
  // Use query parameters for filter and sort
  val Seq(
    (filter, setFilter, _),
    (sort, setSort, _)
  ) = useQueryState(
    Seq(
      "filter" -> null,  // No default filter (show all)
      "sort" -> "newest"  // Default sort is "newest"
    )
  )
  
  div(
    // Filter control
    div(
      label("Filter:"),
      select(
        value := filter,
        onChange := ((e: dom.Event) => 
          setFilter(e.target.asInstanceOf[dom.html.Select].value)
        ),
        option(value := "", "All"),
        option(value := "category1", "Category 1"),
        option(value := "category2", "Category 2")
      )
    ),
    
    // Sort control
    div(
      label("Sort by:"),
      select(
        value := sort,
        onChange := ((e: dom.Event) => 
          setSort(e.target.asInstanceOf[dom.html.Select].value)
        ),
        option(value := "newest", "Newest"),
        option(value := "oldest", "Oldest"),
        option(value := "price", "Price")
      )
    )
  )
}
```

### Complete E-commerce Example

The library includes a comprehensive demo in the examples project showing how to build a complete product listing page with:

- View toggle (grid/list)
- Category filtering
- Sorting options
- Pagination
- Items per page selector

Here's a simplified version of the key parts:

```scala
val ProductsPage = () => {
  // Use all query parameters in a single hook call
  val Seq(
    (view, setView, _),
    (category, setCategory, _),
    (sort, setSort, _),
    (page, setPage, updatePage),
    (perPage, setPerPage, _),
  ) = useQueryState(
    Seq(
      "view"     -> "grid",     // Default to grid view
      "category" -> null,       // No default category (show all)
      "sort"     -> "name-asc", // Default sort
      "page"     -> "1",        // Default to first page
      "perPage"  -> "6",        // Default items per page
    )
  )

  // Parse numeric parameters
  val currentPage = page.toIntOption.getOrElse(1)
  val itemsPerPage = perPage.toIntOption.getOrElse(6)

  // Rest of component logic...
  
  div(
    // View toggle
    div(
      button(
        cls := s"btn ${if (view == "grid") "btn-active" else ""}",
        onClick := (() => setView("grid")),
        "Grid"
      ),
      button(
        cls := s"btn ${if (view == "list") "btn-active" else ""}",
        onClick := (() => setView("list")),
        "List"
      )
    ),
    
    // Category selector
    select(
      value := (if (category == null) "" else category),
      onChange := ((e: dom.Event) => 
        setCategory(e.target.asInstanceOf[dom.html.Select].value)
      ),
      option(value := "", "All Categories"),
      // Category options...
    ),
    
    // Sort selector
    select(
      value := sort,
      onChange := ((e: dom.Event) => 
        setSort(e.target.asInstanceOf[dom.html.Select].value)
      ),
      // Sort options...
    ),
    
    // Pagination
    div(
      button(
        disabled := currentPage <= 1,
        onClick := (() => setPage((currentPage - 1).toString)),
        "Previous"
      ),
      // Page numbers...
      button(
        disabled := currentPage >= totalPages,
        onClick := (() => setPage((currentPage + 1).toString)),
        "Next"
      )
    )
  )
}
```

### Multi-step Form with History

```scala
val FormWizard = () => {
  // Use query parameter for current step
  val Seq((step, setStep, _)) = useQueryState(
    Seq("step" -> "1")
  )
  
  val stepNum = step.toIntOption.getOrElse(1)
  
  div(
    // Form steps
    div(
      cls := "steps",
      // Step indicators...
    ),
    
    // Current step content
    div(
      cls := "step-content",
      stepNum match {
        case 1 => Step1Form <> StepProps(onNext = () => setStep("2"))
        case 2 => Step2Form <> StepProps(
          onPrev = () => setStep("1"),
          onNext = () => setStep("3")
        )
        case 3 => Step3Form <> StepProps(onPrev = () => setStep("2"))
        case _ => div("Invalid step")
      }
    )
  )
}
```

## URL Formats

### Standard Mode (default)

```
https://yourapp.com/path?tab=home&filter=category1&sort=newest
```

### Hash Mode

```
https://yourapp.com/path#tab=home&filter=category1&sort=newest
```

## Benefits

- **Shareable URLs**: Users can share their exact application state through URLs
- **Browser Navigation**: Support for browser back/forward buttons without losing application state
- **Bookmarkable States**: Users can bookmark specific application states
- **Server-Side Rendering Compatibility**: Works well with server-side rendering approaches
- **SEO Benefits**: Search engines can index different application states when using standard query parameters

## Browser Support

Works with all modern browsers that support the History API.

## License

ISC License