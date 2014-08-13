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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.io7m.jfunctional.FunctionType;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;

/**
 * A task that synchronizes a fossil repository with a remote.
 */

public final class JFPFossilSyncTask implements Callable<Boolean>
{
  private final JFPFossilExecutable           fossil_exec;
  private final File                          fossil_repos;
  private final OptionType<JFPFossilUserName> fossil_username;
  private final LogUsableType                 log;
  private final JFPErrorReporterType          reporter;

  /**
   * Construct a synchronization task.
   *
   * @param in_log
   *          A log interface.
   * @param in_fossil_exec
   *          The executable.
   * @param in_fossil_repos
   *          The repository.
   * @param in_fossil_username
   *          The username.
   * @param in_reporter
   *          The error reporter.
   */

  public JFPFossilSyncTask(
    final LogUsableType in_log,
    final JFPFossilExecutable in_fossil_exec,
    final File in_fossil_repos,
    final OptionType<JFPFossilUserName> in_fossil_username,
    final JFPErrorReporterType in_reporter)
  {
    this.log = NullCheck.notNull(in_log, "Log");
    this.fossil_exec = NullCheck.notNull(in_fossil_exec, "Fossil executable");
    this.fossil_repos =
      NullCheck.notNull(in_fossil_repos, "Fossil repository");
    this.fossil_username =
      NullCheck.notNull(in_fossil_username, "Fossil username");
    this.reporter = NullCheck.notNull(in_reporter, "Reporter");
  }

  @Override public Boolean call()
    throws Exception
  {
    try {

      /**
       * If a sync request has just occurred, then the chances are that the
       * fossil database is going to be locked, because a sync is probably in
       * progress. Wait a few seconds before trying to sync with a remote.
       */

      Thread.sleep(TimeUnit.MILLISECONDS.convert(5, TimeUnit.SECONDS));

      final List<String> args = new ArrayList<String>();
      args.add(this.fossil_exec.getActual().getCanonicalPath());
      args.add("sync");
      args.add("--verily");

      this.fossil_username.map(new FunctionType<JFPFossilUserName, Unit>() {
        @Override public Unit call(
          final JFPFossilUserName name)
        {
          args.add("--user");
          args.add(name.getActual());
          return Unit.unit();
        }
      });

      args.add("-R");
      args.add(this.fossil_repos.toString());
      return JFPLoggedProgramExec
        .executeLogged(this.log, args, this.reporter);
    } catch (final IOException e) {
      this.log.error("i/o exception: " + e.getMessage());
      return Boolean.FALSE;
    } catch (final InterruptedException e) {
      this.log.error("interrupted exception: " + e.getMessage());
      return Boolean.FALSE;
    }
  }
}
