-- ============================================
-- 电商平台测试数据（商品 + 属性相关）
-- 使用前先执行 ecommerce_platform.sql 建表
--
-- 密码说明：BCrypt 加密，请替换为实际加密值
--   生成方式：
--     1. 在线生成：https://www.bcrypt.online/
--     2. Java 代码：System.out.println(new BCryptPasswordEncoder().encode("123456"));
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 1. 角色 ====================
INSERT INTO `roles` (`id`, `name`, `code`, `description`, `status`, `created_at`, `updated_at`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '系统超级管理员', 1, NOW(), NOW()),
(2, '商家', 'SELLER', '商家用户', 1, NOW(), NOW()),
(3, '店铺管理员', 'STORE_ADMIN', '店铺日常运营管理', 1, NOW(), NOW()),
(4, '普通用户', 'USER', '普通买家', 1, NOW(), NOW());

-- ==================== 2. 用户 ====================
-- 密码统一为 123456，请替换为 BCrypt 加密值
INSERT INTO `users` (`id`, `username`, `password`, `email`, `phone`, `avatar`, `real_name`, `status`, `created_at`, `updated_at`) VALUES
(1, 'admin',        '$2a$10$REPLACE_ME', 'admin@shop.com',       '13800000000', NULL, '系统管理员',  1, NOW(), NOW()),
(2, 'zhang_seller', '$2a$10$REPLACE_ME', 'zhang@shop.com',      '13800000001', NULL, '张经理',     1, NOW(), NOW()),
(3, 'li_seller',    '$2a$10$REPLACE_ME', 'li@shop.com',         '13800000002', NULL, '李经理',     1, NOW(), NOW()),
(4, 'wang_buyer',   '$2a$10$REPLACE_ME', 'wang@mail.com',       '13800000003', NULL, '王小明',     1, NOW(), NOW());

-- ==================== 3. 用户角色关联 ====================
INSERT INTO `user_roles` (`id`, `user_id`, `role_id`, `created_at`) VALUES
(1, 1, 1, NOW()),   -- admin → SUPER_ADMIN
(2, 2, 2, NOW()),   -- zhang_seller → SELLER
(3, 3, 2, NOW()),   -- li_seller → SELLER
(4, 4, 4, NOW());   -- wang_buyer → USER

-- ==================== 4. 店铺 ====================
INSERT INTO `stores` (`id`, `name`, `seller_id`, `logo`, `description`, `phone`, `address`, `status`, `sort`, `created_at`, `updated_at`) VALUES
(1, '小米官方授权店', 2, NULL, '小米官方授权店铺，正品保证', '400-100-5678', '北京市海淀区中关村大街1号', 1, 1, NOW(), NOW()),
(2, '华为体验店',     3, NULL, '华为官方合作体验店',       '400-100-5679', '深圳市南山区科技园路1号',  1, 2, NOW(), NOW());

-- ==================== 5. 分类 ====================
INSERT INTO `categories` (`id`, `name`, `parent_id`, `level`, `sort`, `status`, `is_deleted`, `created_at`, `updated_at`) VALUES
(1,  '手机通讯',  0, 1, 1, 1, 0, NOW(), NOW()),
(2,  '电脑办公',  0, 1, 2, 1, 0, NOW(), NOW()),
(3,  '智能设备',  0, 1, 3, 1, 0, NOW(), NOW()),
(4,  '智能手机',  1, 2, 1, 1, 0, NOW(), NOW()),
(5,  '功能手机',  1, 2, 2, 1, 0, NOW(), NOW()),
(6,  '笔记本电脑', 2, 2, 1, 1, 0, NOW(), NOW()),
(7,  '平板电脑',  2, 2, 2, 1, 0, NOW(), NOW()),
(8,  '智能手表',  3, 2, 1, 1, 0, NOW(), NOW()),
(9,  '蓝牙耳机',  3, 2, 2, 1, 0, NOW(), NOW());

