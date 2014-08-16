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

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import com.io7m.jfprop.JFPAdminDatabaseTransactionType;
import com.io7m.jfprop.JFPAllDatabaseType;
import com.io7m.jfprop.JFPDatabase;
import com.io7m.jfprop.JFPException;
import com.io7m.jfprop.JFPExceptionDatabaseIncorrectVersion;
import com.io7m.jfprop.JFPExceptionDuplicate;
import com.io7m.jfprop.JFPExceptionNonexistent;
import com.io7m.jfprop.JFPKey;
import com.io7m.jfprop.JFPMassSyncSpec;
import com.io7m.jfprop.JFPRemote;
import com.io7m.jfprop.JFPRepositoryPath;
import com.io7m.jfprop.JFPServerDatabaseTransactionType;
import com.io7m.jfprop.JFPUserName;
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.Log;
import com.io7m.jlog.LogLevel;
import com.io7m.jlog.LogPolicyAllOn;
import com.io7m.junreachable.UnreachableCodeException;

@SuppressWarnings({ "static-method", "unused" }) public final class JFPDatabaseTest
{
  private JFPAllDatabaseType database()
  {
    try {
      return JFPDatabase.openInMemory(Log.newLog(
        LogPolicyAllOn.newPolicy(LogLevel.LOG_DEBUG),
        "tests"));
    } catch (final JFPExceptionDatabaseIncorrectVersion e) {
      throw new UnreachableCodeException(e);
    }
  }

  @Test(expected = JFPExceptionNonexistent.class) public
    void
    testProjectNonexistent_0()
      throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final JFPRepositoryPath p = new JFPRepositoryPath("/a/b/c.fossil");

