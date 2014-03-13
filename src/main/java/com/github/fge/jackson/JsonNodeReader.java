/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of both licenses is available under the src/resources/ directory of
 * this project (under the names LGPL-3.0.txt and ASL-2.0.txt respectively).
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.jackson;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Closer;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Class dedicated to reading JSON values from {@link InputStream}s and {@link
 * Reader}s
 *
 * <p>You can customize this class in two ways:</p>
 *
 * <ul>
 *     <li>passing custom parsing options;</li>
 *     <li>perform a full read or not.</li>
 * </ul>
 *
 * <p>The latter option is to circumvent the default behaviour of Jackson when
 * deserializing from an input source; by default, it will stop reading from the
 * source when it has successfully read a value. For instance, given this input;
 * </p>
 *
 * <pre>
 *     []]]
 * </pre>
 *
 * <p>it will read the initial empty array ({@code []}) and stop there. With
 * this class, you have the option of reading all the input and deem it as
 * invalid given the trailing input.</p>
 *
 * <p>An instance of this class provided by the no-arg constructor will have no
 * parsing options and will perform a full read.</p>
 *
 * <p>Note: the input sources are closed by the read methods.</p>
 *
 * <p>Note also that all decimal numbers will be deserialized as {@link
 * BigDecimal}s.</p>
 *
 * @see JsonParser
 * @see JsonParser.Feature
 * @see JacksonUtils#newMapper()
 * @since 1.6
 */
@ThreadSafe
public final class JsonNodeReader
{
    private static final EnumSet<JsonParser.Feature> DEFAULT_FEATURES;

    static {
        final EnumSet<JsonParser.Feature> set
            = EnumSet.noneOf(JsonParser.Feature.class);
        for (final JsonParser.Feature feature: JsonParser.Feature.values())
            if (feature.enabledByDefault())
                set.add(feature);
        DEFAULT_FEATURES = EnumSet.copyOf(set);
    }

    private final JsonFactory factory;
    private final boolean fullRead;

    /**
     * Main constructor
     *
     * @param features list of parsing features
     * @param fullRead whether to perform a full read of sources
     */
    public JsonNodeReader(final Collection<JsonParser.Feature> features,
        final boolean fullRead)
    {
        this.fullRead = fullRead;
        final ObjectMapper mapper = JacksonUtils.newMapper();
        final EnumSet<JsonParser.Feature> set
            = EnumSet.copyOf(DEFAULT_FEATURES);
        set.addAll(features);

        for (final JsonParser.Feature feature: set)
            mapper.configure(feature, true);

        factory = mapper.getFactory();
    }

    /**
     * Alternative constructor
     *
     * <p>This calls the main constructor with an empty parser feature set.</p>
     *
     * @param fullRead whether to perform a full read of sources
     */
    public JsonNodeReader(final boolean fullRead)
    {
        this(EnumSet.noneOf(JsonParser.Feature.class), fullRead);
    }

    /**
     * No-arg constructor (see description)
     */
    public JsonNodeReader()
    {
        this(true);
    }

    /**
     * Read a JSON value from an {@link InputStream}
     *
     * @param in the input stream
     * @return the value
     * @throws IOException malformed input, or problem encountered when reading
     * from the stream
     */
    public JsonNode readFrom(final InputStream in)
        throws IOException
    {
        final Closer closer = Closer.create();
        final JsonParser parser;

        try {
            parser = closer.register(factory.createParser(in));
            return readNode(parser);
        } finally {
            closer.close();
        }
    }

    /**
     * Read a JSON value from a {@link Reader}
     *
     * @param reader the reader
     * @return the value
     * @throws IOException malformed input, or problem encountered when reading
     * from the reader
     */
    public JsonNode readFrom(final Reader reader)
        throws IOException
    {
        final Closer closer = Closer.create();
        final JsonParser parser;

        try {
            parser = closer.register(factory.createParser(reader));
            return readNode(parser);
        } finally {
            closer.close();
        }
    }

    private JsonNode readNode(final JsonParser parser)
        throws IOException
    {
        final JsonNode ret = parser.readValueAsTree();
        if (!fullRead)
            return ret;
        final JsonLocation location = parser.getCurrentLocation();

        try {
            if (parser.nextToken() != null)
                throw new IOException();
        } catch (IOException e) {
            throw new IOException(String.format("trailing input detected" +
                " (last valid location: line %d, column %d)",
                location.getLineNr(), location.getColumnNr()), e);
        }
        return ret;
    }
}
