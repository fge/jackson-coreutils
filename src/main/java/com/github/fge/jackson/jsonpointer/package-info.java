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

/**
 * JSON Pointer related classes
 *
 * <p>This package, while primarily centered on {@link
 * com.github.fge.jackson.jsonpointer.JsonPointer}, is a generalization of JSON
 * Pointer to all implementations of Jackson's {@link
 * com.fasterxml.jackson.core.TreeNode}.</p>
 *
 * <p>The fundamentals of JSON Pointer remain the same, however: a JSON pointer
 * is a set of reference tokens separated by the {@code /} character. One
 * reference token is materialized by the {@link
 * com.github.fge.jackson.jsonpointer.ReferenceToken} class, and advancing
 * one level into a tree is materialized by {@link
 * com.github.fge.jackson.jsonpointer.TokenResolver}. A {@link
 * com.github.fge.jackson.jsonpointer.TreePointer} is a collection of token
 * resolvers.</p>
 */
package com.github.fge.jackson.jsonpointer;
