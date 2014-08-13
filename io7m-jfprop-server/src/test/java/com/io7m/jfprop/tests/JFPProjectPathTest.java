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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jfprop.JFPExceptionInvalidArgument;
import com.io7m.jfprop.JFPProjectPath;
import com.io7m.jnull.NullCheckException;

@SuppressWarnings({ "static-method", "unused" }) public final class JFPProjectPathTest
{
  @Test public void testActual()
    throws Exception
  {
    Assert.assertEquals(
      new JFPProjectPath("/time/for/bed").toString(),
      "/time/for/bed");
  }

  @Test public void testCompareTo()
    throws Exception
  {
    Assert.assertEquals(new JFPProjectPath("/time/for/bed")
      .compareTo(new JFPProjectPath("/time/for/bed")), 0);
    Assert.assertEquals(
      new JFPProjectPath("/a").compareTo(new JFPProjectPath("/b")),
      -1);
    Assert.assertEquals(
      new JFPProjectPath("/b").compareTo(new JFPProjectPath("/a")),
      1);
  }

  @Test public void testComponents()
    throws Exception
  {
    final JFPProjectPath p = new JFPProjectPath("/time/for/bed");
    final List<String> c = p.getComponents();
    Assert.assertEquals(3, c.size());
    Assert.assertEquals("time", c.get(0));
    Assert.assertEquals("for", c.get(1));
    Assert.assertEquals("bed", c.get(2));
  }

  @Test public void testEquals()
    throws Exception
  {
    Assert.assertEquals(
      new JFPProjectPath("/time/for/bed"),
      new JFPProjectPath("/time/for/bed"));

    final JFPProjectPath x = new JFPProjectPath("/sugar/lumps");
    Assert.assertEquals(x, x);
    Assert.assertNotEquals(new JFPProjectPath("/someone"), new Object());
    Assert.assertNotEquals(new JFPProjectPath("/someone"), null);
  }

  @Test public void testHashCode()
    throws Exception
  {
    Assert.assertEquals(
      new JFPProjectPath("/time/for/bed").hashCode(),
      new JFPProjectPath("/time/for/bed").hashCode());
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testNotValid_0()
      throws JFPExceptionInvalidArgument
  {
    Assert.assertFalse(JFPProjectPath.validPath(" "));
    new JFPProjectPath(" ");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testNotValid_1()
      throws JFPExceptionInvalidArgument
  {
    Assert.assertFalse(JFPProjectPath.validPath("+"));
    new JFPProjectPath("+");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testNotValid_2()
      throws JFPExceptionInvalidArgument
  {
    Assert.assertFalse(JFPProjectPath.validPath(""));
    new JFPProjectPath("");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testNotValid_3()
      throws JFPExceptionInvalidArgument
  {
    Assert.assertFalse(JFPProjectPath.validPath("/a/b/../c"));
    new JFPProjectPath("/a/b/../c");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testNotValid_4()
      throws JFPExceptionInvalidArgument
  {
    Assert.assertFalse(JFPProjectPath.validPath("/"));
    new JFPProjectPath("/");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testNotValid_5()
      throws JFPExceptionInvalidArgument
  {
    Assert.assertFalse(JFPProjectPath.validPath("////////////"));
    new JFPProjectPath("////////////");
  }

  @Test public void testNotValidComponent_0()
  {
    Assert.assertFalse(JFPProjectPath.validPathComponent(""));
  }

  @Test public void testNotValidComponent_1()
  {
    Assert.assertFalse(JFPProjectPath.validPathComponent("/"));
  }

  @Test public void testNotValidComponent_2()
  {
    Assert.assertFalse(JFPProjectPath.validPathComponent(".."));
  }

  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    JFPProjectPath.validPath((String) TestUtilities.actuallyNull());
  }

  @Test public void testValid_0()
    throws Exception
  {
    Assert.assertTrue(JFPProjectPath.validPath("/io7m0.fossil"));
    Assert.assertEquals(
      "/io7m0.fossil",
      new JFPProjectPath("/io7m0.fossil").toString());
  }

  @Test public void testValid_1()
    throws Exception
  {
    Assert.assertTrue(JFPProjectPath.validPath("/a/b/c"));
    Assert.assertEquals("/a/b/c", new JFPProjectPath("/a/b/c").toString());
  }

  @Test public void testValid_2()
    throws Exception
  {
    Assert.assertTrue(JFPProjectPath.validPath("/a/b/c/"));
    Assert.assertEquals("/a/b/c", new JFPProjectPath("/a/b/c/").toString());
  }

  @Test public void testValidComponent_0()
  {
    Assert.assertTrue(JFPProjectPath.validPathComponent("io7m0.fossil"));
  }
}
