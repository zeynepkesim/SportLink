package com.sportlink.booking.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.sportlink.booking.dto.BookingDto;
import com.sportlink.booking.dto.CreateBookingRequest;
import com.sportlink.booking.dto.FacilityDto;
import com.sportlink.booking.dto.MoneyDto;
import com.sportlink.booking.dto.PaymentDto;
import com.sportlink.booking.dto.PaymentRequestDto;
import com.sportlink.booking.model.Booking;
import com.sportlink.booking.model.BookingStatus;
import com.sportlink.booking.model.Money;
import com.sportlink.booking.model.Payment;
import com.sportlink.booking.model.PaymentStatus;
import com.sportlink.booking.repository.BookingRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${services.scheduling.url:http://localhost:8085}")
    private String schedulingServiceUrl;

    @Value("${services.search.url:http://localhost:8086}")
    private String searchServiceUrl;

    public BookingDto createBooking(CreateBookingRequest request) {

        // Synchronous call to Scheduling Service to reserve the time slot.
        // If the slot can't be reserved, the booking should not be created.
        try {
            webClientBuilder
                .build()
                .post()
                .uri(schedulingServiceUrl + "/api/timeslots/reserve")
                .bodyValue(new SlotReservationRequest(request.getSportCenterId(), request.getTimeSlotId()))
                .retrieve()
                .bodyToMono(Object.class)
                .block();
        } catch (Exception e) {
            log.error("Failed to reserve time slot {} via Scheduling Service: {}",
                    request.getTimeSlotId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not reserve time slot via Scheduling Service");
        }

        // Build the embedded Money + Payment + Booking aggregate.
        PaymentRequestDto pr = request.getPayment();
        Money money = Money.builder()
                .amount(pr != null ? pr.getAmount() : null)
                .currency(pr != null ? pr.getCurrency() : null)
                .build();

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .paymentMethod(pr != null ? pr.getPaymentMethod() : null)
                .paymentStatus(PaymentStatus.PENDING)
                .money(money)
                .build();

        Booking booking = Booking.builder()
                .bookingId(UUID.randomUUID().toString())
                .sportCenterId(request.getSportCenterId())
                .activityId(request.getActivityId())
                .timeSlotId(request.getTimeSlotId())
                .bookingStatus(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .payment(payment)
                .build();

        bookingRepository.save(booking);
        log.info("Booking {} created with status PENDING", booking.getBookingId());

        // TODO (Checkpoint 3): publish BookingCreated event asynchronously.
        return mapToBookingDto(booking);
    }

    public Optional<BookingDto> getBooking(String bookingId) {
        return bookingRepository.findById(bookingId).map(this::mapToBookingDto);
    }

    public List<BookingDto> listBookings(String activityId, String sportCenterId, BookingStatus bookingStatus) {
        List<Booking> bookings;

        boolean hasActivity = activityId != null && !activityId.isBlank();
        boolean hasCenter = sportCenterId != null && !sportCenterId.isBlank();
        boolean hasStatus = bookingStatus != null;

        if (hasActivity && hasCenter && hasStatus) {
            bookings = bookingRepository.findByActivityIdAndSportCenterIdAndBookingStatus(
                    activityId, sportCenterId, bookingStatus);
        } else if (hasActivity && hasCenter) {
            bookings = bookingRepository.findByActivityIdAndSportCenterId(activityId, sportCenterId);
        } else if (hasActivity && hasStatus) {
            bookings = bookingRepository.findByActivityIdAndBookingStatus(activityId, bookingStatus);
        } else if (hasCenter && hasStatus) {
            bookings = bookingRepository.findBySportCenterIdAndBookingStatus(sportCenterId, bookingStatus);
        } else if (hasActivity) {
            bookings = bookingRepository.findByActivityId(activityId);
        } else if (hasCenter) {
            bookings = bookingRepository.findBySportCenterId(sportCenterId);
        } else if (hasStatus) {
            bookings = bookingRepository.findByBookingStatus(bookingStatus);
        } else {
            bookings = new ArrayList<>();
            bookingRepository.findAll().forEach(bookings::add);
        }

        return bookings.stream().map(this::mapToBookingDto).toList();
    }

    public BookingDto confirmBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking " + bookingId + " not found"));

        // only PENDING bookings can be confirmed.
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot confirm booking with status " + booking.getBookingStatus());
        }

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        if (booking.getPayment() != null) {
            booking.getPayment().setPaymentStatus(PaymentStatus.PAID);
            booking.getPayment().setPaidAt(LocalDateTime.now());
        }
        bookingRepository.save(booking);
        log.info("Booking {} confirmed and payment marked as PAID", bookingId);

        // TODO (Checkpoint 3): publish BookingConfirmed event.
        return mapToBookingDto(booking);
    }

    public BookingDto cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking " + bookingId + " not found"));

        // cannot cancel an already-cancelled booking.
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Booking is already cancelled");
        }

        // Synchronous call to Scheduling Service to release the time slot.
        try {
            webClientBuilder
                .build()
                .delete()
                .uri(schedulingServiceUrl + "/api/timeslots/{timeSlotId}/release", booking.getTimeSlotId())
                .retrieve()
                .bodyToMono(Object.class)
                .block();
        } catch (Exception e) {
            log.error("Failed to release time slot {} via Scheduling Service: {}",
                    booking.getTimeSlotId(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not release time slot via Scheduling Service");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking {} cancelled", bookingId);

        // TODO (Checkpoint 3): publish BookingCancelled event.
        return mapToBookingDto(booking);
    }


    public BookingDto refundBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Booking " + bookingId + " not found"));

        // only refundable when CANCELLED + payment was PAID.
        if (booking.getBookingStatus() != BookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Refund only valid when bookingStatus is CANCELLED");
        }
        if (booking.getPayment() == null
                || booking.getPayment().getPaymentStatus() != PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Refund only valid when paymentStatus is PAID");
        }

        booking.getPayment().setPaymentStatus(PaymentStatus.REFUNDED);
        booking.getPayment().setRefundedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        log.info("Booking {} refunded", bookingId);

        // TODO (Checkpoint 3): publish PaymentRefunded event.
        return mapToBookingDto(booking);
    }

    // ----------------------------------------------------------------------
    // GET /api/bookings/facilities/search - Search bookable facilities
    // Delegates synchronously to Search Service.
    // ----------------------------------------------------------------------
    public List<FacilityDto> searchFacilities(String sportType, String location, String timeSlotId) {
        // Build the query string manually so we can use the same simple .uri(String) pattern
        // shown in the reference repo (M-Gharib/ESI-W4 product-service).
        StringBuilder url = new StringBuilder(searchServiceUrl)
                .append("/api/v1/search?entityType=FACILITY");
        if (sportType != null && !sportType.isBlank()) {
            url.append("&sportType=").append(sportType);
        }
        if (location != null && !location.isBlank()) {
            url.append("&location=").append(location);
        }
        if (timeSlotId != null && !timeSlotId.isBlank()) {
            url.append("&timeSlotId=").append(timeSlotId);
        }

        try {
            List<FacilityDto> facilities = webClientBuilder
                .build()
                .get()
                .uri(url.toString())
                .retrieve()
                .bodyToFlux(FacilityDto.class)
                .collectList()
                .block();

            return facilities != null ? facilities : new ArrayList<>();
        } catch (Exception e) {
            log.error("Facility search via Search Service failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Search Service unavailable");
        }
    }

    // Helpers
    private BookingDto mapToBookingDto(Booking booking) {
        return BookingDto.builder()
                .bookingId(booking.getBookingId())
                .sportCenterId(booking.getSportCenterId())
                .activityId(booking.getActivityId())
                .timeSlotId(booking.getTimeSlotId())
                .bookingStatus(booking.getBookingStatus())
                .createdAt(booking.getCreatedAt())
                .payment(mapToPaymentDto(booking.getPayment()))
                .build();
    }

    private PaymentDto mapToPaymentDto(Payment payment) {
        if (payment == null) {
            return null;
        }
        MoneyDto moneyDto = null;
        if (payment.getMoney() != null) {
            moneyDto = MoneyDto.builder()
                    .amount(payment.getMoney().getAmount())
                    .currency(payment.getMoney().getCurrency())
                    .build();
        }
        return PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paidAt(payment.getPaidAt())
                .refundedAt(payment.getRefundedAt())
                .money(moneyDto)
                .build();
    }

    // small inner record for the body sent to Scheduling Service /reserve
    private record SlotReservationRequest(String facilityId, String timeSlotId) {}
}
