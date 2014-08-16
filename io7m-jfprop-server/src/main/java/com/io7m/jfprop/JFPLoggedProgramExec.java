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
import java.io.InputStream;
import java.util.List;

import com.io7m.jlog.LogUsableType;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * Functions to execute programs, logging the outputs.
 */

public final class JFPLoggedProgramExec
{
  /**
   * Execute the program given by <code>args</code>.
   *
   * @param log
   *          A log interface
   * @param args
   *          The program and arguments
   * @param reporter
   *          An error reporter
   * @return <code>true</code> if execution proceeds without errors
   * @throws IOException
   *           On I/O errors
   * @throws InterruptedException
   *           Upon failing to wait for a program to finish.
   */

  public static boolean executeLogged(
    final LogUsableType log,
    final List<String> args,
    final JFPErrorReporterType reporter)
      throws IOException,
      InterruptedException
  {
    log.debug("exec: " + args);

    final ProcessBuilder pb = new ProcessBuilder(args);
    pb.redirectErrorStream(true);

    final Process p = pb.start();
    final byte[] buffer = new byte[8192];
    final StringBuilder output = new StringBuilder();
    final InputStream stdout = p.getInputStream();

    for (;;) {
      final int r = stdout.read(buffer);
      if (r == -1) {
        break;
      }

      final String s = new String(buffer, 0, r);
      output.append(s);
    }

    final int r = p.waitFor();
    log.debug("process exited with code " + r);

    if (r != 0) {
      reporter.onExternalProgramError(output, args, r);
      return false;
    }

    return true;
  }

  private JFPLoggedProgramExec()
  {
    throw new UnreachableCodeException();
  }
}
