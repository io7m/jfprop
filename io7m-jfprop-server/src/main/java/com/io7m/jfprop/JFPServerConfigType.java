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

import com.io7m.jfunctional.OptionType;
import com.io7m.jlog.LogLevel;

/**
 * The interface presented by configurations.
 */

public interface JFPServerConfigType
{
  /**
   * @return The admin password.
   */

  String getAdminPassword();

  /**
   * @return The fossil executable.
   */

  JFPFossilExecutable getFossilExecutable();

  /**
   * @return The fossil repository directory.
   */

  File getFossilRepositoryDirectory();

  /**
   * @return The fossil user names used for synchronizing repositories, if
   *         any.
   */

  OptionType<JFPFossilUserName> getFossilUserName();

  /**
   * @return The log level.
   */

  LogLevel getLogLevel();

  /**
   * @return The mail configuration for logging.
   */

  OptionType<JFPMailConfig> getMailConfiguration();

  /**
   * @return The server database file.
   */

  File getServerDatabaseFile();

  /**
   * @return The HTTP server configuration (if enabled).
   */

  OptionType<JFPServerHTTPConfigType> getServerHTTPConfig();

  /**
   * @return The HTTPS server configuration (if enabled).
   */

  OptionType<JFPServerHTTPSConfigType> getServerHTTPSConfig();

  /**
   * @return The server log directory.
   */

  File getServerLogDirectory();

  /**
   * @return The HTTP management server configuration (if enabled).
   */

    OptionType<JFPServerManagementHTTPConfigType>
    getServerManagementHTTPConfig();
}
