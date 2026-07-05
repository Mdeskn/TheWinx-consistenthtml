package com.winx.booking.api;

import com.winx.booking.application.BookingService;
import com.winx.booking.domain.Booking;
import com.winx.booking.domain.vo.RideLocation;
import com.winx.booking.domain.vo.RideSummary;
import com.winx.booking.domain.vo.VehicleSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookingService service;

    private Booking sampleBooking() {
        VehicleSnapshot snapshot = new VehicleSnapshot(1L, null, "E_SCOOTER", new BigDecimal("0.20"), "PER_HOUR");
        return Booking.start(1L, snapshot, new RideLocation(52.52, 13.40), "CARD");
    }

    @Test
    void createBookingReturns201() throws Exception {
        when(service.createBooking(eq("tok-123"), any())).thenReturn(sampleBooking());

        mvc.perform(post("/bookings")
                        .header("X-Auth-Token", "tok-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vehicleId\":1,\"startLatitude\":52.52,\"startLongitude\":13.40}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.vehicleId").value(1));
    }

    @Test
    void cancelBookingReturnsCancelled() throws Exception {
        Booking booking = sampleBooking();
        booking.cancel();
        when(service.cancelBooking(5L)).thenReturn(booking);

        mvc.perform(post("/bookings/5/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void endBookingReturnsCompleted() throws Exception {
        Booking booking = sampleBooking();
        booking.complete(new RideLocation(52.50, 13.41),
                new RideSummary(3.2, new BigDecimal("4.50")), LocalDateTime.now());
        when(service.endBooking(eq(7L), any())).thenReturn(booking);

        mvc.perform(post("/bookings/7/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"endLatitude\":52.50,\"endLongitude\":13.41,\"distanceKm\":3.2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.vehicleId").value(1));
    }
}
