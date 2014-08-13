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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jfprop.JFPFossilExecutable;
import com.io7m.jfprop.JFPFossilUserName;
import com.io7m.jfprop.JFPMailConfig;
import com.io7m.jfprop.JFPProperties;
import com.io7m.jfprop.JFPServerConfigFromProperties;
import com.io7m.jfprop.JFPServerConfigType;
import com.io7m.jfunctional.Pair;
import com.io7m.jfunctional.Some;
import com.io7m.jlog.LogLevel;
import com.io7m.jproperties.JPropertyIncorrectType;
import com.io7m.jproperties.JPropertyNonexistent;

@SuppressWarnings({ "static-method", "unused" }) public final class JFPServerConfigFromPropertiesTest
{
  private void checkComplete0(
    final JFPServerConfigType c)
  {
    Assert.assertEquals(LogLevel.LOG_DEBUG, c.getLogLevel());

    Assert.assertEquals("abcd", c.getAdminPassword());

    Assert.assertEquals(
      new File("/tmp/server-logs"),
      c.getServerLogDirectory());
    Assert.assertEquals(
      new File("/tmp/server-database"),
      c.getServerDatabaseFile());

    {
      final Some<InetSocketAddress> some =
        (Some<InetSocketAddress>) c.getServerHTTPAddress();
      final InetSocketAddress inet = some.get();
      Assert.assertEquals("/127.0.0.1", inet.getAddress().toString());
      Assert.assertEquals(8080, inet.getPort());
    }

    {
      final Some<InetSocketAddress> some =
        (Some<InetSocketAddress>) c.getServerManagementAddress();
      final InetSocketAddress inet = some.get();
      Assert.assertEquals("/127.0.0.1", inet.getAddress().toString());
      Assert.assertEquals(8080, inet.getPort());
    }

    Assert.assertEquals(
      new JFPFossilExecutable(new File("fossil")),
      c.getFossilExecutable());
    Assert.assertEquals(
      new File("/tmp/server-repositories"),
      c.getFossilRepositoryDirectory());

    {
      final Some<JFPFossilUserName> some =
        (Some<JFPFossilUserName>) c.getFossilUserName();

      Assert.assertEquals("someone", some.get().getActual());
    }

    {
      final Some<JFPMailConfig> some =
        (Some<JFPMailConfig>) c.getMailConfiguration();
      final JFPMailConfig mc = some.get();
      Assert.assertEquals(mc.getServer(), "127.0.0.1");
      Assert.assertEquals(mc.getRecipient().getAddress(), "root@localhost");
      Assert.assertEquals(mc.getSender().getAddress(), "jfprop@localhost");

      final Some<Pair<String, String>> some_ma =
        (Some<Pair<String, String>>) mc.getServerAuthentication();
      final Pair<String, String> ma = some_ma.get();
      Assert.assertEquals("someone", ma.getLeft());
      Assert.assertEquals("12345678", ma.getRight());
    }
  }

  private void checkComplete1(
    final JFPServerConfigType c)
  {
    Assert.assertEquals(LogLevel.LOG_DEBUG, c.getLogLevel());

    Assert.assertEquals("abcd", c.getAdminPassword());

    Assert.assertEquals(
      new File("/tmp/server-logs"),
      c.getServerLogDirectory());
    Assert.assertEquals(
      new File("/tmp/server-database"),
      c.getServerDatabaseFile());

    {
      Assert.assertTrue(c.getServerHTTPAddress().isNone());
    }

    {
      Assert.assertTrue(c.getServerManagementAddress().isNone());
    }

    Assert.assertEquals(
      new JFPFossilExecutable(new File("fossil")),
      c.getFossilExecutable());
    Assert.assertEquals(
      new File("/tmp/server-repositories"),
      c.getFossilRepositoryDirectory());

    {
      Assert.assertTrue(c.getFossilUserName().isNone());
    }

    {
      Assert.assertTrue(c.getMailConfiguration().isNone());
    }
  }

  private Properties makeComplete0()
  {
    final Properties p = new Properties();
    p.setProperty(
      JFPProperties.name("server_log_level"),
      LogLevel.LOG_DEBUG.toString());
    p.setProperty(JFPProperties.name("server_admin_password"), "abcd");

    p.setProperty(JFPProperties.name("server_http_enabled"), "true");
    p.setProperty(JFPProperties.name("server_http_port"), "8080");
    p.setProperty(JFPProperties.name("server_http_address"), "127.0.0.1");

    p.setProperty(JFPProperties.name("server_management_enabled"), "true");
    p.setProperty(JFPProperties.name("server_management_port"), "8080");
    p.setProperty(
      JFPProperties.name("server_management_address"),
      "127.0.0.1");

    p.setProperty(
      JFPProperties.name("server_database_file"),
      "/tmp/server-database");
    p.setProperty(
      JFPProperties.name("server_log_directory"),
      "/tmp/server-logs");

    p.setProperty(
      JFPProperties.name("fossil_repository_directory"),
      "/tmp/server-repositories");
    p.setProperty(JFPProperties.name("fossil_executable"), "fossil");
    p.setProperty(JFPProperties.name("fossil_user_name"), "someone");

    p.setProperty(JFPProperties.name("mail_enabled"), "true");
    p.setProperty(JFPProperties.name("mail_server_address"), "127.0.0.1");
    p.setProperty(JFPProperties.name("mail_recipient"), "root@localhost");
    p.setProperty(JFPProperties.name("mail_sender"), "jfprop@localhost");
    p.setProperty(JFPProperties.name("mail_auth_user"), "someone");
    p.setProperty(JFPProperties.name("mail_auth_password"), "12345678");
    return p;
  }

