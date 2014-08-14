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

/**
 * The type of server event functions.
 *
 * Mostly just for testing, or for embeddeding the server in other programs.
 */

public interface JFPServerEventsType
{
  /**
   * Called when the server has crashed.
   *
   * @param x
   *          The exception that caused the crash.
   */

  void serverCrashed(
    final Exception x);

  /**
   * Called when the server has started and is running.
   *
   * @param c
   *          The control interface.
   * @throws Exception
   *           If required.
   */

  void serverStarted(
    final JFPServerControlType c)
    throws Exception;

  /**
   * Called when the server has stopped.
   */

  void serverStopped();
}
