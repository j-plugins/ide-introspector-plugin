# Persisting Sensitive Data

The Credentials Store API allows you to store sensitive user data securely, like passwords, server URLs, etc.

## How to Use

Use [PasswordSafe](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/credential-store/src/ide/passwordSafe/PasswordSafe.kt) to work with credentials.

Common Utility Method:

Kotlin:

```KOTLIN
private fun createCredentialAttributes(key: String): CredentialAttributes {
  return CredentialAttributes(generateServiceName("MySystem", key))
}
```

Java:

```JAVA
private CredentialAttributes createCredentialAttributes(String key) {
  return new CredentialAttributes(
    CredentialAttributesKt.generateServiceName("MySystem", key)
  );
}
```

The [generateServiceName()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/credential-store/src/credentialStore/CredentialAttributes.kt) function helps name credentials in a consistent way so that they can be easily recognized in password managers and when users are asked to allow the IDE to access a secret.
Consider passing a subsystem name that identifies the plugin or its area of functionality, and a key that identifies the specific secret (for example, account name for password, server name for access token, etc.).
Examples:

| Subsystem |Key |Generated Service Name |
------------------------------------------
| MyService API |joe.doe |IntelliJ Platform MyService API — joe.doe |
| Acme Repository |example.com/repo |IntelliJ Platform Acme Repository — example.com/repo |

### Retrieve Stored Credentials

Kotlin:

```KOTLIN
val key = "serverURL" // e.g. serverURL, accountID
val attributes = createCredentialAttributes(key)
val passwordSafe = PasswordSafe.instance

val credentials = passwordSafe.get(attributes)
val password = credentials?.getPasswordAsString()

// or get password only
val passwordOnly = passwordSafe.getPassword(attributes)
```

Java:

```JAVA
String key = null; // e.g. serverURL, accountID
CredentialAttributes attributes = createCredentialAttributes(key);
PasswordSafe passwordSafe = PasswordSafe.getInstance();

Credentials credentials = passwordSafe.get(attributes);
if (credentials != null) {
  String password = credentials.getPasswordAsString();
}

// or get password only
String password = passwordSafe.getPassword(attributes);
```

Warning:

`PasswordSafe.get()` is blocking and shouldn't be called on EDT.

#### Retrieving Credentials in Remote Development Context

Since 2025.3, a new method was introduced in `PasswordSafe`:

```KOTLIN
suspend fun getAsync(attributes: CredentialAttributes): Ephemeral<Credentials>
```

Besides being coroutine-friendly, it returns "ephemeral" credentials that are valid only while the client is connected to the backend in the [Remote Development](https://www.jetbrains.com/help/idea/remote-development-overview.html) context.
When the client disconnects, the credentials are erased so that nothing can be done on the user's behalf without the user.

### Store Credentials

Kotlin:

```KOTLIN
val attributes = createCredentialAttributes(key)
val credentials = Credentials(username, password)
PasswordSafe.instance.set(attributes, credentials)
```

Java:

```JAVA
CredentialAttributes attributes = createCredentialAttributes(key);
Credentials credentials = new Credentials(username, password);
PasswordSafe.getInstance().set(attributes, credentials);
```

To remove stored credentials, pass `null` for the `credentials` parameter.

Warning:

`PasswordSafe.set()` is blocking and shouldn't be called on EDT.

## Storage

The default storage format depends on the OS.

| OS |Storage |
---------------
| Windows |File in [KeePass](https://keepass.info) format |
| macOS |Keychain using [Security Framework](https://developer.apple.com/documentation/security/keychain_services) |
| Linux |[Secret Service API](https://specifications.freedesktop.org/secret-service-spec/latest/) using [libsecret](https://wiki.gnome.org/Projects/Libsecret) |

Users can override the default behavior in `Settings | Appearance & Behavior | System Settings | Passwords`.

### Storage in Remote Development Context

Before 2025.3, passwords were stored on the backend side in plain text.

Since 2025.3, they are being transparently redirected to the frontend and are stored according to the local environment and settings (KeePass, keychain, etc.).

> Source: IntelliJ Platform SDK docs — Persisting Sensitive Data (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt
