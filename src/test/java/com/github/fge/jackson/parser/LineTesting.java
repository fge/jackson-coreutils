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

package com.github.fge.jackson.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.parse.LineRecorderJsonFactory;
import com.google.common.io.Closer;

import java.io.IOException;
import java.io.InputStream;

public final class LineTesting
{
    public static void main(final String... args)
        throws IOException
    {
        final JsonFactory factory = new LineRecorderJsonFactory();
        final ObjectMapper mapper = new ObjectMapper(factory);
        final Closer closer = Closer.create();
        final InputStream in;

        try {
            in = closer.register(LineTesting.class.getResourceAsStream
                ("/testfile.json"));
            mapper.readTree(in);
        } finally {
            closer.close();
        }
    }
}
