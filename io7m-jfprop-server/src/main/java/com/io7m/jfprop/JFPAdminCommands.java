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
      final Handler h = new JFPAdminCommandDefault(c, db, l);
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
      final Handler h =
        new JFPAdminCommandRepositoryRemoteGlobalAdd(c, db, l);
      final ContextHandler ch =
        new ContextHandler("/repository-remote-global-add");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandRepositoryRemoteList(c, db, l);
      final ContextHandler ch = new ContextHandler("/repository-remote-list");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandRepositoryRemoteAdd(c, db, l);
      final ContextHandler ch = new ContextHandler("/repository-remote-add");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandMassSyncAdd(c, db, l);
      final ContextHandler ch = new ContextHandler("/mass-sync-add");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandMassSyncList(c, db, l);
      final ContextHandler ch = new ContextHandler("/mass-sync-list");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandMassSyncRemove(c, db, l);
      final ContextHandler ch = new ContextHandler("/mass-sync-remove");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandMassSyncIsEnabled(c, db, l);
      final ContextHandler ch = new ContextHandler("/mass-sync-is-enabled");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandMassSyncEnable(c, db, l);
      final ContextHandler ch = new ContextHandler("/mass-sync-enable");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPAdminCommandMassSyncTestPattern(c, db, l);
      final ContextHandler ch = new ContextHandler("/mass-sync-test-pattern");
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
