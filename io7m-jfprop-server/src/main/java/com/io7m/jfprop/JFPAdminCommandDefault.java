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
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.io7m.jnull.Nullable;

/**
 * A handler for missing commands.
 */

final class JFPAdminCommandDefault extends AbstractHandler
{
  JFPAdminCommandDefault()
  {

  }

  @Override public void handle(
    final @Nullable String target,
    final @Nullable Request base_request,
    final @Nullable HttpServletRequest request,
    final @Nullable HttpServletResponse response)
    throws IOException,
      ServletException
  {
    assert base_request != null;
    assert request != null;
    assert response != null;

    base_request.setHandled(true);
    JFPResponseUtilities.sendText(
      response,
      HttpServletResponse.SC_NOT_FOUND,
      "Unknown or unspecified command");
  }
}
