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

/**
 * The type of time component specifications.
 */

public interface JFPTimeComponentSpecType extends Serializable
{
  /**
   * Accept a generic time component visitor.
   *
   * @param v
   *          The visitor.
   * @return The value returned by the visitor.
   * @throws JFPException
   *           If the visitor raises {@link JFPException}.
   * @throws E
   *           If the visitor raises <code>E</code>.
   * @param <A>
   *          The type of returned values.
   * @param <E>
   *          The type of raised exceptions.
   */

  <A, E extends Exception> A acceptTimeComponent(
    final JFPTimeComponentSpecVisitorType<A, E> v)
      throws JFPException,
      E;

  /**
   * @param x
   *          The incoming value.
   * @return <code>true</code> if the spec matches <code>x</code>.
   */

  boolean matches(
    final int x);
}
