## Read me first

This project, as of version 1.5, is licensed under both LGPLv3 and ASL 2.0. See
file LICENSE for more details. Versions 1.0 and lower are licensed under LGPLv3
only.

This project uses [Gradle](http://www.gradle.org) as a build system. See file `BUILD.md` for
details.

Credits where they are due: other people have contributed to this project, and this project would
not have reached its current state without them. Please refer to the `CONTRIBUTORS.md` file in this
project for details.

## What this is

This package is meant to be used with Jackson 2.2.x. It provides the three following features:

* write/read JSON decimal numbers using `BigDecimal` (instead of `double`) for optimal numeric
  precision;
* (since 1.6) preconfigured JSON reader with trailing data detection support (see below);
* JSON numeric equivalence;
* [JSON Pointer](http://tools.ietf.org/html/rfc6901) support.

## Versions

The current verson is **1.8**. Its Javadoc is [available
online](http://fge.github.io/jackson-coreutils/index.html).

Please see file `RELEASE-NOTES.md` for more information.

## Using in Gradle/Maven

With Gradle:

```groovy
dependencies {
    compile(group: "com.github.fge", name: "jackson-coreutils", version: "1.8");
}
```

With Maven:

```xml
<dependency>
    <groupId>com.github.fge</groupId>
    <artifactId>jackson-coreutils</artifactId>
    <version>1.8</version>
</dependency>
```

## Description

### `BigDecimal` to read decimal values (instead of `double`)

All classes in this package whose purpose is to load JSON data (`JsonLoader`, Jackson mappers
provided by `JacksonUtils`, and `JsonLoader` since version 1.6) will deserialize all decimal
instances using `BigDecimal` (more appropriately, Jackson's `DecimalNode`). This allows to retain
the numeric value with full precision and not be a victim of IEEE 754's limitations in this regard.

### Trailing data detection (since 1.6)

Jackson's default JSON deserialization, when reading an input such as this:

```
[]]
```

will read the initial value (`[]`) and stop there, ignoring the trailing `]`. This behaviour can be
beneficial in the event that you have a "streaming" JSON source, however it is not suitable if you
know you have only one JSON value to read and want to report an error if trailing data is detected.

This package provides a `JsonNodeReader` class which will fail with an exception on trailing input.

### JSON numeric equivalence

When reading JSON into a `JsonNode`, Jackson will serialize `1` as an `IntNode` but `1.0` as a
`DoubleNode` (or a `DecimalNode`).

Understandably so, Jackson <b>will not</b> consider such nodes to be equal, since they are not of
the same class. But, understandably so as well, some uses of JSON out there, including [JSON
Schema](http://tools.ietf.org/html/draft-zyp-json-schema-04) and [JSON
Patch](http://tools.ietf.org/html/rfc6902)'s test operation, want to consider such nodes as equal.

This package provides an implementation of Guava's `Equivalence` which considers that two numeric
JSON values are equal if their value is mathematically equal -- recursively so. That is, JSON values
`1` and `1.0` _will_ be considered equivalent; but so will be all possible JSON representations of
mathematical value 1 (including, for instance, `10e-1`). And evaluation is recursive, which means
that:

```json
[ 1, 2, 3 ]
```

will be considered equivalent to:

```json
[ 10e-1, 2.0, 0.3e1 ]
```

### JSON Pointer

JSON Pointer is an IETF RFC (6901) which allows to unambiguously address any value into a JSON document
(including the document itself, with the empty pointer). It is used in several
IETF drafts and/or RFCs:

* [JSON Reference](http://tools.ietf.org/html/draft-pbryan-zyp-json-ref-03) (as the fragment part);
* [JSON Patch](http://tools.ietf.org/html/rfc6902).

The implementation in this package applies to all `TreeNode`s as of Jackson 2.2.x; this includes
`JsonNode`.

## Usage

### Read JSON using `BigDecimal` for decimal numbers

Several options are available:

```java
// JsonLoader
final JsonNode node = JsonLoader.fromFile(new File("whatever.json"));
final JsonNode node = JsonLoader.fromResource("/in/class/path/my.json");
// Get a preconfigured ObjectMapper or reader with BigDecimal deserialization
final ObjectMapper mapper = JacksonUtils.newMapper();
final ObjectReader reader = JacksonUtils.getReader();
// Get a JsonNodeReader; see below
final JsonNodeReader reader = new JsonNodeReader();
```

### Read JSON with trailing data detection support

The class to use is `JsonNodeReader`:

```java
// Default reader, no additional options
final JsonNodeReader reader = new JsonNodeReader();
// Provide a custom ObjectMapper
final ObjectMapper mapper = ...;
final JsonNodeReader reader = new JsonNodeReader(mapper);
// Read from an InputStream, a Reader
final JsonNode node = reader.fromInputStream(...);
final JsonNode node = reader.fromReader(...);
```

Note that the `JsonLoader` class uses a `JsonNodeReader`.

### Numeric equivalence

Given two `JsonNode` instances which you want to be equivalent if their JSON number values are the
same, you can use:

```java
if (JsonNumEquals.getInstance().equivalent(node1, node2))
    // do something
```

You can also use this package to add `JsonNode` instances to a set:

```java
final Equivalence<JsonNode> eq = JsonNumEquals.getInstance();
// Note: uses Guava's Sets to create the set
final Set<Equivalence.Wrapper<JsonNode>> set
    = Sets.newHashSet();

// Insert values
set.add(eq.wrap(node1));
set.add(eq.wrap(node2));
// etc
```

### JSON Pointer

There are several ways you can build one:

```java
// Build from an input string -- potentially throws JsonPointerException on malformed inputs
final JsonPointer ptr = new JsonPointer("/foo/bar");
// Build from a series of raw tokens -- never throws an exception
final JsonPointer ptr = JsonPointer.of("foo", "bar", 1); // Yields pointer "/foo/bar/1"
// Get another pointer's parent:
final JsonPointer parent = ptr.parent();
```

Note that `JsonPointer` is **immutable**:

```java
// DON'T DO THAT: value of "ptr" will not change
ptr.append("foo");
// Do that instead
ptr = ptr.append("foo");
```

Then, to use it, use either the `.get()` or the `.path()` methods:

```java
// "node" is a JsonNode
// .get() returns null if there is no such path
final JsonNode child = ptr.get(node);
// Test if a path exists with .path()
if (!ptr.path(node).isMissingNode())
    // do something
```

