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
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Properties;

import javax.mail.internet.AddressException;

import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jnull.NullCheck;
import com.io7m.jproperties.JProperties;
import com.io7m.jproperties.JPropertyException;
import com.io7m.jranges.RangeCheck;

/**
 * The type of HTTPS server configuration data that can be loaded from
 * properties.
 */

public final class JFPServerHTTPSConfigFromProperties implements
JFPServerHTTPSConfigType
{
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

  public static JFPServerHTTPSConfigType fromProperties(
    final Properties p)
      throws JPropertyException,
      JFPExceptionInvalidArgument,
      AddressException
  {
    final String address =
      JProperties.getString(p, JFPProperties.name("server_https_address"));
    final BigInteger server_port =
      RangeCheck
      .checkIncludedInBig(
        JProperties.getBigInteger(
          p,
          JFPProperties.name("server_https_port")),
          "Port",
          JFPServerConfigFromProperties.PORT_RANGE,
        "Valid port range");
    final InetSocketAddress a =
      new InetSocketAddress(address, server_port.intValue());

    final File ks_path =
      new File(JProperties.getString(
        p,
        JFPProperties.name("server_https_keystore_path")));
    final String ks_pass =
      JProperties.getString(
        p,
        JFPProperties.name("server_https_keystore_password"));
    final String ks_type =
      JProperties.getString(
        p,
        JFPProperties.name("server_https_keystore_type"));

    final File ts_path =
      new File(JProperties.getString(
        p,
        JFPProperties.name("server_https_truststore_path")));
    final String ts_pass =
      JProperties.getString(
        p,
        JFPProperties.name("server_https_truststore_password"));
    final String ts_type =
      JProperties.getString(
        p,
        JFPProperties.name("server_https_truststore_type"));

    return new JFPServerHTTPSConfigFromProperties(
      a,
      ks_path,
      ks_pass,
      ks_type,
      ts_path,
      ts_pass,
      ts_type);
  }

  /**
   * Load (optional) configuration data from the given properties.
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

  public static OptionType<JFPServerHTTPSConfigType> fromPropertiesOptional(
    final Properties p)
      throws JPropertyException,
      JFPExceptionInvalidArgument,
      AddressException
      {
    final boolean http_enabled =
      JProperties.getBooleanOptional(
        p,
        JFPProperties.name("server_https_enabled"),
        false);

    if (http_enabled) {
      return Option
        .some(JFPServerHTTPSConfigFromProperties.fromProperties(p));
    }

    return Option.none();
      }

  private final InetSocketAddress address;
  private final String            keystore_pass;
  private final File              keystore_path;
  private final String            keystore_type;
  private final String            truststore_pass;
  private final File              truststore_path;
  private final String            truststore_type;

  private JFPServerHTTPSConfigFromProperties(
    final InetSocketAddress in_address,
    final File in_keystore_path,
    final String in_keystore_password,
    final String in_keystore_type,
    final File in_truststore_path,
    final String in_truststore_password,
    final String in_truststore_type)
  {
    this.address = NullCheck.notNull(in_address, "Server address");

    this.keystore_path = NullCheck.notNull(in_keystore_path, "Keystore path");
    this.keystore_pass =
      NullCheck.notNull(in_keystore_password, "Keystore password");
    this.keystore_type = NullCheck.notNull(in_keystore_type, "Keystore type");

    this.truststore_path =
      NullCheck.notNull(in_truststore_path, "Truststore path");
    this.truststore_pass =
      NullCheck.notNull(in_truststore_password, "Truststore password");
    this.truststore_type =
      NullCheck.notNull(in_truststore_type, "Truststore type");
  }

  @Override public InetSocketAddress getAddress()
  {
    return this.address;
  }

  @Override public String getKeyStorePassword()
  {
    return this.keystore_pass;
  }

  @Override public File getKeyStorePath()
  {
    return this.keystore_path;
  }

  @Override public String getKeyStoreType()
  {
    return this.keystore_type;
  }

  @Override public String getTrustStorePassword()
  {
    return this.truststore_pass;
  }

  @Override public File getTrustStorePath()
  {
    return this.truststore_path;
  }

  @Override public String getTrustStoreType()
  {
    return this.truststore_type;
  }
}
