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
import com.io7m.jfprop.JFPKey;
import com.io7m.jnull.NullCheckException;

@SuppressWarnings({ "static-method", "unused" }) public final class JFPKeyTest
{
  @Test public void testActual()
    throws Exception
  {
    Assert.assertEquals(new JFPKey("fghj7890").getActual(), "fghj7890");
    Assert.assertEquals(new JFPKey("fghj7890").toString(), "fghj7890");
  }

  @Test public void testCompareTo()
    throws Exception
  {
    Assert.assertEquals(
      new JFPKey("fghj7890").compareTo(new JFPKey("fghj7890")),
      0);
    Assert.assertEquals(new JFPKey("a").compareTo(new JFPKey("b")), -1);
    Assert.assertEquals(new JFPKey("b").compareTo(new JFPKey("a")), 1);
  }

  @Test public void testEquals()
    throws Exception
  {
    Assert.assertEquals(new JFPKey("fghj7890"), new JFPKey("fghj7890"));

    final JFPKey x = new JFPKey("3456bcde");
    Assert.assertEquals(x, x);
    Assert.assertNotEquals(new JFPKey("abcd1234"), new Object());
    Assert.assertNotEquals(new JFPKey("abcd1234"), null);
  }

  @Test public void testHashCode()
    throws Exception
  {
    Assert.assertEquals(new JFPKey("fghj7890").hashCode(), new JFPKey(
      "fghj7890").hashCode());
  }

  @Test(expected = JFPExceptionAuthentication.class) public
    void
    testNotValid_0()
      throws Exception
  {
    Assert.assertFalse(JFPKey.validKey(" "));
    new JFPKey(" ");
  }

  @Test(expected = JFPExceptionAuthentication.class) public
    void
    testNotValid_1()
      throws Exception
  {
    Assert.assertFalse(JFPKey.validKey("+"));
    new JFPKey("+");
  }

  @Test(expected = JFPExceptionAuthentication.class) public
    void
    testNotValid_2()
      throws Exception
  {
    Assert.assertFalse(JFPKey.validKey(""));
    new JFPKey("");
  }

  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    JFPKey.validKey((String) TestUtilities.actuallyNull());
  }

  @Test public void testValid_0()
  {
    Assert
      .assertTrue(JFPKey.validKey("abcdefghijklmnopqrstuvwxyz0123456789"));
  }

  @Test public void testValid_1()
  {
    Assert.assertTrue(JFPKey.validKey("abcd1234"));
  }
}
