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

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;

/**
 * Main implementation of the {@link JFPRemoteControllerType} interface.
 */

final class JFPRemoteController implements JFPRemoteControllerType
{
  private static URI makeURI(
    final JFPRemote remote,
    final JFPProjectPath project)
  {
    final URI base = remote.getURI();
    final String base_text = base.toString();

    final StringBuilder sb = new StringBuilder();
    sb.append(base_text);
    if (base_text.endsWith("/") == false) {
      sb.append("/");
    }
    sb.append("on-commit?");
    sb.append("user=");
    sb.append(remote.getUser().getActual());
    sb.append("&");
    sb.append("key=");
    sb.append(remote.getKey().getActual());
    sb.append("&");
    sb.append("project=");
    sb.append(project.toString());

    return URI.create(sb.toString());
  }

  public static JFPRemoteControllerType newController(
    final LogUsableType log,
    final ExecutorService in_executor,
    final JFPErrorReporterType in_reporter)
  {
    return new JFPRemoteController(log, in_executor, in_reporter);
  }

  private final ExecutorService                   executor;
  private final LogUsableType                     log;
  private final JFPErrorReporterType reporter;

  private JFPRemoteController(
    final LogUsableType in_log,
    final ExecutorService in_executor,
    final JFPErrorReporterType in_reporter)
  {
    this.log = NullCheck.notNull(in_log, "Log");
    this.executor = NullCheck.notNull(in_executor, "Executor");
    this.reporter = NullCheck.notNull(in_reporter, "Reporter");
  }

  @Override public Future<Boolean> doCommitNotify(
    final JFPProjectPath project,
    final JFPRemote remote)
  {
    final URI sync = JFPRemoteController.makeURI(remote, project);
    return this.executor.submit(new JFPRemoteNotifyTask(
      this.log,
      sync,
      this.reporter));
  }
}
