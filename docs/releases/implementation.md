[//]: # (This file was automatically generated - do not edit)

## Implementation

### Latest Version

The latest release is [`1.0.0-alpha01`](../releases.md)

### Plugin Releases

Here's a summary of the latest versions:

|    Version    |               Release Notes                | Release Date |
|:-------------:|:------------------------------------------:|:------------:|
| 1.0.0-alpha01 | [changelog 🔗](changelog/1.0.0-alpha01.md) | 10 Feb 2024  |

### Using Version Catalog

#### Declare Components

This catalog provides the implementation details of Gleam libraries and individual libraries, in
TOML format.

=== "Default"

    ```toml title="gradle/libs.versions.toml"
    [versions]
    gleam = "1.0.0-alpha01"

    [libraries]
    gleam = { id = "dev.teogor.gleam", name = "gleam", version.ref = "gleam" }
    gleam-navigation = { id = "dev.teogor.gleam", name = "gleam-navigation", version.ref = "gleam" }
    ```

#### Dependencies Implementation

=== "Kotlin"

    ```kotlin title="build.gradle.kts"
    dependencies {
      // Gleam Libraries
      implementation(libs.gleam)
      implementation(libs.gleam.navigation)
    }
    ```

=== "Groovy"

    ```groovy title="build.gradle"
    dependencies {
      // Gleam Libraries
      implementation libs.gleam
      implementation libs.gleam.navigation
    }
    ```
