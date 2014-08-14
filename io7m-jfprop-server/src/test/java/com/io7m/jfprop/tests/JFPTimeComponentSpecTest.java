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

import com.io7m.jfprop.JFPExceptionInvalidArgument;
import com.io7m.jfprop.JFPTimeComponentSpec;
import com.io7m.jfprop.JFPTimeComponentSpecDivisible;
import com.io7m.jfprop.JFPTimeComponentSpecExact;
import com.io7m.jfprop.JFPTimeComponentSpecType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Some;

@SuppressWarnings({ "static-method", "unused" }) public final class JFPTimeComponentSpecTest
{
  @Test public void testExactOK_0()
    throws Exception
  {
    final Some<JFPTimeComponentSpecType> e =
      (Some<JFPTimeComponentSpecType>) JFPTimeComponentSpec.parse("exact 5");
    Assert.assertTrue(e.get() instanceof JFPTimeComponentSpecExact);
    Assert.assertTrue(e.get().matches(5));
  }

  @Test public void testExactOK_1()
    throws Exception
  {
    final Some<JFPTimeComponentSpecType> e =
      (Some<JFPTimeComponentSpecType>) JFPTimeComponentSpec.parse("exact\t5");
    Assert.assertTrue(e.get() instanceof JFPTimeComponentSpecExact);
    Assert.assertTrue(e.get().matches(5));
  }

  @Test public void testExactOK_2()
    throws Exception
  {
    final Some<JFPTimeComponentSpecType> e =
      (Some<JFPTimeComponentSpecType>) JFPTimeComponentSpec
        .parse("exact               5");
    Assert.assertTrue(e.get() instanceof JFPTimeComponentSpecExact);
    Assert.assertTrue(e.get().matches(5));
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testExactFail_0()
      throws Exception
  {
    JFPTimeComponentSpec.parse("exact z");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testExactFail_1()
      throws Exception
  {
    JFPTimeComponentSpec.parse("exact");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testFail_0()
      throws Exception
  {
    JFPTimeComponentSpec.parse("unknown");
  }

  @Test public void testAny_0()
    throws Exception
  {
    final OptionType<JFPTimeComponentSpecType> r =
      JFPTimeComponentSpec.parse("any");
    Assert.assertTrue(r.isNone());
  }

  @Test public void testDivisibleOK_0()
    throws Exception
  {
    final Some<JFPTimeComponentSpecType> e =
      (Some<JFPTimeComponentSpecType>) JFPTimeComponentSpec
        .parse("divisible 5");
    Assert.assertTrue(e.get() instanceof JFPTimeComponentSpecDivisible);
    Assert.assertTrue(e.get().matches(5));
  }

  @Test public void testDivisibleOK_1()
    throws Exception
  {
    final Some<JFPTimeComponentSpecType> e =
      (Some<JFPTimeComponentSpecType>) JFPTimeComponentSpec
        .parse("divisible\t5");
    Assert.assertTrue(e.get() instanceof JFPTimeComponentSpecDivisible);
    Assert.assertTrue(e.get().matches(5));
  }

  @Test public void testDivisibleOK_2()
    throws Exception
  {
    final Some<JFPTimeComponentSpecType> e =
      (Some<JFPTimeComponentSpecType>) JFPTimeComponentSpec
        .parse("divisible               5");
    Assert.assertTrue(e.get() instanceof JFPTimeComponentSpecDivisible);
    Assert.assertTrue(e.get().matches(5));
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testDivisibleFail_2()
      throws Exception
  {
    JFPTimeComponentSpec.parse("divisible 0");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testDivisibleFail_0()
      throws Exception
  {
    JFPTimeComponentSpec.parse("divisible z");
  }

  @Test(expected = JFPExceptionInvalidArgument.class) public
    void
    testDivisibleFail_1()
      throws Exception
  {
    JFPTimeComponentSpec.parse("divisible");
  }
}
