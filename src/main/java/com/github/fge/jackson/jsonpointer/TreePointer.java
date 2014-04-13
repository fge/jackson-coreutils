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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Iterator;
import java.util.List;

/**
 * A pointer into a {@link TreeNode}
 *
 * <p>Note that all pointers are <b>absolute</b>: they start from the root of
 * the tree. This is to mirror the behaviour of JSON Pointer proper.</p>
 *
 * <p>The class does not decode a JSON Pointer representation itself; however
 * it provides all the necessary methods for implementations to achieve this.
 * </p>
 *
 * <p>This class has two traversal methods: {@link #get(TreeNode)} and {@link
 * #path(TreeNode)}. The difference between both is that {@code path()} may
 * return another node than {@code null} if the tree representation has such
 * a node. This is the case, for instance, for {@link JsonNode}, which has a
 * {@link MissingNode}.</p>
 *
 * <p>At the core, this class is essentially a(n ordered!) {@link List} of
 * {@link TokenResolver}s (which is iterable via the class itself).</p>
 *
 * <p>Note that this class' {@link #hashCode()}, {@link #equals(Object)} and
 * {@link #toString()} are final.</p>
 *
 * @param <T> the type of the tree
 */
@ThreadSafe
@JsonSerialize(using = ToStringSerializer.class)
public abstract class TreePointer<T extends TreeNode>
    implements Iterable<TokenResolver<T>>
{
    protected static final MessageBundle BUNDLE
        = MessageBundles.getBundle(JsonPointerMessages.class);
    /**
     * The reference token separator
     */
    private static final char SLASH = '/';

    /**
     * What this tree can see as a missing node (may be {@code null})
     */
    private final T missing;

    /**
     * The list of token resolvers
     */
    protected final List<TokenResolver<T>> tokenResolvers;

    /**
     * Main protected constructor
     *
     * <p>This constructor makes an immutable copy of the list it receives as
     * an argument.</p>
     *
     * @param missing the representation of a missing node (may be null)
     * @param tokenResolvers the list of reference token resolvers
     */
    protected TreePointer(final T missing,
        final List<TokenResolver<T>> tokenResolvers)
    {
        this.missing = missing;
        this.tokenResolvers = ImmutableList.copyOf(tokenResolvers);
    }

    /**
     * Alternate constructor
     *
     * <p>This is the same as calling {@link #TreePointer(TreeNode, List)} with
     * {@code null} as the missing node.</p>
     *
     * @param tokenResolvers the list of token resolvers
     */
    protected TreePointer(final List<TokenResolver<T>> tokenResolvers)
    {
        this(null, tokenResolvers);
    }

    /**
     * Traverse a node and return the result
     *
     * <p>Note that this method shortcuts: it stops at the first node it cannot
     * traverse.</p>
     *
     * @param node the node to traverse
     * @return the resulting node, {@code null} if not found
     */
    public final T get(final T node)
    {
        T ret = node;
        for (final TokenResolver<T> tokenResolver: tokenResolvers) {
            if (ret == null)
                break;
            ret = tokenResolver.get(ret);
        }

        return ret;
    }

    /**
     * Traverse a node and return the result
     *
     * <p>This is like {@link #get(TreeNode)}, but it will return the missing
     * node if traversal fails.</p>
     *
     * @param node the node to traverse
     * @return the result, or the missing node
     * @see #TreePointer(TreeNode, List)
     */
    public final T path(final T node)
    {
        final T ret = get(node);
        return ret == null ? missing : ret;
    }

    /**
     * Tell whether this pointer is empty
     *
     * @return true if the reference token list is empty
     */
    public final boolean isEmpty()
    {
        return tokenResolvers.isEmpty();
    }

    @Override
    public final Iterator<TokenResolver<T>> iterator()
    {
        return tokenResolvers.iterator();
    }

    @Override
    public final int hashCode()
    {
        return tokenResolvers.hashCode();
    }

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        final TreePointer<?> other = (TreePointer<?>) obj;
        return tokenResolvers.equals(other.tokenResolvers);
    }

    @Override
    public final String toString()
    {
        final StringBuilder sb = new StringBuilder();
        /*
         * This works fine: a TokenResolver's .toString() always returns the
         * cooked representation of its underlying ReferenceToken.
         */
        for (final TokenResolver<T> tokenResolver: tokenResolvers)
            sb.append('/').append(tokenResolver);

        return sb.toString();
    }

    /**
     * Decode an input into a list of reference tokens
     *
     * @param input the input
     * @return the list of reference tokens
     * @throws JsonPointerException input is not a valid JSON Pointer
     * @throws NullPointerException input is null
     */
    protected static List<ReferenceToken> tokensFromInput(final String input)
        throws JsonPointerException
    {
        String s = BUNDLE.checkNotNull(input, "nullInput");
        final List<ReferenceToken> ret = Lists.newArrayList();
        String cooked;
        int index;
        char c;

        // TODO: see how this can be replaced with a CharBuffer -- seek etc
        while (!s.isEmpty()) {
            c = s.charAt(0);
            if (c != SLASH)
                throw new JsonPointerException(BUNDLE.getMessage("notSlash"));
            s = s.substring(1);
            index = s.indexOf(SLASH);
            cooked = index == -1 ? s : s.substring(0, index);
            ret.add(ReferenceToken.fromCooked(cooked));
            if (index == -1)
                break;
            s = s.substring(index);
        }

        return ret;
    }
}
