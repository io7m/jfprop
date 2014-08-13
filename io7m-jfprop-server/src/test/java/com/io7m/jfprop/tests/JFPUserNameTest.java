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

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jfprop.JFPExceptionAuthentication;
import com.io7m.jfprop.JFPUserName;
import com.io7m.jnull.NullCheckException;

@SuppressWarnings({ "static-method", "unused" }) public final class JFPUserNameTest
{
  @Test public void testActual()
    throws Exception
  {
    Assert.assertEquals(new JFPUserName("zebedee").getActual(), "zebedee");
    Assert.assertEquals(new JFPUserName("zebedee").toString(), "zebedee");
  }

  @Test public void testCompareTo()
    throws Exception
  {
    Assert.assertEquals(
      new JFPUserName("zebedee").compareTo(new JFPUserName("zebedee")),
      0);
    Assert.assertEquals(
      new JFPUserName("a").compareTo(new JFPUserName("b")),
      -1);
    Assert.assertEquals(
      new JFPUserName("b").compareTo(new JFPUserName("a")),
      1);
  }

  @Test public void testEquals()
    throws Exception
  {
    Assert.assertEquals(
      new JFPUserName("zebedee"),
      new JFPUserName("zebedee"));

    final JFPUserName x = new JFPUserName("dougal");
    Assert.assertEquals(x, x);
    Assert.assertNotEquals(new JFPUserName("someone"), new Object());
    Assert.assertNotEquals(new JFPUserName("someone"), null);
  }

  @Test public void testHashCode()
    throws Exception
  {
    Assert.assertEquals(
      new JFPUserName("zebedee").hashCode(),
      new JFPUserName("zebedee").hashCode());
  }

  @Test(expected = JFPExceptionAuthentication.class) public
    void
    testNotValid_0()
      throws Exception
  {
    Assert.assertFalse(JFPUserName.validName(" "));
    new JFPUserName(" ");
  }

  @Test(expected = JFPExceptionAuthentication.class) public
    void
    testNotValid_1()
      throws Exception
  {
    Assert.assertFalse(JFPUserName.validName("+"));
    new JFPUserName("+");
  }

  @Test(expected = JFPExceptionAuthentication.class) public
    void
    testNotValid_2()
      throws Exception
  {
    Assert.assertFalse(JFPUserName.validName(""));
    new JFPUserName("");
  }

  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    JFPUserName.validName((String) TestUtilities.actuallyNull());
  }

  @Test public void testValid_0()
  {
    Assert
      .assertTrue(JFPUserName
        .validName("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012345678_"));
  }

  @Test public void testValid_1()
  {
    Assert.assertTrue(JFPUserName.validName("someone"));
  }
}
