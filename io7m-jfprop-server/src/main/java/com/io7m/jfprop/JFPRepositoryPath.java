/*
 * Copyright © 2014 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jfprop;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * The type of valid repository paths.
 */

public final class JFPRepositoryPath implements
Serializable,
Comparable<JFPRepositoryPath>
{
  private static final long serialVersionUID = 3708595950836755359L;

  private static boolean isValidNormalized(
    final String normal)
  {
    if (normal.isEmpty()) {
      return false;
    }
    if (normal.codePointAt(0) != '/') {
      return false;
    }
    if (normal.length() == 1) {
      return false;
    }

    final String[] elements = normal.substring(1).split("/");

    for (int index = 0; index < elements.length; ++index) {
      final String name =
        NullCheck.notNull(elements[index], "Elements(" + index + ")");
      if (JFPRepositoryPath.validPathComponent(name) == false) {
        return false;
      }
    }

    return true;
  }

  private static List<String> makeComponents(
    final String p)
    {
    final List<String> cs = new ArrayList<String>();
    final String[] c = p.split("/");
    assert c.length > 1;
    for (int index = 1; index < c.length; ++index) {
      final String component = c[index];
      cs.add(component);
    }
    return Collections.unmodifiableList(cs);
    }

  /**
   * Normalize a path - remove trailing slashes and convert sequences to
   * slashes to a single slash.
   *
   * @param path
   *          The path.
   * @return A normalized path.
   */

  public static String normalizePath(
    final String path)
  {
    NullCheck.notNull(path, "Path");
    final String collapse_slashes = path.replaceAll("/+", "/");
    if ("/".equals(collapse_slashes)) {
      assert collapse_slashes != null;
      return collapse_slashes;
    }

    final String r = collapse_slashes.replaceAll("/+$", "");
    assert r != null;
    return r;
  }

  /**
   * Check path validity.
   *
   * @param path
   *          The name.
   * @return <code>true</code> iff all of the following conditions hold:
   *         <ul>
   *         <li>The path is not empty.</li>
   *         <li>The path begins with a forward slash (U+002F)</li>
   *         <li>The path is not equal to "/" (U+002F)</li>
   *         <li>Each path component, separated by "/" (U+002F), is valid.</li>
   *         </ul>
   * @see #validPathComponent(String)
   */

  public static boolean validPath(
    final String path)
  {
    final String normal = JFPRepositoryPath.normalizePath(path);
    return JFPRepositoryPath.isValidNormalized(normal);
  }

  /**
   * Check path component validity.
   *
   * @param name
   *          The name
   * @return <code>true</code> iff all of the following conditions hold:
   *         <ul>
   *         <li>The name is not empty.</li>
   *         <li>The name does not contain the string ".."</li>
   *         <li>The name matches the pattern
   *         <code>[[.-_][A-Z][a-z][0-9]]+</code></li>
   *         </ul>
   */

  public static boolean validPathComponent(
    final String name)
  {
    NullCheck.notNull(name, "Name");

    if (name.isEmpty()) {
      return false;
    }
    if (name.contains("..")) {
      return false;
    }
    if (name.matches("[[\\.\\-_][A-Z][a-z][0-9]]+") == false) {
      return false;
    }

    return true;
  }

  private final List<String> components;
  private final String       image;

  /**
   * Construct a name, raising {@link JFPExceptionInvalidArgument} if the name
   * is not valid according to {@link #validPath(String)}.
   *
   * @param in_actual
   *          The actual name.
   * @throws JFPExceptionInvalidArgument
   *           If the path is not valid.
   */

  public JFPRepositoryPath(
    final String in_actual)
      throws JFPExceptionInvalidArgument
  {
    final String p = JFPRepositoryPath.normalizePath(in_actual);
    if (JFPRepositoryPath.isValidNormalized(p) == false) {
      final String m = String.format("path '%s' is not valid", in_actual);
      assert m != null;
      throw new JFPExceptionInvalidArgument(m);
    }

    this.components = JFPRepositoryPath.makeComponents(p);
    this.image = p;
  }

  @Override public int compareTo(
    final @Nullable JFPRepositoryPath o)
  {
    return this.image.compareTo(NullCheck.notNull(o, "Other").image);
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final JFPRepositoryPath other = (JFPRepositoryPath) obj;
    return this.image.equals(other.image);
  }

  /**
   * @return A read-only list of the path components.
   */

  public List<String> getComponents()
  {
    return this.components;
  }

  @Override public int hashCode()
  {
    return this.image.hashCode();
  }

  @Override public String toString()
  {
    return this.image;
  }
}
