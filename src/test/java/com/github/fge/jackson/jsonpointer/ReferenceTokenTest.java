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

import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

import static org.testng.Assert.*;

public final class ReferenceTokenTest
{
    private static final MessageBundle BUNDLE
        = MessageBundles.getBundle(JsonPointerMessages.class);

    @Test
    public void nullCookedRaisesError()
        throws JsonPointerException
    {
        try {
            ReferenceToken.fromCooked(null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), BUNDLE.getMessage("nullInput"));
        }
    }
    @Test
    public void nullRawRaisesError()
    {
        try {
            ReferenceToken.fromRaw(null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), BUNDLE.getMessage("nullInput"));
        }
    }

    @Test
    public void emptyEscapeRaisesTheAppropriateException()
    {
        try {
            ReferenceToken.fromCooked("whatever~");
            fail("No exception thrown!!");
        } catch (JsonPointerException e) {
            assertEquals(e.getMessage(), BUNDLE.getMessage("emptyEscape"));
        }
    }

    @Test
    public void illegalEscapeRaisesTheAppropriateException()
    {
        try {
            ReferenceToken.fromCooked("~a");
            fail("No exception thrown!!");
        } catch (JsonPointerException e) {
            assertEquals(e.getMessage(), BUNDLE.getMessage("illegalEscape"));
        }
    }

    @DataProvider
    public Iterator<Object[]> cookedRaw()
    {
        return ImmutableList.of(
            new Object[] { "~0", "~" },
            new Object[] { "~1", "/" },
            new Object[] { "", "" },
            new Object[] { "~0user", "~user" },
            new Object[] { "foobar", "foobar" },
            new Object[] { "~1var~1lib~1mysql", "/var/lib/mysql" }
        ).iterator();
    }

    @Test(dataProvider = "cookedRaw")
    public void fromCookedOrFromRawYieldsSameResults(final String cooked,
        final String raw)
        throws JsonPointerException
    {
        final ReferenceToken token1 = ReferenceToken.fromCooked(cooked);
        final ReferenceToken token2 = ReferenceToken.fromRaw(raw);

        assertTrue(token1.equals(token2));
        assertEquals(token2.toString(), cooked);
    }

    @DataProvider
    public Iterator<Object[]> indices()
    {
        return ImmutableList.of(
            new Object[] { 0, "0" },
            new Object[] { -1, "-1" },
            new Object[]{ 13, "13" }
        ).iterator();
    }

    @Test(dataProvider = "indices")
    public void fromIndexOrStringYieldsSameResults(final int index,
        final String asString)
        throws JsonPointerException
    {
        final ReferenceToken fromInt = ReferenceToken.fromInt(index);
        final ReferenceToken cooked = ReferenceToken.fromCooked(asString);
        final ReferenceToken raw = ReferenceToken.fromRaw(asString);

        assertTrue(fromInt.equals(cooked));
        assertTrue(cooked.equals(raw));
        assertTrue(raw.equals(fromInt));

        assertEquals(fromInt.toString(), asString);
    }

    @Test
    public void zeroAndZeroZeroAreNotTheSame()
        throws JsonPointerException
    {
        final ReferenceToken zero = ReferenceToken.fromCooked("0");
        final ReferenceToken zerozero = ReferenceToken.fromCooked("00");

        assertFalse(zero.equals(zerozero));
    }
}
