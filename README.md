# Proxy use case

It is KSP plugin for generation of proxy use cases.

## Usage

Mark the class for which you want to generate use cases with the `@GenerateUseCases` annotation.
The plugin will generate classes for all public and internal methods with name `{method_name}UseCase`

### Example

```kotlin
package org.example

@GenerateUseCases
class Repository {
  fun loadUsers(): List<User> { ... }

  internal fun loadUser(): User { ... }

  private fun fetchUser(user: User) { ... }
}
```

Plugin will generate:

```kotlin
package org.example

import javax.inject.Inject
import kotlin.collections.List

public class LoadUsersUseCase @Inject constructor(
  private val target: Repository,
) {
  public operator fun invoke(): List<User> = with(target) {
    loadUsers()
  }
}
```

```kotlin
package org.example

import javax.inject.Inject

public class LoadUserUseCase @Inject constructor(
  private val target: Repository,
) {
  internal operator fun invoke(): User = with(target) {
    loadUser()
  }
}
```

## Dependency

```kotlin
implementation("io.github.perforators:proxy_use_case:1.2")
ksp("io.github.perforators:proxy_use_case:1.2")
```
