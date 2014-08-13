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

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import com.io7m.jlog.LogUsableType;
import com.io7m.junreachable.UnreachableCodeException;

final class JFPAdminCommands
{
  static ContextHandlerCollection getHandlers(
    final JFPServerConfigType c,
    final JFPAdminDatabaseType db,
    final LogUsableType l)
  {
    final ContextHandlerCollection handlers = new ContextHandlerCollection();

    {
      final Handler h = new JFPAdminCommandDefault();
      final ContextHandler ch = new ContextHandler("/");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandUserList(c, db, l);
      final ContextHandler ch = new ContextHandler("/user-list");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandUserAdd(c, db, l);
      final ContextHandler ch = new ContextHandler("/user-add");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandUserGenerateKey(c, db, l);
      final ContextHandler ch = new ContextHandler("/user-generate-key");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandUserRevokeKey(c, db, l);
      final ContextHandler ch = new ContextHandler("/user-revoke-key");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandUserListKeys(c, db, l);
      final ContextHandler ch = new ContextHandler("/user-list-keys");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandRemoteList(c, db, l);
      final ContextHandler ch = new ContextHandler("/remote-list");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandRemoteAdd(c, db, l);
      final ContextHandler ch = new ContextHandler("/remote-add");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandProjectRemoteGlobalAdd(c, db, l);
      final ContextHandler ch =
        new ContextHandler("/project-remote-global-add");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandProjectRemoteList(c, db, l);
      final ContextHandler ch = new ContextHandler("/project-remote-list");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandProjectRemoteAdd(c, db, l);
      final ContextHandler ch = new ContextHandler("/project-remote-add");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    return handlers;
  }

  private JFPAdminCommands()
  {
    throw new UnreachableCodeException();
  }
}
