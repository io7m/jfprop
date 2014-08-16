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

import java.util.Map;

import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * Functions to retrieve values from requests.
 */

public final class JFPRequestUtilities
{
  /**
   * Retrieve a value from an HTTP request.
   *
   * @param request
   *          The request parameters.
   * @param name
   *          The parameter name.
   *
   * @return The value.
   * @throws JFPExceptionNonexistent
   *           If the value is missing.
   * @throws JFPExceptionInvalidArgument
   *           If the parameter has multiple values.
   */

  public static String getValueSingle(
    final Map<String, String[]> request,
    final String name)
    throws JFPExceptionNonexistent,
      JFPExceptionInvalidArgument
  {
    NullCheck.notNull(request, "Request");
    NullCheck.notNull(name, "Name");

    if (request.containsKey(name) == false) {
      throw new JFPExceptionNonexistent(String.format(
        "No such parameter '%s'",
        name));
    }
    final String[] k = request.get(name);
    if (k == null) {
      throw new JFPExceptionNonexistent(String.format(
        "No such parameter '%s'",
        name));
    }
    if (k.length != 1) {
      throw new JFPExceptionInvalidArgument(String.format(
        "Parameter '%s' has multiple values",
        name));
    }

    final String value = k[0];
    if (value == null) {
      throw new JFPExceptionInvalidArgument(String.format(
        "Parameter '%s' has null value",
        name));
    }

    return value;
  }

  private JFPRequestUtilities()
  {
    throw new UnreachableCodeException();
  }
}
