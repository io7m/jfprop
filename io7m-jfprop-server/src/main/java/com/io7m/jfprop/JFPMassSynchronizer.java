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
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;

/**
 * A mass synchronizer.
 */

public final class JFPMassSynchronizer implements Runnable
{
  private final JFPAdminDatabaseType    db;
  private final ExecutorService         exec;
  private final JFPFossilControllerType fossil;
  private final File                    fossil_root;
  private final LogUsableType           log;
  private final AtomicBoolean           want_stop;

  JFPMassSynchronizer(
    final JFPAdminDatabaseType in_db,
    final JFPFossilControllerType in_fossil,
    final ExecutorService in_exec,
    final File in_fossil_root,
    final LogUsableType in_log)
    {
    this.db = NullCheck.notNull(in_db, "Database");
    this.exec = NullCheck.notNull(in_exec, "Executor");
    this.fossil = NullCheck.notNull(in_fossil, "Fossil controller");
    this.fossil_root = NullCheck.notNull(in_fossil_root, "Fossil root");
    this.log = NullCheck.notNull(in_log, "Log");
    this.want_stop = new AtomicBoolean(false);
    }

  private void doSyncAll(
    final Calendar current_time,
    final JFPAdminDatabaseTransactionType t)
  {
    final SortedMap<Integer, JFPMassSyncSpec> ms = t.massSyncList();

    for (final Integer id : ms.keySet()) {
      final JFPMassSyncSpec sync = ms.get(id);
      this.doSyncSingle(current_time, id, sync);
    }
  }

  private void doSyncSingle(
    final Calendar current_time,
    final Integer id,
    final JFPMassSyncSpec sync)
  {
    final LogUsableType l = JFPMassSynchronizer.this.log;

    if (sync.syncSpecTimeMatches(current_time)) {
      l.debug("mass sync " + id + " matches, performing sync");

      final JFPFossilControllerType fsc = this.fossil;
      final File root = this.fossil_root;

      this.exec.execute(new Runnable() {
        @Override public void run()
        {
          try {
            final List<String> repositories =
              fsc.listFossilRepositories(root);
            for (final String r : repositories) {
              if (sync.syncSpecRepositoryMatches(r)) {
                l.debug("repository " + r + " matches, syncing");
                final Future<Boolean> f =
                  fsc.doSync(new JFPRepositoryPath(r));
                try {
                  f.get();
                } catch (final InterruptedException e) {
                  l.critical(e.toString());
                } catch (final ExecutionException e) {
                  l.critical(e.toString());
                }
              }
            }
          } catch (final JFPExceptionInvalidArgument e) {
            l.critical(e.toString());
          } catch (final IOException e) {
            l.critical(e.toString());
          }
        }
      });
    }
  }

  @Override public void run()
  {
    this.log.info("starting mass synchronizer");

    while (this.want_stop.get() == false) {
      try {
        final Calendar current_time =
          Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        this.db
        .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
          @Override public Unit call(
            final JFPAdminDatabaseTransactionType t)
              throws JFPException
          {
            if (t.massSyncIsEnabled()) {
              JFPMassSynchronizer.this.doSyncAll(current_time, t);
            }
            return Unit.unit();
          }
        });

        Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES));
      } catch (final JFPException e) {
        this.log.critical(e.toString());
      } catch (final InterruptedException e) {
        this.log.critical(e.toString());
      }
    }
  }

  /**
   * Instruct the server to stop.
   */

  public void stop()
  {
    this.want_stop.set(true);
  }
}
