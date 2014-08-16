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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.io7m.jnull.Nullable;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * Utilities for constructing data for responses.
 */

public final class JFPResponseUtilities
{
  static <T> byte[] encodeList(
    final List<T> i)
    throws UnsupportedEncodingException,
      IOException
  {
    final ByteArrayOutputStream bao = new ByteArrayOutputStream();

    for (int index = 0; index < i.size(); ++index) {
      final String s = i.get(index).toString() + "\r\n";
      bao.write(s.getBytes("UTF-8"));
    }

    final byte[] r = bao.toByteArray();
    assert r != null;
    return r;
  }

  static <A, B> byte[] encodeMap(
    final Map<A, B> map)
    throws UnsupportedEncodingException,
      IOException
  {
    final ByteArrayOutputStream bao = new ByteArrayOutputStream();

    final StringBuilder sb = new StringBuilder();
    for (final A k : map.keySet()) {
      final B v = map.get(k);
      sb.setLength(0);
      sb.append(k);
      sb.append("|");
      sb.append(v);
      sb.append("\r\n");
      bao.write(sb.toString().getBytes("UTF-8"));
    }

    final byte[] r = bao.toByteArray();
    assert r != null;
    return r;
  }

  static <T> byte[] encodeSet(
    final Set<T> set)
    throws UnsupportedEncodingException,
      IOException
  {
    final ByteArrayOutputStream bao = new ByteArrayOutputStream();

    for (final T x : set) {
      final String s = x + "\r\n";
      bao.write(s.getBytes("UTF-8"));
    }

    final byte[] r = bao.toByteArray();
    assert r != null;
    return r;
  }

  static void sendBytesAsUTF8(
    final HttpServletResponse response,
    final byte[] e)
    throws IOException
  {
    JFPResponseUtilities.sendBytesAsUTF8WithStatus(
      response,
      HttpServletResponse.SC_OK,
      e);
  }

  static void sendBytesAsUTF8WithStatus(
    final HttpServletResponse response,
    final Integer status,
    final byte[] data)
    throws IOException
  {
    response.setStatus(status.intValue());
    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");
    response.setContentLength(data.length);
    response.getOutputStream().write(data);
    response.flushBuffer();
  }

  static void sendText(
    final HttpServletResponse response,
    final int code,
    final @Nullable String message)
    throws IOException
  {
    response.setStatus(code);
    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");
    final byte[] b =
      ((message != null ? message : "") + "\r\n").getBytes("UTF-8");
    response.setContentLength(b.length);
    response.getOutputStream().write(b);
    response.flushBuffer();
  }

  private JFPResponseUtilities()
  {
    throw new UnreachableCodeException();
  }
}
