-- Test demand data (15 real demand records)
-- For testing flyer order control functionality

INSERT INTO `demand` (
    `farmer_id`, `flyer_id`, `land_name`, `land_boundary`, `crop_type`, `pest_type`, 
    `land_location`, `land_area`, `expected_time`, `budget`, `status`, `order_type`
) VALUES
-- Farmer 1 demands
(1, NULL, 'Zhangjiawan Wheat Field', '39.9042,116.4074,39.9142,116.4174,39.9142,116.4074,39.9042,116.4074', 'Wheat', 'Aphid', 'Chaoyang District, Beijing', 50, '2025-01-15 08:00:00', 1500.00, 0, 1),
(1, NULL, 'Lijiapo Corn Field', '39.9242,116.4274,39.9342,116.4374,39.9342,116.4274,39.9242,116.4274', 'Corn', 'Corn Borer', 'Chaoyang District, Beijing', 30, '2025-01-16 14:00:00', 900.00, 0, 1),

-- Farmer 3 demands
(3, NULL, 'Wang Family Orchard', '39.8942,116.3974,39.9042,116.4074,39.9042,116.3974,39.8942,116.3974', 'Apple', 'Red Spider Mite', 'Haidian District, Beijing', 20, '2025-01-17 09:00:00', 1200.00, 0, 1),
(3, NULL, 'Zhao Family Vegetable Base', '39.8842,116.3874,39.8942,116.3974,39.8942,116.3874,39.8842,116.3874', 'Cucumber', 'Downy Mildew', 'Haidian District, Beijing', 15, '2025-01-18 10:00:00', 750.00, 0, 1),

-- Farmer 6 demands
(6, NULL, 'Cai Family Rice Field', '39.9342,116.4374,39.9442,116.4474,39.9442,116.4374,39.9342,116.4374', 'Rice', 'Rice Planthopper', 'Tongzhou District, Beijing', 40, '2025-01-19 08:30:00', 1600.00, 0, 1),
(6, NULL, 'Tianyou Orchard', '39.9442,116.4474,39.9542,116.4574,39.9542,116.4474,39.9442,116.4474', 'Pear', 'Pear Psylla', 'Tongzhou District, Beijing', 25, '2025-01-20 15:00:00', 1000.00, 0, 1),
(6, NULL, 'Vegetable Greenhouse Inspection', '39.9542,116.4574,39.9642,116.4674,39.9642,116.4574,39.9542,116.4574', 'Vegetable', NULL, 'Tongzhou District, Beijing', 10, '2025-01-21 09:30:00', 800.00, 0, 2),

-- Farmer 7 demands
(7, NULL, 'Fangjia Rice Field', '39.8742,116.3774,39.8842,116.3874,39.8842,116.3774,39.8742,116.3774', 'Rice', 'Sheath Blight', 'Shunyi District, Beijing', 35, '2025-01-22 14:30:00', 1400.00, 0, 1),
(7, NULL, 'Orchard Pest Inspection', '39.8642,116.3674,39.8742,116.3774,39.8742,116.3674,39.8642,116.3674', 'Orange', NULL, 'Shunyi District, Beijing', 18, '2025-01-23 10:30:00', 900.00, 0, 2),

-- Farmer 9 demands
(9, NULL, 'HZH Wheat Field', '39.9642,116.4674,39.9742,116.4774,39.9742,116.4674,39.9642,116.4674', 'Wheat', 'Powdery Mildew', 'Changping District, Beijing', 60, '2025-01-24 08:00:00', 1800.00, 0, 1),
(9, NULL, 'Corn Field Inspection', '39.9742,116.4774,39.9842,116.4874,39.9842,116.4774,39.9742,116.4774', 'Corn', NULL, 'Changping District, Beijing', 22, '2025-01-25 16:00:00', 1100.00, 0, 2),
(9, NULL, 'Vegetable Base Spraying', '39.9842,116.4874,39.9942,116.4974,39.9942,116.4874,39.9842,116.4874', 'Tomato', 'Late Blight', 'Changping District, Beijing', 28, '2025-01-26 09:00:00', 1260.00, 0, 1),

-- Mixed demands
(1, NULL, 'Spring Wheat Pest Control', '39.8542,116.3574,39.8642,116.3674,39.8642,116.3574,39.8542,116.3574', 'Wheat', 'Rust', 'Daxing District, Beijing', 55, '2025-01-27 13:00:00', 1650.00, 0, 1),
(3, NULL, 'Orchard Spring Inspection', '39.8442,116.3474,39.8542,116.3574,39.8542,116.3474,39.8442,116.3474', 'Apple', NULL, 'Daxing District, Beijing', 17, '2025-01-28 11:00:00', 850.00, 0, 2),
(6, NULL, 'Rice Field Pest Control', '39.8342,116.3374,39.8442,116.3474,39.8442,116.3374,39.8342,116.3374', 'Rice', 'Striped Stem Borer', 'Daxing District, Beijing', 45, '2025-01-29 15:30:00', 1800.00, 0, 1);
