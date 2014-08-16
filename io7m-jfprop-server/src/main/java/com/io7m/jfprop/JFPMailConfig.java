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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.Pair;
import com.io7m.jnull.NullCheck;
import com.io7m.jproperties.JProperties;
import com.io7m.jproperties.JPropertyException;
import com.io7m.jproperties.JPropertyIncorrectType;
import com.io7m.jproperties.JPropertyNonexistent;

/**
 * Mail configuration.
 */

public final class JFPMailConfig
{
  private static JFPMailConfig fromProperties(
    final Properties p)
      throws JPropertyNonexistent,
      AddressException
  {
    final String server_address =
      JProperties.getString(p, JFPProperties.name("mail_server_address"));

    OptionType<Pair<String, String>> in_server_auth;
    if (p.containsKey(JFPProperties.name("mail_auth_user"))) {
      final String mail_user =
        JProperties.getString(p, JFPProperties.name("mail_auth_user"));
      final String mail_pass =
        JProperties.getString(p, JFPProperties.name("mail_auth_password"));
      in_server_auth = Option.some(Pair.pair(mail_user, mail_pass));
    } else {
      in_server_auth = Option.none();
    }

    final String in_recipient_text =
      JProperties.getString(p, JFPProperties.name("mail_recipient"));
    final String in_sender_text =
      JProperties.getString(p, JFPProperties.name("mail_sender"));

    final InternetAddress in_recipient =
      new InternetAddress(in_recipient_text, true);
    final InternetAddress in_sender =
      new InternetAddress(in_sender_text, true);
    return new JFPMailConfig(
      server_address,
      in_server_auth,
      in_recipient,
      in_sender);
  }

  /**
   * Load (optional) configuration data from the given properties.
   *
   * @param p
   *          The properties.
   * @return Configuration data.
   * @throws JPropertyNonexistent
   *           On malformed properties.
   * @throws JPropertyIncorrectType
   *           On malformed properties.
   * @throws AddressException
   *           On invalid email addresses.
   */

  public static OptionType<JFPMailConfig> fromPropertiesOptional(
    final Properties p)
      throws JPropertyNonexistent,
      JPropertyIncorrectType,
      AddressException
      {
    NullCheck.notNull(p, "Properties");

    final boolean enabled =
      JProperties.getBooleanOptional(
        p,
        JFPProperties.name("mail_enabled"),
        false);
    if (enabled) {
      return Option.some(JFPMailConfig.fromProperties(p));
    }
    return Option.none();
      }

  /**
   * Load configuration data from the given stream.
   *
   * @param stream
   *          The stream
   * @return Configuration data
   * @throws IOException
   *           On I/O errors
   * @throws JPropertyException
   *           On malformed config files
   * @throws JFPExceptionInvalidArgument
   *           On invalid arguments
   * @throws AddressException
   *           On malformed email addresses.
   */

  public static JFPMailConfig fromStream(
    final InputStream stream)
      throws IOException,
      JPropertyException,
      JFPExceptionInvalidArgument,
      AddressException
  {
    NullCheck.notNull(stream, "Stream");

    final Properties p = new Properties();
    p.load(stream);

    return JFPMailConfig.fromProperties(p);
  }

  /**
   * Load (optional) configuration data from the given stream.
   *
   * @param stream
   *          The stream
   * @return Configuration data
   * @throws IOException
   *           On I/O errors
   * @throws JPropertyException
   *           On malformed config files
   * @throws JFPExceptionInvalidArgument
   *           On invalid arguments
   * @throws AddressException
   *           On invalid email addresses.
   */

  public static OptionType<JFPMailConfig> fromStreamOptional(
    final InputStream stream)
      throws IOException,
      JPropertyException,
      JFPExceptionInvalidArgument,
      AddressException
      {
    NullCheck.notNull(stream, "Stream");

    final Properties p = new Properties();
    p.load(stream);

    return JFPMailConfig.fromPropertiesOptional(p);
      }

  private final InternetAddress                  recipient;
  private final InternetAddress                  sender;
  private final String                           server;
  private final OptionType<Pair<String, String>> server_auth;

  private JFPMailConfig(
    final String in_server,
    final OptionType<Pair<String, String>> in_server_auth,
    final InternetAddress in_recipient,
    final InternetAddress in_sender)
  {
    this.server = NullCheck.notNull(in_server, "Server address");
    this.server_auth =
      NullCheck.notNull(in_server_auth, "Server authentication");
    this.recipient = NullCheck.notNull(in_recipient, "Recipient");
    this.sender = NullCheck.notNull(in_sender, "Sender address");
  }

  /**
   * @return The address of the recipient.
   */

  public InternetAddress getRecipient()
  {
    return this.recipient;
  }

  /**
   * @return The address of the sender.
   */

  public InternetAddress getSender()
  {
    return this.sender;
  }

  /**
   * @return The address of the mail server.
   */

  public String getServer()
  {
    return this.server;
  }

  /**
   * @return The server authentication information, if any.
   */

  public OptionType<Pair<String, String>> getServerAuthentication()
  {
    return this.server_auth;
  }
}
