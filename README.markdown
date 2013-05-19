## Read me first

The license of this project is LGPLv3 or later. See file src/main/resources/LICENSE for the full
text.

## What this is

This package provides two items:

* it uses Guava's
[Equivalence](http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/base/Equivalence.html)
over Jackson's
[JsonNode](http://fasterxml.github.com/jackson-databind/javadoc/2.1.1/com/fasterxml/jackson/databind/JsonNode.html)
to provide a means to compare JSON number values mathematically;
* it has a generalized [JSON Pointer](http://tools.ietf.org/html/rfc6901)
implementation over Jackson's `TreeNode`, along with a dedicated implementation over `JsonNode`.

## Why

### Mathematical value equality

When reading JSON into a `JsonNode`, Jackson will serialize `1` as an `IntNode` but `1.0` as a
`DoubleNode` (or a `DecimalNode`).

Understandably so, Jackson <b>will not</b> consider such nodes to be equal, since they do not share
the same class. But, understandably so as well, some uses of JSON out there, including [JSON
Schema](http://tools.ietf.org/html/draft-zyp-json-schema-04) and [JSON
Patch](http://tools.ietf.org/html/rfc6902), want to consider such nodes as
equal.

And this is where this package comes in. It allows you to consider that two numeric JSON values are
mathematically equal -- recursively so. That is, JSON values `1` and `1.0` will be considered
equivalent; but so will be all possible JSON representations of mathematical value 1 (including, for
instance, `10e-1`). And evaluation is recursive, which means that:

```json
[ 1, 2, 3 ]
```

will be considered equivalent to:

```json
[ 10e-1, 2.0, 0.3e1 ]
```

### JSON Pointer

JSON Pointer is an IETF draft which allows to unambiguously address any value into a JSON document
(including the document itself, with the empty pointer). It is used in several IETF drafts:

* [JSON Reference](http://tools.ietf.org/html/draft-pbryan-zyp-json-ref-03) (as the fragment part);
* [JSON Patch](http://tools.ietf.org/html/rfc6902).

The implementation in this package applies to all `TreeNode`s. If all goes to plan, it may be an
integral part of a future Jackson tree model (see
[jackson-tree](https://github.com/fge/jackson-tree)).

## Versions

The current verson is **1.0**. Note that it depends on Jackson 2.1.x.

## Maven artifact

```xml
<dependency>
    <groupId>com.github.fge</groupId>
    <artifactId>jackson-coreutils</artifactId>
    <version>1.0</version>
</dependency>
```

## Usage

### Numeric equivalence

It is **highly recommended**, though not mandatory, that for accuracy reasons, you ask Jackson
that all floating point numbers be deserialized as `BigDecimal` by default:

```java
new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
```

This package also provides facilities for reading `JsonNode`s using an `ObjectMapper` configured as
above: `JacksonUtils` and `JsonLoader`. For instance:

```java
// Get a reader
final ObjectReader reader = JacksonUtils.getReader();
// Load a JsonNode with all decimals read as DecimalNode, from a file
final JsonNode node = JsonLoader.fromFile("/path/to/file.json");
```

When having got hold of two `JsonNode` instances which you want to be equivalent if their JSON
number values are the same, you can use:

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

This section concentrates on the `JsonNode` specific JSON Pointer implementation: `JsonNode`.

There are several ways you can build one:

```java
// Build from an input string -- potentially throws JsonPointerException on malformed inputs
final JsonPointer ptr = new JsonPointer("/foo/bar");
// Build from a series of raw tokens
final JsonPointer ptr = JsonPointer.of("foo", "bar", 1); // Yields pointer "/foo/bar/1"
```

Note that `JsonPointer` (and, for that matter, `TreePointer` as well) is **immutable**:

```java
// DON'T DO THAT: value of "ptr" will not change
ptr.append("foo");
// Do that instead
ptr = ptr.append("foo");
```

