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
 * The type of valid user names.
 */

public final class JFPFossilUserName implements
  Serializable,
  Comparable<JFPFossilUserName>
{
  private static final long serialVersionUID = 3708595950836755359L;

  /**
   * @param name
   *          The name.
   * @return <code>true</code> iff
   *         <code>length(name) &gt; 0 ∧ ∀i. isLetter(name(i)) ∨ isDigit(name(i)) ∨ name(i) = '_'</code>
   *         .
   */

  public static boolean validName(
    final String name)
  {
    NullCheck.notNull(name, "Name");

    if (name.isEmpty()) {
      return false;
    }

    for (int index = 0; index < name.length(); ++index) {
      final int code = name.codePointAt(index);
      if (Character.isLetter(code)) {
        continue;
      }
      if (Character.isDigit(code)) {
        continue;
      }
      if (code == '_') {
        continue;
      }

      return false;
    }

    return true;
  }

  private final String actual;

  /**
   * Construct a name, raising {@link JFPExceptionInvalidArgument} if the name
   * is not valid according to {@link #validName(String)}.
   *
   * @throws JFPExceptionInvalidArgument
   *           On invalid names.
   * @param in_actual
   *          The actual name.
   */

  public JFPFossilUserName(
    final String in_actual)
    throws JFPExceptionInvalidArgument
  {
    if (JFPFossilUserName.validName(in_actual) == false) {
      final String m = String.format("name '%s' is not valid", in_actual);
      assert m != null;
      throw new JFPExceptionInvalidArgument(m);
    }
    this.actual = NullCheck.notNull(in_actual, "Actual");
  }

  @Override public int compareTo(
    final @Nullable JFPFossilUserName o)
  {
    return this.actual.compareTo(NullCheck.notNull(o, "Other").actual);
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
    final JFPFossilUserName other = (JFPFossilUserName) obj;
    return this.actual.equals(other.actual);
  }

  /**
   * @return The actual name as a string.
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
