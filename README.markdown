## Read me first

The license of this project is LGPLv3 or later. See file src/main/resources/LICENSE for the full
text.

## What this is

This package uses Guava's
[Equivalence](http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/base/Equivalence.html)
over Jackson's
[JsonNode](http://fasterxml.github.com/jackson-databind/javadoc/2.1.1/com/fasterxml/jackson/databind/JsonNode.html)
to provide a means to compare JSON number values mathematically.

That is, JSON values `1` and `1.0` will be considered equivalent; but so will be all possible JSON
representations of mathematical value 1 (including, for instance, `10e-1`).  And evaluation is
recursive, which means that:

```json
[ 1, 2, 3 ]
```

will be considered equivalent to:

```json
[ 10e-1, 2.0, 0.3e1 ]
```

## Why

When reading JSON into a `JsonNode`, Jackson will serialize `1` as an `IntNode` but `1.0` as a
`DoubleNode` (or a `DecimalNode`).

Understandably so, Jackson <b>will not</b> consider such nodes to be equal, since they do not share
the same class. But, understandably so as well, some uses of JSON out there, including JSON Schema
and JSON Patch, want to consider such nodes as equal.

And this is where this package comes in. It allows you to consider that two numeric JSON values
are mathematically equal -- recursively so.

## Versions

The current verson is **1.0**. Note that it depends on Jackson 2.1.x.

## Maven artifact

```xml
<dependency>
    <groupId>com.github.fge</groupId>
    <artifactId>jackson-numequals</artifactId>
    <version>1.0</version>
</dependency>
```

## Usage

It is **highly recommended**, though not mandatory, that for accuracy reasons, you ask Jackson
that all floating point numbers be deserialized as `BigDecimal` by default:

```java
new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
```

When having got hold of your two `JsonNode` instances which you want to be equivalent if their JSON
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

