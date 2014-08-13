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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jfprop.JFPFossilExecutable;
import com.io7m.jnull.NullCheckException;

@SuppressWarnings({ "static-method", "unused" }) public final class JFPFossilExecutableTest
{
  @Test public void testActual()
    throws Exception
  {
    Assert.assertEquals(
      new JFPFossilExecutable(new File("fghj7890")).getActual(),
      new File("fghj7890"));
    Assert.assertEquals(
      new JFPFossilExecutable(new File("fghj7890")).toString(),
      "fghj7890");
  }

  @Test public void testEquals()
    throws Exception
  {
    Assert.assertEquals(
      new JFPFossilExecutable(new File("fghj7890")),
      new JFPFossilExecutable(new File("fghj7890")));

    final JFPFossilExecutable x =
      new JFPFossilExecutable(new File("3456bcde"));
    Assert.assertEquals(x, x);
    Assert.assertNotEquals(
      new JFPFossilExecutable(new File("abcd1234")),
      new Object());
    Assert.assertNotEquals(
      new JFPFossilExecutable(new File("abcd1234")),
      null);
  }

  @Test public void testHashCode()
    throws Exception
  {
    Assert.assertEquals(
      new JFPFossilExecutable(new File("fghj7890")).hashCode(),
      new JFPFossilExecutable(new File("fghj7890")).hashCode());
  }

  @Test(expected = NullCheckException.class) public void testNull_0()
  {
    new JFPFossilExecutable((File) TestUtilities.actuallyNull());
  }
}
