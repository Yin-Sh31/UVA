-- Test data script - users, flyers, owners and demands

-- Insert farmer users (10)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `role_type`, `balance`, `planting_type`) VALUES
('farmer001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Zhang San', '13800138001', 1, 5000.00, 'rice,wheat'),
('farmer002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Li Si', '13800138002', 1, 8000.00, 'corn,soybean'),
('farmer003', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Wang Wu', '13800138003', 1, 3000.00, 'vegetable,fruit'),
('farmer004', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Zhao Liu', '13800138004', 1, 10000.00, 'cotton,peanut'),
('farmer005', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Qian Qi', '13800138005', 1, 6000.00, 'tea,tobacco'),
('farmer006', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Sun Ba', '13800138006', 1, 4000.00, 'rice,corn'),
('farmer007', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Zhou Jiu', '13800138007', 1, 7000.00, 'wheat,vegetable'),
('farmer008', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Wu Shi', '13800138008', 1, 2000.00, 'fruit,tea'),
('farmer009', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Zheng Shiyi', '13800138009', 1, 9000.00, 'soybean,cotton'),
('farmer010', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Wang Shi''er', '13800138010', 1, 5000.00, 'tobacco,rice');

-- Insert flyer users (5)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `role_type`, `audit_status`, `balance`) VALUES
('flyer001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Zhang Feishou', '13900139001', 2, 1, 0.00),
('flyer002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Li Feishou', '13900139002', 2, 1, 0.00),
('flyer003', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Wang Feishou', '13900139003', 2, 1, 0.00),
('flyer004', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Zhao Feishou', '13900139004', 2, 1, 0.00),
('flyer005', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Qian Feishou', '13900139005', 2, 1, 0.00);

-- Insert owner users (2)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `role_type`, `audit_status`, `balance`) VALUES
('owner001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Liu Jizhu', '13700137001', 3, 1, 0.00),
('owner002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Chen Jizhu', '13700137002', 3, 1, 0.00);

-- Insert flyer info
INSERT INTO `user_flyer` (`user_id`, `user_name`, `license_type`, `skill_level`, `price_per_acre`, `is_free`, `audit_status`) VALUES
(11, 'Zhang Feishou', 'AOPA', 'All Skill', 120.00, 1, 1),
(12, 'Li Feishou', 'CAAC', 'Spray', 100.00, 1, 1),
(13, 'Wang Feishou', 'AOPA', 'Inspection', 80.00, 1, 1),
(14, 'Zhao Feishou', 'CAAC', 'All Skill', 150.00, 0, 1),
(15, 'Qian Feishou', 'AOPA', 'Spray', 110.00, 1, 1);

-- Insert owner info
INSERT INTO `user_owner` (`user_id`, `real_name`, `device_total`, `available_device_count`, `credit_score`, `audit_status`) VALUES
(16, 'Liu Jizhu', 5, 4, 95, 1),
(17, 'Chen Jizhu', 3, 3, 90, 1);

-- Insert drone devices
INSERT INTO `drone_device` (`owner_id`, `device_name`, `model`, `brand`, `device_no`, `status`, `max_load`, `endurance`, `rental_status`) VALUES
(16, 'DJI M300', 'M300 RTK', 'DJI', 'DJI-M300-001', 1, 20.00, '40min', 0),
(16, 'DJI M210', 'M210 RTK', 'DJI', 'DJI-M210-001', 1, 15.00, '35min', 0),
(16, 'DJI M600', 'M600 Pro', 'DJI', 'DJI-M600-001', 2, 25.00, '30min', 0),
(16, 'XAIRCRAFT P80', 'P80', 'XAIRCRAFT', 'XAIRCRAFT-P80-001', 1, 80.00, '25min', 0),
(16, 'XAIRCRAFT P40', 'P40', 'XAIRCRAFT', 'XAIRCRAFT-P40-001', 1, 40.00, '20min', 0),
(17, 'DJI Mavic 3', 'Mavic 3 Enterprise', 'DJI', 'DJI-Mavic3-001', 1, 1.00, '45min', 0),
(17, 'DJI Air 2S', 'Air 2S', 'DJI', 'DJI-Air2S-001', 1, 0.50, '35min', 0),
(17, 'XAIRCRAFT XP2022', 'XP2022', 'XAIRCRAFT', 'XAIRCRAFT-XP2022-001', 1, 20.00, '25min', 0);

-- Insert banner data
INSERT INTO `banner` (`title`, `image_url`, `type`, `sort`, `status`) VALUES
('Spring Pest Control', '/images/banner1.jpg', 'farmer', 1, 1),
('Drone Spray Service', '/images/banner2.jpg', 'farmer', 2, 1),
('Farm Inspection', '/images/banner3.jpg', 'farmer', 3, 1),
('Flyer Recruitment', '/images/banner4.jpg', 'flyer', 1, 1),
('Equipment Rental', '/images/banner5.jpg', 'flyer', 2, 1);

-- Insert demand data (30)
INSERT INTO `demand` (`farmer_id`, `flyer_id`, `land_name`, `land_boundary`, `crop_type`, `pest_type`, `land_location`, `land_area`, `expected_time`, `budget`, `status`, `order_type`) VALUES
(1, NULL, 'Zhangjiazhuang Rice Field', '39.9042,116.4074,39.9142,116.4174,39.9142,116.4074,39.9042,116.4074', 'rice', 'rice planthopper', 'Chaoyang District', 50, '2026-04-10 08:00:00', 1500.00, 0, 1),
(1, NULL, 'Zhangjiazhuang Wheat Field', '39.9242,116.4274,39.9342,116.4374,39.9342,116.4274,39.9242,116.4274', 'wheat', 'aphid', 'Chaoyang District', 30, '2026-04-12 09:00:00', 900.00, 0, 1),
(1, 11, 'Zhangjiazhuang Corn Field', '39.8942,116.3974,39.9042,116.4074,39.9042,116.3974,39.8942,116.3974', 'corn', 'corn borer', 'Chaoyang District', 40, '2026-04-08 14:00:00', 1200.00, 2, 1),
(2, NULL, 'Lijiazhuang Corn Field', '39.8842,116.3874,39.8942,116.3974,39.8942,116.3874,39.8842,116.3874', 'corn', 'corn borer', 'Haidian District', 60, '2026-04-15 10:00:00', 1800.00, 0, 1),
(2, NULL, 'Lijiazhuang Soybean Field', '39.8742,116.3774,39.8842,116.3874,39.8842,116.3774,39.8742,116.3774', 'soybean', 'soybean pod borer', 'Haidian District', 25, '2026-04-18 15:00:00', 750.00, 0, 1),
(2, 12, 'Lijiazhuang Vegetable Base', '39.8642,116.3674,39.8742,116.3774,39.8742,116.3674,39.8642,116.3674', 'vegetable', 'downy mildew', 'Haidian District', 15, '2026-04-05 08:30:00', 750.00, 4, 1),
(3, NULL, 'Wangjiazhuang Orchard', '39.9342,116.4374,39.9442,116.4474,39.9442,116.4374,39.9342,116.4374', 'apple', 'red spider mite', 'Tongzhou District', 20, '2026-04-20 09:00:00', 1200.00, 0, 1),
(3, NULL, 'Wangjiazhuang Vegetable Greenhouse', '39.9442,116.4474,39.9542,116.4574,39.9542,116.4474,39.9442,116.4474', 'cucumber', 'powdery mildew', 'Tongzhou District', 10, '2026-04-22 14:00:00', 500.00, 0, 1),
(3, 13, 'Wangjiazhuang Tea Garden', '39.9542,116.4574,39.9642,116.4674,39.9642,116.4574,39.9542,116.4574', 'tea', 'tea leafhopper', 'Tongzhou District', 30, '2026-04-03 10:30:00', 900.00, 3, 1),
(4, NULL, 'Zhaojiazhuang Cotton Field', '39.8542,116.3574,39.8642,116.3674,39.8642,116.3574,39.8542,116.3574', 'cotton', 'cotton bollworm', 'Shunyi District', 45, '2026-04-25 08:00:00', 1350.00, 0, 1),
(4, NULL, 'Zhaojiazhuang Peanut Field', '39.8442,116.3474,39.8542,116.3574,39.8542,116.3474,39.8442,116.3474', 'peanut', 'aphid', 'Shunyi District', 35, '2026-04-28 09:00:00', 1050.00, 0, 1),
(4, 14, 'Zhaojiazhuang Corn Field', '39.8342,116.3374,39.8442,116.3474,39.8442,116.3374,39.8342,116.3374', 'corn', 'corn aphid', 'Shunyi District', 55, '2026-04-01 16:00:00', 1650.00, 1, 1),
(5, NULL, 'Qianjiazhuang Tea Garden', '39.9642,116.4674,39.9742,116.4774,39.9742,116.4674,39.9642,116.4674', 'tea', 'tea geometrid', 'Changping District', 25, '2026-04-30 10:00:00', 750.00, 0, 1),
(5, NULL, 'Qianjiazhuang Tobacco Field', '39.9742,116.4774,39.9842,116.4874,39.9842,116.4774,39.9742,116.4774', 'tobacco', 'tobacco budworm', 'Changping District', 40, '2026-05-02 08:30:00', 1200.00, 0, 1),
(5, 15, 'Qianjiazhuang Rice Field', '39.9842,116.4874,39.9942,116.4974,39.9942,116.4874,39.9842,116.4874', 'rice', 'sheath blight', 'Changping District', 50, '2026-03-28 09:30:00', 1500.00, 4, 1),
(6, NULL, 'Sunjiazhuang Rice Field', '39.8242,116.3274,39.8342,116.3374,39.8342,116.3274,39.8242,116.3274', 'rice', 'rice blast', 'Daxing District', 60, '2026-05-05 14:00:00', 1800.00, 0, 1),
(6, NULL, 'Sunjiazhuang Corn Field', '39.8142,116.3174,39.8242,116.3274,39.8242,116.3174,39.8142,116.3174', 'corn', 'northern corn leaf blight', 'Daxing District', 45, '2026-05-08 10:00:00', 1350.00, 0, 1),
(6, 11, 'Sunjiazhuang Wheat Field', '39.8042,116.3074,39.8142,116.3174,39.8142,116.3074,39.8042,116.3074', 'wheat', 'powdery mildew', 'Daxing District', 50, '2026-03-25 08:00:00', 1500.00, 4, 1),
(7, NULL, 'Zhoujiazhuang Wheat Field', '39.7942,116.2974,39.8042,116.3074,39.8042,116.2974,39.7942,116.2974', 'wheat', 'rust', 'Fangshan District', 55, '2026-05-10 09:00:00', 1650.00, 0, 1),
(7, NULL, 'Zhoujiazhuang Vegetable Base', '39.7842,116.2874,39.7942,116.2974,39.7942,116.2874,39.7842,116.2874', 'vegetable', 'cabbage worm', 'Fangshan District', 20, '2026-05-12 15:00:00', 1000.00, 0, 1),
(7, 12, 'Zhoujiazhuang Orchard', '39.7742,116.2774,39.7842,116.2874,39.7842,116.2774,39.7742,116.2774', 'fruit', 'fruit borer', 'Fangshan District', 25, '2026-03-20 10:30:00', 1250.00, 3, 1),
(8, NULL, 'Wujiazhuang Orchard', '39.7642,116.2674,39.7742,116.2774,39.7742,116.2674,39.7642,116.2674', 'apple', 'aphid', 'Mentougou District', 18, '2026-05-15 08:30:00', 900.00, 0, 1),
(8, NULL, 'Wujiazhuang Tea Garden', '39.7542,116.2574,39.7642,116.2674,39.7642,116.2574,39.7542,116.2574', 'tea', 'tea caterpillar', 'Mentougou District', 30, '2026-05-18 09:30:00', 900.00, 0, 1),
(8, 13, 'Wujiazhuang Rice Field', '39.7442,116.2474,39.7542,116.2574,39.7542,116.2474,39.7442,116.2474', 'rice', 'bacterial leaf streak', 'Mentougou District', 40, '2026-03-15 14:00:00', 1200.00, 4, 1),
(9, NULL, 'Zhengjiazhuang Soybean Field', '39.7342,116.2374,39.7442,116.2474,39.7442,116.2374,39.7342,116.2374', 'soybean', 'soybean pod borer', 'Pinggu District', 35, '2026-05-20 10:00:00', 1050.00, 0, 1),
(9, NULL, 'Zhengjiazhuang Cotton Field', '39.7242,116.2274,39.7342,116.2374,39.7342,116.2274,39.7242,116.2274', 'cotton', 'red spider mite', 'Pinggu District', 45, '2026-05-22 08:00:00', 1350.00, 0, 1),
(9, 14, 'Zhengjiazhuang Corn Field', '39.7142,116.2174,39.7242,116.2274,39.7242,116.2174,39.7142,116.2174', 'corn', 'head smut', 'Pinggu District', 50, '2026-03-10 09:00:00', 1500.00, 4, 1),
(10, NULL, 'Wangjiazhuang Tobacco Field', '39.7042,116.2074,39.7142,116.2174,39.7142,116.2074,39.7042,116.2074', 'tobacco', 'tobacco mosaic virus', 'Huairou District', 25, '2026-05-25 14:00:00', 750.00, 0, 1),
(10, NULL, 'Wangjiazhuang Rice Field', '39.6942,116.1974,39.7042,116.2074,39.7042,116.1974,39.6942,116.1974', 'rice', 'bacterial leaf streak', 'Huairou District', 55, '2026-05-28 09:00:00', 1650.00, 0, 1),
(10, 15, 'Wangjiazhuang Wheat Field', '39.6842,116.1874,39.6942,116.1974,39.6942,116.1874,39.6842,116.1874', 'wheat', 'fusarium head blight', 'Huairou District', 45, '2026-03-05 10:30:00', 1350.00, 4, 1);

-- Insert inspection demands
INSERT INTO `demand` (`farmer_id`, `flyer_id`, `land_name`, `land_boundary`, `crop_type`, `land_location`, `land_area`, `expected_time`, `budget`, `status`, `order_type`, `inspection_purpose`, `expected_resolution`) VALUES
(1, NULL, 'Zhangjiazhuang Farm Inspection', '39.9042,116.4074,39.9142,116.4174,39.9142,116.4074,39.9042,116.4074', 'rice', 'Chaoyang District', 50, '2026-04-09 10:00:00', 1000.00, 0, 2, 'pest monitoring', '4K'),
(2, NULL, 'Lijiazhuang Orchard Inspection', '39.8842,116.3874,39.8942,116.3974,39.8942,116.3874,39.8842,116.3874', 'apple', 'Haidian District', 20, '2026-04-16 09:00:00', 600.00, 0, 2, 'fruit tree growth', '4K'),
(3, NULL, 'Wangjiazhuang Tea Garden Inspection', '39.9342,116.4374,39.9442,116.4474,39.9442,116.4374,39.9342,116.4374', 'tea', 'Tongzhou District', 30, '2026-04-21 14:00:00', 900.00, 0, 2, 'tea pest monitoring', '2K'),
(4, NULL, 'Zhaojiazhuang Cotton Field Inspection', '39.8542,116.3574,39.8642,116.3674,39.8642,116.3574,39.8542,116.3574', 'cotton', 'Shunyi District', 45, '2026-04-26 10:00:00', 1350.00, 0, 2, 'cotton growth monitoring', '4K'),
(5, NULL, 'Qianjiazhuang Tobacco Field Inspection', '39.9642,116.4674,39.9742,116.4774,39.9742,116.4674,39.9642,116.4674', 'tobacco', 'Changping District', 40, '2026-04-29 09:00:00', 1200.00, 0, 2, 'tobacco pest monitoring', '2K');
