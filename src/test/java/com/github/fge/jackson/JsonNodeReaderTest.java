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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.bundle.PropertiesBundle;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public final class JsonNodeReaderTest
{
    private final MessageBundle bundle
        = PropertiesBundle.forPath("/com/github/fge/jackson/jsonNodeReader");

    @Test
    public void streamIsClosedOnRead()
        throws IOException
    {
        final Supplier<InputStream> supplier = provideInputStream("[]");
        final InputStream in = spy(supplier.get());
        final JsonNode node = new JsonNodeReader().fromInputStream(in);
        verify(in).close();
        assertEquals(node, new ObjectMapper().readTree(supplier.get()));
    }

    @Test
    public void readerIsClosedOnRead()
        throws IOException
    {
        final Supplier<Reader> supplier = provideReader("[]");
        final Reader reader = spy(supplier.get());
        final JsonNode node = new JsonNodeReader().fromReader(reader);
        assertEquals(node, new ObjectMapper().readTree(supplier.get()));
        verify(reader).close();
    }

    @DataProvider
    public Iterator<Object[]> getMalformedData()
    {
        final List<Object[]> list = new ArrayList<Object[]>();

        list.add(new Object[] { "", "read.noContent"});
        list.add(new Object[] { "[]{}", "read.trailingData"});
        list.add(new Object[] { "[]]", "read.trailingData"});

        return list.iterator();
    }

    @Test(dataProvider = "getMalformedData")
    public void malformedDataThrowsExpectedException(final String input,
        final String errmsg)
        throws IOException
    {
        final Supplier<InputStream> supplier = provideInputStream(input);
        final String message = bundle.getMessage(errmsg);

        final JsonNodeReader reader = new JsonNodeReader();

        try {
            reader.fromInputStream(supplier.get());
            fail("No exception thrown!!");
        } catch (JsonParseException e) {
            assertEquals(e.getOriginalMessage(), message);
        }
    }

    private static Supplier<InputStream> provideInputStream(final String input)
    {
        return new Supplier<InputStream>()
        {
            @Override
            public InputStream get()
            {
                try {
                    return new ByteArrayInputStream(input.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Unhandled exception", e);
                }
            }
        };
    }

    private static Supplier<Reader> provideReader(final String input)
    {
        return new Supplier<Reader>()
        {
            @Override
            public Reader get()
            {
                return new StringReader(input);
            }
        };
    }

    /**
     * A class that can supply objects of a single type.  Semantically, this could
     * be a factory, generator, builder, closure, or something else entirely. No
     * guarantees are implied by this interface.
     *
     * @author Harry Heymann
     */
    public interface Supplier<T> {
        /**
         * Retrieves an instance of the appropriate type. The returned object may or
         * may not be a new instance, depending on the implementation.
         *
         * @return an instance of the appropriate type
         */
        T get();
    }
}
