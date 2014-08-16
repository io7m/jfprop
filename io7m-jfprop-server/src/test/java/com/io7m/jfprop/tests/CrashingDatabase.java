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

package com.io7m.jfprop.tests;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.mapdb.TxRollbackException;

import com.io7m.jfprop.JFPAdminDatabaseTransactionType;
import com.io7m.jfprop.JFPAllDatabaseType;
import com.io7m.jfprop.JFPExceptionAuthentication;
import com.io7m.jfprop.JFPExceptionDuplicate;
import com.io7m.jfprop.JFPExceptionNonexistent;
import com.io7m.jfprop.JFPKey;
import com.io7m.jfprop.JFPMassSyncSpec;
import com.io7m.jfprop.JFPRemote;
import com.io7m.jfprop.JFPRepositoryPath;
import com.io7m.jfprop.JFPServerDatabaseTransactionType;
import com.io7m.jfprop.JFPUserName;
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.junreachable.UnreachableCodeException;

public final class CrashingDatabase implements JFPAllDatabaseType
{
  public final class CrashingTransaction implements
    JFPAdminDatabaseTransactionType,
    JFPServerDatabaseTransactionType
  {
    @Override public Set<Integer> repositoryListRemotes(
      final JFPRepositoryPath repository)
      throws JFPExceptionNonexistent
    {
      return new HashSet<Integer>();
    }

    @Override public SortedMap<Integer, JFPRemote> remotesGet()
    {
      return new TreeMap<Integer, JFPRemote>();
    }

    @Override public Integer massSyncAdd(
      final JFPMassSyncSpec m)
    {
      return 0;
    }

    @Override public void massSyncEnable(
      final boolean enabled)
    {

    }

    @Override public boolean massSyncIsEnabled()
    {
      return true;
    }

    @Override public SortedMap<Integer, JFPMassSyncSpec> massSyncList()
    {
      return new TreeMap<Integer, JFPMassSyncSpec>();
    }

    @Override public void massSyncRemove(
      final Integer i)
    {

    }

    @Override public Integer remoteAdd(
      final JFPRemote r)
    {
      return 0;
    }

    @Override public void repositoryAddGlobalRemote(
      final Integer remote)
      throws JFPExceptionNonexistent
    {

    }

    @Override public void repositoryAddRemote(
      final JFPRepositoryPath repository,
      final Integer remote)
      throws JFPExceptionNonexistent
    {

    }

    @Override public void userAdd(
      final JFPUserName user)
      throws JFPExceptionDuplicate
    {

    }

    @Override public JFPKey userGenerateKey(
      final JFPUserName user)
      throws JFPExceptionNonexistent
    {
      try {
        return new JFPKey("abcd1234");
      } catch (final JFPExceptionAuthentication e) {
        throw new UnreachableCodeException(e);
      }
    }

    @Override public SortedSet<JFPUserName> userListGet()
    {
      return new TreeSet<JFPUserName>();
    }

    @Override public Set<JFPKey> userListKeys(
      final JFPUserName user)
      throws JFPExceptionNonexistent
    {
      return new HashSet<JFPKey>();
    }

    @Override public void userRevokeKey(
      final JFPUserName user,
      final JFPKey key)
      throws JFPExceptionNonexistent
    {

    }

    @Override public boolean checkNameAndKey(
      final JFPUserName name,
      final JFPKey key)
    {
      return true;
    }
  }

  public final class CrashingException extends RuntimeException
  {
    private static final long serialVersionUID = -5533787019188221871L;
  }

  @Override public <T, E extends Exception> T withTransaction(
    final PartialFunctionType<JFPServerDatabaseTransactionType, T, E> f)
    throws E
  {
    final CrashingTransaction t = new CrashingTransaction();
    f.call(t);
    throw new TxRollbackException();
  }

  @Override public <T, E extends Exception> T withAdminTransaction(
    final PartialFunctionType<JFPAdminDatabaseTransactionType, T, E> f)
    throws E
  {
    final CrashingTransaction t = new CrashingTransaction();
    f.call(t);
    throw new TxRollbackException();
  }
}
