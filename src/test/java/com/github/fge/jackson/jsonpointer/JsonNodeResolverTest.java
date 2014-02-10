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

package com.github.fge.jackson.jsonpointer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.SampleNodeProvider;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

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

    @Test
    public void invalidIndicesYieldNull()
    {
        final JsonNode target = FACTORY.textNode("b");
        final ArrayNode node = FACTORY.arrayNode();

        node.add(target);

        ReferenceToken refToken;
        JsonNodeResolver resolver;

        refToken = ReferenceToken.fromInt(-1);
        resolver = new JsonNodeResolver(refToken);
        assertNull(resolver.get(node));

        refToken = ReferenceToken.fromRaw("00");
        resolver = new JsonNodeResolver(refToken);
        assertNull(resolver.get(node));
    }
}
