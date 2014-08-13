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

import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * The type of valid keys.
 */

public final class JFPKey implements Serializable, Comparable<JFPKey>
{
  private static final long serialVersionUID = 3708595950836755359L;

  /**
   * @param key
   *          The key.
   * @return <code>true</code> iff <code>matches(key, "[a-z0-9]+")</code> .
   */

  public static boolean validKey(
    final String key)
  {
    NullCheck.notNull(key, "Key");
    return key.matches("[a-z0-9]+");
  }

  private final String actual;

  /**
   * Construct a key, raising {@link JFPExceptionAuthentication} if the key is
   * not valid according to {@link #validKey(String)}.
   *
   * @throws JFPExceptionAuthentication
   *           On invalid keys.
   * @param in_actual
   *          The actual key.
   */

  public JFPKey(
    final String in_actual)
      throws JFPExceptionAuthentication
  {
    if (JFPKey.validKey(in_actual) == false) {
      final String m = String.format("key '%s' is not valid", in_actual);
      assert m != null;
      throw new JFPExceptionAuthentication(m);
    }
    this.actual = NullCheck.notNull(in_actual, "Actual");
  }

  @Override public int compareTo(
    final @Nullable JFPKey o)
  {
    return this.actual.compareTo(NullCheck.notNull(o).actual);
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
    final JFPKey other = (JFPKey) obj;
    return this.actual.equals(other.actual);
  }

  /**
   * @return The actual key as a string.
   */

  public String getActual()
  {
    return this.actual;
  }

  @Override public int hashCode()
  {
    return this.actual.hashCode();
  }

  @Override public String toString()
  {
    return this.actual;
  }
}
