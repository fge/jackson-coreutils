/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