          t.repositoryAddRemote(p, Integer.valueOf(23));
          return Unit.unit();
        }
      });
  }

  @Test public void testProjectRemoteAdd_0()
    throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final JFPRepositoryPath p = new JFPRepositoryPath("/a/b/c.fossil");
          final URI uri = URI.create("http://127.0.0.1");
          final JFPUserName user = new JFPUserName("someone");
          final JFPKey key = new JFPKey("abcd1234");
          final JFPRemote r = new JFPRemote(uri, user, key);

          final Integer rid = t.remoteAdd(r);
          final SortedMap<Integer, JFPRemote> m = t.remotesGet();
          Assert.assertEquals(1, m.size());
          Assert.assertTrue(m.values().contains(r));

          t.repositoryAddRemote(p, rid);

          final Set<Integer> rs = t.repositoryListRemotes(p);
          Assert.assertTrue(rs.contains(rid));
          return Unit.unit();
        }
      });
  }

  @Test public void testProjectRemoteAdd_1()
    throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final JFPRepositoryPath p = new JFPRepositoryPath("/a/b/c.fossil");

          final URI uri = URI.create("http://127.0.0.1");
          final JFPUserName user = new JFPUserName("someone");
          final JFPKey key = new JFPKey("abcd1234");
          final JFPRemote r = new JFPRemote(uri, user, key);

          final Integer rid0 = t.remoteAdd(r);
          final Integer rid1 = t.remoteAdd(r);

          t.repositoryAddRemote(p, rid0);
          t.repositoryAddRemote(p, rid1);

          final Set<Integer> rs = t.repositoryListRemotes(p);
          Assert.assertEquals(2, rs.size());
          Assert.assertTrue(rs.contains(rid0));
          Assert.assertTrue(rs.contains(rid1));
          return Unit.unit();
        }
      });
  }

  @Test public void testProjectRemoteAddGlobal_0()
    throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final JFPRepositoryPath p = new JFPRepositoryPath("/a/b/c.fossil");

          final URI uri = URI.create("http://127.0.0.1");
          final JFPUserName user = new JFPUserName("someone");
          final JFPKey key = new JFPKey("abcd1234");
          final JFPRemote r = new JFPRemote(uri, user, key);

          final Integer rid0 = t.remoteAdd(r);
          final Integer rid1 = t.remoteAdd(r);

          t.repositoryAddGlobalRemote(rid0);
          t.repositoryAddRemote(p, rid1);

          final Set<Integer> rs = t.repositoryListRemotes(p);
          Assert.assertEquals(2, rs.size());
          Assert.assertTrue(rs.contains(rid0));
          Assert.assertTrue(rs.contains(rid1));
          return Unit.unit();
        }
      });
  }

  @Test(expected = JFPExceptionNonexistent.class) public
    void
    testProjectRemoteAddGlobal_1()
      throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final JFPRepositoryPath p = new JFPRepositoryPath("/a/b/c.fossil");
          t.repositoryAddGlobalRemote(Integer.valueOf(23));
          return Unit.unit();
        }
      });
  }

  @Test public void testRemotesAdd()
    throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final URI uri = URI.create("http://127.0.0.1");
          final JFPUserName user = new JFPUserName("someone");
          final JFPKey key = new JFPKey("abcd1234");
          final JFPRemote r = new JFPRemote(uri, user, key);

          t.remoteAdd(r);
          final SortedMap<Integer, JFPRemote> m = t.remotesGet();
          Assert.assertEquals(1, m.size());
          Assert.assertTrue(m.values().contains(r));

          t.remoteAdd(r);
          Assert.assertEquals(1, m.size());
          Assert.assertEquals(1, m.values().size());
          Assert.assertTrue(m.values().contains(r));

          final SortedMap<Integer, JFPRemote> m2 = t.remotesGet();
          Assert.assertEquals(2, m2.size());
          Assert.assertEquals(2, m2.values().size());
          Assert.assertTrue(m2.values().contains(r));

          return Unit.unit();
        }
      });
  }

  @Test public void testRemotesEmpty()
    throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withTransaction(new PartialFunctionType<JFPServerDatabaseTransactionType, Unit, UnreachableCodeException>() {
        @Override public Unit call(
          final JFPServerDatabaseTransactionType t)
          throws UnreachableCodeException
        {
          Assert.assertTrue(t.remotesGet().isEmpty());
          return Unit.unit();
        }
      });
  }

  @Test(expected = JFPExceptionNonexistent.class) public
    void
    testUserGenerateKey_Nonexistent_0()
      throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final JFPUserName u = new JFPUserName("someone");
          t.userGenerateKey(u);
          return Unit.unit();
        }
      });
  }

  @Test public void testUserGenerateKey_0()
    throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final JFPUserName u = new JFPUserName("someone");

          t.userAdd(u);

          final Set<JFPKey> keys = new HashSet<JFPKey>();
          for (int index = 0; index < 100; ++index) {
            final JFPKey k = t.userGenerateKey(u);
            Assert.assertFalse(keys.contains(k));
            keys.add(k);
          }

          Assert.assertEquals(100, keys.size());

          final Set<JFPKey> kg = t.userListKeys(u);
          Assert.assertEquals(keys, kg);
          return Unit.unit();
        }
      });
  }

  @Test public void testUserAdd_0()
    throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final JFPUserName u = new JFPUserName("someone");
          t.userAdd(u);

          final SortedSet<JFPUserName> us = t.userListGet();
          Assert.assertEquals(1, us.size());
          Assert.assertTrue(us.contains(u));
          return Unit.unit();
        }
      });
  }

  @Test(expected = JFPExceptionDuplicate.class) public void testUserAdd_1()
    throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final JFPUserName u = new JFPUserName("someone");
          t.userAdd(u);
          t.userAdd(u);
          return Unit.unit();
        }
      });
  }

  @Test(expected = JFPExceptionNonexistent.class) public
    void
    testUserListKeys_0()
      throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final JFPUserName u = new JFPUserName("someone");
          t.userListKeys(u);
          return Unit.unit();
        }
      });
  }

  @Test public void testUserCheck_0()
    throws Exception
  {
    final JFPUserName u = new JFPUserName("someone");
    final AtomicReference<JFPKey> k = new AtomicReference<JFPKey>();
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          t.userAdd(u);
          k.set(t.userGenerateKey(u));
          return Unit.unit();
        }
      });

    db
      .withTransaction(new PartialFunctionType<JFPServerDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPServerDatabaseTransactionType t)
          throws JFPException
        {
          Assert.assertTrue(t.checkNameAndKey(u, k.get()));
          Assert.assertFalse(t.checkNameAndKey(u, new JFPKey("abcd1234")));
          Assert.assertFalse(t.checkNameAndKey(
            new JFPUserName("nonexistent"),
            k.get()));
          return Unit.unit();
        }
      });
  }

  @Test public void testUserKeyRevoke_0()
    throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final JFPUserName u = new JFPUserName("someone");

          t.userAdd(u);

          final Set<JFPKey> keys = new HashSet<JFPKey>();
          for (int index = 0; index < 100; ++index) {
            final JFPKey k = t.userGenerateKey(u);
            Assert.assertFalse(keys.contains(k));
            keys.add(k);
          }

          Assert.assertEquals(100, keys.size());

          for (final JFPKey k : keys) {
            t.userRevokeKey(u, k);
          }

          final Set<JFPKey> kg = t.userListKeys(u);
          Assert.assertTrue(kg.isEmpty());
          return Unit.unit();
        }
      });
  }

  @Test(expected = JFPExceptionNonexistent.class) public
    void
    testUserKeyRevokeNonexistent_0()
      throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final JFPUserName u = new JFPUserName("someone");
          t.userRevokeKey(u, new JFPKey("abcd1234"));
          return Unit.unit();
        }
      });
  }

  @Test public void testMassSyncAdd_0()
    throws Exception
  {
    final JFPAllDatabaseType db = this.database();
    db
      .withAdminTransaction(new PartialFunctionType<JFPAdminDatabaseTransactionType, Unit, JFPException>() {
        @Override public Unit call(
          final JFPAdminDatabaseTransactionType t)
          throws JFPException
        {
          final Map<String, String[]> r = new HashMap<String, String[]>();
          r.put("time_spec_day", new String[] { "any" });
          r.put("time_spec_hour", new String[] { "any" });
          r.put("time_spec_minute", new String[] { "any" });
          r.put("pattern", new String[] { "[a-z]+" });

          final JFPMassSyncSpec m = JFPMassSyncSpec.parseFromRequest(r);
          final Integer mi = t.massSyncAdd(m);

          {
            Assert.assertTrue(mi.intValue() > 0);
            final SortedMap<Integer, JFPMassSyncSpec> ml = t.massSyncList();
            Assert.assertTrue(ml.containsKey(mi));
            Assert.assertEquals(1, ml.keySet().size());
            Assert.assertEquals(1, ml.size());
          }

          {
            t.massSyncRemove(mi);
            final SortedMap<Integer, JFPMassSyncSpec> ml = t.massSyncList();
            Assert.assertFalse(ml.containsKey(mi));
            Assert.assertEquals(0, ml.keySet().size());
            Assert.assertEquals(0, ml.size());
          }

          return Unit.unit();
        }
      });
  }
}
