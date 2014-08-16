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

import com.io7m.jranges.RangeCheck;

/**
 * A time spec component that exactly matches a value.
 *
 * The {@link #matches(int)} function returns <code>true</code> iff the given
 * value matches the component's value exactly.
 */

public final class JFPTimeComponentSpecExact implements
JFPTimeComponentSpecType
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = 7519113013858992825L;
  }

  private final int         value;

  /**
   * @param in_value
   *          The value.
   */

  public JFPTimeComponentSpecExact(
    final int in_value)
  {
    this.value =
      (int) RangeCheck.checkGreaterEqual(
        in_value,
        "Value",
        0,
        "Minimum value");
  }

  @Override public <A, E extends Exception> A acceptTimeComponent(
    final JFPTimeComponentSpecVisitorType<A, E> v)
      throws JFPException,
      E
  {
    return v.exact(this);
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append("exact ");
    b.append(this.value);
    final String r = b.toString();
    assert r != null;
    return r;
  }

  @Override public boolean matches(
    final int x)
  {
    return x == this.value;
  }
}
