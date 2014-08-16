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

/**
 * The type of generic time spec component visitors.
 *
 * @param <A>
 *          The type of returned values.
 * @param <E>
 *          The type of raised exceptions.
 */

public interface JFPTimeComponentSpecVisitorType<A, E extends Exception>
{
  /**
   * Visit a time spec component.
   *
   * @param e
   *          The component.
   * @return A value of <code>A</code>
   * @throws JFPException
   *           If required.
   * @throws E
   *           If required.
   */

  A divisible(
    final JFPTimeComponentSpecDivisible e)
      throws JFPException,
      E;

  /**
   * Visit a time spec component.
   *
   * @param e
   *          The component.
   * @return A value of <code>A</code>
   * @throws JFPException
   *           If required.
   * @throws E
   *           If required.
   */

  A exact(
    final JFPTimeComponentSpecExact e)
      throws JFPException,
      E;
}
