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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.SampleNodeProvider;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;

public final class JsonNodeResolverTest
{
    private static final JsonNodeFactory FACTORY = JacksonUtils.nodeFactory();

    @Test
    public void resolvingNullReturnsNull()
    {
        final JsonNodeResolver resolver
            = new JsonNodeResolver(ReferenceToken.fromRaw("whatever"));

        assertNull(resolver.get(null));
    }

    @DataProvider
    public Iterator<Object[]> nonContainers()
    {
        return SampleNodeProvider.getSamplesExcept(NodeType.ARRAY,
            NodeType.OBJECT);
    }

    @Test(dataProvider = "nonContainers")
    public void resolvingNonContainerNodeReturnsNull(final JsonNode node)
    {
        final JsonNodeResolver resolver
            = new JsonNodeResolver(ReferenceToken.fromRaw("whatever"));

        assertNull(resolver.get(node));
    }

    @Test
    public void resolvingObjectMembersWorks()
    {
        final JsonNodeResolver resolver
            = new JsonNodeResolver(ReferenceToken.fromRaw("a"));
        final JsonNode target = FACTORY.textNode("b");

        ObjectNode node;

        node = FACTORY.objectNode();
        node.put("a", target);

        final JsonNode resolved = resolver.get(node);
        assertEquals(resolved, target);

        node = FACTORY.objectNode();
        node.put("b", target);

        assertNull(resolver.get(node));
    }

    @Test
    public void resolvingArrayIndicesWorks()
    {
        final JsonNodeResolver resolver
            = new JsonNodeResolver(ReferenceToken.fromInt(1));

        final JsonNode target = FACTORY.textNode("b");
        final ArrayNode node = FACTORY.arrayNode();

        node.add(target);
        assertNull(resolver.get(node));

        node.add(target);
        assertEquals(target, resolver.get(node));
    }

    @DataProvider
    public Iterator<Object[]> invalidIndices()
    {
        final List<Object[]> list = Lists.newArrayList();

        list.add(new Object[] { "-1" });
        list.add(new Object[] { "232398087298731987987232" });
        list.add(new Object[] { "00" });
        list.add(new Object[] { "0 " });
        list.add(new Object[] { " 0" });

        return list.iterator();
    }


    @Test(dataProvider = "invalidIndices")
    public void invalidIndicesYieldNull(final String raw)
    {
        final JsonNode target = FACTORY.textNode("b");
        final ArrayNode node = FACTORY.arrayNode();

        node.add(target);

        final ReferenceToken refToken = ReferenceToken.fromRaw(raw);
        final JsonNodeResolver resolver = new JsonNodeResolver(refToken);
        assertNull(resolver.get(node));
    }
}
