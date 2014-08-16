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
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.io7m.jfunctional.Pair;
import com.io7m.jlog.LogUsableType;

/**
 * Command to list users.
 */

public final class JFPAdminCommandUserList extends JFPAdminHandlerAbstract
{
  JFPAdminCommandUserList(
    final JFPServerConfigType in_config,
    final JFPAdminDatabaseType db,
    final LogUsableType in_log)
  {
    super(in_config, db, in_log);
  }

  @Override public Pair<Integer, byte[]> handleAuthenticated(
    final String target,
    final Request base_request,
    final HttpServletRequest request,
    final JFPAdminDatabaseTransactionType transaction)
    throws JFPException,
      IOException
  {
    this.log.debug(this.getClass().getCanonicalName() + " handle()");

    final SortedSet<JFPUserName> names = transaction.userListGet();
    final byte[] e = JFPResponseUtilities.encodeSet(names);
    return Pair.pair(HttpServletResponse.SC_OK, e);
  }
}
