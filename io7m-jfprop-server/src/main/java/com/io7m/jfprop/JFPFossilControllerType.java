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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

/**
 * The interface exposed by the fossil controller, responsible for performing
 * syncs, etc.
 */

public interface JFPFossilControllerType
{
  /**
   * Perform a sync for the repository for the given project.
   *
   * @param project
   *          The project upon which the commit occurred.
   * @return A future representing a sync in progress, with <code>true</code>
   *         indicating a successful sync.
   */

  Future<Boolean> doSync(
    final JFPProjectPath project);

  /**
   * Recursively list all fossil repositories in the given directory.
   *
   * @return A list of fossil repositories, relative to the root directory.
   * @param root
   *          The root directory.
   * @throws IOException
   *           On I/O errors.
   */

  List<String> listFossilRepositories(
    final File root)
    throws IOException;
}
