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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;

import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.OptionVisitorType;
import com.io7m.jfunctional.Some;
import com.io7m.jnull.NullCheck;

/**
 * The type of time specifications.
 */

public final class JFPTimeSpec implements Serializable
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = -2010117391552171355L;
  }

  /**
   * Attempt to parse a time spec from the given request.
   *
   * @param request
   *          The request parameters.
   * @return A time specification.
   * @throws JFPExceptionInvalidArgument
   *           On invalid arguments.
   * @throws JFPExceptionNonexistent
   *           On missing parameters.
   */

  public static JFPTimeSpec parseFromRequest(
    final Map<String, String[]> request)
    throws JFPExceptionInvalidArgument,
      JFPExceptionNonexistent
  {
    final OptionType<JFPTimeComponentSpecType> d =
      JFPTimeComponentSpec.parse(JFPRequestUtilities.getValueSingle(
        request,
        "time_spec_day"));
    final OptionType<JFPTimeComponentSpecType> h =
      JFPTimeComponentSpec.parse(JFPRequestUtilities.getValueSingle(
        request,
        "time_spec_hour"));
    final OptionType<JFPTimeComponentSpecType> m =
      JFPTimeComponentSpec.parse(JFPRequestUtilities.getValueSingle(
        request,
        "time_spec_minute"));
    return new JFPTimeSpec(d, h, m);
  }

  private final OptionType<JFPTimeComponentSpecType> day;
  private final OptionType<JFPTimeComponentSpecType> hour;
  private final OptionType<JFPTimeComponentSpecType> minute;

  private JFPTimeSpec(
    final OptionType<JFPTimeComponentSpecType> in_day,
    final OptionType<JFPTimeComponentSpecType> in_hour,
    final OptionType<JFPTimeComponentSpecType> in_minute)
  {
    this.day = NullCheck.notNull(in_day, "Day");
    this.hour = NullCheck.notNull(in_hour, "Hour");
    this.minute = NullCheck.notNull(in_minute, "Minute");
  }

  /**
   * @return The specification for matching days.
   */

  public OptionType<JFPTimeComponentSpecType> getDay()
  {
    return this.day;
  }

  /**
   * @return The specification for matching hours.
   */

  public OptionType<JFPTimeComponentSpecType> getHour()
  {
    return this.hour;
  }

  /**
   * @return The specification for matching minutes.
   */

  public OptionType<JFPTimeComponentSpecType> getMinute()
  {
    return this.minute;
  }

  /**
   * @param time
   *          The time.
   * @return <code>true</code> if the specification matches the given time.
   */

  @SuppressWarnings({ "boxing", "null" }) public boolean matches(
    final Calendar time)
  {
    NullCheck.notNull(time, "Time");

    final Boolean day_matches =
      this.day
        .accept(new OptionVisitorType<JFPTimeComponentSpecType, Boolean>() {
          @Override public Boolean none(
            final None<JFPTimeComponentSpecType> n)
          {
            return Boolean.TRUE;
          }

          @Override public Boolean some(
            final Some<JFPTimeComponentSpecType> s)
          {
            return s.get().matches(time.get(Calendar.DAY_OF_MONTH));
          }
        });

    final Boolean hour_matches =
      this.hour
        .accept(new OptionVisitorType<JFPTimeComponentSpecType, Boolean>() {
          @Override public Boolean none(
            final None<JFPTimeComponentSpecType> n)
          {
            return Boolean.TRUE;
          }

          @Override public Boolean some(
            final Some<JFPTimeComponentSpecType> s)
          {
            return s.get().matches(time.get(Calendar.HOUR_OF_DAY));
          }
        });

    final Boolean minute_matches =
      this.minute
        .accept(new OptionVisitorType<JFPTimeComponentSpecType, Boolean>() {
          @Override public Boolean none(
            final None<JFPTimeComponentSpecType> n)
          {
            return Boolean.TRUE;
          }

          @Override public Boolean some(
            final Some<JFPTimeComponentSpecType> s)
          {
            return s.get().matches(time.get(Calendar.MINUTE));
          }
        });

    return day_matches && hour_matches && minute_matches;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();

    b.append(this.day
      .accept(new OptionVisitorType<JFPTimeComponentSpecType, String>() {
        @Override public String none(
          final None<JFPTimeComponentSpecType> n)
        {
          return "any";
        }

        @Override public String some(
          final Some<JFPTimeComponentSpecType> s)
        {
          return s.get().toString();
        }
      }));
    b.append("|");
    b.append(this.hour
      .accept(new OptionVisitorType<JFPTimeComponentSpecType, String>() {
        @Override public String none(
          final None<JFPTimeComponentSpecType> n)
        {
          return "any";
        }

        @Override public String some(
          final Some<JFPTimeComponentSpecType> s)
        {
          return s.get().toString();
        }
      }));
    b.append("|");
    b.append(this.minute
      .accept(new OptionVisitorType<JFPTimeComponentSpecType, String>() {
        @Override public String none(
          final None<JFPTimeComponentSpecType> n)
        {
          return "any";
        }

        @Override public String some(
          final Some<JFPTimeComponentSpecType> s)
        {
          return s.get().toString();
        }
      }));

    final String r = b.toString();
    assert r != null;
    return r;
  }
}