  private Properties makeComplete1()
  {
    final Properties p = new Properties();
    p.setProperty(
      JFPProperties.name("server_log_level"),
      LogLevel.LOG_DEBUG.toString());
    p.setProperty(JFPProperties.name("server_admin_password"), "abcd");

    p.setProperty(JFPProperties.name("server_http_enabled"), "false");
    p.setProperty(JFPProperties.name("server_management_enabled"), "false");

    p.setProperty(
      JFPProperties.name("server_database_file"),
      "/tmp/server-database");
    p.setProperty(
      JFPProperties.name("server_log_directory"),
      "/tmp/server-logs");

    p.setProperty(
      JFPProperties.name("fossil_repository_directory"),
      "/tmp/server-repositories");
    p.setProperty(JFPProperties.name("fossil_executable"), "fossil");

    p.setProperty(JFPProperties.name("mail_enabled"), "false");
    return p;
  }

  @Test public void testComplete_0()
    throws Exception
  {
    final Properties p = this.makeComplete0();

    final JFPServerConfigType c =
      JFPServerConfigFromProperties.fromProperties(p);

    this.checkComplete0(c);
  }

  @Test public void testComplete_1()
    throws Exception
  {
    final Properties p = this.makeComplete1();

    final JFPServerConfigType c =
      JFPServerConfigFromProperties.fromProperties(p);

    this.checkComplete1(c);
  }

  @Test public void testCompleteFile_0()
    throws Exception
  {
    final Properties p = this.makeComplete0();

    final File tf = File.createTempFile("jfp-server-config", ".txt");
    final FileOutputStream tfs = new FileOutputStream(tf);
    p.store(tfs, "");
    final JFPServerConfigType c = JFPServerConfigFromProperties.fromFile(tf);

    this.checkComplete0(c);
  }

  @Test public void testCompleteFile_1()
    throws Exception
  {
    final Properties p = this.makeComplete1();

    final File tf = File.createTempFile("jfp-server-config", ".txt");
    final FileOutputStream tfs = new FileOutputStream(tf);
    p.store(tfs, "");
    final JFPServerConfigType c = JFPServerConfigFromProperties.fromFile(tf);

    this.checkComplete1(c);
  }

  @Test public void testCompleteStream_0()
    throws Exception
  {
    final Properties p = this.makeComplete0();

    final ByteArrayOutputStream bao = new ByteArrayOutputStream();
    p.store(bao, "");
    final ByteArrayInputStream bai =
      new ByteArrayInputStream(bao.toByteArray());

    final JFPServerConfigType c =
      JFPServerConfigFromProperties.fromStream(bai);

    this.checkComplete0(c);
  }

  @Test public void testCompleteStream_1()
    throws Exception
  {
    final Properties p = this.makeComplete1();

    final ByteArrayOutputStream bao = new ByteArrayOutputStream();
    p.store(bao, "");
    final ByteArrayInputStream bai =
      new ByteArrayInputStream(bao.toByteArray());

    final JFPServerConfigType c =
      JFPServerConfigFromProperties.fromStream(bai);

    this.checkComplete1(c);
  }

  @Test(expected = JPropertyIncorrectType.class) public void testInvalid_0()
    throws Exception
  {
    final Properties p = new Properties();
    p.setProperty(JFPProperties.name("server_log_level"), "nonsense");

    JFPServerConfigFromProperties.fromProperties(p);
  }

  @Test(expected = JPropertyNonexistent.class) public void testMissing_0()
    throws Exception
  {
    final Properties p = new Properties();
    JFPServerConfigFromProperties.fromProperties(p);
  }

  @Test(expected = JPropertyNonexistent.class) public void testMissing_1()
    throws Exception
  {
    final Properties p = new Properties();
    p.setProperty(
      JFPProperties.name("server_log_level"),
      LogLevel.LOG_DEBUG.toString());

    JFPServerConfigFromProperties.fromProperties(p);
  }

  @Test(expected = JPropertyNonexistent.class) public void testMissing_2()
    throws Exception
  {
    final Properties p = new Properties();
    p.setProperty(
      JFPProperties.name("server_log_level"),
      LogLevel.LOG_DEBUG.toString());
    p.setProperty(JFPProperties.name("server_admin_password"), "abcd");

    JFPServerConfigFromProperties.fromProperties(p);
  }

  @Test(expected = JPropertyNonexistent.class) public void testMissing_3()
    throws Exception
  {
    final Properties p = new Properties();
    p.setProperty(
      JFPProperties.name("server_log_level"),
      LogLevel.LOG_DEBUG.toString());
    p.setProperty(JFPProperties.name("server_admin_password"), "abcd");
    p.setProperty(JFPProperties.name("server_http_enabled"), "true");

    JFPServerConfigFromProperties.fromProperties(p);
  }
}
