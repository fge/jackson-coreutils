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

package com.github.fge.jackson.jsonpointer;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.SampleNodeProvider;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public final class JsonPointerTest
{
    private static final MessageBundle BUNDLE
        = MessageBundles.getBundle(JsonPointerMessages.class);

    private final JsonNode testData;
    private final JsonNode document;

    public JsonPointerTest()
        throws IOException
    {
        testData = JsonLoader.fromResource("/jsonpointer/jsonpointer.json");
        document = testData.get("document");
    }

    @Test
    public void cannotAppendNullPointer()
    {
        final JsonPointer foo = null;
        try {
            JsonPointer.empty().append(foo);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), BUNDLE.getMessage("nullInput"));
        }
    }

    @DataProvider
    public Iterator<Object[]> rawPointers()
    {
        final List<Object[]> list = Lists.newArrayList();
        final JsonNode testNode = testData.get("pointers");
        final Map<String, JsonNode> map = JacksonUtils.asMap(testNode);

        for (final Map.Entry<String, JsonNode> entry: map.entrySet())
            list.add(new Object[] { entry.getKey(), entry.getValue() });

        return list.iterator();
    }

    @Test(dataProvider = "rawPointers")
    public void rawPointerResolvingWorks(final String input,
        final JsonNode expected)
        throws JsonPointerException
    {
        final JsonPointer pointer = new JsonPointer(input);

        assertEquals(pointer.get(document), expected);
    }

    @DataProvider
    public Iterator<Object[]> uriPointers()
    {
        final List<Object[]> list = Lists.newArrayList();
        final JsonNode testNode = testData.get("uris");
        final Map<String, JsonNode> map = JacksonUtils.asMap(testNode);

        for (final Map.Entry<String, JsonNode> entry: map.entrySet())
            list.add(new Object[] { entry.getKey(), entry.getValue() });

        return list.iterator();
    }

    @Test(dataProvider = "uriPointers")
    public void uriPointerResolvingWorks(final String input,
        final JsonNode expected)
        throws URISyntaxException, JsonPointerException
    {
        final URI uri = new URI(input);
        final JsonPointer pointer = new JsonPointer(uri.getFragment());

        assertEquals(pointer.get(document), expected);
    }

    @Test
    public void appendingRawTokensToAPointerWorks()
        throws JsonPointerException
    {
        final JsonPointer ptr = new JsonPointer("/foo/bar");
        final String raw = "/0~";
        final JsonPointer expected = new JsonPointer("/foo/bar/~10~0");

        assertEquals(ptr.append(raw), expected);
    }

    @Test
    public void appendingIndicesToAPointerWorks()
        throws JsonPointerException
    {
        final JsonPointer ptr = new JsonPointer("/foo/bar/");
        final int index = 33;
        final JsonPointer expected = new JsonPointer("/foo/bar//33");

        assertEquals(ptr.append(index), expected);
    }

    @Test
    public void appendingOnePointerToAnotherWorks()
        throws JsonPointerException
    {
        final JsonPointer ptr = new JsonPointer("/a/b");
        final JsonPointer appended = new JsonPointer("/c/d");
        final JsonPointer expected = new JsonPointer("/a/b/c/d");

        assertEquals(ptr.append(appended), expected);
    }

    @DataProvider
    public Iterator<Object[]> allInstanceTypes()
    {
        return SampleNodeProvider.getSamples(EnumSet.allOf(NodeType.class));
    }

    @Test(dataProvider = "allInstanceTypes")
    public void emptyPointerAlwaysReturnsTheSameInstance(final JsonNode node)
    {
        assertEquals(JsonPointer.empty().get(node), node);
    }

    @Test
    public void staticConstructionFromTokensWorks()
        throws JsonPointerException
    {
        JsonPointer ptr1, ptr2;

        ptr1 = JsonPointer.of("a", "b");
        ptr2 = new JsonPointer("/a/b");
        assertEquals(ptr1, ptr2);

        ptr1 = JsonPointer.of("", "/", "~");
        ptr2 = new JsonPointer("//~1/~0");
        assertEquals(ptr1, ptr2);

        ptr1 = JsonPointer.of(1, "xx", 0);
        ptr2 = new JsonPointer("/1/xx/0");
        assertEquals(ptr1, ptr2);

        ptr1 = JsonPointer.of("");
        ptr2 = new JsonPointer("/");
        assertEquals(ptr1, ptr2);
    }

    @DataProvider
    public Iterator<Object[]> parentTestData()
    {
        final List<Object[]> list = Lists.newArrayList();

        // Empty
        list.add(new Object[] { JsonPointer.empty(), JsonPointer.empty() });
        // Single token pointer
        list.add(new Object[] { JsonPointer.of(1), JsonPointer.empty() });
        list.add(new Object[] { JsonPointer.of("a"), JsonPointer.empty() });
        // Multiple token pointer
        list.add(new Object[] { JsonPointer.of("a", "b"),
            JsonPointer.of("a") });
        list.add(new Object[] { JsonPointer.of("a", "b", "c" ),
            JsonPointer.of("a", "b") });

        return list.iterator();
    }

    @Test(dataProvider = "parentTestData")
    public void parentComputationWorks(final JsonPointer child,
        final JsonPointer parent)
    {
        assertEquals(child.parent(), parent);
    }
}
