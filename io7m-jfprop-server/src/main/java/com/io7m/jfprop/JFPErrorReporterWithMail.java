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
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import com.io7m.jfunctional.None;
import com.io7m.jfunctional.OptionPartialVisitorType;
import com.io7m.jfunctional.Pair;
import com.io7m.jfunctional.Some;
import com.io7m.jfunctional.Unit;
import com.io7m.jlog.LogUsableType;
import com.io7m.jnull.NullCheck;

final class JFPErrorReporterWithMail
{
  public static JFPErrorReporterType newReporter(
    final LogUsableType log,
    final JFPMailConfig config)
  {
    return new JFPErrorReporterType() {
      @Override public void onExternalHTTPError(
        final URI uri,
        final int code,
        final String operation)
      {
        log.debug("sending report to " + config.getRecipient());

        final StringBuilder text = new StringBuilder();
        text
          .append("An error occurred whilst attempting to access an external URI.\n");
        text.append("\n");
        text.append("Operation: ");
        text.append(operation);
        text.append("\n");
        text.append("URI: ");
        text.append(uri);
        text.append("\n");
        text.append("Code: ");
        text.append(code);
        text.append("\n");

        JFPErrorReporterWithMail.sendMail(log, config, text);
      }

      @Override public void onExternalProgramError(
        final StringBuilder output,
        final List<String> args,
        final int code)
      {
        final String out = output.toString();
        log.error(out.trim());
        log.debug("sending report to " + config.getRecipient());

        final StringBuilder text = new StringBuilder();
        text.append("An error occurred whilst attempting to sync.\n");
        text.append("\n");
        text.append("Command: ");

        for (final String a : args) {
          text.append(a);
          text.append(" ");
        }
        text.append("\n");
        text.append("Exit code: ");
        text.append(code);
        text.append("\n");
        text.append("\n");
        text.append(out);
        text.append("\n");

        JFPErrorReporterWithMail.sendMail(log, config, text);
      }
    };
  }

  private static void sendMail(
    final LogUsableType log,
    final JFPMailConfig config,
    final StringBuilder text)
  {
    final Properties props = new Properties();
    props.put("mail.smtp.host", config.getServer());
    final Session session = Session.getInstance(props, null);

    try {
      final MimeMessage msg = new MimeMessage(session);
      msg.setFrom(config.getSender());

      final Address[] recipients = new Address[1];
      recipients[0] = config.getRecipient();

      msg.setRecipients(Message.RecipientType.TO, recipients);
      msg.setSubject("jfprop error report");
      msg.setSentDate(new Date());
      msg.setText(text.toString());

      config
        .getServerAuthentication()
        .acceptPartial(
          new OptionPartialVisitorType<Pair<String, String>, Unit, MessagingException>() {
            @Override public Unit none(
              final None<Pair<String, String>> n)
              throws MessagingException
            {
              Transport.send(msg, recipients);
              return Unit.unit();
            }

            @Override public Unit some(
              final Some<Pair<String, String>> s)
              throws MessagingException
            {
              final Pair<String, String> p = s.get();
              Transport.send(msg, recipients, p.getLeft(), p.getRight());
              return Unit.unit();
            }
          });

    } catch (final MessagingException mex) {
      log.critical("could not send mail: " + mex);
    }
  }

  private final JFPMailConfig config;

  private JFPErrorReporterWithMail(
    final JFPMailConfig in_config)
  {
    this.config = NullCheck.notNull(in_config, "Configuration");
  }
}
