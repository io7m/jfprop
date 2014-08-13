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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Properties;

import javax.mail.internet.AddressException;

import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jlog.LogLevel;
import com.io7m.jnull.NullCheck;
import com.io7m.jproperties.JProperties;
import com.io7m.jproperties.JPropertyException;
import com.io7m.jproperties.JPropertyIncorrectType;
import com.io7m.jproperties.JPropertyNonexistent;
import com.io7m.jranges.RangeCheck;
import com.io7m.jranges.RangeInclusiveB;

/**
 * Initial server configuration.
 */

public final class JFPServerConfigFromProperties implements
JFPServerConfigType
{
  /**
   * Load configuration data from the given file.
   *
   * @param file
   *          The file
   * @return Configuration data
   * @throws IOException
   *           On I/O errors.
   * @throws JPropertyException
   *           On malformed config files.
   * @throws JFPExceptionInvalidArgument
   *           On malformed config files.
   * @throws AddressException
   *           On invalid email addresses.
   */

  public static JFPServerConfigType fromFile(
    final File file)
      throws IOException,
      JPropertyException,
      JFPExceptionInvalidArgument,
      AddressException
  {
    final FileInputStream stream =
      new FileInputStream(NullCheck.notNull(file, "File"));

    try {
      return JFPServerConfigFromProperties.fromStream(stream);
    } finally {
      stream.close();
    }
  }

  /**
   * Load configuration data from the given properties.
   *
   * @param p
   *          The properties
   * @return Configuration data
   * @throws JPropertyException
   *           On malformed config files.
   * @throws JFPExceptionInvalidArgument
   *           On invalid arguments.
   * @throws AddressException
   *           On invalid email addresses.
   */

  public static JFPServerConfigType fromProperties(
    final Properties p)
      throws JPropertyException,
      JFPExceptionInvalidArgument,
      AddressException
  {
    final LogLevel log_level = JFPServerConfigFromProperties.getLogLevel(p);

    final String server_admin_password =
      JProperties.getString(p, JFPProperties.name("server_admin_password"));

    OptionType<InetSocketAddress> server_http_address;
    if (JProperties.getBooleanOptional(
      p,
      JFPProperties.name("server_http_enabled"),
      false)) {
      final BigInteger server_http_port =
        RangeCheck
        .checkIncludedInBig(
          JProperties.getBigInteger(
            p,
            JFPProperties.name("server_http_port")),
            "Port",
            new RangeInclusiveB(BigInteger.ONE, BigInteger.valueOf(65535)),
          "Valid port range");
      final String server_http_host =
        JProperties.getString(p, JFPProperties.name("server_http_address"));
      server_http_address =
        Option.some(new InetSocketAddress(server_http_host, server_http_port
          .intValue()));
    } else {
      server_http_address = Option.none();
    }

    OptionType<InetSocketAddress> server_management_address;
    if (JProperties.getBooleanOptional(
      p,
      JFPProperties.name("server_management_enabled"),
      false)) {
      final BigInteger server_management_port =
        RangeCheck.checkIncludedInBig(
          JProperties.getBigInteger(
            p,
            JFPProperties.name("server_management_port")),
            "Port",
            new RangeInclusiveB(BigInteger.ONE, BigInteger.valueOf(65535)),
          "Valid port range");
      final String server_management_host =
        JProperties.getString(
          p,
          JFPProperties.name("server_management_address"));
      server_management_address =
        Option.some(new InetSocketAddress(
          server_management_host,
          server_management_port.intValue()));
    } else {
      server_management_address = Option.none();
    }

    final File server_database_file =
      new File(JProperties.getString(
        p,
        JFPProperties.name("server_database_file")));
    final File server_log_directory =
      new File(JProperties.getString(
        p,
        JFPProperties.name("server_log_directory")));

    final File fossil_repository_directory =
      new File(JProperties.getString(
        p,
        JFPProperties.name("fossil_repository_directory")));
    final JFPFossilExecutable fossil_executable =
      new JFPFossilExecutable(new File(JProperties.getString(
        p,
        JFPProperties.name("fossil_executable"))));

    final OptionType<JFPFossilUserName> fossil_user_name;
    if (p.containsKey(JFPProperties.name("fossil_user_name"))) {
      fossil_user_name =
        Option.some(new JFPFossilUserName(JProperties.getString(
          p,
          JFPProperties.name("fossil_user_name"))));
    } else {
      fossil_user_name = Option.none();
    }

    final OptionType<JFPMailConfig> in_mail_config =
      JFPMailConfig.fromPropertiesOptional(p);

    return new JFPServerConfigFromProperties(
      server_admin_password,
      server_http_address,
      server_management_address,
      fossil_repository_directory,
      fossil_executable,
      fossil_user_name,
      log_level,
      server_log_directory,
      server_database_file,
      in_mail_config);
  }

  /**
   * Load configuration data from the given stream.
   *
   * @param stream
   *          The stream
   * @return Configuration data
   * @throws IOException
   *           On I/O errors.
   * @throws JPropertyException
   *           On malformed config files.
   * @throws JFPExceptionInvalidArgument
   *           On invalid arguments.
   * @throws AddressException
   *           On invalid email addresses.
   */

  public static JFPServerConfigType fromStream(
    final InputStream stream)
      throws IOException,
      JPropertyException,
      JFPExceptionInvalidArgument,
      AddressException
  {
    NullCheck.notNull(stream, "Stream");

    final Properties p = new Properties();
    p.load(stream);

    return JFPServerConfigFromProperties.fromProperties(p);
  }

  private static LogLevel getLogLevel(
    final Properties p)
      throws JPropertyIncorrectType,
      JPropertyNonexistent
  {
    try {
      return LogLevel.valueOf(JProperties.getString(
        p,
        JFPProperties.name("server_log_level")));
    } catch (final IllegalArgumentException e) {
      final StringBuilder m = new StringBuilder();
      m.append("Key ");
      m.append(JFPProperties.name("server_log_level"));
      m.append(" must be of type LogLevel\n");
      m.append("  Valid values include:\n");

      for (final LogLevel v : LogLevel.values()) {
        m.append("    ");
        m.append(v.toString());
        m.append("\n");
      }

      throw new JPropertyIncorrectType(m.toString());
    }
  }

  private final String                        admin_password;
  private final JFPFossilExecutable           fossil_executable;
  private final File                          fossil_repository_directory;
  private final OptionType<JFPFossilUserName> fossil_user_name;
  private final LogLevel                      log_level;
  private final OptionType<JFPMailConfig>     mail_config;
  private final File                          server_database_file;
  private final OptionType<InetSocketAddress> server_http_address;
  private final File                          server_log_directory;
  private final OptionType<InetSocketAddress> server_management_address;

  private JFPServerConfigFromProperties(
    final String in_admin_password,
    final OptionType<InetSocketAddress> in_server_http_address,
    final OptionType<InetSocketAddress> in_server_management_address,
    final File in_fossil_repository_directory,
    final JFPFossilExecutable in_fossil_executable,
    final OptionType<JFPFossilUserName> in_fossil_user_name,
    final LogLevel in_log_level,
    final File in_server_log_directory,
    final File in_server_database_file,
    final OptionType<JFPMailConfig> in_mail_config)
  {
    this.admin_password = NullCheck.notNull(in_admin_password, "Password");
    this.server_http_address =
      NullCheck.notNull(in_server_http_address, "Address");
    this.server_management_address =
      NullCheck.notNull(
        in_server_management_address,
        "Server management address");

    this.fossil_repository_directory =
      NullCheck.notNull(
        in_fossil_repository_directory,
        "Fossil repository directory");
    this.fossil_executable =
      NullCheck.notNull(in_fossil_executable, "Fossil executable");
    this.fossil_user_name =
      NullCheck.notNull(in_fossil_user_name, "Fossil user name");

    this.server_log_directory =
      NullCheck.notNull(in_server_log_directory, "Log directory");
    this.server_database_file =
      NullCheck.notNull(in_server_database_file, "Database file");

    this.mail_config =
      NullCheck.notNull(in_mail_config, "Mail configuration");

    this.log_level = NullCheck.notNull(in_log_level, "Log level");
  }

  @Override public String getAdminPassword()
  {
    return this.admin_password;
  }

  @Override public JFPFossilExecutable getFossilExecutable()
  {
    return this.fossil_executable;
  }

  @Override public File getFossilRepositoryDirectory()
  {
    return this.fossil_repository_directory;
  }

  @Override public OptionType<JFPFossilUserName> getFossilUserName()
  {
    return this.fossil_user_name;
  }

  @Override public LogLevel getLogLevel()
  {
    return this.log_level;
  }

  @Override public OptionType<JFPMailConfig> getMailConfiguration()
  {
    return this.mail_config;
  }

  @Override public File getServerDatabaseFile()
  {
    return this.server_database_file;
  }

  @Override public OptionType<InetSocketAddress> getServerHTTPAddress()
  {
    return this.server_http_address;
  }

  @Override public File getServerLogDirectory()
  {
    return this.server_log_directory;
  }

  @Override public OptionType<InetSocketAddress> getServerManagementAddress()
  {
    return this.server_management_address;
  }
}