-- ==================== 6. 品牌 ====================
INSERT INTO `brands` (`id`, `name`, `logo`, `description`, `sort`, `status`, `is_deleted`, `created_at`, `updated_at`) VALUES
(1, '小米',   NULL, '小米科技有限责任公司', 1, 1, 0, NOW(), NOW()),
(2, '华为',   NULL, '华为技术有限公司',    2, 1, 0, NOW(), NOW()),
(3, '苹果',   NULL, 'Apple Inc.',          3, 1, 0, NOW(), NOW()),
(4, 'OPPO',   NULL, 'OPPO广东移动通信',    4, 1, 0, NOW(), NOW()),
(5, 'vivo',   NULL, '维沃移动通信',        5, 1, 0, NOW(), NOW());

-- ==================== 7. 属性定义 ====================
-- attr_type: 1=销售属性, 2=基本属性
INSERT INTO `attributes` (`id`, `name`, `attr_type`, `sort`, `created_at`, `updated_at`) VALUES
-- 销售属性
(1, '颜色',     1, 1, NOW(), NOW()),
(2, '存储容量', 1, 2, NOW(), NOW()),
(3, '运行内存', 1, 3, NOW(), NOW()),
-- 基本属性
(4, '电池容量', 2, 1, NOW(), NOW()),
(5, '屏幕尺寸', 2, 2, NOW(), NOW()),
(6, '网络制式', 2, 3, NOW(), NOW());

-- ==================== 8. 属性值 ====================
-- seller_id=NULL 表示平台预设值
INSERT INTO `attribute_values` (`id`, `attr_id`, `value`, `seller_id`, `image_url`, `sort`, `created_at`, `updated_at`) VALUES
-- 颜色（attr_id=1）
(1,  1, '黑色',   NULL, NULL, 1, NOW(), NOW()),
(2,  1, '白色',   NULL, NULL, 2, NOW(), NOW()),
(3,  1, '蓝色',   NULL, NULL, 3, NOW(), NOW()),
(4,  1, '紫色',   NULL, NULL, 4, NOW(), NOW()),
(5,  1, '星河银', NULL, NULL, 5, NOW(), NOW()),
-- 存储容量（attr_id=2）
(6,  2, '128G',  NULL, NULL, 1, NOW(), NOW()),
(7,  2, '256G',  NULL, NULL, 2, NOW(), NOW()),
(8,  2, '512G',  NULL, NULL, 3, NOW(), NOW()),
(9,  2, '1TB',   NULL, NULL, 4, NOW(), NOW()),
-- 运行内存（attr_id=3）
(10, 3, '8G',  NULL, NULL, 1, NOW(), NOW()),
(11, 3, '12G', NULL, NULL, 2, NOW(), NOW()),
(12, 3, '16G', NULL, NULL, 3, NOW(), NOW()),
-- 电池容量（attr_id=4）
(13, 4, '4500mAh', NULL, NULL, 1, NOW(), NOW()),
(14, 4, '5000mAh', NULL, NULL, 2, NOW(), NOW()),
(15, 4, '5400mAh', NULL, NULL, 3, NOW(), NOW()),
-- 屏幕尺寸（attr_id=5）
(16, 5, '6.1英寸', NULL, NULL, 1, NOW(), NOW()),
(17, 5, '6.5英寸', NULL, NULL, 2, NOW(), NOW()),
(18, 5, '6.7英寸', NULL, NULL, 3, NOW(), NOW()),
(19, 5, '6.9英寸', NULL, NULL, 4, NOW(), NOW()),
-- 网络制式（attr_id=6）
(20, 6, '5G全网通', NULL, NULL, 1, NOW(), NOW()),
(21, 6, '4G全网通', NULL, NULL, 2, NOW(), NOW());

-- ==================== 9. 分类-属性绑定 ====================
-- 智能手机（category_id=4）绑定所有属性
INSERT INTO `category_attributes` (`id`, `category_id`, `attr_id`, `sort`, `created_at`, `updated_at`) VALUES
(1, 4, 1, 1, NOW(), NOW()),   -- 颜色
(2, 4, 2, 2, NOW(), NOW()),   -- 存储容量
(3, 4, 3, 3, NOW(), NOW()),   -- 运行内存
(4, 4, 4, 4, NOW(), NOW()),   -- 电池容量
(5, 4, 5, 5, NOW(), NOW()),   -- 屏幕尺寸
(6, 4, 6, 6, NOW(), NOW());   -- 网络制式

