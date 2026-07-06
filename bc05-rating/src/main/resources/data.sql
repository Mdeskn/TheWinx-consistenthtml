-- Seeded ratings referencing real BC-02 vehicle IDs (1-8) and BC-01 provider IDs (1-3)
-- booking_ids use 9001+ to avoid conflicts with live bookings created during the demo
INSERT INTO ratings (user_id, created_at, vehicle_score, provider_score, comment, vehicle_id, provider_id, booking_id)
VALUES
  (4, '2026-06-01 10:15:00', 5, 4, 'Super smooth ride, battery lasted the whole trip!',    1, 1, 9001),
  (4, '2026-06-02 14:30:00', 4, 5, 'Bicycle was spotless. Tier support was very helpful.',  2, 1, 9002),
  (4, '2026-06-03 09:20:00', 5, 5, 'ShareNow e-car felt brand new. Highly recommend.',     4, 2, 9003),
  (4, '2026-06-04 17:45:00', 3, 4, 'Voi scooter had a slightly low tyre but still rode ok.',5, 2, 9004),
  (4, '2026-06-05 11:00:00', 5, 5, 'Bolt e-bike is the best in the city!',                 7, 3, 9005),
  (4, '2026-06-06 08:30:00', 4, 3, 'Sixt e-car was clean but charging was slow.',          8, 3, 9006),
  (4, '2026-06-07 16:00:00', 2, 4, 'Lime e-bike had a brake issue. Provider responded fast.',3, 1, 9007),
  (4, '2026-06-08 13:15:00', 5, 5, 'Swapfiets bicycle — smoothest ride I have had!',       6, 2, 9008);
