INSERT INTO vehicles (
    provider_id,
    name,
    type,
    description,
    status,
    current_latitude,
    current_longitude,
    price_per_unit,
    billing_model,
    max_duration_minutes,
    max_kilometers,
    min_age,
    max_persons
)
VALUES
(1, 'Tier E-Scooter',      'E_SCOOTER', 'Dortmund City vehicle',              'AVAILABLE', 51.5136, 7.4653,  8.00, 'PER_HOUR',      120,  30, 18, 1),
(1, 'Nextbike Bicycle',    'BICYCLE',   'City bicycle near Dortmund Hbf',     'AVAILABLE', 51.5142, 7.4628,  6.00, 'PER_HOUR',      240,  50, 16, 1),
(1, 'Lime E-Bike',         'E_BIKE',    'Electric bike for city travel',       'BOOKED',    51.5150, 7.4680, 12.00, 'PER_HOUR',      180,  60, 18, 1),
(2, 'ShareNow E-Car',      'E_CAR',     'Electric car for short trips',        'AVAILABLE', 51.5105, 7.4701,  0.45, 'PER_KILOMETER', 480, 250, 21, 4),
(2, 'Voi E-Scooter',       'E_SCOOTER', 'Compact e-scooter',                  'AVAILABLE', 51.5161, 7.4599,  9.00, 'PER_HOUR',      120,  25, 18, 1),
(2, 'Swapfiets Bicycle',   'BICYCLE',   'Comfort city bicycle',               'BOOKED',    51.5180, 7.4665,  7.00, 'PER_HOUR',      300,  70, 16, 1),
(3, 'Bolt E-Bike',         'E_BIKE',    'E-bike with removable battery',       'AVAILABLE', 51.5122, 7.4720,  0.20, 'PER_KILOMETER', 180,  50, 18, 1),
(3, 'Sixt E-Car',          'E_CAR',     'Electric car for up to five persons', 'AVAILABLE', 51.5098, 7.4612,  0.50, 'PER_KILOMETER', 720, 300, 21, 5);
