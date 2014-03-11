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

import com.fasterxml.jackson.core.JsonParser;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.EnumSet;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public final class JsonNodeReaderTest
{
    @Test
    public void streamIsClosedOnRead()
        throws IOException
    {
        final InputStream in = spy(stringToInputStream("[]"));
        new JsonNodeReader(EnumSet.noneOf(JsonParser.Feature.class), false)
            .readFrom(in);
        verify(in).close();
    }

    @Test
    public void parsingOptionsAreRespected()
        throws IOException
    {
        final Collection<JsonParser.Feature> features
            = EnumSet.of(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        final JsonNodeReader reader = new JsonNodeReader(features, false);
        final InputStream in = spy(stringToInputStream("'hello'"));
        assertEquals(reader.readFrom(in),
            JacksonUtils.nodeFactory().textNode("hello"));
    }

    @Test
    public void noFullReadDoesNotThrowExceptionOnExtraInput()
        throws IOException
    {
        final JsonNodeReader reader
            = new JsonNodeReader(EnumSet.noneOf(JsonParser.Feature.class), false);
        reader.readFrom(stringToInputStream("[]]"));
        assertTrue(true);
    }

    @Test
    public void fullReadThrowsExceptionOnExtraInput()
        throws UnsupportedEncodingException
    {
        final JsonNodeReader reader
            = new JsonNodeReader(EnumSet.noneOf(JsonParser.Feature.class), true);
        final InputStream in = stringToInputStream("[]]");
        try {
            reader.readFrom(in);
            fail("No exception thrown!");
        } catch (IOException e) {
            assertTrue(e.getMessage().startsWith("trailing input detected"));
        }
        assertTrue(true);

    }

    private static InputStream stringToInputStream(final String input)
        throws UnsupportedEncodingException
    {
        return new ByteArrayInputStream(input.getBytes("UTF-8"));
    }
}
