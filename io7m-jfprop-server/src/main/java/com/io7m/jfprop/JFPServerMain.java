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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.mail.internet.AddressException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.io7m.jfunctional.FunctionType;
import com.io7m.jfunctional.None;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.OptionVisitorType;
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogCreatableType;
import com.io7m.jlog.LogType;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jproperties.JPropertyException;

/**
 * Main server.
 */

@SuppressWarnings("synthetic-access") public final class JFPServerMain implements
  Runnable,
  JFPServerControlType
{
  private static File checkCreateLogAccessDirectory(
    final File server_log_directory)
    throws IOException
  {
    final File directory_access = new File(server_log_directory, "access");
    if (directory_access.isDirectory() == false) {
      if (directory_access.mkdir() == false) {
        throw new IOException("could not create " + directory_access);
      }
    }
    return directory_access;
  }

  private static Server getHTTPServer(
    final JFPFossilControllerType fossil_controller,
    final JFPRemoteControllerType remote_controller,
    final RequestLogHandler request_log_handler,
    final JFPServerHTTPConfigType http,
    final JFPServerConfigType in_config,
    final JFPServerDatabaseType in_database,
    final ExecutorService in_executor,
    final LogCreatableType in_log)
  {
    final InetSocketAddress addr = http.getAddress();

    final Server s = new Server(addr);
    @SuppressWarnings("resource") final ServerConnector c =
      new ServerConnector(s);
    c.setPort(addr.getPort());
    c.setReuseAddress(true);

    final ContextHandlerCollection ch =
      JFPServerCommands.getHandlers(
        in_config,
        in_database,
        in_executor,
        fossil_controller,
        remote_controller,
        in_log.with("http"));

    final HandlerCollection hs = new HandlerCollection();
    hs.addHandler(ch);
    hs.addHandler(request_log_handler);

    s.setHandler(hs);
    return s;
  }

  private static Server getHTTPSServer(
    final JFPFossilControllerType fossil_controller,
    final JFPRemoteControllerType remote_controller,
    final RequestLogHandler request_log_handler,
    final JFPServerHTTPSConfigType https,
    final JFPServerConfigType in_config,
    final JFPServerDatabaseType in_database,
    final ExecutorService in_executor,
    final LogUsableType in_log)
    throws JFPException
  {
    try {
      final InetSocketAddress addr = https.getAddress();

      /**
       * Load keystore.
       */

      final KeyStore ks = KeyStore.getInstance(https.getKeyStoreType());
      final File ks_path = https.getKeyStorePath().getCanonicalFile();
      final String ks_pass = https.getKeyStorePassword();
      final FileInputStream ks_stream = new FileInputStream(ks_path);
      ks.load(ks_stream, ks_pass.toCharArray());
      ks_stream.close();

      in_log.debug("keystore is " + ks_path);

      /**
       * Load truststore.
       */

      final KeyStore ts = KeyStore.getInstance(https.getTrustStoreType());
      final File ts_path = https.getTrustStorePath().getCanonicalFile();
      final String ts_pass = https.getTrustStorePassword();
      final FileInputStream ts_stream = new FileInputStream(ts_path);
      ts.load(ts_stream, ts_pass.toCharArray());
      ts_stream.close();

      in_log.debug("truststore is " + ts_path);

      /**
       * Initialise a KeyManagerFactory object to encapsulate the underlying
       * keystore.
       */

      final KeyManagerFactory kmf =
        KeyManagerFactory
          .getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(ks, ks_pass.toCharArray());

      /**
       * Initialise a TrustManagerFactory with the truststore.
       */

      final TrustManagerFactory tmf =
        TrustManagerFactory.getInstance("SunX509");
      tmf.init(ts);

      /**
       * Initialize SSL context.
       */

      final SSLContext sc = SSLContext.getInstance("TLS");
      final SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
      sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), sr);

      /**
       * Tell Jetty about the SSL context.
       */

      final SslContextFactory scf = new SslContextFactory();
      scf.setSslContext(sc);

      /**
       * Create the server.
       */

      final HttpConfiguration https_conf = new HttpConfiguration();
      https_conf.setSecurePort(addr.getPort());
      https_conf.setSecureScheme("https");
      https_conf.addCustomizer(new SecureRequestCustomizer());

      final Server s = new Server();
      @SuppressWarnings("resource") final ServerConnector sconn =
        new ServerConnector(
          s,
          new SslConnectionFactory(scf, "http/1.1"),
          new HttpConnectionFactory(https_conf));
      sconn.setHost(addr.getHostName());
      sconn.setReuseAddress(true);
      sconn.setPort(addr.getPort());

      final ServerConnector[] cs = new ServerConnector[1];
      cs[0] = sconn;

      final ContextHandlerCollection ch =
        JFPServerCommands.getHandlers(
          in_config,
          in_database,
          in_executor,
          fossil_controller,
          remote_controller,
          in_log.with("https"));

      final HandlerCollection hs = new HandlerCollection();
      hs.addHandler(ch);
      hs.addHandler(request_log_handler);

      s.setHandler(hs);
      s.setConnectors(cs);
      return s;
    } catch (final KeyStoreException e) {
      throw new JFPExceptionCrypto(e);
    } catch (final NoSuchAlgorithmException e) {
      throw new JFPExceptionCrypto(e);
    } catch (final CertificateException e) {
      throw new JFPExceptionCrypto(e);
    } catch (final UnrecoverableKeyException e) {
      throw new JFPExceptionCrypto(e);
    } catch (final KeyManagementException e) {
      throw new JFPExceptionCrypto(e);
    } catch (final NoSuchProviderException e) {
      throw new JFPExceptionCrypto(e);
    } catch (final IOException e) {
      throw new JFPExceptionIO(e);
    }
  }

  private static Server getManagementHTTPServer(
    final RequestLogHandler request_log_handler,
    final JFPServerAdminHTTPConfigType m_http,
    final JFPServerConfigType in_config,
    final JFPAdminDatabaseType in_database,
    final LogUsableType in_log)
  {
    final InetSocketAddress addr = m_http.getAddress();

    final Server s = new Server(addr);
    @SuppressWarnings("resource") final ServerConnector c =
      new ServerConnector(s);
    c.setPort(addr.getPort());
    c.setReuseAddress(true);

    final ContextHandlerCollection ch =
      JFPAdminCommands.getHandlers(
        in_config,
        in_database,
        in_log.with("management-http"));

    final HandlerCollection hs = new HandlerCollection();
    hs.addHandler(ch);
    hs.addHandler(request_log_handler);

    s.setHandler(hs);
    return s;
  }

  private static Server getManagementHTTPSServer(
    final RequestLogHandler request_log_handler,
    final JFPServerAdminHTTPSConfigType m_https,
    final JFPServerConfigType in_config,
    final JFPAdminDatabaseType in_database,
    final LogUsableType in_log)
    throws JFPException
  {
    try {
      final InetSocketAddress addr = m_https.getAddress();

      /**
       * Load keystore.
       */

      final KeyStore ks = KeyStore.getInstance(m_https.getKeyStoreType());
      final File ks_path = m_https.getKeyStorePath().getCanonicalFile();
      final String ks_pass = m_https.getKeyStorePassword();
      final FileInputStream ks_stream = new FileInputStream(ks_path);
      ks.load(ks_stream, ks_pass.toCharArray());
      ks_stream.close();

      in_log.debug("keystore is " + ks_path);

      /**
       * Load truststore.
       */

      final KeyStore ts = KeyStore.getInstance(m_https.getTrustStoreType());
      final File ts_path = m_https.getTrustStorePath().getCanonicalFile();
      final String ts_pass = m_https.getTrustStorePassword();
      final FileInputStream ts_stream = new FileInputStream(ts_path);
      ts.load(ts_stream, ts_pass.toCharArray());
      ts_stream.close();

      in_log.debug("truststore is " + ts_path);

      /**
       * Initialise a KeyManagerFactory object to encapsulate the underlying
       * keystore.
       */

      final KeyManagerFactory kmf =
        KeyManagerFactory
          .getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(ks, ks_pass.toCharArray());

      /**
       * Initialise a TrustManagerFactory with the truststore.
       */

      final TrustManagerFactory tmf =
        TrustManagerFactory.getInstance("SunX509");
      tmf.init(ts);

      /**
       * Initialize SSL context.
       */

      final SSLContext sc = SSLContext.getInstance("TLS");
      final SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
      sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), sr);

      /**
       * Tell Jetty about the SSL context.
       */

      final SslContextFactory scf = new SslContextFactory();
      scf.setSslContext(sc);

      /**
       * Create the server.
       */

      final HttpConfiguration https_conf = new HttpConfiguration();
      https_conf.setSecurePort(addr.getPort());
      https_conf.setSecureScheme("https");
      https_conf.addCustomizer(new SecureRequestCustomizer());

      final Server s = new Server();
      @SuppressWarnings("resource") final ServerConnector sconn =
        new ServerConnector(
          s,
          new SslConnectionFactory(scf, "http/1.1"),
          new HttpConnectionFactory(https_conf));
      sconn.setHost(addr.getHostName());
      sconn.setReuseAddress(true);
      sconn.setPort(addr.getPort());

      final ServerConnector[] cs = new ServerConnector[1];
      cs[0] = sconn;

      final ContextHandlerCollection ch =
        JFPAdminCommands.getHandlers(
          in_config,
          in_database,
          in_log.with("management-https"));

      final HandlerCollection hs = new HandlerCollection();
      hs.addHandler(ch);
      hs.addHandler(request_log_handler);

      s.setHandler(hs);
      s.setConnectors(cs);
      return s;
    } catch (final UnrecoverableKeyException e) {
      throw new JFPExceptionCrypto(e);
    } catch (final KeyManagementException e) {
      throw new JFPExceptionCrypto(e);
    } catch (final KeyStoreException e) {
      throw new JFPExceptionCrypto(e);
    } catch (final NoSuchAlgorithmException e) {
      throw new JFPExceptionCrypto(e);
    } catch (final CertificateException e) {
      throw new JFPExceptionCrypto(e);
    } catch (final NoSuchProviderException e) {
      throw new JFPExceptionCrypto(e);
    } catch (final IOException e) {
      throw new JFPExceptionIO(e);
    }
  }

  private static JFPErrorReporterType getReporter(
    final JFPServerConfigType config,
    final LogType log_fossil)
  {
    return config.getMailConfiguration().accept(
      new OptionVisitorType<JFPMailConfig, JFPErrorReporterType>() {
        @Override public JFPErrorReporterType none(
          final None<JFPMailConfig> n)
        {
          return JFPErrorReporterWithoutMail.newReporter(log_fossil
            .with("reporter"));
        }

        @Override public JFPErrorReporterType some(
          final Some<JFPMailConfig> s)
        {
          return JFPErrorReporterWithMail.newReporter(
            log_fossil.with("mail-reporter"),
            s.get());
        }
      });
  }

  private static JFPServerMain getServer(
    final JFPServerConfigType c,
    final JFPJettyLogger in_logger,
    final JFPAllDatabaseType in_database,
    final @Nullable JFPServerEventsType in_events)
    throws JFPException,
      IOException
  {
    System.setProperty(
      "org.eclipse.jetty.util.log.class",
      JFPJettyLogger.class.getCanonicalName());

    NullCheck.notNull(c, "Config");

    final File server_log_directory = c.getServerLogDirectory();
    if (server_log_directory.isDirectory() == false) {
      throw new FileNotFoundException(server_log_directory.toString());
    }

    final File server_repository_directory = c.getFossilRepositoryDirectory();
    if (server_repository_directory.isDirectory() == false) {
      throw new FileNotFoundException(server_repository_directory.toString());
    }

    final LogType in_log = in_logger.getMainLog();
    in_log.setLevel(c.getLogLevel());
    in_log.info(JFPVersion.getVersionString() + " starting");

    final JFPErrorReporterType reporter =
      JFPServerMain.getReporter(c, in_log);

    final ExecutorService in_executor = Executors.newCachedThreadPool();
    assert in_executor != null;

    final JFPFossilControllerType fossil_controller =
      JFPFossilController.newController(
        in_log.with("fossil-control"),
        in_executor,
        c,
        reporter);

    final JFPRemoteControllerType remote_controller =
      JFPRemoteController.newController(
        in_log.with("remote-control"),
        in_executor,
        reporter);

    final File directory_access =
      JFPServerMain.checkCreateLogAccessDirectory(server_log_directory);
    final RequestLogHandler request_log_handler =
      JFPServerMain.newRequestLog(directory_access);

    final OptionType<JFPServerHTTPConfigType> http_opt =
      c.getServerHTTPConfig();
    final OptionType<JFPServerHTTPSConfigType> https_opt =
      c.getServerHTTPSConfig();
    final OptionType<JFPServerAdminHTTPConfigType> m_http_opt =
      c.getServerManagementHTTPConfig();
    final OptionType<JFPServerAdminHTTPSConfigType> m_https_opt =
      c.getServerManagementHTTPSConfig();

    final OptionType<Server> in_http_server =
      http_opt.map(new FunctionType<JFPServerHTTPConfigType, Server>() {
        @Override public Server call(
          final JFPServerHTTPConfigType http)
        {
          return JFPServerMain.getHTTPServer(
            fossil_controller,
            remote_controller,
            request_log_handler,
            http,
            c,
            in_database,
            in_executor,
            in_log);
        }
      });

    final OptionType<Server> in_https_server =
      https_opt
        .mapPartial(new PartialFunctionType<JFPServerHTTPSConfigType, Server, JFPException>() {
          @Override public Server call(
            final JFPServerHTTPSConfigType https)
            throws JFPException
          {
            return JFPServerMain.getHTTPSServer(
              fossil_controller,
              remote_controller,
              request_log_handler,
              https,
              c,
              in_database,
              in_executor,
              in_log);
          }
        });

    final OptionType<Server> in_m_server =
      m_http_opt
        .map(new FunctionType<JFPServerAdminHTTPConfigType, Server>() {
          @Override public Server call(
            final JFPServerAdminHTTPConfigType m_http)
          {
            return JFPServerMain.getManagementHTTPServer(
              request_log_handler,
              m_http,
              c,
              in_database,
              in_log);
          }
        });

    final OptionType<Server> in_ms_server =
      m_https_opt
        .mapPartial(new PartialFunctionType<JFPServerAdminHTTPSConfigType, Server, JFPException>() {
          @Override public Server call(
            final JFPServerAdminHTTPSConfigType m_https)
            throws JFPException
          {
            return JFPServerMain.getManagementHTTPSServer(
              request_log_handler,
              m_https,
              c,
              in_database,
              in_log);
          }
        });

    OptionType<JFPMassSynchronizer> in_mass_sync;
    if (c.getServerMassSynchronizerEnabled()) {
      in_mass_sync =
        Option.some(new JFPMassSynchronizer(
          in_database,
          fossil_controller,
          in_executor,
          server_repository_directory,
          in_log.with("mass-sync")));
    } else {
      in_mass_sync = Option.none();
    }

    return new JFPServerMain(
      c,
      in_database,
      in_events,
      in_http_server,
      in_https_server,
      in_log,
      in_logger,
      in_m_server,
      in_ms_server,
      in_mass_sync,
      in_executor);
  }

  /**
   * Main function.
   *
   * @param args
   *          Command line arguments.
   */

  public static void main(
    final String[] args)
  {
    try {
      if (args.length < 1) {
        System.err.println("fatal: usage: server.conf");
        System.exit(1);
      }

      final File file =
        new File(NullCheck.notNull(args[0], "Config file name"));
      final JFPServerConfigType c =
        JFPServerConfigFromProperties.fromFile(file);
      final JFPServerMain s = JFPServerMain.newServer(c);
      s.run();

    } catch (final IOException e) {
      System.err.println("fatal: i/o error: " + e);
    } catch (final JPropertyException e) {
      System.err.println("fatal: configuration error: " + e.getMessage());
    } catch (final JFPExceptionConfigError e) {
      System.err.println("fatal: configuration error: " + e.getMessage());
    } catch (final JFPExceptionInvalidArgument e) {
      System.err.println("fatal: configuration error: " + e.getMessage());
    } catch (final AddressException e) {
      System.err
        .println("fatal: configuration error: invalid email address: "
          + e.getMessage());
    } catch (final JFPException e) {
      System.err.println("fatal: error: " + e.getMessage());
    }
  }

  private static RequestLogHandler newRequestLog(
    final File directory_access)
  {
    final File access_log_template =
      new File(directory_access, "yyyy_MM_dd.log");
    final RequestLogHandler request_log_handler = new RequestLogHandler();
    final NCSARequestLog request_log =
      new NCSARequestLog(access_log_template.getAbsolutePath());
    request_log.setRetainDays(31);
    request_log.setExtended(true);
    request_log.setAppend(true);
    request_log.setLogTimeZone("UTC");
    request_log_handler.setRequestLog(request_log);
    return request_log_handler;
  }

  /**
   * Create a new server with the given config.
   *
   * @param c
   *          The config.
   * @return A new server.
   * @throws IOException
   *           On I/O errors.
   * @throws JFPException
   *           On errors.
   */

  public static JFPServerMain newServer(
    final JFPServerConfigType c)
    throws JFPException,
      IOException
  {
    final JFPJettyLogger in_logger = new JFPJettyLogger();

    final File server_database_file = c.getServerDatabaseFile();
    final JFPAllDatabaseType in_database =
      JFPDatabase.open(in_logger.getMainLog(), server_database_file);

    return JFPServerMain.getServer(c, in_logger, in_database, null);
  }

  /**
   * Create a new server with the given config, calling the given function
   * when the server has started.
   *
   * @param c
   *          The config.
   * @param in_events
   *          An events interface to call when various events occur on the
   *          server.
   * @return A new server.
   * @throws IOException
   *           On I/O errors.
   * @throws JFPException
   *           On errors.
   */

  public static JFPServerMain newServerWithEvents(
    final JFPServerConfigType c,
    final JFPServerEventsType in_events)
    throws IOException,
      JFPException
  {
    NullCheck.notNull(c, "Configuration");

    final JFPJettyLogger in_logger = new JFPJettyLogger();

    final File server_database_file = c.getServerDatabaseFile();
    final JFPAllDatabaseType in_database =
      JFPDatabase.open(in_logger.getMainLog(), server_database_file);

    return JFPServerMain.getServer(
      c,
      in_logger,
      in_database,
      NullCheck.notNull(in_events, "Events"));
  }

  /**
   * Create a new server with the given config, calling the given function
   * when the server has started.
   *
   * @param c
   *          The config.
   * @param in_database
   *          The database.
   * @param in_events
   *          An events interface to call when various events occur on the
   *          server.
   * @return A new server.
   * @throws IOException
   *           On I/O errors.
   * @throws JFPException
   *           On errors.
   */

  public static JFPServerMain newServerWithEventsAndDatabase(
    final JFPServerConfigType c,
    final JFPAllDatabaseType in_database,
    final JFPServerEventsType in_events)
    throws IOException,
      JFPException
  {
    NullCheck.notNull(c, "Configuration");
    NullCheck.notNull(in_database, "Database");

    final JFPJettyLogger in_logger = new JFPJettyLogger();

    return JFPServerMain.getServer(
      c,
      in_logger,
      in_database,
      NullCheck.notNull(in_events, "Events"));
  }

  private final JFPServerConfigType             config;
  private final JFPAllDatabaseType              database;
  private final @Nullable JFPServerEventsType   events;
  private final ExecutorService                 executor;
  private final OptionType<Server>              http_server;
  private final OptionType<Server>              https_server;
  private final LogType                         log;
  private final JFPJettyLogger                  logger;
  private final OptionType<Server>              m_server;
  private final OptionType<JFPMassSynchronizer> mass_sync;
  private final OptionType<Server>              ms_server;
  private final AtomicBoolean                   want_stop;

  private JFPServerMain(
    final JFPServerConfigType in_config,
    final JFPAllDatabaseType in_database,
    final @Nullable JFPServerEventsType in_events,
    final OptionType<Server> in_http_server,
    final OptionType<Server> in_https_server,
    final LogType in_log,
    final JFPJettyLogger in_logger,
    final OptionType<Server> in_m_server,
    final OptionType<Server> in_ms_server,
    final OptionType<JFPMassSynchronizer> in_mass_sync,
    final ExecutorService in_executor)
  {
    this.config = in_config;
    this.database = in_database;
    this.events = in_events;
    this.http_server = in_http_server;
    this.https_server = in_https_server;
    this.log = in_log;
    this.logger = in_logger;
    this.m_server = in_m_server;
    this.ms_server = in_ms_server;
    this.mass_sync = in_mass_sync;
    this.executor = in_executor;
    this.want_stop = new AtomicBoolean(false);
  }

  @SuppressWarnings({ "boxing", "resource" }) @Override public void run()
  {
    final JFPServerEventsType ev = this.events;

    try {
      final LogType l = this.log;

      this.http_server
        .mapPartial(new PartialFunctionType<Server, Unit, Exception>() {
          @Override public Unit call(
            final Server server)
            throws Exception
          {
            final ServerConnector sc =
              (ServerConnector) server.getConnectors()[0];
            final String m =
              String.format(
                "starting http server on http://%s:%d",
                sc.getHost(),
                sc.getPort());
            assert m != null;
            l.info(m);

            server.start();
            return Unit.unit();
          }
        });

      this.https_server
        .mapPartial(new PartialFunctionType<Server, Unit, Exception>() {
          @Override public Unit call(
            final Server server)
            throws Exception
          {
            final ServerConnector sc =
              (ServerConnector) server.getConnectors()[0];
            final String m =
              String.format(
                "starting https server on https://%s:%d",
                sc.getHost(),
                sc.getPort());
            assert m != null;
            l.info(m);

            server.start();
            return Unit.unit();
          }
        });

      this.m_server
        .mapPartial(new PartialFunctionType<Server, Unit, Exception>() {
          @Override public Unit call(
            final Server server)
            throws Exception
          {
            final ServerConnector sc =
              (ServerConnector) server.getConnectors()[0];
            final String m =
              String.format(
                "starting management http server on http://%s:%d",
                sc.getHost(),
                sc.getPort());
            assert m != null;
            l.info(m);

            server.start();
            return Unit.unit();
          }
        });

      this.ms_server
        .mapPartial(new PartialFunctionType<Server, Unit, Exception>() {
          @Override public Unit call(
            final Server server)
            throws Exception
          {
            final ServerConnector sc =
              (ServerConnector) server.getConnectors()[0];
            final String m =
              String.format(
                "starting management https server on https://%s:%d",
                sc.getHost(),
                sc.getPort());
            assert m != null;
            l.info(m);

            server.start();
            return Unit.unit();
          }
        });

      this.mass_sync.map(new FunctionType<JFPMassSynchronizer, Unit>() {
        @Override public Unit call(
          final JFPMassSynchronizer s)
        {
          JFPServerMain.this.executor.execute(s);
          return Unit.unit();
        }
      });

      if (ev != null) {
        ev.serverStarted(this);
      }

      while (this.want_stop.get() == false) {
        Thread.sleep(1000);
      }

      this.stopAll();

    } catch (final Throwable e) {
      this.log.critical("shutting down: " + e + ": " + e.getMessage());

      if (ev != null) {
        if (e instanceof Exception) {
          ev.serverCrashed((Exception) e);
        } else {
          ev.serverCrashed(new Exception(e));
        }
      }

      try {
        this.stopAll();
      } catch (final Exception x) {
        this.log.critical("could not shut down cleanly: "
          + e
          + ": "
          + e.getMessage());
      }

    } finally {
      if (ev != null) {
        ev.serverStopped();
      }
    }
  }

  @Override public void stop()
  {
    this.want_stop.set(true);
  }

  private void stopAll()
    throws Exception
  {
    this.http_server
      .mapPartial(new PartialFunctionType<Server, Unit, Exception>() {
        @Override public Unit call(
          final Server server)
          throws Exception
        {
          server.stop();
          server.join();
          return Unit.unit();
        }
      });

    this.https_server
      .mapPartial(new PartialFunctionType<Server, Unit, Exception>() {
        @Override public Unit call(
          final Server server)
          throws Exception
        {
          server.stop();
          server.join();
          return Unit.unit();
        }
      });

    this.m_server
      .mapPartial(new PartialFunctionType<Server, Unit, Exception>() {
        @Override public Unit call(
          final Server server)
          throws Exception
        {
          server.stop();
          server.join();
          return Unit.unit();
        }
      });

    this.ms_server
      .mapPartial(new PartialFunctionType<Server, Unit, Exception>() {
        @Override public Unit call(
          final Server server)
          throws Exception
        {
          server.stop();
          server.join();
          return Unit.unit();
        }
      });

    this.mass_sync.map(new FunctionType<JFPMassSynchronizer, Unit>() {
      @Override public Unit call(
        final JFPMassSynchronizer s)
      {
        s.stop();
        return Unit.unit();
      }
    });
  }
}
