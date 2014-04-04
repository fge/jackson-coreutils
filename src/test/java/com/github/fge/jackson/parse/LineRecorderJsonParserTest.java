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

package com.github.fge.jackson.parse;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.Closer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public final class LineRecorderJsonParserTest
{
    private static final String INCORRECT_LINE_INFO
        = "generated line info is incorrect; expected: %s, actual: %s";

    private static final Function<String, Object[]> STRING_TO_OBJECT_ARRAY
        = new Function<String, Object[]>()
    {
        @Nullable
        @Override
        public Object[] apply(@Nullable final String input)
        {
            return new Object[] { input };
        }
    };

    private JsonFactory factory;
    private ObjectMapper mapper;

    @BeforeMethod
    public void initFactory()
    {
        factory = new LineRecorderJsonFactory();
        mapper = new ObjectMapper();
    }

    @DataProvider
    public Iterator<Object[]> getLineData()
    {
        final List<String> list = ImmutableList.of(
            "primitiveOnFirstLine",
            "primitiveNotOnFirstLine",
            "arrayOfPrimitives",
            "objectOfPrimitives"
        );

        return Iterables.transform(list, STRING_TO_OBJECT_ARRAY).iterator();
    }

    @Test(dataProvider = "getLineData")
    public void lineNumbersAreCorrectlyReported(final String subdir)
        throws IOException
    {
        final String basePath = "/parser/" + subdir + '/';
        final Closer closer = Closer.create();
        final TypeReference<Map<JsonPointer, Integer>> typeRef
            = new TypeReference<Map<JsonPointer, Integer>>() {};

        final InputStream input, lines;
        final Map<JsonPointer, Integer> actual, expected;
        final LineRecorderJsonParser parser;

        try {
            input = closer.register(inputFrom(basePath + "input.json"));
            lines = closer.register(inputFrom(basePath + "lines.json"));

            expected = mapper.readValue(lines, typeRef);

            parser = (LineRecorderJsonParser) factory.createParser(input);
            mapper.readTree(parser);
            actual = parser.getLineInfo();

            assertEquals(actual, expected,
                String.format(INCORRECT_LINE_INFO, expected, actual));
        } catch (IOException e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private static InputStream inputFrom(final String path)
    {
        return LineRecorderJsonParserTest.class.getResourceAsStream(path);
    }
}
