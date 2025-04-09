# fluxus-querystate

[![License: ISC](https://img.shields.io/badge/License-ISC-blue.svg)](https://opensource.org/license/isc-license-txt/)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.18.2.svg)](https://www.scala-js.org)
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
libraryDependencies += "io.github.edadma" %%% "fluxus-querystate" % "0.0.1"
```

## Getting Started

### Basic Usage

```scala
import io.github.edadma.fluxus._
import io.github.edadma.fluxus.querystate.QueryState
import io.github.edadma.fluxus.querystate.useQueryParam

// Initialize with default values
QueryState.init(
  defaults = Map(
    "tab" -> "home",
    "filter" -> "",
    "sort" -> "newest",
  ),
  useHash = false  // Set to true to use hash-based URLs
)

// In your component
val YourComponent: (props: YourComponentProps) => {
  // Use query parameters in component
  val (activeTab, setActiveTab, _) = useQueryParam("tab", "home")
  
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

#### Initialization

```scala
// Initialize with default values
QueryState.init(
  defaults: Map[String, String] = Map(),  // Default parameter values
  useHash: Boolean = false                // Whether to use hash-based URLs
)
```

#### Hooks

```scala
// Use a query parameter in a component
val (value, setValue, updateValue) = useQueryParam(key: String, defaultValue: String = "")
```

Returns a tuple with:
- `value`: Current parameter value
- `setValue`: Function to set the parameter directly
- `updateValue`: Function to update the parameter based on current value

#### Methods

```scala
// Get or create a parameter signal
QueryState.param(key: String, defaultValue: String = ""): Var[String]

// Update a single parameter
QueryState.setParam(key: String, value: String): Unit

// Update multiple parameters
QueryState.updateParams(params: Map[String, String]): Unit

// Reset all parameters to defaults or empty
QueryState.reset(defaults: Map[String, String] = Map()): Unit

// Get current parameters as a Map
QueryState.currentParams: Map[String, String]

// Generate a URL with given parameters
QueryState.urlFor(params: Map[String, String]): String
```

## Examples

### Filter and Sort

```scala
val ProductsTab = () => {
  // Use query parameters for filter and sort
  val (filter, setFilter, _) = useQueryParam("filter", "")
  val (sort, setSort, _) = useQueryParam("sort", "newest")
  
  div(
    // Filter control
    div(
      label("Filter:"),
      select(
        value := filter,
        onChange := ((e) => setFilter(e.target.value)),
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
        onChange := ((e) => setSort(e.target.value)),
        option(value := "newest", "Newest"),
        option(value := "oldest", "Oldest"),
        option(value := "price", "Price")
      )
    )
  )
}
```

### Multi-step Form with History

```scala
val FormWizard = () => {
  // Use query parameter for current step
  val (step, setStep, _) = useQueryParam("step", "1")
  val stepNum = step.toInt
  
  div(
    // Form steps
    div(
      cls := "steps",
      renderStepIndicators(stepNum)
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

## Browser Support

Works with all modern browsers that support the History API.

## License

ISC License