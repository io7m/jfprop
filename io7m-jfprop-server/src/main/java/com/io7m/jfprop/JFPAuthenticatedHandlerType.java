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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;

/**
 * The type of command handlers that require authenticated users.
 */

public interface JFPAuthenticatedHandlerType extends Handler
{
  /**
   * Handle a request.
   *
   * @param target
   *          The target.
   * @param base_request
   *          The base request.
   * @param request
   *          The actual servlet request.
   * @param response
   *          The servlet response.
   * @param user
   *          The authenticated user.
   * @param key
   *          The key the user presented.
   * @param transaction
   *          The current database transaction.
   * @throws JFPException
   *           On errors.
   * @throws IOException
   *           On I/O errors.
   */

  void handleAuthenticated(
    final String target,
    final Request base_request,
    final HttpServletRequest request,
    final HttpServletResponse response,
    final JFPUserName user,
    final JFPKey key,
    final JFPServerDatabaseTransactionType transaction)
    throws JFPException,
      IOException;
}
