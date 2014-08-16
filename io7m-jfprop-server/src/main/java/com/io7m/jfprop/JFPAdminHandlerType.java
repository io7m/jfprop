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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;

import com.io7m.jfunctional.Pair;

/**
 * The type of command handlers that require admin credentials.
 */

public interface JFPAdminHandlerType extends Handler
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
   * @param transaction
   *          The current database transaction.
   * @throws JFPException
   *           On errors.
   * @throws IOException
   *           On I/O errors.
   * @throws ServletException
   *           On servlet errors.
   * 
   * @return A pair consisting of an HTTP status code, and some bytes
   *         representing UTF-8 text.
   */

  Pair<Integer, byte[]> handleAuthenticated(
    final String target,
    final Request base_request,
    final HttpServletRequest request,
    final JFPAdminDatabaseTransactionType transaction)
    throws JFPException,
      IOException,
      ServletException;
}
