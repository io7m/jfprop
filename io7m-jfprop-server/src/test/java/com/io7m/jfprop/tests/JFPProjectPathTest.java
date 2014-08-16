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
import com.io7m.jfprop.JFPRepositoryPath;
import com.io7m.jnull.NullCheckException;

@SuppressWarnings({ "static-method", "unused" }) public final class JFPProjectPathTest
{
  @Test public void testActual()
    throws Exception
  {
    Assert.assertEquals(
      new JFPRepositoryPath("/time/for/bed").toString(),
      "/time/for/bed");
  }

  @Test public void testCompareTo()
    throws Exception
  {
    Assert.assertEquals(new JFPRepositoryPath("/time/for/bed")
      .compareTo(new JFPRepositoryPath("/time/for/bed")), 0);
    Assert.assertEquals(
      new JFPRepositoryPath("/a").compareTo(new JFPRepositoryPath("/b")),
      -1);
    Assert.assertEquals(
      new JFPRepositoryPath("/b").compareTo(new JFPRepositoryPath("/a")),
      1);
  }

  @Test public void testComponents()
    throws Exception
  {
    final JFPRepositoryPath p = new JFPRepositoryPath("/time/for/bed");
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
      new JFPRepositoryPath("/time/for/bed"),
      new JFPRepositoryPath("/time/for/bed"));

    final JFPRepositoryPath x = new JFPRepositoryPath("/sugar/lumps");
    Assert.assertEquals(x, x);
    Assert.assertNotEquals(new JFPRepositoryPath("/someone"), new Object());
    Assert.assertNotEquals(new JFPRepositoryPath("/someone"), null);
  }

  @Test public void testHashCode()
    throws Exception
  {
    Assert.assertEquals(
      new JFPRepositoryPath("/time/for/bed").hashCode(),
      new JFPRepositoryPath("/time/for/bed").hashCode());
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testNotValid_0()
      throws JFPExceptionInvalidArgument
  {
    Assert.assertFalse(JFPRepositoryPath.validPath(" "));
    new JFPRepositoryPath(" ");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testNotValid_1()
      throws JFPExceptionInvalidArgument
  {
    Assert.assertFalse(JFPRepositoryPath.validPath("+"));
    new JFPRepositoryPath("+");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testNotValid_2()
      throws JFPExceptionInvalidArgument
  {
    Assert.assertFalse(JFPRepositoryPath.validPath(""));
    new JFPRepositoryPath("");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testNotValid_3()
      throws JFPExceptionInvalidArgument
  {
    Assert.assertFalse(JFPRepositoryPath.validPath("/a/b/../c"));
    new JFPRepositoryPath("/a/b/../c");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testNotValid_4()
      throws JFPExceptionInvalidArgument
  {
    Assert.assertFalse(JFPRepositoryPath.validPath("/"));
    new JFPRepositoryPath("/");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testNotValid_5()
      throws JFPExceptionInvalidArgument
  {
    Assert.assertFalse(JFPRepositoryPath.validPath("////////////"));
    new JFPRepositoryPath("////////////");
  }

  @Test public void testNotValidComponent_0()
  {
    Assert.assertFalse(JFPRepositoryPath.validPathComponent(""));
  }

  @Test public void testNotValidComponent_1()
  {
    Assert.assertFalse(JFPRepositoryPath.validPathComponent("/"));
  }

  @Test public void testNotValidComponent_2()
  {
    Assert.assertFalse(JFPRepositoryPath.validPathComponent(".."));
  }

  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    JFPRepositoryPath.validPath((String) TestUtilities.actuallyNull());
  }

  @Test public void testValid_0()
    throws Exception
  {
    Assert.assertTrue(JFPRepositoryPath.validPath("/io7m0.fossil"));
    Assert.assertEquals(
      "/io7m0.fossil",
      new JFPRepositoryPath("/io7m0.fossil").toString());
  }

  @Test public void testValid_1()
    throws Exception
  {
    Assert.assertTrue(JFPRepositoryPath.validPath("/a/b/c"));
    Assert.assertEquals("/a/b/c", new JFPRepositoryPath("/a/b/c").toString());
  }

  @Test public void testValid_2()
    throws Exception
  {
    Assert.assertTrue(JFPRepositoryPath.validPath("/a/b/c/"));
    Assert.assertEquals("/a/b/c", new JFPRepositoryPath("/a/b/c/").toString());
  }

  @Test public void testValidComponent_0()
  {
    Assert.assertTrue(JFPRepositoryPath.validPathComponent("io7m0.fossil"));
  }
}
