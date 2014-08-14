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
import java.util.SortedMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;

/**
 * A handler for the "on-commit" command.
 */

final class JFPServerCommandOnCommit extends JFPAuthenticatedHandlerAbstract
{
  private final ExecutorService         exec;
  private final JFPFossilControllerType fossil_controller;
  private final JFPRemoteControllerType remote_controller;

  JFPServerCommandOnCommit(
    final JFPServerConfigType in_config,
    final JFPServerDatabaseType db,
    final LogUsableType in_log,
    final ExecutorService in_exec,
    final JFPFossilControllerType in_fossil_controller,
    final JFPRemoteControllerType in_remote_controller)
  {
    super(in_config, db, in_log);
    this.fossil_controller =
      NullCheck.notNull(in_fossil_controller, "Fossil controller");
    this.remote_controller =
      NullCheck.notNull(in_remote_controller, "Remote controller");
    this.exec = NullCheck.notNull(in_exec, "Executor");
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
    try {
      this.log.debug("sync request received");

      final JFPProjectPath project =
        new JFPProjectPath(JFPRequestUtilities.getValueSingle(
          request.getParameterMap(),
          "project"));

      this.log.debug("sync project " + project);

      final SortedMap<Integer, JFPRemote> remotes = transaction.remotesGet();

      final JFPFossilControllerType fc = this.fossil_controller;
      final JFPRemoteControllerType rc = this.remote_controller;
      final LogUsableType l = this.log;

      this.exec.execute(new Runnable() {
        @Override public void run()
        {
          try {
            final Future<Boolean> fsr = fc.doSync(project);
            if (fsr.get()) {
              for (final JFPRemote r : remotes.values()) {
                rc.doCommitNotify(project, r);
              }
            }
          } catch (final InterruptedException e) {
            l.error("interrupted exception: " + e);
          } catch (final ExecutionException e) {
            l.error("execution exception: " + e);
          }
        }
      });

      JFPResponseUtilities
        .sendText(response, HttpServletResponse.SC_OK, null);

    } catch (final JFPExceptionInvalidArgument e) {
      JFPResponseUtilities.sendText(
        response,
        HttpServletResponse.SC_BAD_REQUEST,
        e.getMessage());
    } catch (final JFPExceptionNonexistent e) {
      JFPResponseUtilities.sendText(
        response,
        HttpServletResponse.SC_BAD_REQUEST,
        e.getMessage());
    }
  }
}
