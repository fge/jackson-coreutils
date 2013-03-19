<h2>Read me first</h2>

<p>The license of this project is LGPLv3 or later. See file src/main/resources/LICENSE for the full
text.</p>

<h2>What this is</h2>

<p>This package uses Guava's <a
href="http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/base/Equivalence.html">Equivalence</a>
over Jackson's <a
href="http://fasterxml.github.com/jackson-databind/javadoc/2.1.1/com/fasterxml/jackson/databind/JsonNode.html">JsonNode</a>
to provide a means to compare JSON number values mathematically.</p>

<p>That is, JSON values <tt>1</tt> and <tt>1.0</tt> will be considered equivalent; but so will be
all possible JSON representations of mathematical value 1 (including, for instance, <tt>10e-1</tt>).</p>

<h2>Why</h2>

<p>When reading JSON into a <tt>JsonNode</tt>, Jackson will serialize <tt>1</tt> as an
<tt>IntNode</tt> but <tt>1.0</tt> as a <tt>DoubleNode</tt> (or a <tt>DecimalNode</tt>).</p>

<p>Understandably so, Jackson <b>will not</b> consider such nodes to be equal, since they do not
share the same class. But, understandably so as well, some uses of JSON out there, including JSON
Schema and JSON Patch, want to consider such nodes as equal.</p>

<p>And this is where this package comes in.</p>

<h2>Versions</h2>

<p>The current verson is <b>1.0</b>. Note that it depends on Jackson 2.1.x.</p>

<h2>Maven artifact</h2>

```xml
<dependency>
    <groupId>com.github.fge</groupId>
    <artifactId>jackson-numequals</artifactId>
    <version>1.0</version>
</dependency>
```

