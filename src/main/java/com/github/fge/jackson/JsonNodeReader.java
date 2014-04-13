/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.jackson;


import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.github.fge.Builder;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.bundle.PropertiesBundle;
import com.google.common.io.Closer;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Class dedicated to reading JSON values from {@link InputStream}s and {@link
 * Reader}s
 *
 * <p>This class wraps a Jackson {@link ObjectMapper} so that it read one, and
 * only one, JSON text from a source. By default, when you read and map an
 * input source, Jackson will stop after it has read the first valid JSON text;
 * this means, for instance, that with this as an input:</p>
 *
 * <pre>
 *     []]]
 * </pre>
 *
 * <p>it will read the initial empty array ({@code []}) and stop there. This
 * class, instead, will peek to see whether anything is after the initial array,
 * and throw an exception if it finds anything.</p>
 *
 * <p>Note: the input sources are closed by the read methods.</p>
 *
 * @see ObjectMapper#readValues(JsonParser, Class)
 * @since 1.6
 */
@ThreadSafe
public final class JsonNodeReader
{
    private static final MessageBundle BUNDLE
        = PropertiesBundle.forPath("/com/github/fge/jackson/jsonNodeReader");

    private final ObjectReader reader;

    public JsonNodeReader(final ObjectMapper mapper)
    {
        reader = mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)
            .reader(JsonNode.class);
    }

    /**
     * No-arg constructor (see description)
     */
    public JsonNodeReader()
    {
        this(JacksonUtils.newMapper());
    }

    /**
     * Read a JSON value from an {@link InputStream}
     *
     * @param in the input stream
     * @return the value
     * @throws IOException malformed input, or problem encountered when reading
     * from the stream
     */
    public JsonNode fromInputStream(final InputStream in)
        throws IOException
    {
        final Closer closer = Closer.create();
        final JsonParser parser;
        final MappingIterator<JsonNode> iterator;

        try {
            parser = closer.register(reader.getFactory().createParser(in));
            iterator = reader.readValues(parser);
            return readNode(closer.register(iterator));
        } finally {
            closer.close();
        }
    }

    /**
     * Read a JSON value from a {@link Reader}
     *
     * @param r the reader
     * @return the value
     * @throws IOException malformed input, or problem encountered when reading
     * from the reader
     */
    public JsonNode fromReader(final Reader r)
        throws IOException
    {
        final Closer closer = Closer.create();
        final JsonParser parser;
        final MappingIterator<JsonNode> iterator;

        try {
            parser = closer.register(reader.getFactory().createParser(r));
            iterator = reader.readValues(parser);
            return readNode(closer.register(iterator));
        } finally {
            closer.close();
        }
    }

    private static JsonNode readNode(final MappingIterator<JsonNode> iterator)
        throws IOException
    {
        final Object source = iterator.getParser().getInputSource();
        final JsonParseExceptionBuilder builder
            = new JsonParseExceptionBuilder(source);

       builder.setMessage(BUNDLE.getMessage("read.noContent"));

        if (!iterator.hasNextValue())
            throw builder.build();

        final JsonNode ret = iterator.nextValue();

        builder.setMessage(BUNDLE.getMessage("read.trailingData"))
            .setLocation(iterator.getCurrentLocation());

        try {
            if (iterator.hasNextValue())
                throw builder.build();
        } catch (JsonParseException e) {
            throw builder.setLocation(e.getLocation()).build();
        }

        return ret;
    }

    private static final class JsonParseExceptionBuilder
        implements Builder<JsonParseException>
    {
        private String message = "";
        private JsonLocation location;

        private JsonParseExceptionBuilder(@Nonnull final Object source)
        {
            BUNDLE.checkNotNull(source, "read.nullArgument");
            location = new JsonLocation(source, 0L, 1, 1);
        }

        private JsonParseExceptionBuilder setMessage(
            @Nonnull final String message)
        {
            this.message = BUNDLE.checkNotNull(message, "read.nullArgument");
            return this;
        }

        private JsonParseExceptionBuilder setLocation(
            @Nonnull final JsonLocation location)
        {
            this.location = BUNDLE.checkNotNull(location, "read.nullArgument");
            return this;
        }

        @Override
        public JsonParseException build()
        {
            return new JsonParseException(message, location);
        }
    }
}
