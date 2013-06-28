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
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.io.Closer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

/**
 * Utility class to load JSON values from various sources as {@link JsonNode}s.
 *
 * <p>This class uses {@link JacksonUtils#getReader()} as an {@link
 * ObjectReader} to parse JSON inputs.</p>
 */
public final class JsonLoader
{
    /**
     * The mapper which does everything behind the scenes...
     */
    private static final ObjectReader READER = JacksonUtils.getReader();

    private JsonLoader()
    {
    }

    /**
     * Read a {@link JsonNode} from a resource path.
     *
     * <p>This method explicitly throws an {@link IOException} if the resource
     * does not exist, instead of letting a {@link NullPointerException} slip
     * through.</p>
     *
     * @param resource The path to the resource
     * @return the JSON document at the resource
     * @throws IOException if the resource does not exist or there was a
     * problem loading it, or if the JSON document is invalid
     */
    public static JsonNode fromResource(final String resource)
        throws IOException
    {
        final URL url = JsonLoader.class.getResource(resource);

        if (url == null)
            throw new IOException("resource " + resource + " not found");

        final Closer closer = Closer.create();
        final JsonNode ret;
        final InputStream in;

        try {
            in = closer.register(url.openStream());
            ret = READER.readTree(in);
        } finally {
            closer.close();
        }

        return ret;
    }

    /**
     * Read a {@link JsonNode} from an URL.
     *
     * @param url The URL to fetch the JSON document from
     * @return The document at that URL
     * @throws IOException in case of network problems etc.
     */
    public static JsonNode fromURL(final URL url)
        throws IOException
    {
        return READER.readTree(url.openStream());
    }

    /**
     * Read a {@link JsonNode} from a file on the local filesystem.
     *
     * @param path the path (relative or absolute) to the file
     * @return the document in the file
     * @throws IOException if this is not a file, if it cannot be read, etc.
     */
    public static JsonNode fromPath(final String path)
        throws IOException
    {
        final Closer closer = Closer.create();
        final JsonNode ret;
        final FileInputStream in;

        try {
            in = closer.register(new FileInputStream(path));
            ret = READER.readTree(in);
        } finally {
            closer.close();
        }

        return ret;
    }

    /**
     * Same as {@link #fromPath(String)}, but this time the user supplies the
     * {@link File} object instead
     *
     * @param file the File object
     * @return The document
     * @throws IOException in many cases!
     */
    public static JsonNode fromFile(final File file)
        throws IOException
    {
        final Closer closer = Closer.create();
        final JsonNode ret;
        final FileInputStream in;

        try {
            in = closer.register(new FileInputStream(file));
            ret = READER.readTree(in);
        } finally {
            closer.close();
        }

        return ret;
    }

    /**
     * Read a {@link JsonNode} from a user supplied {@link Reader}
     *
     * @param reader The reader
     * @return the document
     * @throws IOException if the reader has problems
     */
    public static JsonNode fromReader(final Reader reader)
        throws IOException
    {
        return READER.readTree(reader);
    }

    /**
     * Read a {@link JsonNode} from a string input
     *
     * @param json the JSON as a string
     * @return the document
     * @throws IOException could not read from string
     */
    public static JsonNode fromString(final String json)
        throws IOException
    {
        return fromReader(new StringReader(json));
    }
}
