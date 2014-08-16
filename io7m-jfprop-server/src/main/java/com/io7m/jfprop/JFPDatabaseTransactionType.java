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

import java.util.Set;
import java.util.SortedMap;

/**
 * The common type of database transactions.
 */

public interface JFPDatabaseTransactionType
{
  /**
   * List all remotes (including global remotes that are implicitly assigned
   * to all repositories) for the given repository.
   *
   * @param repository
   *          The repository.
   * @return The set of remotes for the repository.
   * @throws JFPExceptionNonexistent
   *           If the repository does not exist.
   */

  Set<Integer> repositoryListRemotes(
    final JFPRepositoryPath repository)
    throws JFPExceptionNonexistent;

  /**
   * @return A read-only map of the current remotes.
   */

  SortedMap<Integer, JFPRemote> remotesGet();
}