-- ==================== 10. SPU ====================
INSERT INTO `spu` (`id`, `name`, `category_id`, `brand_id`, `seller_id`, `store_id`, `min_price`, `sales`, `description`, `keywords`, `status`, `is_deleted`, `created_at`, `updated_at`) VALUES
(1, '小米15 Pro 旗舰手机', 4, 1, 2, 1, 4999.00, 1520,
 '小米15 Pro 搭载全新骁龙8至尊版处理器，5400mAh大电池，徕卡专业影像系统，120Hz高刷屏。',
 '小米,15Pro,旗舰,手机', 1, 0, NOW(), NOW()),
(2, '华为Mate 70 Pro 旗舰手机', 4, 2, 3, 2, 5499.00, 980,
 '华为Mate 70 Pro 搭载麒麟芯片，5000mAh大电池，XMAGE影像系统，卫星通信功能。',
 '华为,Mate70,Pro,旗舰', 1, 0, NOW(), NOW());

-- ==================== 11. SPU 基本属性值 ====================
INSERT INTO `spu_basic_attr_values` (`id`, `spu_id`, `attr_id`, `attr_value_id`, `manual_value`, `created_at`, `updated_at`) VALUES
-- 小米15 Pro
(1, 1, 4, 15, NULL, NOW(), NOW()),   -- 电池容量=5400mAh
(2, 1, 5, 18, NULL, NOW(), NOW()),   -- 屏幕尺寸=6.7英寸
(3, 1, 6, 20, NULL, NOW(), NOW()),   -- 网络制式=5G全网通
-- 华为Mate 70 Pro
(4, 2, 4, 14, NULL, NOW(), NOW()),   -- 电池容量=5000mAh
(5, 2, 5, 19, NULL, NOW(), NOW()),   -- 屏幕尺寸=6.9英寸
(6, 2, 6, 20, NULL, NOW(), NOW());   -- 网络制式=5G全网通

-- ==================== 12. SPU 销售属性选择 ====================
-- selected_values 为 JSON 数组，存储属性值ID
INSERT INTO `spu_sale_attr_choice` (`id`, `spu_id`, `attr_id`, `selected_values`, `created_at`, `updated_at`) VALUES
-- 小米15 Pro
(1, 1, 1, '[1, 2, 3]',   NOW(), NOW()),   -- 颜色：黑色,白色,蓝色
(2, 1, 2, '[7, 8, 9]',   NOW(), NOW()),   -- 存储：256G,512G,1TB
(3, 1, 3, '[11, 12]',    NOW(), NOW()),   -- 内存：12G,16G
-- 华为Mate 70 Pro
(4, 2, 1, '[1, 2, 4]',   NOW(), NOW()),   -- 颜色：黑色,白色,紫色
(5, 2, 2, '[7, 8]',      NOW(), NOW()),   -- 存储：256G,512G
(6, 2, 3, '[11, 12]',    NOW(), NOW());   -- 内存：12G,16G

-- ==================== 13. SKU ====================
INSERT INTO `sku` (`id`, `spu_id`, `price`, `market_price`, `cost_price`, `stock`, `frozen_stock`, `warn_stock`, `weight`, `status`, `is_deleted`, `created_at`, `updated_at`) VALUES
-- 小米15 Pro SKU（颜色+存储+内存）
(1,  1, 4999.00, 5499.00, 4200.00, 100, 0, 10, 0.210, 1, 0, NOW(), NOW()),   -- 黑色+256G+12G
(2,  1, 5299.00, 5799.00, 4500.00,  80, 0, 10, 0.210, 1, 0, NOW(), NOW()),   -- 黑色+256G+16G
(3,  1, 5499.00, 5999.00, 4700.00,  60, 0, 10, 0.210, 1, 0, NOW(), NOW()),   -- 白色+512G+12G
(4,  1, 5799.00, 6299.00, 4900.00,  50, 0, 10, 0.210, 1, 0, NOW(), NOW()),   -- 白色+512G+16G
(5,  1, 5999.00, 6499.00, 5100.00,  30, 0, 10, 0.210, 1, 0, NOW(), NOW()),   -- 蓝色+1TB+12G
(6,  1, 6299.00, 6799.00, 5400.00,  20, 0, 10, 0.210, 1, 0, NOW(), NOW()),   -- 蓝色+1TB+16G

