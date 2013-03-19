package com.github.fge.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.*;

public final class JsonNumEqualsTest
{
    private final JsonNode testData;

    public JsonNumEqualsTest()
        throws IOException
    {
        final ObjectReader reader = new ObjectMapper()
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .reader();

        final InputStream in = JsonNumEqualsTest.class
            .getResourceAsStream("/testfile.json");
        testData = reader.readTree(in);
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
}
