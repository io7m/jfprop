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

package com.io7m.jfprop.tests;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jfprop.JFPExceptionInvalidArgument;
import com.io7m.jfprop.JFPExceptionNonexistent;
import com.io7m.jfprop.JFPRemote;
import com.io7m.jnull.NullCheckException;

@SuppressWarnings({ "static-method", "unchecked", "unused" }) public final class JFPRemoteTest
{
  @Test(expected = NullCheckException.class) public
    void
    testFromParameters_Null_0()
      throws Exception
  {
    JFPRemote.fromParameters((Map<String, String[]>) TestUtilities
      .actuallyNull());
  }

  @Test(expected = JFPExceptionNonexistent.class) public
    void
    testFromParameters_Missing_0()
      throws Exception
  {
    final Map<String, String[]> request = new HashMap<String, String[]>();
    JFPRemote.fromParameters(request);
  }

  @Test(expected = JFPExceptionNonexistent.class) public
    void
    testFromParameters_Missing_1()
      throws Exception
  {
    final Map<String, String[]> request = new HashMap<String, String[]>();
    request.put("remote_key", new String[] { "abcd1234" });

    JFPRemote.fromParameters(request);
  }

  @Test(expected = JFPExceptionNonexistent.class) public
    void
    testFromParameters_Missing_2()
      throws Exception
  {
    final Map<String, String[]> request = new HashMap<String, String[]>();
    request.put("remote_key", new String[] { "abcd1234" });
    request.put("remote_uri", new String[] { "http://example.com/" });

    JFPRemote.fromParameters(request);
  }

  @Test public void testFromParameters_OK_0()
    throws Exception
  {
    final Map<String, String[]> request = new HashMap<String, String[]>();
    request.put("remote_key", new String[] { "abcd1234" });
    request.put("remote_uri", new String[] { "http://example.com/" });
    request.put("remote_user", new String[] { "someone" });

    final JFPRemote r = JFPRemote.fromParameters(request);
    Assert.assertEquals("abcd1234", r.getKey().getActual());
    Assert.assertEquals("http://example.com/", r.getURI().toString());
    Assert.assertEquals("someone", r.getUser().getActual());
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testFromParameters_Invalid_0()
      throws Exception
  {
    final Map<String, String[]> request = new HashMap<String, String[]>();
    request.put("remote_key", new String[] { "_" });
    request.put("remote_uri", new String[] { "http://example.com/" });
    request.put("remote_user", new String[] { "someone" });

    JFPRemote.fromParameters(request);
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testFromParameters_Invalid_1()
      throws Exception
  {
    final Map<String, String[]> request = new HashMap<String, String[]>();
    request.put("remote_key", new String[] { "abcd1234" });
    request.put("remote_uri", new String[] { "not valid" });
    request.put("remote_user", new String[] { "someone" });

    JFPRemote.fromParameters(request);
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testFromParameters_Invalid_2()
      throws Exception
  {
    final Map<String, String[]> request = new HashMap<String, String[]>();
    request.put("remote_key", new String[] { "abcd1234" });
    request.put("remote_uri", new String[] { "http://example.com/" });
    request.put("remote_user", new String[] { "+" });

    JFPRemote.fromParameters(request);
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testFromParameters_Invalid_3()
      throws Exception
  {
    final Map<String, String[]> request = new HashMap<String, String[]>();
    request.put("remote_key", new String[] { "abcd1234", "abcd1234" });
    request.put("remote_uri", new String[] { "http://example.com/" });
    request.put("remote_user", new String[] { "someone" });

    JFPRemote.fromParameters(request);
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testFromParameters_Invalid_4()
      throws Exception
  {
    final Map<String, String[]> request = new HashMap<String, String[]>();
    request.put("remote_key", new String[] { "abcd1234", "abcd1234" });
    request.put("remote_uri", new String[] {
      "http://example.com/",
      "http://example.com/" });
    request.put("remote_user", new String[] { "someone" });

    JFPRemote.fromParameters(request);
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testFromParameters_Invalid_5()
      throws Exception
  {
    final Map<String, String[]> request = new HashMap<String, String[]>();
    request.put("remote_key", new String[] { "abcd1234", "abcd1234" });
    request.put("remote_uri", new String[] {
      "http://example.com/",
      "http://example.com/" });
    request.put("remote_user", new String[] { "someone", "someone" });

    JFPRemote.fromParameters(request);
  }
}
