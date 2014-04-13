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

import javax.annotation.concurrent.ThreadSafe;

/**
 * Reference token traversal class
 *
 * <p>This class is meant to be extended and implemented for all types of trees
 * inheriting {@link TreeNode}.</p>
 *
 * <p>This package contains one implementation of this class for {@link
 * JsonNode}.</p>
 *
 * <p>Note that its {@link #equals(Object)}, {@link #hashCode()} and {@link
 * #toString()} are final.</p>
 *
 * @param <T> the type of tree to traverse
 *
 * @see JsonNodeResolver
 */
@ThreadSafe
public abstract class TokenResolver<T extends TreeNode>
{
    /**
     * The associated reference token
     */
    protected final ReferenceToken token;

    /**
     * The only constructor
     *
     * @param token the reference token
     */
    protected TokenResolver(final ReferenceToken token)
    {
        this.token = token;
    }

    /**
     * Advance one level into the tree
     *
     * <p>Note: it is <b>required</b> that this method return null on
     * traversal failure.</p>
     *
     * <p>Note 2: handling {@code null} itself is up to implementations.</p>
     *
     * @param node the node to traverse
     * @return the other node, or {@code null} if no such node exists for that
     * token
     */
    public abstract T get(final T node);

    public final ReferenceToken getToken()
    {
        return token;
    }

    @Override
    public final int hashCode()
    {
        return token.hashCode();
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
        final TokenResolver<?> other = (TokenResolver<?>) obj;
        return token.equals(other.token);
    }

    @Override
    public final String toString()
    {
        return token.toString();
    }
}
