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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;

/**
 * A handler for the "list" command.
 */

final class JFPServerCommandList extends JFPAuthenticatedHandlerAbstract implements
PartialFunctionType<Unit, List<String>, Exception>
{
  JFPServerCommandList(
    final JFPServerConfigType in_config,
    final JFPServerDatabaseType db,
    final LogUsableType in_log)
    {
    super(in_config, db, in_log);
    }

  @Override public List<String> call(
    final Unit _)
    {
    final List<String> r = new ArrayList<String>();

    this.callListDirectory(
      new File("/"),
      this.config.getFossilRepositoryDirectory(),
      r);

    return r;
    }

  private void callListDirectory(
    final File accum,
    final File current,
    final List<String> r)
  {
    final String[] items = current.list();
    if (items == null) {
      return;
    }

    for (final String name : items) {
      final File actual = new File(current, name);
      if (actual.isFile()) {
        if (name.endsWith(".fossil")) {
          r.add(new File(accum, name).toString());
        }
      } else if (actual.isDirectory()) {
        this.callListDirectory(
          new File(accum, name),
          new File(current, name),
          r);
      }
    }
  }

  @Override public void handleAuthenticated(
    final String target,
    final Request base_request,
    final HttpServletRequest request,
    final HttpServletResponse response,
    final JFPUserName user,
    final JFPKey key,
    final JFPServerDatabaseTransactionType transaction)
      throws JFPException,
      IOException
  {
    final List<String> r = JFPServerCommandList.this.call(Unit.unit());
    final byte[] e = JFPResponseUtilities.encodeList(r);
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");
    response.setContentLength(e.length);
    response.getOutputStream().write(e);
  }
}
