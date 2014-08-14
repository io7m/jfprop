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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.io7m.jfprop.JFPKey;
import com.io7m.jfprop.JFPProperties;
import com.io7m.jfprop.JFPServerConfigFromProperties;
import com.io7m.jfprop.JFPServerConfigType;
import com.io7m.jfprop.JFPServerControlType;
import com.io7m.jfprop.JFPServerEventsType;
import com.io7m.jfprop.JFPServerMain;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogLevel;

@SuppressWarnings({ "static-method", "unused" }) public final class JFPServerTest
{
  private Properties makeProperties(
    final File temp_dir,
    final boolean start_http,
    final boolean start_management)
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

    if (start_management) {
      p.setProperty(JFPProperties.name("server_management_enabled"), "true");
      p.setProperty(
        JFPProperties.name("server_management_address"),
        "localhost");
      p.setProperty(JFPProperties.name("server_management_port"), "32768");
    } else {
      p.setProperty(JFPProperties.name("server_management_enabled"), "false");
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
    final boolean start_management,
    final Callable<Unit> r)
    throws Exception
  {
    this.temporary_folder = new TemporaryFolder();
    this.temporary_folder.create();

    final Properties p =
      this.makeProperties(
        this.temporary_folder.getRoot(),
        start_http,
        start_management);

    final JFPServerConfigType c =
      JFPServerConfigFromProperties.fromProperties(p);

    final AtomicBoolean stopped = new AtomicBoolean(false);
    final AtomicReference<Exception> error = new AtomicReference<Exception>();

    final JFPServerMain s =
      JFPServerMain.newServer(c, new JFPServerEventsType() {
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

    this.run(true, false, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL("http://localhost:32767/list")
                .openConnection();
            c.setRequestMethod("GET");
            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testListAuthenticated()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {
          final JFPKey key;

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL("http://localhost:32768/user-add")
                .openConnection();
            c.setRequestMethod("POST");
            c.setDoOutput(true);

            final DataOutputStream s =
              new DataOutputStream(c.getOutputStream());
            s.writeBytes("admin_password=abcd&user=someone");
            s.flush();

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-generate-key").openConnection();
            c.setRequestMethod("POST");
            c.setDoOutput(true);

            final DataOutputStream s =
              new DataOutputStream(c.getOutputStream());
            s.writeBytes("admin_password=abcd&user=someone");
            s.flush();

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);

            final String text =
              JFPServerTest.this.readAll(c.getInputStream());
            key = new JFPKey(text.trim());
          }

          final TemporaryFolder tf = JFPServerTest.this.temporary_folder;
          final File repos = new File(tf.getRoot(), "server-repositories");
          new File(repos, "io7m0.fossil").createNewFile();
          new File(repos, "io7m1.fossil").createNewFile();
          final File sub = new File(repos, "sub");
          sub.mkdir();
          new File(sub, "io7m2.fossil").createNewFile();
          new File(sub, "io7m3.fossil").createNewFile();
          final File subsub = new File(sub, "subsub");
          subsub.mkdir();
          new File(subsub, "io7m4.fossil").createNewFile();
          new File(subsub, "io7m5.fossil").createNewFile();

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL("http://localhost:32767/list")
                .openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);

            final DataOutputStream s =
              new DataOutputStream(c.getOutputStream());
            s.writeBytes("user=someone&key=" + key.getActual());
            s.flush();

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);

            final Set<String> names = new HashSet<String>();
            final BufferedReader br =
              new BufferedReader(new InputStreamReader(c.getInputStream()));

            for (;;) {
              final String line = br.readLine();
              if (line == null) {
                break;
              }
              names.add(line.trim());
            }

            Assert.assertEquals(6, names.size());
            Assert.assertTrue(names.contains("/io7m0.fossil"));
            Assert.assertTrue(names.contains("/io7m1.fossil"));
            Assert.assertTrue(names.contains("/sub/io7m2.fossil"));
            Assert.assertTrue(names.contains("/sub/io7m3.fossil"));
            Assert.assertTrue(names.contains("/sub/subsub/io7m4.fossil"));
            Assert.assertTrue(names.contains("/sub/subsub/io7m5.fossil"));
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  protected String readAll(
    final InputStream is)
    throws IOException
  {
    final StringBuilder bs = new StringBuilder();
    final byte[] b = new byte[8192];
    for (;;) {
      final int r = is.read(b);
      if (r == -1) {
        break;
      }
      bs.append(new String(b, 0, r, "UTF-8"));
    }

    return bs.toString();
  }

  @Test(timeout = 1000000) public void testAdminUserDefault()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL("http://localhost:32768/")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserAddGet()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-add?admin_password=abcd&user=someone")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserAddNoPassword()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL("http://localhost:32768/user-add")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserAddNoName()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-add?admin_password=abcd")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserAddDuplicate()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-add?admin_password=abcd&user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-add?admin_password=abcd&user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_CONFLICT, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserGenerateKeyGet()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-generate-key?admin_password=abcd&user=someone")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserGenerateKeyNonexistent()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-generate-key?admin_password=abcd&user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserGenerateKeyNoPassword()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-generate-key?user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserListOK()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-list?admin_password=abcd")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserListNoPassword()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL("http://localhost:32768/user-list")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserListKeysMissing()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-list-keys?admin_password=abcd")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserListKeysOK()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-add?admin_password=abcd&user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-generate-key?admin_password=abcd&user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-list-keys?admin_password=abcd&user=someone")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserListKeysNonexistent()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-list-keys?admin_password=abcd&user=someone")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserListKeysNoPassword()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-list-keys?user=someone")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserRevokeKeyGet()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-revoke-key?admin_password=abcd&user=someone&key=abcd1234")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserRevokeKeyNonexistent()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-revoke-key?admin_password=abcd&user=someone&key=abcd1234")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserRevokeKeyNoPassword()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-revoke-key?user=someone&key=abcd1234")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserRevokeKeyOK()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-add?admin_password=abcd&user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/user-revoke-key?admin_password=abcd&user=someone&key=abcd1234")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserRemoteAddOK()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          Integer rid;

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/remote-add?admin_password=abcd&remote_uri=http://example.com&remote_key=abcd1234&remote_user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);

            final String text =
              JFPServerTest.this.readAll(c.getInputStream());
            rid = Integer.valueOf(text.trim());

            Assert.assertTrue(rid.intValue() > 0);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserRemoteAddMissing_0()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/remote-add?admin_password=abcd&remote_key=abcd1234&remote_user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserRemoteAddMissing_1()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/remote-add?admin_password=abcd&remote_uri=http://example.com&remote_user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserRemoteAddMissing_2()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/remote-add?admin_password=abcd&remote_uri=http://example.com&remote_key=abcd1234")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserRemoteAddBad_0()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/remote-add?admin_password=abcd&remote_uri={}&remote_key=abcd1234&remote_user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserRemoteAddBad_1()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/remote-add?admin_password=abcd&remote_uri=http://example.com&remote_key=XXXX&remote_user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminUserRemoteAddBad_2()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/remote-add?admin_password=abcd&remote_uri=http://example.com&remote_key=abcd1234&remote_user=####")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminProjectRemoteAddOK()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          Integer rid;

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/remote-add?admin_password=abcd&remote_uri=http://example.com&remote_key=abcd1234&remote_user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);

            final String text =
              JFPServerTest.this.readAll(c.getInputStream());
            rid = Integer.valueOf(text.trim());

            Assert.assertTrue(rid.intValue() > 0);
          }

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/project-remote-add?admin_password=abcd&project=/a/b/c&remote_id="
                  + rid).openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminProjectRemoteListOK()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          Integer rid;

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/remote-add?admin_password=abcd&remote_uri=http://example.com&remote_key=abcd1234&remote_user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);

            final String text =
              JFPServerTest.this.readAll(c.getInputStream());
            rid = Integer.valueOf(text.trim());

            Assert.assertTrue(rid.intValue() > 0);
          }

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/project-remote-add?admin_password=abcd&project=/a/b/c&remote_id="
                  + rid).openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/project-remote-list?admin_password=abcd&project=/a/b/c")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);

            final String text =
              JFPServerTest.this.readAll(c.getInputStream());
            Assert.assertEquals(rid.toString(), text.trim());
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminProjectRemoteListGet()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/project-remote-list?admin_password=abcd&project=/a/b/c")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public
    void
    testAdminProjectRemoteListNonexistent()
      throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/project-remote-list?admin_password=abcd&project=/a/b/c")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminProjectRemoteListInvalid()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/project-remote-list?admin_password=abcd&project=/a/../c")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminProjectRemoteGlobalAddOK()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          Integer rid;

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/remote-add?admin_password=abcd&remote_uri=http://example.com&remote_key=abcd1234&remote_user=someone")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);

            final String text =
              JFPServerTest.this.readAll(c.getInputStream());
            rid = Integer.valueOf(text.trim());

            Assert.assertTrue(rid.intValue() > 0);
          }

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/project-remote-global-add?admin_password=abcd&remote_id="
                  + rid).openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminProjectRemoteAddNonexistent()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/project-remote-add?admin_password=abcd&project=/a/b/c&remote_id=1")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public
    void
    testAdminProjectRemoteGlobalAddNonexistent()
      throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/project-remote-global-add?admin_password=abcd&project=/a/b/c&remote_id=1")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public
    void
    testAdminProjectRemoteGlobalAddInvalid()
      throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/project-remote-global-add?admin_password=abcd&project=/a/b/c&remote_id=Z")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminProjectRemoteGlobalAddGet()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/project-remote-global-add?admin_password=abcd&project=/a/b/c&remote_id=1")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminProjectRemoteAddInvalid()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/project-remote-add?admin_password=abcd&project=/a/b/c&remote_id=z")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminProjectRemoteAddGet()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/project-remote-add?admin_password=abcd&project=/a/b/c&remote_id=1")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminRemoteListOK()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
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
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminMassSyncList_0()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/mass-sync-list?admin_password=abcd")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminMassSyncAddBad_0()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/mass-sync-add?admin_password=abcd")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminMassSyncRemoveBad_0()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/mass-sync-remove?admin_password=abcd")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, r);
          }

          return Unit.unit();
        } finally {
          ran.set(true);
        }
      }
    });

    Assert.assertTrue(ran.get());
  }

  @Test(timeout = 1000000) public void testAdminMassSyncEnable()
    throws Exception
  {
    final AtomicBoolean ran = new AtomicBoolean(false);

    this.run(true, true, new Callable<Unit>() {
      @Override public Unit call()
        throws Exception
      {
        try {

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/mass-sync-is-enabled?admin_password=abcd")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);

            final String text =
              JFPServerTest.this.readAll(c.getInputStream());
            Assert.assertEquals("true", text.trim());
          }

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/mass-sync-enable?admin_password=abcd&enable=false")
                .openConnection();
            c.setRequestMethod("POST");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);
          }

          {
            final HttpURLConnection c =
              (HttpURLConnection) new URL(
                "http://localhost:32768/mass-sync-is-enabled?admin_password=abcd")
                .openConnection();
            c.setRequestMethod("GET");

            final int r = c.getResponseCode();
            Assert.assertEquals(HttpServletResponse.SC_OK, r);

            final String text =
              JFPServerTest.this.readAll(c.getInputStream());
            Assert.assertEquals("false", text.trim());
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
