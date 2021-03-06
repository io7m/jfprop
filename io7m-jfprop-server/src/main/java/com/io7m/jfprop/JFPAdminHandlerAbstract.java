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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.mapdb.TxRollbackException;

import com.io7m.jfunctional.Pair;
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

/**
 * The default implementation of authenticated handlers.
 */

abstract class JFPAdminHandlerAbstract extends AbstractHandler implements
  JFPAdminHandlerType
{
  protected final JFPServerConfigType  config;
  protected final JFPAdminDatabaseType database;
  protected final LogUsableType        log;

  JFPAdminHandlerAbstract(
    final JFPServerConfigType in_config,
    final JFPAdminDatabaseType db,
    final LogUsableType in_log)
  {
    this.config = NullCheck.notNull(in_config, "Configuration");
    this.database = NullCheck.notNull(db, "Database");
    this.log = NullCheck.notNull(in_log, "Log");
  }

  @Override public final void handle(
    final @Nullable String target,
    final @Nullable Request base_request,
    final @Nullable HttpServletRequest request,
    final @Nullable HttpServletResponse response)
    throws IOException,
      ServletException
  {
    assert target != null;
    assert base_request != null;
    assert request != null;
    assert response != null;

    base_request.setHandled(true);

    try {
      final String method = request.getMethod();
      if ("POST".equals(method) == false) {
        JFPResponseUtilities.sendText(
          response,
          HttpServletResponse.SC_METHOD_NOT_ALLOWED,
          "POST is required");
        return;
      }

      final Map<String, String[]> ps = request.getParameterMap();
      assert ps != null;
      final String password = JFPAuthentication.getAdminPassword(ps);

      if (this.config.getAdminPassword().equals(password) == false) {
        throw new JFPExceptionAuthentication("incorrect password");
      }

      final Pair<Integer, byte[]> r =
        this.database
          .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Pair<Integer, byte[]>, Exception>() {
            @Override public Pair<Integer, byte[]> call(
              final JFPAdminDatabaseTransactionType t)
              throws Exception
            {
              return JFPAdminHandlerAbstract.this.handleAuthenticated(
                target,
                base_request,
                request,
                t);
            }
          });

      JFPResponseUtilities.sendBytesAsUTF8WithStatus(
        response,
        r.getLeft(),
        r.getRight());

    } catch (final JFPExceptionAuthentication e) {
      this.log.error("authentication failed: " + e.getMessage());
      JFPResponseUtilities.sendText(
        response,
        HttpServletResponse.SC_FORBIDDEN,
        "Access denied");
    } catch (final TxRollbackException e) {
      this.log.debug("database transaction failed: " + e.toString());
      JFPResponseUtilities
        .sendText(
          response,
          HttpServletResponse.SC_CONFLICT,
          "The command conflicted with an in-progress database transaction and was reverted");
    } catch (final Throwable e) {
      this.log.critical(e.toString());
      JFPResponseUtilities.sendText(
        response,
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        "Internal server error: " + e.toString());
    }
  }
}
