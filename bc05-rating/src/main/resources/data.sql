-- Sample ratings for BC-05 (Instant Mobility Platform)
-- Runs after Hibernate creates the schema (defer-datasource-initialization: true)
INSERT INTO ratings (user_id, created_at, vehicle_score, provider_score, comment, vehicle_id, provider_id, booking_id)
VALUES
  (1, '2026-06-01 10:00:00', 5, 4, 'Great e-scooter, very smooth ride!',       10, 100, 1001),
  (2, '2026-06-02 14:30:00', 3, 5, 'Bike was fine, provider was excellent.',    11, 100, 1002),
  (3, '2026-06-03 09:15:00', 4, 4, 'Good experience overall.',                  12, 101, 1003),
  (1, '2026-06-04 18:45:00', 5, 5, 'Perfect! Would book again.',                13, 102, 1004),
  (4, '2026-06-05 12:00:00', 2, 3, 'Car had minor issues, provider was helpful.',14, 103, 1005);
