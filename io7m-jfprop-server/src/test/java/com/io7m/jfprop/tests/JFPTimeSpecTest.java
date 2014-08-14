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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jfprop.JFPExceptionNonexistent;
import com.io7m.jfprop.JFPTimeSpec;

@SuppressWarnings({ "static-method", "unused" }) public final class JFPTimeSpecTest
{
  @Test public void testOK_0()
    throws Exception
  {
    final Map<String, String[]> r = new HashMap<String, String[]>();
    r.put("time_spec_day", new String[] { "any" });
    r.put("time_spec_hour", new String[] { "any" });
    r.put("time_spec_minute", new String[] { "any" });

    final JFPTimeSpec t = JFPTimeSpec.parseFromRequest(r);
    Assert.assertTrue(t.getDay().isNone());
    Assert.assertTrue(t.getMinute().isNone());
    Assert.assertTrue(t.getHour().isNone());
  }

  @Test(expected = JFPExceptionNonexistent.class) public void testBad_0()
    throws Exception
  {
    final Map<String, String[]> r = new HashMap<String, String[]>();
    r.put("time_spec_hour", new String[] { "any" });
    r.put("time_spec_minute", new String[] { "any" });

    JFPTimeSpec.parseFromRequest(r);
  }

  @Test(expected = JFPExceptionNonexistent.class) public void testBad_1()
    throws Exception
  {
    final Map<String, String[]> r = new HashMap<String, String[]>();
    r.put("time_spec_day", new String[] { "any" });
    r.put("time_spec_minute", new String[] { "any" });

    JFPTimeSpec.parseFromRequest(r);
  }

  @Test(expected = JFPExceptionNonexistent.class) public void testBad_2()
    throws Exception
  {
    final Map<String, String[]> r = new HashMap<String, String[]>();
    r.put("time_spec_day", new String[] { "any" });
    r.put("time_spec_hour", new String[] { "any" });

    JFPTimeSpec.parseFromRequest(r);
  }

  @Test public void testMatches_0()
    throws Exception
  {
    final Map<String, String[]> r = new HashMap<String, String[]>();
    r.put("time_spec_day", new String[] { "any" });
    r.put("time_spec_hour", new String[] { "any" });
    r.put("time_spec_minute", new String[] { "any" });

    final JFPTimeSpec t = JFPTimeSpec.parseFromRequest(r);

    final Calendar c = Calendar.getInstance();
    c.clear();
    c.set(Calendar.DAY_OF_MONTH, 0);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);

    Assert.assertTrue(t.matches(c));
  }

  @Test public void testMatches_1()
    throws Exception
  {
    final Map<String, String[]> r = new HashMap<String, String[]>();
    r.put("time_spec_day", new String[] { "exact 5" });
    r.put("time_spec_hour", new String[] { "exact 5" });
    r.put("time_spec_minute", new String[] { "exact 5" });

    final JFPTimeSpec t = JFPTimeSpec.parseFromRequest(r);

    final Calendar c = Calendar.getInstance();
    c.clear();
    c.set(Calendar.DAY_OF_MONTH, 5);
    c.set(Calendar.HOUR_OF_DAY, 5);
    c.set(Calendar.MINUTE, 5);

    Assert.assertTrue(t.matches(c));
  }

  @Test public void testNoMatch_0()
    throws Exception
  {
    final Map<String, String[]> r = new HashMap<String, String[]>();
    r.put("time_spec_day", new String[] { "exact 4" });
    r.put("time_spec_hour", new String[] { "exact 5" });
    r.put("time_spec_minute", new String[] { "exact 5" });

    final JFPTimeSpec t = JFPTimeSpec.parseFromRequest(r);

    final Calendar c = Calendar.getInstance();
    c.clear();
    c.set(Calendar.DAY_OF_MONTH, 5);
    c.set(Calendar.HOUR_OF_DAY, 5);
    c.set(Calendar.MINUTE, 5);

    Assert.assertFalse(t.matches(c));
  }

  @Test public void testNoMatch_1()
    throws Exception
  {
    final Map<String, String[]> r = new HashMap<String, String[]>();
    r.put("time_spec_day", new String[] { "exact 5" });
    r.put("time_spec_hour", new String[] { "exact 4" });
    r.put("time_spec_minute", new String[] { "exact 5" });

    final JFPTimeSpec t = JFPTimeSpec.parseFromRequest(r);

    final Calendar c = Calendar.getInstance();
    c.clear();
    c.set(Calendar.DAY_OF_MONTH, 5);
    c.set(Calendar.HOUR_OF_DAY, 5);
    c.set(Calendar.MINUTE, 5);

    Assert.assertFalse(t.matches(c));
  }

  @Test public void testNoMatch_2()
    throws Exception
  {
    final Map<String, String[]> r = new HashMap<String, String[]>();
    r.put("time_spec_day", new String[] { "exact 5" });
    r.put("time_spec_hour", new String[] { "exact 5" });
    r.put("time_spec_minute", new String[] { "exact 4" });

    final JFPTimeSpec t = JFPTimeSpec.parseFromRequest(r);

    final Calendar c = Calendar.getInstance();
    c.clear();
    c.set(Calendar.DAY_OF_MONTH, 5);
    c.set(Calendar.HOUR_OF_DAY, 5);
    c.set(Calendar.MINUTE, 5);

    Assert.assertFalse(t.matches(c));
  }
}
