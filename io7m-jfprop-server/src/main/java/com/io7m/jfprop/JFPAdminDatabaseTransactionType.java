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
import java.util.SortedSet;

/**
 * The type of database transactions.
 */

public interface JFPAdminDatabaseTransactionType extends
  JFPDatabaseTransactionType
{
  /**
   * Add a new mass sync.
   *
   * @return The identifier of the added mass sync.
   * @param m
   *          The mass sync spec.
   */

  Integer massSyncAdd(
    JFPMassSyncSpec m);

  /**
   * Enable or disable mass syncing.
   *
   * @param enabled
   *          <code>true</code> if mass syncing is enabled.
   */

  void massSyncEnable(
    final boolean enabled);

  /**
   * @return <code>true</code> if mass syncing is enabled.
   */

  boolean massSyncIsEnabled();

  /**
   * @return The list of defined mass syncs.
   */

  SortedMap<Integer, JFPMassSyncSpec> massSyncList();

  /**
   * Remove a new mass sync.
   *
   * @param i
   *          The mass sync ID.
   */

  void massSyncRemove(
    Integer i);

  /**
   * Add the given remote.
   *
   * @param r
   *          The remote.
   * @return The identifier of the new remote.
   */

  Integer remoteAdd(
    JFPRemote r);

  /**
   * Add the remote with the given identifier to the list of global remotes.
   * That is, remotes that will be synchronized for all repositories.
   *
   * @param remote
   *          The remote.
   * @throws JFPExceptionNonexistent
   *           If the remote does not exist.
   */

  void repositoryAddGlobalRemote(
    Integer remote)
    throws JFPExceptionNonexistent;

  /**
   * Add the remote with the given identifier to the given repository.
   *
   * @param repository
   *          The repository.
   * @param remote
   *          The remote.
   * @throws JFPExceptionNonexistent
   *           If the remote does not exist.
   */

  void repositoryAddRemote(
    final JFPRepositoryPath repository,
    Integer remote)
    throws JFPExceptionNonexistent;

  /**
   * Create a new user.
   *
   * @param user
   *          The name.
   * @throws JFPExceptionDuplicate
   *           If the user already exists.
   */

  void userAdd(
    JFPUserName user)
    throws JFPExceptionDuplicate;

  /**
   * Generate a new key and add it to the given user.
   *
   * @param user
   *          The user.
   * @return The new key.
   * @throws JFPExceptionNonexistent
   *           If the user does not exist.
   */

  JFPKey userGenerateKey(
    JFPUserName user)
    throws JFPExceptionNonexistent;

  /**
   * @return The current list of users.
   */

  SortedSet<JFPUserName> userListGet();

  /**
   * List all keys for the given user.
   *
   * @param user
   *          The user.
   * @return The list of keys.
   * @throws JFPExceptionNonexistent
   *           If the user does not exist.
   */

  Set<JFPKey> userListKeys(
    JFPUserName user)
    throws JFPExceptionNonexistent;

  /**
   * Revoke the given key from given user.
   *
   * @param user
   *          The user.
   * @param key
   *          The key.
   * @throws JFPExceptionNonexistent
   *           If the user does not exist.
   */

  void userRevokeKey(
    JFPUserName user,
    JFPKey key)
    throws JFPExceptionNonexistent;
}
