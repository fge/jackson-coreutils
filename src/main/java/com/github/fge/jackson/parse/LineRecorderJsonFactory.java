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

package com.github.fge.jackson.parse;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.IOContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public final class LineRecorderJsonFactory
    extends JsonFactory
{
    @Override
    protected JsonParser _createParser(final InputStream in,
        final IOContext ctxt)
        throws IOException, JsonParseException
    {
        final JsonParser parser = super._createParser(in, ctxt);
        return new LineRecorderJsonParser(parser);
    }

    @Override
    protected JsonParser _createParser(final Reader r, final IOContext ctxt)
        throws IOException, JsonParseException
    {
        final JsonParser parser = super._createParser(r, ctxt);
        return new LineRecorderJsonParser(parser);
    }

    @Override
    protected JsonParser _createParser(final byte[] data, final int offset,
        final int len, final IOContext ctxt)
        throws IOException, JsonParseException
    {
        final JsonParser parser = super._createParser(data, offset, len, ctxt);
        return new LineRecorderJsonParser(parser);
    }
}
