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

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * A specification of a remote.
 */

public final class JFPRemote implements Serializable
{
  private static final long serialVersionUID;

  static {
    serialVersionUID = -3781275233576159926L;
  }

  /**
   * Read the specification of a remote from an HTTP request.
   *
   * @param request
   *          The request.
   * @return The remote.
   * @throws JFPExceptionNonexistent
   *           If any part of the remote is missing.
   * @throws JFPExceptionInvalidArgument
   *           If any part of the remote is invalid.
   */

  public static JFPRemote fromParameters(
    final Map<String, String[]> request)
    throws JFPExceptionNonexistent,
      JFPExceptionInvalidArgument
  {
    NullCheck.notNull(request, "Request");

    try {
      final JFPKey r_key =
        new JFPKey(JFPRequestUtilities.getValueSingle(request, "remote_key"));
      final URI r_uri =
        new URI(JFPRequestUtilities.getValueSingle(request, "remote_uri"));
      final JFPUserName r_user =
        new JFPUserName(JFPRequestUtilities.getValueSingle(
          request,
          "remote_user"));
      return new JFPRemote(r_uri, r_user, r_key);
    } catch (final JFPExceptionAuthentication e) {
      throw new JFPExceptionInvalidArgument(e);
    } catch (final URISyntaxException e) {
      throw new JFPExceptionInvalidArgument(e);
    }
  }

  private final JFPKey      key;
  private final URI         uri;
  private final JFPUserName user;

  /**
   * Construct a remote.
   *
   * @param in_uri
   *          The URI.
   * @param in_user
   *          The user.
   * @param in_key
   *          The key.
   */

  public JFPRemote(
    final URI in_uri,
    final JFPUserName in_user,
    final JFPKey in_key)
  {
    this.uri = NullCheck.notNull(in_uri, "URI");
    this.user = NullCheck.notNull(in_user, "User");
    this.key = NullCheck.notNull(in_key, "Key");
  }

  @Override public boolean equals(
    final @Nullable Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final JFPRemote other = (JFPRemote) obj;
    return this.key.equals(other.key)
      && this.uri.equals(other.uri)
      && this.user.equals(other.user);
  }

  /**
   * @return The remote key.
   */

  public JFPKey getKey()
  {
    return this.key;
  }

  /**
   * @return The remote URI.
   */

  public URI getURI()
  {
    return this.uri;
  }

  /**
   * @return The remote user.
   */

  public JFPUserName getUser()
  {
    return this.user;
  }

  @Override public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.key.hashCode();
    result = (prime * result) + this.uri.hashCode();
    result = (prime * result) + this.user.hashCode();
    return result;
  }

  @Override public String toString()
  {
    final StringBuilder b = new StringBuilder();
    b.append(this.uri);
    b.append("|");
    b.append(this.user);
    b.append("|");
    b.append(this.key);
    final String r = b.toString();
    assert r != null;
    return r;
  }
}
