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
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.TxMaker;

import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;

/**
 * The main database implementation.
 */

public final class JFPDatabase
{
  private static final class Database implements JFPAllDatabaseType
  {
    private final TxMaker       db;
    private final LogUsableType log;

    private Database(
      final TxMaker in_db,
      final LogUsableType in_log)
    {
      this.db = in_db;
      this.log = in_log;
    }

    @Override public <T, E extends Exception> T withAdminTransaction(
      final PartialFunctionType<JFPAdminDatabaseTransactionType, T, E> f)
      throws E
    {
      final DB d = this.db.makeTx();
      assert d != null;
      final Transaction t = new Transaction(d, this.log);

      try {
        final T x = f.call(t);
        d.commit();
        return x;
      } finally {
        d.close();
      }
    }

    @Override public <T, E extends Exception> T withTransaction(
      final PartialFunctionType<JFPServerDatabaseTransactionType, T, E> f)
      throws E
    {
      final DB d = this.db.makeTx();
      assert d != null;
      final Transaction t = new Transaction(d, this.log);

      try {
        final T x = f.call(t);
        d.commit();
        return x;
      } finally {
        d.close();
      }
    }
  }

  private static final class Transaction implements
    JFPServerDatabaseTransactionType,
    JFPAdminDatabaseTransactionType
  {
    private final DB            db;
    private final LogUsableType log;

    public Transaction(
      final DB in_d,
      final LogUsableType in_log)
    {
      this.db = NullCheck.notNull(in_d, "Database");
      this.log = NullCheck.notNull(in_log, "Log");
    }

    @Override public boolean checkNameAndKey(
      final JFPUserName name,
      final JFPKey key)
    {
      final BTreeMap<JFPUserName, Set<JFPKey>> users = this.getUsers();
      if (users.containsKey(name)) {
        final Set<JFPKey> keys = users.get(name);
        return keys.contains(key);
      }
      return false;
    }

    private BTreeMap<JFPProjectPath, Set<Integer>> getProjects()
    {
      final BTreeMap<JFPProjectPath, Set<Integer>> projects =
        this.db.getTreeMap("projects");
      return projects;
    }

    private BTreeMap<Integer, JFPRemote> getRemotes()
    {
      final BTreeMap<Integer, JFPRemote> remotes =
        this.db.getTreeMap("remotes");
      return remotes;
    }

    private Set<Integer> getRemotesGlobal()
    {
      final Set<Integer> remotes = this.db.getHashSet("remotes_global");
      return remotes;
    }

    private BTreeMap<JFPUserName, Set<JFPKey>> getUsers()
    {
      final BTreeMap<JFPUserName, Set<JFPKey>> users =
        this.db.getTreeMap("users");
      return users;
    }

    @Override public void projectAddRemote(
      final JFPProjectPath project,
      final Integer remote)
      throws JFPExceptionNonexistent
    {
      final BTreeMap<Integer, JFPRemote> remotes = this.getRemotes();
      final BTreeMap<JFPProjectPath, Set<Integer>> projects =
        this.getProjects();

      if (remotes.containsKey(remote) == false) {
        throw new JFPExceptionNonexistent(String.format(
          "Nonexistent remote '%s'",
          remote));
      }

      Set<Integer> project_remotes;
      if (projects.containsKey(project)) {
        project_remotes = projects.get(project);
      } else {
        project_remotes = new HashSet<Integer>();
      }
      project_remotes.add(remote);
      projects.put(project, project_remotes);
    }

    @Override public Set<Integer> projectListRemotes(
      final JFPProjectPath project)
      throws JFPExceptionNonexistent
    {
      final BTreeMap<JFPProjectPath, Set<Integer>> projects =
        this.getProjects();
      final Set<Integer> globals = this.getRemotesGlobal();

      if (projects.containsKey(project) == false) {
        throw new JFPExceptionNonexistent(String.format(
          "Nonexistent project '%s'",
          project));
      }

      final Set<Integer> project_remotes = projects.get(project);
      final Set<Integer> r = new HashSet<Integer>();
      r.addAll(project_remotes);
      r.addAll(globals);
      return r;
    }

    @Override public Set<JFPRemote> projectRemotesGet(
      final JFPProjectPath project)
    {
      try {
        final BTreeMap<JFPProjectPath, Set<Integer>> projects =
          this.getProjects();
        final BTreeMap<Integer, JFPRemote> remotes = this.getRemotes();

        if (projects.containsKey(project)) {
          final Set<JFPRemote> r = new HashSet<JFPRemote>();
          final Set<Integer> ids = this.projectListRemotes(project);
          for (final Integer i : ids) {
            r.add(remotes.get(i));
          }
          return r;
        }

        return new HashSet<JFPRemote>();
      } catch (final JFPExceptionNonexistent e) {
        throw new UnreachableCodeException(e);
      }
    }

    @Override public void projectsAddGlobalRemote(
      final Integer remote)
      throws JFPExceptionNonexistent
    {
      final Set<Integer> remotes_global = this.getRemotesGlobal();
      final BTreeMap<Integer, JFPRemote> remotes = this.getRemotes();

      if (remotes.containsKey(remote) == false) {
        throw new JFPExceptionNonexistent(String.format(
          "Nonexistent remote '%s'",
          remote));
      }

      remotes_global.add(remote);
    }

    @Override public Integer remoteAdd(
      final JFPRemote r)
    {
      final org.mapdb.Atomic.Integer pool =
        this.db.getAtomicInteger("remote_ids");
      final int id = pool.incrementAndGet();

      final BTreeMap<Integer, JFPRemote> remotes = this.getRemotes();
      assert remotes.containsKey(id) == false;

      remotes.put(id, r);
      return id;
    }

    @Override public SortedMap<Integer, JFPRemote> remotesGet()
    {
      final BTreeMap<Integer, JFPRemote> remotes = this.getRemotes();
      final SortedMap<Integer, JFPRemote> m =
        new TreeMap<Integer, JFPRemote>();
      m.putAll(remotes);
      return m;
    }

    @Override public void userAdd(
      final JFPUserName user)
      throws JFPExceptionDuplicate
    {
      final BTreeMap<JFPUserName, Set<JFPKey>> users = this.getUsers();
      if (users.containsKey(user)) {
        throw new JFPExceptionDuplicate(String.format(
          "Duplicate user '%s'",
          user));
      }

      users.put(user, new HashSet<JFPKey>());
    }

    @Override public JFPKey userGenerateKey(
      final JFPUserName user)
      throws JFPExceptionNonexistent
    {
      try {
        final BTreeMap<JFPUserName, Set<JFPKey>> users = this.getUsers();

        if (users.containsKey(user) == false) {
          throw new JFPExceptionNonexistent(String.format(
            "Nonexistent user '%s'",
            user));
        }

        final Set<JFPKey> keys = users.get(user);
        final StringBuilder sb = new StringBuilder();
        final SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
        for (int index = 0; index < 1000; ++index) {
          final byte[] bytes = new byte[32];
          sr.nextBytes(bytes);
          sb.setLength(0);
          for (final byte b : bytes) {
            sb.append(String.format("%02x", b));
          }
          final JFPKey key = new JFPKey(sb.toString());
          if (keys.contains(key) == false) {
            keys.add(key);
            users.put(user, keys);
            return key;
          }
        }

        /**
         * The chances of failing to find a unique 128-bit number after 1000
         * attempts is remote. If it has happened, it's likely indicative of a
         * serious problem with the random number generator.
         */

        throw new UnreachableCodeException();

      } catch (final NoSuchAlgorithmException e) {
        throw new UnreachableCodeException(e);
      } catch (final NoSuchProviderException e) {
        throw new UnreachableCodeException(e);
      } catch (final JFPExceptionAuthentication e) {
        throw new UnreachableCodeException(e);
      }
    }

    @Override public SortedSet<JFPUserName> userListGet()
    {
      final BTreeMap<JFPUserName, Set<JFPKey>> users = this.getUsers();
      return Collections.unmodifiableSortedSet(users.keySet());
    }

    @Override public Set<JFPKey> userListKeys(
      final JFPUserName user)
      throws JFPExceptionNonexistent
    {
      final BTreeMap<JFPUserName, Set<JFPKey>> users = this.getUsers();

      if (users.containsKey(user) == false) {
        throw new JFPExceptionNonexistent(String.format(
          "Nonexistent user '%s'",
          user));
      }

      return users.get(user);
    }

    @Override public void userRevokeKey(
      final JFPUserName user,
      final JFPKey key)
      throws JFPExceptionNonexistent
    {
      final BTreeMap<JFPUserName, Set<JFPKey>> users = this.getUsers();

      if (users.containsKey(user) == false) {
        throw new JFPExceptionNonexistent(String.format(
          "Nonexistent user '%s'",
          user));
      }

      final Set<JFPKey> keys = users.get(user);
      keys.remove(key);
      users.put(user, keys);
    }
  }

  /**
   * Open a database, creating it if it does not exist.
   *
   * @param log
   *          A log interface
   * @param file
   *          The database file
   * @return A database.
   */

  public static JFPAllDatabaseType open(
    final LogUsableType log,
    final File file)
  {
    NullCheck.notNull(file, "File");

    final DBMaker<?> maker =
      DBMaker.newFileDB(file).closeOnJvmShutdown().checksumEnable();

    final TxMaker db = maker.makeTxMaker();
    assert db != null;

    return new Database(db, log);
  }

  /**
   * Open an in-memory database, creating it if it does not exist.
   *
   * @param log
   *          A log interface
   * @return A database.
   */

  public static JFPAllDatabaseType openInMemory(
    final LogUsableType log)
  {
    final DBMaker<?> maker = DBMaker.newHeapDB();

    final TxMaker db = maker.makeTxMaker();
    assert db != null;

    return new Database(db, log);
  }

  private JFPDatabase()
  {
    throw new UnreachableCodeException();
  }
}
