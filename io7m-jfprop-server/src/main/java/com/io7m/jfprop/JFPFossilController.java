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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.io7m.jfunctional.OptionType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;

/**
 * The default implementation of the fossil controller.
 */

final class JFPFossilController implements JFPFossilControllerType
{
  public static JFPFossilControllerType newController(
    final LogUsableType log,
    final ExecutorService in_executor,
    final JFPServerConfigType in_config,
    final JFPErrorReporterType in_reporter)
  {
    return new JFPFossilController(log, in_executor, in_config, in_reporter);
  }

  private final JFPServerConfigType  config;
  private final ExecutorService      executor;
  private final LogUsableType        log;
  private final JFPErrorReporterType reporter;

  private JFPFossilController(
    final LogUsableType in_log,
    final ExecutorService in_executor,
    final JFPServerConfigType in_config,
    final JFPErrorReporterType in_reporter)
  {
    this.log = NullCheck.notNull(in_log, "Log");
    this.executor = NullCheck.notNull(in_executor, "Executor");
    this.config = NullCheck.notNull(in_config, "Configuration");
    this.reporter = NullCheck.notNull(in_reporter, "Reporter");
  }

  @Override public Future<Boolean> doSync(
    final JFPProjectPath project)
  {
    NullCheck.notNull(project, "Project");

    File fossil_repos = this.config.getFossilRepositoryDirectory();
    final List<String> components = project.getComponents();
    for (int index = 0; index < components.size(); ++index) {
      fossil_repos = new File(fossil_repos, components.get(index));
    }

    final JFPFossilExecutable exec = this.config.getFossilExecutable();
    final OptionType<JFPFossilUserName> fossil_username =
      this.config.getFossilUserName();

    if (fossil_repos.isFile()) {
      return this.executor.submit(new JFPFossilSyncTask(
        this.log,
        exec,
        fossil_repos,
        fossil_username,
        this.reporter));
    }

    this.log.debug(fossil_repos + " is not a file: ignoring request");

    return new FutureTask<Boolean>(new Runnable() {
      @Override public void run()
      {

      }
    }, Boolean.FALSE);
  }

  private void callListDirectory(
    final File accum,
    final File current,
    final List<String> r)
  {
    final String[] items = current.list();
    if (items == null) {
      return;
    }

    for (final String name : items) {
      final File actual = new File(current, name);
      if (actual.isFile()) {
        if (name.endsWith(".fossil") && (".fossil".equals(name) == false)) {
          r.add(new File(accum, name).toString());
        }
      } else if (actual.isDirectory()) {
        this.callListDirectory(
          new File(accum, name),
          new File(current, name),
          r);
      }
    }
  }

  @Override public List<String> listFossilRepositories(
    final File root)
    throws IOException
  {
    if (root.isDirectory() == false) {
      throw new IOException("Not a directory: " + root);
    }

    final List<String> r = new ArrayList<String>();
    this.callListDirectory(new File("/"), root, r);
    return r;
  }

}
