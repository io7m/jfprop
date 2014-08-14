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
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.io7m.jfunctional.OptionType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;

/**
 * A task to sync a large number of fossil repositories.
 */

public final class JFPFossilMassSyncTask implements Runnable
{
  private final JFPFossilExecutable           fossil_exec;
  private final List<File>                    fossil_repositories;
  private final OptionType<JFPFossilUserName> fossil_username;
  private final LogUsableType                 log;
  private final JFPErrorReporterType          reporter;

  /**
   * Construct a mass synchronization task.
   *
   * @param in_log
   *          A log interface.
   * @param in_fossil_exec
   *          The executable.
   * @param in_fossil_repositories
   *          The repositories.
   * @param in_fossil_username
   *          The username.
   * @param in_reporter
   *          The error reporter.
   */

  private JFPFossilMassSyncTask(
    final JFPFossilExecutable in_fossil_exec,
    final List<File> in_fossil_repositories,
    final OptionType<JFPFossilUserName> in_fossil_username,
    final LogUsableType in_log,
    final JFPErrorReporterType in_reporter)
  {
    this.log = NullCheck.notNull(in_log, "Log");
    this.fossil_exec = NullCheck.notNull(in_fossil_exec, "Fossil executable");
    this.fossil_repositories =
      NullCheck.notNull(in_fossil_repositories, "Fossil repositories");
    this.fossil_username =
      NullCheck.notNull(in_fossil_username, "Fossil username");
    this.reporter = NullCheck.notNull(in_reporter, "Reporter");
  }

  @Override public void run()
  {
    for (final File f : this.fossil_repositories) {
      try {
        final JFPFossilSyncTask task =
          new JFPFossilSyncTask(
            this.log,
            this.fossil_exec,
            f,
            this.fossil_username,
            this.reporter);
        task.call();
        Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS));
      } catch (final InterruptedException e) {
        this.log.error("interrupted: " + e);
      } catch (final Exception e) {
        this.log.error(e.toString());
      }
    }
  }
}
