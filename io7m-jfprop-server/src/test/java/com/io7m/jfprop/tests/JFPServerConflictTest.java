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

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.io7m.jfprop.JFPAllDatabaseType;
import com.io7m.jfprop.JFPProperties;
import com.io7m.jfprop.JFPServerConfigFromProperties;
import com.io7m.jfprop.JFPServerConfigType;
import com.io7m.jfprop.JFPServerControlType;
import com.io7m.jfprop.JFPServerEventsType;
import com.io7m.jfprop.JFPServerMain;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;

@SuppressWarnings({ "static-method", "unused" }) public final class JFPServerConflictTest
{
  private Properties makeProperties(
    final File temp_dir,
    final boolean start_http,
    final boolean start_admin)
  {
    System.out.println("temporary directory: " + temp_dir);

    final Properties p = new Properties();
    p.setProperty(
      JFPProperties.name("server_log_level"),
      LogLevel.LOG_DEBUG.toString());
    p.setProperty(JFPProperties.name("server_admin_password"), "abcd");

    if (start_http) {
      p.setProperty(JFPProperties.name("server_http_enabled"), "true");
      p.setProperty(JFPProperties.name("server_http_address"), "localhost");
      p.setProperty(JFPProperties.name("server_http_port"), "32767");
    } else {
      p.setProperty(JFPProperties.name("server_http_enabled"), "false");
    }

    if (start_admin) {
      p.setProperty(JFPProperties.name("server_admin_enabled"), "true");
      p.setProperty(JFPProperties.name("server_admin_address"), "localhost");
      p.setProperty(JFPProperties.name("server_admin_port"), "32768");
    } else {
      p.setProperty(JFPProperties.name("server_admin_enabled"), "false");
    }

    p.setProperty(JFPProperties.name("server_database_file"), new File(
      temp_dir,
      "server-database").toString());

    final File logs = new File(temp_dir, "server-logs");
    Assert.assertTrue(logs.mkdir());

    p
      .setProperty(
        JFPProperties.name("server_log_directory"),
        logs.toString());

    final File repos = new File(temp_dir, "server-repositories");
    Assert.assertTrue(repos.mkdir());

    p.setProperty(
      JFPProperties.name("fossil_repository_directory"),
      repos.toString());

    p.setProperty(JFPProperties.name("fossil_executable"), "fossil");

    p.setProperty(JFPProperties.name("mail_enabled"), "false");
    return p;
  }

  private final ExecutorService pool = Executors.newFixedThreadPool(1);
  private TemporaryFolder       temporary_folder;

  private void run(
    final boolean start_http,
    final boolean start_admin,
    final JFPAllDatabaseType database,
    final Callable<Unit> r)
    throws Exception
  {
    this.temporary_folder = new TemporaryFolder();
    this.temporary_folder.create();

    final Properties p =
      this.makeProperties(
        this.temporary_folder.getRoot(),
        start_http,
        start_admin);

    final JFPServerConfigType c =
      JFPServerConfigFromProperties.fromProperties(p);

    final AtomicBoolean stopped = new AtomicBoolean(false);
    final AtomicReference<Exception> error = new AtomicReference<Exception>();

    final JFPServerMain s =
      JFPServerMain.newServerWithEventsAndDatabase(
        c,
        database,
        new JFPServerEventsType() {
          @Override public void serverStarted(
            final JFPServerControlType sc)
            throws Exception
          {
            try {
              System.out.println("tests: Server started");
              r.call();
            } finally {
              sc.stop();
            }
          }

          @Override public void serverStopped()
          {
            System.out.println("tests: Server stopped");
            stopped.set(true);
          }

          @Override public void serverCrashed(
            final Exception x)
          {
            System.out.println("tests: Server crashed " + x);
            error.set(x);
          }
        });

    this.pool.execute(s);

    while (stopped.get() == false) {
      Thread.sleep(50);
    }

    if (error.get() != null) {
      throw error.get();
    }
  }

  @Test(timeout = 1000000) public void testListUnauthenticated()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    final CrashingDatabase db = new CrashingDatabase();

    this.run(false, true, db, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/remote-list?admin_password=abcd")
                .openConnection();
            c.setRequestMethod("POST");
            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_CONFLICT, r);

            final String s = JFPServerTest.readAll(c.getErrorStream());
            System.out.println(s);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }
}