-- 华为Mate 70 Pro SKU
(7,  2, 5499.00, 5999.00, 4600.00, 100, 0, 10, 0.225, 1, 0, NOW(), NOW()),   -- 黑色+256G+12G
(8,  2, 5999.00, 6499.00, 5000.00,  80, 0, 10, 0.225, 1, 0, NOW(), NOW()),   -- 黑色+512G+12G
(9,  2, 5799.00, 6299.00, 4800.00,  60, 0, 10, 0.225, 1, 0, NOW(), NOW()),   -- 白色+256G+16G
(10, 2, 6299.00, 6799.00, 5300.00,  50, 0, 10, 0.225, 1, 0, NOW(), NOW()),   -- 白色+512G+16G
(11, 2, 5699.00, 6199.00, 4700.00,  40, 0, 10, 0.225, 1, 0, NOW(), NOW()),   -- 紫色+256G+12G
(12, 2, 6499.00, 6999.00, 5500.00,  30, 0, 10, 0.225, 1, 0, NOW(), NOW());   -- 紫色+512G+16G

-- ==================== 14. SKU 销售属性值关联 ====================
-- 每个 SKU 绑定 3 个属性值：颜色 + 存储 + 内存
INSERT INTO `sku_sale_attr_values` (`id`, `sku_id`, `attr_value_id`, `created_at`, `updated_at`) VALUES
-- 小米15 Pro SKU
-- SKU 1: 黑色(1) + 256G(7) + 12G(11)
(1,  1, 1,  NOW(), NOW()), (2,  1, 7,  NOW(), NOW()), (3,  1, 11, NOW(), NOW()),
-- SKU 2: 黑色(1) + 256G(7) + 16G(12)
(4,  2, 1,  NOW(), NOW()), (5,  2, 7,  NOW(), NOW()), (6,  2, 12, NOW(), NOW()),
-- SKU 3: 白色(2) + 512G(8) + 12G(11)
(7,  3, 2,  NOW(), NOW()), (8,  3, 8,  NOW(), NOW()), (9,  3, 11, NOW(), NOW()),
-- SKU 4: 白色(2) + 512G(8) + 16G(12)
(10, 4, 2,  NOW(), NOW()), (11, 4, 8,  NOW(), NOW()), (12, 4, 12, NOW(), NOW()),
-- SKU 5: 蓝色(3) + 1TB(9) + 12G(11)
(13, 5, 3,  NOW(), NOW()), (14, 5, 9,  NOW(), NOW()), (15, 5, 11, NOW(), NOW()),
-- SKU 6: 蓝色(3) + 1TB(9) + 16G(12)
(16, 6, 3,  NOW(), NOW()), (17, 6, 9,  NOW(), NOW()), (18, 6, 12, NOW(), NOW()),

-- 华为Mate 70 Pro SKU
-- SKU 7: 黑色(1) + 256G(7) + 12G(11)
(19, 7, 1,  NOW(), NOW()), (20, 7, 7,  NOW(), NOW()), (21, 7, 11, NOW(), NOW()),
-- SKU 8: 黑色(1) + 512G(8) + 12G(11)
(22, 8, 1,  NOW(), NOW()), (23, 8, 8,  NOW(), NOW()), (24, 8, 11, NOW(), NOW()),
-- SKU 9: 白色(2) + 256G(7) + 16G(12)
(25, 9, 2,  NOW(), NOW()), (26, 9, 7,  NOW(), NOW()), (27, 9, 12, NOW(), NOW()),
-- SKU 10: 白色(2) + 512G(8) + 16G(12)
(28, 10, 2, NOW(), NOW()), (29, 10, 8, NOW(), NOW()), (30, 10, 12, NOW(), NOW()),
-- SKU 11: 紫色(4) + 256G(7) + 12G(11)
(31, 11, 4, NOW(), NOW()), (32, 11, 7, NOW(), NOW()), (33, 11, 11, NOW(), NOW()),
-- SKU 12: 紫色(4) + 512G(8) + 16G(12)
(34, 12, 4, NOW(), NOW()), (35, 12, 8, NOW(), NOW()), (36, 12, 12, NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;