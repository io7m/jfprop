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

import java.net.URI;
import java.util.List;

/**
 * The type of error reporters for program executions.
 */

public interface JFPErrorReporterType
{
  /**
   * Called upon failures caused by calling external HTTP URIs.
   *
   * @param uri
   *          The external URI.
   * @param code
   *          The HTTP error code.
   * @param operation
   *          A humanly-readable description of what the operation being
   *          performed.
   */

  void onExternalHTTPError(
    final URI uri,
    int code,
    String operation);

  /**
   * Called upon execution failures of external programs.
   *
   * @param output
   *          The output produced by the execution.
   * @param args
   *          The arguments that produced the execution failure.
   * @param code
   *          The process exit code.
   */

  void onExternalProgramError(
    final StringBuilder output,
    final List<String> args,
    int code);
}
