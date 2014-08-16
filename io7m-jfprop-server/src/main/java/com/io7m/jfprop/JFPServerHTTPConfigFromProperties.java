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
 * The type of HTTP server configuration data that can be loaded from
 * properties.
 */

public final class JFPServerHTTPConfigFromProperties implements
JFPServerHTTPConfigType
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

  public static JFPServerHTTPConfigType fromProperties(
    final Properties p)
      throws JPropertyException,
      JFPExceptionInvalidArgument,
      AddressException
  {
    final String address =
      JProperties.getString(p, JFPProperties.name("server_http_address"));
    final BigInteger server_http_port =
      RangeCheck.checkIncludedInBig(
        JProperties.getBigInteger(p, JFPProperties.name("server_http_port")),
        "Port",
        JFPServerConfigFromProperties.PORT_RANGE,
        "Valid port range");
    final InetSocketAddress a =
      new InetSocketAddress(address, server_http_port.intValue());
    return new JFPServerHTTPConfigFromProperties(a);
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

  public static OptionType<JFPServerHTTPConfigType> fromPropertiesOptional(
    final Properties p)
      throws JPropertyException,
      JFPExceptionInvalidArgument,
      AddressException
      {
    final boolean http_enabled =
      JProperties.getBooleanOptional(
        p,
        JFPProperties.name("server_http_enabled"),
        false);

    if (http_enabled) {
      return Option.some(JFPServerHTTPConfigFromProperties.fromProperties(p));
    }

    return Option.none();
      }

  private final InetSocketAddress address;

  private JFPServerHTTPConfigFromProperties(
    final InetSocketAddress in_address)
  {
    this.address = NullCheck.notNull(in_address, "Server address");
  }

  @Override public InetSocketAddress getAddress()
  {
    return this.address;
  }
}
