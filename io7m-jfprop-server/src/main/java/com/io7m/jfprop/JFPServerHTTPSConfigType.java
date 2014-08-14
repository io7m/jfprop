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
import java.net.InetSocketAddress;

/**
 * The type of HTTPS server configurations.
 */

public interface JFPServerHTTPSConfigType
{
  /**
   * @return The desired address of the HTTPS server.
   */

  InetSocketAddress getAddress();

  /**
   * @return The key store password.
   */

  String getKeyStorePassword();

  /**
   * @return The path to the key store.
   */

  File getKeyStorePath();

  /**
   * @return The key store type.
   */

  String getKeyStoreType();

  /**
   * @return The trust store password.
   */

  String getTrustStorePassword();

  /**
   * @return The path to the trust store.
   */

  File getTrustStorePath();

  /**
   * @return The trust store type.
   */

  String getTrustStoreType();
}
