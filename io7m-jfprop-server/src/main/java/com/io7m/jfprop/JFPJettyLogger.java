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

import org.eclipse.jetty.util.log.Logger;

import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.jlog.LogType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.Nullable;

/**
 * Implementation of Jetty's {@link Logger} interface, to allow it to use
 * jlog.
 */

public final class JFPJettyLogger implements Logger
{
  private static String showStringAndThrowable(
    final @Nullable String msg,
    final @Nullable Throwable thrown)
  {
    assert msg != null;
    assert thrown != null;

    final StringBuilder sb = new StringBuilder();
    sb.append("exception: ");
    sb.append(msg);
    sb.append(": ");
    sb.append(thrown.toString());
    sb.append("\n");

    for (final StackTraceElement e : thrown.getStackTrace()) {
      sb.append("  ");
      sb.append(e.toString());
      sb.append("\n");
    }

    final String r = sb.toString();
    assert r != null;
    return r;
  }

  private final LogUsableType log_jetty;
  private final LogType       log_main;

  /**
   * Construct a logger.
   */

  public JFPJettyLogger()
  {
    this.log_main =
      Log.newLog(LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG), "main");
    this.log_jetty = this.log_main.with("jetty");
  }

  @Override public void debug(
    final @Nullable String msg,
    final long value)
  {
    this.log_jetty.debug(String.format("%s: %s", msg, value));
  }

  @Override public void debug(
    final @Nullable String msg,
    final @Nullable Object... args)
  {
    // this.log_jetty.debug(String.format(msg, args));
  }

  @Override public void debug(
    final @Nullable String msg,
    final @Nullable Throwable thrown)
  {
    // this.log_jetty.debug(String.format("exception: %s: %s", msg, thrown));
  }

  @Override public void debug(
    final @Nullable Throwable thrown)
  {
    // this.log_jetty.debug(String.format("exception: %s", thrown));
  }

  @Override public Logger getLogger(
    final @Nullable String name)
  {
    return this;
  }

  /**
   * @return A reference to the main logger.
   */

  public LogType getMainLog()
  {
    return this.log_main;
  }

  @Override public String getName()
  {
    return this.log_jetty.getAbsoluteDestination();
  }

  @Override public void ignore(
    final @Nullable Throwable ignored)
  {
    // Ignore.
  }

  @Override public void info(
    final @Nullable String msg,
    final @Nullable Object... args)
  {
    // this.log_jetty.info(String.format(msg, args));
  }

  @Override public void info(
    final @Nullable String msg,
    final @Nullable Throwable thrown)
  {
    final String r = JFPJettyLogger.showStringAndThrowable(msg, thrown);
    this.log_jetty.info(r);
  }

  @Override public void info(
    final @Nullable Throwable thrown)
  {
    this.log_jetty.info(String.format("exception: %s", thrown));
  }

  @Override public boolean isDebugEnabled()
  {
    return this.log_jetty.wouldLogAtLevel(LogLevel.LOG_DEBUG);
  }

  @Override public void setDebugEnabled(
    final boolean enabled)
  {
    // Nothing!
  }

  @Override public void warn(
    final @Nullable String msg,
    final @Nullable Object... args)
  {
    this.log_jetty.warn(String.format(msg, args));
  }

  @Override public void warn(
    final @Nullable String msg,
    final @Nullable Throwable thrown)
  {
    final String r = JFPJettyLogger.showStringAndThrowable(msg, thrown);
    this.log_jetty.warn(r);
  }

  @Override public void warn(
    final @Nullable Throwable thrown)
  {
    this.log_jetty.warn(String.format("exception: %s", thrown));
  }
}
