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

import java.util.concurrent.ExecutorService;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import com.io7m.jlog.LogUsableType;
import com.io7m.junreachable.UnreachableCodeException;

final class JFPServerCommands
{
  static ContextHandlerCollection getHandlers(
    final JFPServerConfigType c,
    final JFPServerDatabaseType db,
    final ExecutorService exec,
    final JFPFossilControllerType fossil_controller,
    final JFPRemoteControllerType remote_controller,
    final LogUsableType l)
  {
    final ContextHandlerCollection handlers = new ContextHandlerCollection();

    {
      final Handler h = new JFPServerCommandDefault();
      final ContextHandler ch = new ContextHandler("/");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h = new JFPServerCommandList(c, db, fossil_controller, l);
      final ContextHandler ch = new ContextHandler("/list");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    {
      final Handler h =
        new JFPServerCommandOnCommit(
          c,
          db,
          l,
          exec,
          fossil_controller,
          remote_controller);
      final ContextHandler ch = new ContextHandler("/on-commit");
      ch.setHandler(h);
      ch.setAllowNullPathInfo(true);
      handlers.addHandler(ch);
    }

    return handlers;
  }

  private JFPServerCommands()
  {
    throw new UnreachableCodeException();
  }
}
