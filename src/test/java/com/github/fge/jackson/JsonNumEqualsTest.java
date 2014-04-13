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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;

public final class JsonNumEqualsTest
{
    private static final JsonNodeFactory FACTORY = JsonNodeFactory.instance;
    private JsonNode testData;

    @BeforeClass
    public void initData()
        throws IOException
    {
        testData = JsonLoader.fromResource("/testfile.json");
    }

    @DataProvider
    public Iterator<Object[]> getInputs()
    {
        final List<Object[]> list = Lists.newArrayList();

        JsonNode reference;

        for (final JsonNode element: testData) {
            reference = element.get("reference");
            for (final JsonNode node: element.get("equivalences"))
                list.add(new Object[]{reference, node});
        }

        return list.iterator();
    }

    @Test(dataProvider = "getInputs")
    public void numericEqualityIsAcknowledged(final JsonNode reference,
        final JsonNode node)
    {
        assertTrue(JsonNumEquals.getInstance().equivalent(reference, node));
    }

    @Test(dataProvider = "getInputs")
    public void numericEqualityWorksWithinArrays(final JsonNode reference,
        final JsonNode node)
    {
        final ArrayNode node1 = FACTORY.arrayNode();
        node1.add(reference);
        final ArrayNode node2 = FACTORY.arrayNode();
        node2.add(node);

        assertTrue(JsonNumEquals.getInstance().equivalent(node1, node2));
    }

    @Test(dataProvider = "getInputs")
    public void numericEqualityWorksWithinObjects(final JsonNode reference,
        final JsonNode node)
    {
        final ObjectNode node1 = FACTORY.objectNode();
        node1.put("foo", reference);
        final ObjectNode node2 = FACTORY.objectNode();
        node2.put("foo", node);

        assertTrue(JsonNumEquals.getInstance().equivalent(node1, node2));
    }
}
