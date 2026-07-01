package edu.rutmiit.demo.notificationservice.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import edu.rutmiit.demo.cinemaeventscontract.TicketEvent;
import edu.rutmiit.demo.cinemaeventscontract.WaitlistEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${cinema.notifications.from:no-reply@cinema.local}")
    private String from;

    public void sendTicketCreated(TicketEvent.Created event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(event.customerEmail());
            helper.setSubject("Ваш билет создан");
            helper.setText(buildTicketHtml(event), true);
            helper.addInline("ticketQr", new ByteArrayResource(generateQrPng(event.qrCode())), "image/png");

            mailSender.send(message);
            log.info("ticket email sent with QR: to={} ticketId={} bookingId={}",
                    event.customerEmail(), event.ticketId(), event.bookingId());
        } catch (MessagingException | IOException | WriterException e) {
            log.error("failed to send ticket email with QR: to={} ticketId={} reason={}",
                    event.customerEmail(), event.ticketId(), e.getMessage(), e);
            throw new IllegalStateException("Could not send ticket email", e);
        }
    }

    public void sendWaitlistUserNotified(WaitlistEvent.UserNotified event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(event.customerEmail());
            helper.setSubject("Место снова доступно");
            String seatText = event.rowNumber() == null || event.seatNumber() == null
                    ? "появились свободные места"
                    : "ряд %d, место %d снова доступно".formatted(event.rowNumber(), event.seatNumber());
            helper.setText("""
                    Место по вашей подписке стало доступно.

                    Фильм: %s
                    Сеанс: %s
                    Зал: %s
                    Место: %s

                    Важно: waitlist не резервирует место. Билет может купить любой пользователь.

                    Это тестовое письмо из notification-service.
                    """.formatted(
                    event.movieTitle(),
                    event.showStartTime(),
                    event.hallName(),
                    seatText
            ));

            mailSender.send(message);
            log.info("waitlist email sent: to={} waitlistEntryId={} showId={} seatId={}",
                    event.customerEmail(), event.waitlistEntryId(), event.showId(), event.seatId());
        } catch (MessagingException e) {
            log.error("failed to send waitlist email: to={} waitlistEntryId={} reason={}",
                    event.customerEmail(), event.waitlistEntryId(), e.getMessage(), e);
            throw new IllegalStateException("Could not send waitlist email", e);
        }
    }

    private String buildTicketHtml(TicketEvent.Created event) {
        return """
                <!doctype html>
                <html lang="ru">
                <body style="font-family: Arial, sans-serif; color:#1f2933; line-height:1.45;">
                  <h2 style="margin:0 0 12px;">Ваш билет создан</h2>
                  <p>Покажите QR-код на входе в зал.</p>
                  <table cellpadding="4" cellspacing="0" style="margin:12px 0;">
                    <tr><td><b>Номер билета</b></td><td>%s</td></tr>
                    <tr><td><b>Фильм</b></td><td>%s</td></tr>
                    <tr><td><b>Жанр</b></td><td>%s</td></tr>
                    <tr><td><b>Длительность</b></td><td>%d мин.</td></tr>
                    <tr><td><b>Сеанс</b></td><td>%s — %s</td></tr>
                    <tr><td><b>Зал</b></td><td>%s</td></tr>
                    <tr><td><b>Место</b></td><td>ряд %d, место %d</td></tr>
                    <tr><td><b>Цена к оплате</b></td><td>%s %s</td></tr>
                  </table>
                  <p><img src="cid:ticketQr" alt="QR-код билета" width="220" height="220" /></p>
                  <p style="font-size:12px;color:#6b7280;">QR payload: %s</p>
                </body>
                </html>
                """.formatted(
                event.ticketNumber(),
                event.movieTitle(),
                event.movieGenre(),
                event.movieDurationMinutes(),
                event.showStartTime(),
                event.showEndTime(),
                event.hallName(),
                event.rowNumber(),
                event.seatNumber(),
                event.finalPrice(),
                event.currency(),
                event.qrCode()
        );
    }

    private byte[] generateQrPng(String payload) throws WriterException, IOException {
        String safePayload = payload == null || payload.isBlank() ? "EMPTY-CINEMA-TICKET" : payload;
        BitMatrix matrix = new QRCodeWriter().encode(
                safePayload,
                BarcodeFormat.QR_CODE,
                240,
                240,
                Map.of(EncodeHintType.MARGIN, 1)
        );
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", output);
        return output.toByteArray();
    }
}
