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

import java.util.Map;

import com.io7m.jfunctional.Pair;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * Functions to authenticate users from HTTP requests.
 */

public final class JFPAuthentication
{
  /**
   * Retrieve an admin password from an HTTP request.
   *
   * @param request
   *          The request parameters.
   * @return The password.
   * @throws JFPExceptionAuthentication
   *           If either the user or key are invalid or missing.
   */

  public static String getAdminPassword(
    final Map<String, String[]> request)
      throws JFPExceptionAuthentication
  {
    if (request.containsKey("admin_password") == false) {
      throw new JFPExceptionAuthentication("admin password not specified");
    }
    final String[] u = request.get("admin_password");
    assert u != null;
    assert u.length > 0;
    final String s = u[0];
    assert s != null;
    return s;
  }

  /**
   * Retrieve a key from an HTTP request.
   *
   * @param request
   *          The request parameters.
   * @return The key.
   * @throws JFPExceptionAuthentication
   *           If the key is invalid or missing.
   */

  public static JFPKey getKey(
    final Map<String, String[]> request)
      throws JFPExceptionAuthentication
  {
    if (request.containsKey("key") == false) {
      throw new JFPExceptionAuthentication("key not specified");
    }
    final String[] k = request.get("key");
    assert k != null;
    assert k.length > 0;
    final String s = k[0];
    assert s != null;
    final JFPKey key = new JFPKey(s);
    return key;
  }

  /**
   * Retrieve a username from an HTTP request.
   *
   * @param request
   *          The request parameters.
   * @return The username.
   * @throws JFPExceptionAuthentication
   *           If the username is invalid or missing.
   */

  public static JFPUserName getUser(
    final Map<String, String[]> request)
      throws JFPExceptionAuthentication
  {
    if (request.containsKey("user") == false) {
      throw new JFPExceptionAuthentication("user name not specified");
    }
    final String[] u = request.get("user");
    assert u != null;
    assert u.length > 0;
    final String s = u[0];
    assert s != null;
    final JFPUserName user = new JFPUserName(s);
    return user;
  }

  /**
   * Retrieve a username and key from an HTTP request.
   *
   * @param request
   *          The request parameters.
   * @return The user and key.
   * @throws JFPExceptionAuthentication
   *           If either the user or key are invalid or missing.
   */

  public static Pair<JFPUserName, JFPKey> getUserAndKey(
    final Map<String, String[]> request)
      throws JFPExceptionAuthentication
      {
    final JFPUserName user = JFPAuthentication.getUser(request);
    final JFPKey key = JFPAuthentication.getKey(request);
    return Pair.pair(user, key);
      }

  private JFPAuthentication()
  {
    throw new UnreachableCodeException();
  }
}
