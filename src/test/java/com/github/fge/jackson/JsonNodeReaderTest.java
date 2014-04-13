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

import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public final class JsonNodeReaderTest
{
    @Test
    public void streamIsClosedOnRead()
        throws IOException
    {
        final InputStream in = spy(stringToInputStream("[]"));
        new JsonNodeReader().readFrom(in);
        verify(in).close();
    }

    @Test
    public void readerIsClosedOnRead()
        throws IOException
    {
        final Reader reader = spy(new StringReader("[]"));
        new JsonNodeReader().readFrom(reader);
        verify(reader).close();
    }

    @Test
    public void defaultReaderDoesFullRead()
        throws UnsupportedEncodingException
    {
        final JsonNodeReader reader = new JsonNodeReader();
        final InputStream in = stringToInputStream("[]]");
        try {
            reader.readFrom(in);
            fail("No exception thrown!");
        } catch (IOException e) {
            assertTrue(e.getMessage().startsWith("trailing input detected"));
        }
    }

    private static InputStream stringToInputStream(final String input)
        throws UnsupportedEncodingException
    {
        return new ByteArrayInputStream(input.getBytes("UTF-8"));
    }
}
