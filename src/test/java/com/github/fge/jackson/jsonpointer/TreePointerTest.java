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

import com.fasterxml.jackson.core.TreeNode;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public final class TreePointerTest
{
    private static final MessageBundle BUNDLE
        = MessageBundles.getBundle(JsonPointerMessages.class);

    @Test
    public void attemptToBuildTokensFromNullRaisesAnError()
        throws JsonPointerException
    {
        try {
            TreePointer.tokensFromInput(null);
            fail("No exception thrown!!");
        } catch (NullPointerException e) {
            assertEquals(e.getMessage(), BUNDLE.getMessage("nullInput"));
        }
    }

    @Test
    public void buildingTokenListYellsIfIllegalPointer()
    {
        try {
            TreePointer.tokensFromInput("a/b");
            fail("No exception thrown!!");
        } catch (JsonPointerException e) {
            assertEquals(e.getMessage(), BUNDLE.getMessage("notSlash"));
        }
    }

    @Test
    public void buildingTokenListIsUnfazedByAnEmptyInput()
        throws JsonPointerException
    {
        assertEquals(TreePointer.tokensFromInput(""),
            ImmutableList.<ReferenceToken>of());
    }

    @Test
    public void buildingTokenListIsUnfazedByEmptyToken()
        throws JsonPointerException
    {
        final List<ReferenceToken> expected
            = ImmutableList.of(ReferenceToken.fromCooked(""));
        final List<ReferenceToken> actual = TreePointer.tokensFromInput("/");

        assertEquals(actual, expected);
    }

    @Test
    public void tokenListRespectsOrder()
        throws JsonPointerException
    {
        final List<ReferenceToken> expected = ImmutableList.of(
            ReferenceToken.fromRaw("/"),
            ReferenceToken.fromRaw("~"),
            ReferenceToken.fromRaw("x")
        );
        final List<ReferenceToken> actual
            = TreePointer.tokensFromInput("/~1/~0/x");

        assertEquals(actual, expected);
    }

    @Test
    public void tokenListAccountsForEmptyTokens()
        throws JsonPointerException
    {
        final List<ReferenceToken> expected = ImmutableList.of(
            ReferenceToken.fromRaw("a"),
            ReferenceToken.fromRaw(""),
            ReferenceToken.fromRaw("b")
        );
        final List<ReferenceToken> actual
            = TreePointer.tokensFromInput("/a//b");

        assertEquals(actual, expected);
    }

    @Test
    public void gettingTraversalResultGoesNoFurtherThanFirstMissing()
    {
        @SuppressWarnings("unchecked")
        final TokenResolver<TreeNode> token1 = mock(TokenResolver.class);
        @SuppressWarnings("unchecked")
        final TokenResolver<TreeNode> token2 = mock(TokenResolver.class);
        final TreeNode missing = mock(TreeNode.class);

        when(token1.get(any(TreeNode.class))).thenReturn(null);

        final DummyPointer ptr = new DummyPointer(missing,
            ImmutableList.of(token1, token2));

        final TreeNode node = mock(TreeNode.class);
        final TreeNode ret = ptr.get(node);
        verify(token1, only()).get(node);
        verify(token2, never()).get(any(TreeNode.class));

        assertNull(ret);
    }

    @Test
    public void gettingPathOfMissingNodeReturnsMissingNode()
    {
        @SuppressWarnings("unchecked")
        final TokenResolver<TreeNode> token1 = mock(TokenResolver.class);
        @SuppressWarnings("unchecked")
        final TokenResolver<TreeNode> token2 = mock(TokenResolver.class);
        final TreeNode missing = mock(TreeNode.class);

        when(token1.get(any(TreeNode.class))).thenReturn(null);

        final DummyPointer ptr = new DummyPointer(missing,
            ImmutableList.of(token1, token2));

        final TreeNode node = mock(TreeNode.class);
        final TreeNode ret = ptr.path(node);
        verify(token1, only()).get(node);
        verify(token2, never()).get(any(TreeNode.class));

        assertSame(ret, missing);
    }

    @Test
    public void treePointerCanTellWhetherItIsEmpty()
    {
        final List<TokenResolver<TreeNode>> list = Lists.newArrayList();

        assertTrue(new DummyPointer(null, list).isEmpty());

        @SuppressWarnings("unchecked")
        final TokenResolver<TreeNode> mock = mock(TokenResolver.class);

        list.add(mock);
        assertFalse(new DummyPointer(null, list).isEmpty());
    }

    @Test
    public void treeIsUnalteredWhenOriginalListIsAltered()
    {
        final List<TokenResolver<TreeNode>> list = Lists.newArrayList();
        final DummyPointer dummy = new DummyPointer(null, list);

        @SuppressWarnings("unchecked")
        final TokenResolver<TreeNode> mock = mock(TokenResolver.class);
        list.add(mock);

        assertTrue(dummy.isEmpty());
    }

    private static final class DummyPointer
        extends TreePointer<TreeNode>
    {
        private DummyPointer(final TreeNode missing,
            final List<TokenResolver<TreeNode>> tokenResolvers)
        {
            super(missing, tokenResolvers);
        }
    }
}
