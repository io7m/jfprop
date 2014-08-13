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

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.Callable;

import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnimplementedCodeException;

/**
 * A task that notifies a remote of a sync.
 */

public final class JFPRemoteNotifyTask implements Callable<Boolean>
{
  private final URI                  remote_uri;
  private final LogUsableType        log;
  private final JFPErrorReporterType reporter;

  /**
   * Construct a synchronization task.
   *
   * @param in_log
   *          A log interface.
   * @param in_uri
   *          The remote URI.
   * @param in_reporter
   *          The error reporter.
   */

  public JFPRemoteNotifyTask(
    final LogUsableType in_log,
    final URI in_uri,
    final JFPErrorReporterType in_reporter)
  {
    this.log = NullCheck.notNull(in_log, "Log");
    this.remote_uri = NullCheck.notNull(in_uri, "URI");
    this.reporter = NullCheck.notNull(in_reporter, "Reporter");
  }

  @Override public Boolean call()
    throws Exception
  {
    this.log.debug("notifying " + this.remote_uri);

    if ("http".equals(this.remote_uri.getScheme())) {
      final HttpURLConnection c =
        (HttpURLConnection) this.remote_uri.toURL().openConnection();
      c.setRequestMethod("GET");
      c.setRequestProperty("User-Agent", JFPVersion.getUserAgent());
      final int r = c.getResponseCode();
      if (r != 200) {
        this.log.debug(String.format(
          "error code %d whilst notifying remote",
          r));
        this.reporter.onExternalHTTPError(
          this.remote_uri,
          r,
          "Notifying a remote");
        return Boolean.FALSE;
      }

    } else {
      throw new UnimplementedCodeException();
    }

    this.log.debug("notified remote");
    return Boolean.TRUE;
  }
}
