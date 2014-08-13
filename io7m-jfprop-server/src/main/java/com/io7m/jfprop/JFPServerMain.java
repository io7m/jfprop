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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.mail.internet.AddressException;

import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;

import com.io7m.jfunctional.None;
import com.io7m.jfunctional.Option;
import com.io7m.jfunctional.OptionType;
import com.io7m.jfunctional.OptionVisitorType;
import com.io7m.jfunctional.PartialFunctionType;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jproperties.JPropertyException;

/**
 * Main server.
 */

public final class JFPServerMain implements Runnable, JFPServerControlType
{
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
    }
  }

  /**
   * Create a new server with the given config.
   *
   * @param c
   *          The config.
   * @return A new server.
   * @throws IOException
   *           On I/O errors.
   * @throws JFPExceptionConfigError
   *           On configuration errors.
   */

  public static JFPServerMain newServer(
    final JFPServerConfigType c)
    throws JFPExceptionConfigError,
      IOException
  {
    return new JFPServerMain(c, null);
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
   * @throws JFPExceptionConfigError
   *           On configuration errors.
   */

  public static JFPServerMain newServer(
    final JFPServerConfigType c,
    final JFPServerEventsType in_events)
    throws JFPExceptionConfigError,
      IOException
  {
    return new JFPServerMain(c, NullCheck.notNull(in_events, "Events"));
  }

  private final JFPServerConfigType           config;
  private final JFPAllDatabaseType            database;
  private final @Nullable ServerConnector     http_connector;
  private final OptionType<Server>            http_server;
  private final OptionType<Server>            https_server;
  private final LogType                       log;
  private final JFPJettyLogger                logger;
  private final @Nullable ServerConnector     m_connector;
  private final OptionType<Server>            m_server;
  private final @Nullable JFPServerEventsType events;
  private final AtomicBoolean                 want_stop;

  private JFPServerMain(
    final JFPServerConfigType in_config,
    final @Nullable JFPServerEventsType in_events)
    throws JFPExceptionConfigError,
      IOException
  {
    System.setProperty(
      "org.eclipse.jetty.util.log.class",
      JFPJettyLogger.class.getCanonicalName());

    this.config = NullCheck.notNull(in_config, "Config");
    this.events = in_events;
    this.want_stop = new AtomicBoolean(false);

    final File server_log_directory = this.config.getServerLogDirectory();

    if (server_log_directory.isDirectory() == false) {
      throw new FileNotFoundException(server_log_directory.toString());
    }

    final File server_repository_directory =
      this.config.getFossilRepositoryDirectory();
    if (server_repository_directory.isDirectory() == false) {
      throw new FileNotFoundException(server_repository_directory.toString());
    }

    this.logger = new JFPJettyLogger();
    this.log = this.logger.getMainLog();
    this.log.setLevel(in_config.getLogLevel());

    this.log.info(JFPVersion.getVersionString() + " starting");

    final LogType log_fossil = this.log.with("fossil");

    final JFPErrorReporterType reporter =
      JFPServerMain.getReporter(in_config, this.log);

    final ExecutorService executor = Executors.newCachedThreadPool();
    final JFPFossilControllerType fossil_controller =
      JFPFossilController.newController(
        log_fossil.with("control"),
        executor,
        in_config,
        reporter);

    final JFPRemoteControllerType remote_controller =
      JFPRemoteController.newController(
        this.log.with("remote-control"),
        executor,
        reporter);

    final File server_database_file = this.config.getServerDatabaseFile();
    this.database = JFPDatabase.open(this.log, server_database_file);

    final File directory_access = new File(server_log_directory, "access");
    if (directory_access.isDirectory() == false) {
      if (directory_access.mkdir() == false) {
        throw new IOException("could not create " + directory_access);
      }
    }

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

    final OptionType<InetSocketAddress> http_addr_opt =
      this.config.getServerHTTPAddress();
    final OptionType<InetSocketAddress> m_addr_opt =
      this.config.getServerManagementAddress();

    if (http_addr_opt.isSome()) {
      final Some<InetSocketAddress> some =
        (Some<InetSocketAddress>) http_addr_opt;
      final InetSocketAddress addr = some.get();

      final Server s = new Server(addr);
      final ServerConnector c = new ServerConnector(s);
      c.setPort(addr.getPort());
      c.setReuseAddress(true);

      final ContextHandlerCollection ch =
        JFPServerCommands.getHandlers(
          this.config,
          this.database,
          executor,
          fossil_controller,
          remote_controller,
          this.log.with("http"));

      final HandlerCollection hs = new HandlerCollection();
      hs.addHandler(ch);
      hs.addHandler(request_log_handler);

      s.setHandler(hs);
      this.http_connector = c;
      this.http_server = Option.some(s);
    } else {
      this.http_server = Option.none();
      this.http_connector = null;
    }

    if (m_addr_opt.isSome()) {
      final Some<InetSocketAddress> some =
        (Some<InetSocketAddress>) m_addr_opt;
      final InetSocketAddress addr = some.get();

      final Server s = new Server(addr);
      final ServerConnector c = new ServerConnector(s);
      c.setPort(addr.getPort());
      c.setReuseAddress(true);

      final ContextHandlerCollection ch =
        JFPAdminCommands.getHandlers(this.config, this.database, this.log);

      final HandlerCollection hs = new HandlerCollection();
      hs.addHandler(ch);
      hs.addHandler(request_log_handler);

      s.setHandler(hs);
      this.m_connector = c;
      this.m_server = Option.some(s);
    } else {
      this.m_connector = null;
      this.m_server = Option.none();
    }

    this.https_server = Option.none();

    if (this.http_server.isNone()
      && this.https_server.isNone()
      && this.m_server.isNone()) {
      throw new JFPExceptionConfigError(
        "HTTP, HTTPS, and management servers disabled; nothing to do!");
    }
  }

  @Override public void run()
  {
    final JFPServerEventsType ev = this.events;

    try {
      final LogType l = this.log;
      final JFPServerConfigType c = this.config;

      this.http_server
        .mapPartial(new PartialFunctionType<Server, Unit, Exception>() {
          @Override public Unit call(
            final Server server)
            throws Exception
          {
            l.info(String.format(
              "starting http server on %s",
              c.getServerHTTPAddress()));
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
            l.info(String.format(
              "starting management server on %s",
              c.getServerManagementAddress()));
            server.start();
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
  }
}
