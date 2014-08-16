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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.io7m.jnull.NullCheck;

/**
 * The type of specifications of mass repository syncs.
 *
 * A specification will be evaluated once per second, and if the time
 * specification condition matches, the matching repositories are synced.
 */

public final class JFPMassSyncSpec implements Serializable
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = 6083888407434139170L;
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

  public static JFPMassSyncSpec parseFromRequest(
    final Map<String, String[]> request)
      throws JFPExceptionInvalidArgument,
      JFPExceptionNonexistent
  {
    try {
      final JFPTimeSpec t = JFPTimeSpec.parseFromRequest(request);
      final String p = JFPRequestUtilities.getValueSingle(request, "pattern");
      return new JFPMassSyncSpec(t, Pattern.compile(p));
    } catch (final PatternSyntaxException e) {
      throw new JFPExceptionInvalidArgument(e);
    }
  }

  private final Pattern     repos_match;

  private final JFPTimeSpec time_spec;

  private JFPMassSyncSpec(
    final JFPTimeSpec in_time_spec,
    final Pattern in_repos_match)
  {
    this.time_spec = NullCheck.notNull(in_time_spec, "Time spec");
    this.repos_match = NullCheck.notNull(in_repos_match, "Repos pattern");
  }

  /**
   * @return <code>true</code> if the given repository name matches.
   *
   * @param name
   *          The repository name, relative to the repository directory.
   */

  public boolean syncSpecRepositoryMatches(
    final String name)
  {
    final Matcher m = this.repos_match.matcher(name);
    return m.matches();
  }

  /**
   * @return <code>true</code> if the given time matches the spec.
   * @param time
   *          The current time.
   */

  public boolean syncSpecTimeMatches(
    final Calendar time)
  {
    return this.time_spec.matches(time);
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append(this.time_spec);
    b.append("|");
    b.append(this.repos_match);
    return b.toString();
  }
}
