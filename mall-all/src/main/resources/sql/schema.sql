/*
 Navicat Premium Dump SQL

 Source Server         : root
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : ecommerce_platform

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 02/06/2026 13:22:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for addresses
-- ----------------------------
DROP TABLE IF EXISTS `addresses`;
CREATE TABLE `addresses`  (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `user_id` bigint NOT NULL,
                              `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人',
                              `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人电话',
                              `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省',
                              `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '市',
                              `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区',
                              `detail_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
                              `zip_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮编',
                              `is_default` tinyint NULL DEFAULT 0 COMMENT '是否默认: 1-是 0-否',
                              `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                              `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`) USING BTREE,
                              INDEX `user_id`(`user_id` ASC) USING BTREE,
                              CONSTRAINT `addresses_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收货地址表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of addresses
-- ----------------------------
INSERT INTO `addresses` VALUES (1, 2, '程', '15738971365', '北京市', '北京市', '西城区', '淮滨', NULL, 0, '2026-05-16 23:00:53', '2026-05-16 23:10:44');
INSERT INTO `addresses` VALUES (2, 2, '程xs', '15738971365', '北京市', '北京市', '海淀区', '防胡', NULL, 1, '2026-05-16 23:10:45', '2026-05-18 12:57:20');
INSERT INTO `addresses` VALUES (3, 4, '程', '15738971365', '北京市', '北京市', '朝阳区', '淮滨', NULL, 1, '2026-05-17 20:09:41', '2026-05-18 12:42:04');
INSERT INTO `addresses` VALUES (4, 4, 'c', '15738971365', '北京市', '北京市', '西城区', '河南', NULL, 0, '2026-05-18 12:42:00', '2026-05-18 12:42:03');

-- ----------------------------
-- Table structure for attribute_values
-- ----------------------------
DROP TABLE IF EXISTS `attribute_values`;
CREATE TABLE `attribute_values`  (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `attr_id` bigint NOT NULL COMMENT '属性ID',
                                     `value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '属性值（黑色、8G、5000mAh等）',
                                     `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '销售属性可配图片（如颜色色块）',
                                     `sort` int NULL DEFAULT 0 COMMENT '排序',
                                     `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                     `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     PRIMARY KEY (`id`) USING BTREE,
                                     INDEX `idx_attr_id`(`attr_id` ASC) USING BTREE,
                                     CONSTRAINT `fk_attr_value_attr` FOREIGN KEY (`attr_id`) REFERENCES `attributes` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '属性值表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of attribute_values
-- ----------------------------
INSERT INTO `attribute_values` VALUES (1, 1, '黑色', '/images/phone/black.png', 1, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attribute_values` VALUES (2, 1, '白色', '/images/phone/white.png', 2, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attribute_values` VALUES (3, 1, '绿色', '/images/phone/green.png', 3, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attribute_values` VALUES (4, 1, '粉色', '/images/phone/pink.png', 4, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attribute_values` VALUES (5, 2, '12G', NULL, 1, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attribute_values` VALUES (6, 2, '16G', NULL, 2, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attribute_values` VALUES (7, 3, '256GB', NULL, 1, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attribute_values` VALUES (8, 3, '512GB', NULL, 2, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attribute_values` VALUES (9, 3, '1TB', NULL, 3, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attribute_values` VALUES (10, 4, '5000mAh', NULL, 1, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attribute_values` VALUES (11, 6, '6.73英寸', NULL, 1, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attribute_values` VALUES (12, 7, '徕卡三摄 50MP+50MP+50MP', NULL, 1, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attribute_values` VALUES (13, 7, '最好后置摄像头', NULL, 2, '2026-05-28 14:03:22', '2026-05-28 14:03:22');

-- ----------------------------
-- Table structure for attributes
-- ----------------------------
DROP TABLE IF EXISTS `attributes`;
CREATE TABLE `attributes`  (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '属性名称（颜色、内存、电池容量等）',
                               `attr_type` tinyint NOT NULL COMMENT '属性类型：1=销售属性，2=基本属性',
                               `sort` int NULL DEFAULT 0 COMMENT '排序',
                               `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                               `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`) USING BTREE,
                               INDEX `idx_attr_type`(`attr_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '属性定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of attributes
-- ----------------------------
INSERT INTO `attributes` VALUES (1, '颜色', 1, 1, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attributes` VALUES (2, '运行内存', 1, 2, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attributes` VALUES (3, '存储容量', 1, 3, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attributes` VALUES (4, '电池容量', 2, 4, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attributes` VALUES (5, '处理器', 2, 5, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attributes` VALUES (6, '屏幕尺寸', 2, 6, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `attributes` VALUES (7, '后置摄像头', 2, 7, '2026-05-12 14:07:25', '2026-05-12 14:07:25');

-- ----------------------------
-- Table structure for banners
-- ----------------------------
DROP TABLE IF EXISTS `banners`;
CREATE TABLE `banners`  (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
                            `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
                            `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接',
                            `sort` int NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
                            `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
                            `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of banners
-- ----------------------------

-- ----------------------------
-- Table structure for brands
-- ----------------------------
DROP TABLE IF EXISTS `brands`;
CREATE TABLE `brands`  (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '品牌名称',
                           `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌Logo',
                           `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '品牌描述',
                           `website` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌官网',
                           `sort` int NULL DEFAULT 0 COMMENT '排序',
                           `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
                           `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
                           `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品品牌表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of brands
-- ----------------------------
INSERT INTO `brands` VALUES (1, '华为', '2026/05/07/5f748651-aa40-4c90-b865-aabe8430da18_华为logo.webp', '华为技术有限公司', NULL, 1, 1, 0, '2026-04-30 22:35:36', '2026-05-07 21:08:38');
INSERT INTO `brands` VALUES (2, '苹果', NULL, '苹果公司', NULL, 2, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `brands` VALUES (3, '小米', NULL, '小米科技有限公司', NULL, 3, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `brands` VALUES (4, '三星', NULL, '三星电子', NULL, 4, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `brands` VALUES (5, '耐克', NULL, '耐克公司', NULL, 5, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `brands` VALUES (6, '阿迪达斯', NULL, '阿迪达斯公司', NULL, 6, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `brands` VALUES (7, 'OPPO', NULL, 'OPPO 广东移动通信有限公司', 'https://www.oppo.com', 7, 1, 0, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (8, 'vivo', NULL, '维沃移动通信有限公司', 'https://www.vivo.com', 8, 1, 0, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (9, '荣耀', NULL, '荣耀终端有限公司', 'https://www.honor.com', 9, 1, 0, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (10, '联想', NULL, '联想集团', 'https://www.lenovo.com', 10, 1, 0, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (11, '戴尔', NULL, '戴尔科技集团', 'https://www.dell.com', 11, 1, 0, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (12, '海尔', NULL, '海尔集团', 'https://www.haier.com', 12, 1, 0, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (13, '美的', NULL, '美的集团', 'https://www.midea.com', 13, 1, 0, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (14, '格力', NULL, '格力电器股份有限公司', 'https://www.gree.com', 14, 1, 0, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (15, '安踏', NULL, '安踏体育用品有限公司', 'https://www.anta.com', 15, 1, 0, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (16, '李宁', NULL, '李宁体育用品有限公司', 'https://www.lining.com', 16, 1, 0, '2026-05-08 17:09:39', '2026-05-08 17:09:39');

-- ----------------------------
-- Table structure for cart_items
-- ----------------------------
DROP TABLE IF EXISTS `cart_items`;
CREATE TABLE `cart_items`  (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `user_id` bigint NOT NULL,
                               `sku_id` bigint NOT NULL,
                               `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
                               `selected` tinyint NULL DEFAULT 1 COMMENT '是否选中: 1-选中 0-未选',
                               `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品名称快照',
                               `product_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片快照',
                               `sku_specs` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SKU规格快照',
                               `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '单价快照（加购物车时的价格）',
                               `expire_time` datetime NULL DEFAULT NULL COMMENT '失效时间',
                               `notes` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                               `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                               `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE INDEX `uk_user_sku`(`user_id` ASC, `sku_id` ASC) USING BTREE,
                               INDEX `sku_id`(`sku_id` ASC) USING BTREE,
                               INDEX `idx_cart_items_user`(`user_id` ASC) USING BTREE,
                               CONSTRAINT `cart_items_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                               CONSTRAINT `cart_items_ibfk_2` FOREIGN KEY (`sku_id`) REFERENCES `sku` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cart_items
-- ----------------------------
INSERT INTO `cart_items` VALUES (26, 4, 247, 1, 1, '小米14 Pro2', '2026/05/26/537f9754-aed2-4988-bfbb-754068c2c0c6_dog111.jpg', '颜色:黑色 运行内存:16G 存储容量:512GB', 3499.00, NULL, NULL, '2026-06-01 15:13:03', '2026-06-01 15:13:03');

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
                               `parent_id` bigint NULL DEFAULT 0 COMMENT '父分类ID',
                               `level` int NULL DEFAULT 1 COMMENT '层级',
                               `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类图标',
                               `sort` int NULL DEFAULT 0 COMMENT '排序',
                               `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
                               `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
                               `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                               `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, '电子产品', 0, 1, NULL, 1, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (2, '服装鞋帽', 0, 1, NULL, 2, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (3, '家居用品', 0, 1, NULL, 3, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (4, '食品饮料', 0, 1, NULL, 4, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (5, '手机数码', 1, 2, NULL, 1, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (6, '电脑办公', 1, 2, NULL, 2, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (7, '智能设备', 1, 2, NULL, 3, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (8, '男装', 2, 2, NULL, 1, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (9, '女装', 2, 2, NULL, 2, 1, 0, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (10, '鞋靴', 2, 2, NULL, 3, 1, 0, '2026-04-30 22:35:36', '2026-05-06 21:51:39');
INSERT INTO `categories` VALUES (11, '智能手环', 7, 3, NULL, 1, 1, 0, '2026-05-06 15:49:28', '2026-05-06 15:49:28');
INSERT INTO `categories` VALUES (12, '智能手表', 7, 3, NULL, 2, 1, 0, '2026-05-06 15:49:28', '2026-05-06 15:49:28');
INSERT INTO `categories` VALUES (13, '智能家居', 7, 3, NULL, 3, 1, 0, '2026-05-06 15:49:28', '2026-05-06 15:49:28');
INSERT INTO `categories` VALUES (14, '大家电', 1, 2, NULL, 4, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (15, '厨卫电器', 1, 2, NULL, 5, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (16, '童装', 2, 2, NULL, 4, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (17, '内衣睡衣', 2, 2, NULL, 5, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (18, '箱包配饰', 2, 2, NULL, 6, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (19, '床上用品', 3, 2, NULL, 1, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (20, '厨房用具', 3, 2, NULL, 2, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (21, '收纳整理', 3, 2, NULL, 3, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (22, '家装软饰', 3, 2, NULL, 4, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (23, '休闲食品', 4, 2, NULL, 1, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (24, '生鲜食品', 4, 2, NULL, 2, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (25, '酒水饮料', 4, 2, NULL, 3, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (26, '营养保健', 4, 2, NULL, 4, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (27, '手机', 5, 3, NULL, 1, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (28, '手机配件', 5, 3, NULL, 2, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (29, '数码相机', 5, 3, NULL, 3, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (30, '耳机音响', 5, 3, NULL, 4, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (31, '笔记本电脑', 6, 3, NULL, 1, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (32, '台式电脑', 6, 3, NULL, 2, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (33, '电脑外设', 6, 3, NULL, 3, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (34, '办公用品', 6, 3, NULL, 4, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (35, '电视', 14, 3, NULL, 1, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (36, '冰箱', 14, 3, NULL, 2, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (37, '洗衣机', 14, 3, NULL, 3, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (38, '空调', 14, 3, NULL, 4, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (39, '上衣', 8, 3, NULL, 1, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (40, '裤子', 8, 3, NULL, 2, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (41, '外套', 8, 3, NULL, 3, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (42, '连衣裙', 9, 3, NULL, 1, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (43, '上衣', 9, 3, NULL, 2, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (44, '半身裙', 9, 3, NULL, 3, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (45, '裤子', 9, 3, NULL, 4, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (46, '运动鞋', 10, 3, NULL, 1, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (47, '皮鞋', 10, 3, NULL, 2, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (48, '休闲鞋', 10, 3, NULL, 3, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (49, '凉鞋/拖鞋', 10, 3, NULL, 4, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (50, '背包', 18, 3, NULL, 1, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (51, '钱包', 18, 3, NULL, 2, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (52, '饰品', 18, 3, NULL, 3, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (53, '四件套', 19, 3, NULL, 1, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (54, '被芯', 19, 3, NULL, 2, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (55, '枕芯', 19, 3, NULL, 3, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (56, '锅具', 20, 3, NULL, 1, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (57, '刀具砧板', 20, 3, NULL, 2, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (58, '餐具', 20, 3, NULL, 3, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (59, '坚果炒货', 23, 3, NULL, 1, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (60, '饼干糕点', 23, 3, NULL, 2, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (61, '巧克力糖果', 23, 3, NULL, 3, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (62, '白酒', 25, 3, NULL, 1, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (63, '啤酒', 25, 3, NULL, 2, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (64, '饮料', 25, 3, NULL, 3, 1, 0, '2026-05-06 22:00:00', '2026-05-06 22:00:00');

-- ----------------------------
-- Table structure for category_attributes
-- ----------------------------
DROP TABLE IF EXISTS `category_attributes`;
CREATE TABLE `category_attributes`  (
                                        `id` bigint NOT NULL AUTO_INCREMENT,
                                        `category_id` bigint NOT NULL COMMENT '分类ID',
                                        `attr_id` bigint NOT NULL COMMENT '属性ID',
                                        `sort` int NULL DEFAULT 0 COMMENT '排序',
                                        `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                        `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        PRIMARY KEY (`id`) USING BTREE,
                                        UNIQUE INDEX `uk_category_attr`(`category_id` ASC, `attr_id` ASC) USING BTREE,
                                        INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
                                        INDEX `idx_attr_id`(`attr_id` ASC) USING BTREE,
                                        CONSTRAINT `fk_cat_attr_attribute` FOREIGN KEY (`attr_id`) REFERENCES `attributes` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                        CONSTRAINT `fk_cat_attr_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分类与属性关联表（定义该分类下有哪些属性）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category_attributes
-- ----------------------------
INSERT INTO `category_attributes` VALUES (1, 27, 1, 1, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `category_attributes` VALUES (2, 27, 2, 2, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `category_attributes` VALUES (3, 27, 3, 3, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `category_attributes` VALUES (4, 27, 4, 4, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `category_attributes` VALUES (5, 27, 5, 5, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `category_attributes` VALUES (6, 27, 6, 6, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `category_attributes` VALUES (7, 27, 7, 7, '2026-05-12 14:07:25', '2026-05-12 14:07:25');

-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites`  (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `user_id` bigint NOT NULL,
                              `spu_id` bigint NOT NULL,
                              `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`) USING BTREE,
                              UNIQUE INDEX `uk_user_spu`(`user_id` ASC, `spu_id` ASC) USING BTREE,
                              INDEX `spu_id`(`spu_id` ASC) USING BTREE,
                              CONSTRAINT `favorites_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                              CONSTRAINT `favorites_ibfk_2` FOREIGN KEY (`spu_id`) REFERENCES `spu` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorites
-- ----------------------------

-- ----------------------------
-- Table structure for logistics_companies
-- ----------------------------
DROP TABLE IF EXISTS `logistics_companies`;
CREATE TABLE `logistics_companies`  (
                                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '物流公司ID',
                                        `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物流公司名称（如：顺丰速运）',
                                        `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物流公司代码（如：SF）',
                                        `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流公司Logo',
                                        `website` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流公司官网',
                                        `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流公司客服电话',
                                        `sort` int NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
                                        `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
                                        `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                        `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        PRIMARY KEY (`id`) USING BTREE,
                                        UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物流公司表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of logistics_companies
-- ----------------------------
INSERT INTO `logistics_companies` VALUES (1, '顺丰速运', 'SF', NULL, NULL, NULL, 1, 1, '2026-05-16 15:11:12', '2026-05-16 15:11:12');
INSERT INTO `logistics_companies` VALUES (2, '中通快递', 'ZTO', NULL, NULL, NULL, 2, 1, '2026-05-16 15:11:12', '2026-05-16 15:11:12');
INSERT INTO `logistics_companies` VALUES (3, '圆通速递', 'YTO', NULL, NULL, NULL, 3, 1, '2026-05-16 15:11:12', '2026-05-16 15:11:12');
INSERT INTO `logistics_companies` VALUES (4, '韵达快递', 'YD', NULL, NULL, NULL, 4, 1, '2026-05-16 15:11:12', '2026-05-16 15:11:12');
INSERT INTO `logistics_companies` VALUES (5, '极兔速递', 'JT', NULL, NULL, NULL, 5, 1, '2026-05-16 15:11:12', '2026-05-16 15:11:12');
INSERT INTO `logistics_companies` VALUES (6, '京东物流', 'JD', NULL, NULL, NULL, 6, 1, '2026-05-16 15:11:12', '2026-05-16 15:11:12');
INSERT INTO `logistics_companies` VALUES (7, '德邦快递', 'DB', NULL, NULL, NULL, 7, 1, '2026-05-16 15:11:12', '2026-05-16 15:11:12');
INSERT INTO `logistics_companies` VALUES (8, '邮政EMS', 'EMS', NULL, NULL, NULL, 8, 1, '2026-05-16 15:11:12', '2026-05-16 15:11:12');

-- ----------------------------
-- Table structure for operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `user_id` bigint NULL DEFAULT NULL COMMENT '操作用户ID',
                                   `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作用户名',
                                   `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作模块',
                                   `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型',
                                   `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作描述',
                                   `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法',
                                   `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求URL',
                                   `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
                                   `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
                                   `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理',
                                   `duration` bigint NULL DEFAULT NULL COMMENT '执行时长(毫秒)',
                                   `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-成功 0-失败',
                                   `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
                                   `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `idx_operation_logs_user`(`user_id` ASC) USING BTREE,
                                   INDEX `idx_operation_logs_created`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_logs
-- ----------------------------

-- ----------------------------
-- Table structure for order_delivery
-- ----------------------------
DROP TABLE IF EXISTS `order_delivery`;
CREATE TABLE `order_delivery`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '发货记录ID',
                                   `order_id` bigint NOT NULL COMMENT '订单ID（关联订单表）',
                                   `delivery_company` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物流公司（如：顺丰、中通）',
                                   `delivery_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物流单号',
                                   `delivery_status` tinyint NULL DEFAULT 1 COMMENT '发货状态：1-已发货 2-已签收 3-物流异常',
                                   `sender` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发货人（操作员）',
                                   `sender_id` bigint NULL DEFAULT NULL COMMENT '发货人ID（关联用户表）',
                                   `delivery_time` datetime NOT NULL COMMENT '实际发货时间',
                                   `package_count` int NULL DEFAULT 1 COMMENT '包裹数量',
                                   `weight` decimal(10, 2) NULL DEFAULT NULL COMMENT '包裹重量(kg)',
                                   `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人姓名',
                                   `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人电话',
                                   `sign_time` datetime NULL DEFAULT NULL COMMENT '签收时间',
                                   `signer` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签收人',
                                   `exception_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '异常原因',
                                   `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                                   `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
                                   `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
                                   INDEX `idx_delivery_no`(`delivery_no` ASC) USING BTREE,
                                   INDEX `idx_delivery_status`(`delivery_status` ASC) USING BTREE,
                                   INDEX `idx_delivery_time`(`delivery_time` ASC) USING BTREE,
                                   CONSTRAINT `fk_delivery_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单发货记录表（支持一个订单多次发货）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_delivery
-- ----------------------------
INSERT INTO `order_delivery` VALUES (6, 17, '顺丰速运', '147258', 1, NULL, NULL, '2026-05-30 10:21:23', 1, NULL, '程', '15738971365', NULL, NULL, NULL, NULL, '2026-05-30 10:21:23', '2026-05-30 10:21:23');
INSERT INTO `order_delivery` VALUES (7, 18, '中通快递', '23343', 1, NULL, NULL, '2026-05-30 19:41:03', 1, NULL, '程', '15738971365', NULL, NULL, NULL, NULL, '2026-05-30 19:41:03', '2026-05-30 19:41:03');
INSERT INTO `order_delivery` VALUES (8, 19, '顺丰速运', '123321', 1, NULL, NULL, '2026-06-01 14:40:01', 1, NULL, '程', '15738971365', NULL, NULL, NULL, NULL, '2026-06-01 14:40:01', '2026-06-01 14:40:01');

-- ----------------------------
-- Table structure for order_items
-- ----------------------------
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items`  (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `order_id` bigint NOT NULL,
                                `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
                                `sku_id` bigint NOT NULL,
                                `spu_id` bigint NOT NULL,
                                `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
                                `product_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片',
                                `sku_specs` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SKU规格',
                                `price` decimal(10, 2) NOT NULL COMMENT '单价',
                                `quantity` int NOT NULL COMMENT '数量',
                                `total_amount` decimal(10, 2) NOT NULL COMMENT '小计',
                                `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                `gift_flag` tinyint NULL DEFAULT 0 COMMENT '是否赠品',
                                PRIMARY KEY (`id`) USING BTREE,
                                INDEX `sku_id`(`sku_id` ASC) USING BTREE,
                                INDEX `spu_id`(`spu_id` ASC) USING BTREE,
                                INDEX `idx_order_items_order`(`order_id` ASC) USING BTREE,
                                CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`sku_id`) REFERENCES `sku` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                CONSTRAINT `order_items_ibfk_3` FOREIGN KEY (`spu_id`) REFERENCES `spu` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_items
-- ----------------------------
INSERT INTO `order_items` VALUES (17, 17, '17801076559450232A8C0', 250, 45, '小米14 Pro2', '2026/05/26/537f9754-aed2-4988-bfbb-754068c2c0c6_dog111.jpg', '颜色:白色 运行内存:16G 存储容量:1TB', 4000.00, 2, 80.00, '2026-05-30 10:20:56', 0);
INSERT INTO `order_items` VALUES (18, 17, '17801076559450232A8C0', 34, 3, '小米14 Pro1', '2026/05/08/451cf586-ee5e-4c6d-9072-f92dc46b16a0_小米14主图_.webp', '颜色:粉色 运行内存:16G 存储容量:1TB', 5999.00, 2, 11998.00, '2026-05-30 10:20:56', 0);
INSERT INTO `order_items` VALUES (19, 18, '1780141226687580F09A0', 247, 45, '小米14 Pro2', '2026/05/26/537f9754-aed2-4988-bfbb-754068c2c0c6_dog111.jpg', '颜色:黑色 运行内存:16G 存储容量:512GB', 3499.00, 2, 80.00, '2026-05-30 19:40:27', 0);
INSERT INTO `order_items` VALUES (20, 18, '1780141226687580F09A0', 34, 3, '小米14 Pro1', '2026/05/08/451cf586-ee5e-4c6d-9072-f92dc46b16a0_小米14主图_.webp', '颜色:粉色 运行内存:16G 存储容量:1TB', 5999.00, 1, 5999.00, '2026-05-30 19:40:27', 0);
INSERT INTO `order_items` VALUES (21, 19, '178028062300920A5667C', 248, 45, '小米14 Pro2', '2026/05/26/537f9754-aed2-4988-bfbb-754068c2c0c6_dog111.jpg', '颜色:黑色 运行内存:16G 存储容量:1TB', 4000.00, 3, 12000.00, '2026-06-01 10:23:43', 0);
INSERT INTO `order_items` VALUES (22, 20, '1780280771892C76C638A', 23, 3, '小米14 Pro1', '2026/05/08/451cf586-ee5e-4c6d-9072-f92dc46b16a0_小米14主图_.webp', '颜色:黑色 运行内存:12G 存储容量:256GB', 4299.00, 1, 4299.00, '2026-06-01 10:26:12', 0);

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
                           `user_id` bigint NOT NULL,
                           `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
                           `pay_amount` decimal(10, 2) NOT NULL COMMENT '实付金额',
                           `discount_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '优惠金额',
                           `freight_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '运费',
                           `status` tinyint NULL DEFAULT 1 COMMENT '订单状态: 1-待付款 2-待发货 3-待收货 4-已完成 5-已取消 6-退款中 7-已退款',
                           `pay_status` tinyint NULL DEFAULT 0 COMMENT '支付状态: 0-未支付 1-已支付 2-已退款',
                           `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
                           `expire_time` datetime NULL DEFAULT NULL COMMENT '支付截止时间',
                           `pay_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式: alipay-支付宝 wechat-微信',
                           `delivery_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配送方式',
                           `delivery_company` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流公司',
                           `delivery_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流单号',
                           `delivery_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
                           `receive_time` datetime NULL DEFAULT NULL COMMENT '收货时间',
                           `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人',
                           `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人电话',
                           `receiver_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货地址',
                           `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
                           `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消原因',
                           `version` int NULL DEFAULT 1 COMMENT '乐观锁版本号',
                           PRIMARY KEY (`id`) USING BTREE,
                           UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
                           INDEX `idx_orders_user`(`user_id` ASC) USING BTREE,
                           INDEX `idx_orders_order_no`(`order_no` ASC) USING BTREE,
                           INDEX `idx_orders_status`(`status` ASC) USING BTREE,
                           INDEX `idx_orders_created`(`created_at` ASC) USING BTREE,
                           CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (17, '17801076559450232A8C0', 4, 19998.00, 19998.00, 0.00, 0.00, 4, 1, '2026-05-30 10:20:58', 'alipay', NULL, '顺丰速运', '147258', '2026-05-30 10:21:23', '2026-05-30 10:21:33', '程', '15738971365', '北京市北京市朝阳区淮滨', '', '2026-05-30 10:20:56', '2026-06-01 14:49:09', NULL, 4);
INSERT INTO `orders` VALUES (18, '1780141226687580F09A0', 4, 12997.00, 12997.00, 0.00, 0.00, 4, 1, '2026-05-30 19:40:28', 'alipay', NULL, '中通快递', '23343', '2026-05-30 19:41:03', '2026-05-30 19:41:08', '程', '15738971365', '北京市北京市朝阳区淮滨', '', '2026-05-30 19:40:27', '2026-06-01 14:50:06', NULL, 4);
INSERT INTO `orders` VALUES (19, '178028062300920A5667C', 4, 12000.00, 12000.00, 0.00, 0.00, 4, 1, '2026-06-01 14:39:47', 'alipay', NULL, '顺丰速运', '123321', '2026-06-01 14:40:01', '2026-06-01 14:40:08', '程', '15738971365', '北京市北京市朝阳区淮滨', '', '2026-06-01 10:23:43', '2026-06-01 14:40:08', NULL, 4);
INSERT INTO `orders` VALUES (20, '1780280771892C76C638A', 4, 4299.00, 4299.00, 0.00, 0.00, 1, 0, NULL, 'alipay', NULL, NULL, NULL, NULL, NULL, '程', '15738971365', '北京市北京市朝阳区淮滨', '', '2026-06-01 10:26:12', '2026-06-01 10:26:12', NULL, 1);

-- ----------------------------
-- Table structure for payment_records
-- ----------------------------
DROP TABLE IF EXISTS `payment_records`;
CREATE TABLE `payment_records`  (
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
                                    `transaction_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '第三方交易号',
                                    `pay_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付方式',
                                    `pay_amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
                                    `status` tinyint NULL DEFAULT 0 COMMENT '支付状态: 0-待支付 1-成功 2-失败',
                                    `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
                                    `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                    `user_id` bigint NULL DEFAULT NULL COMMENT '支付用户',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    INDEX `order_no`(`order_no` ASC) USING BTREE,
                                    INDEX `user_id`(`user_id` ASC) USING BTREE,
                                    CONSTRAINT `payment_records_ibfk_1` FOREIGN KEY (`order_no`) REFERENCES `orders` (`order_no`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                    CONSTRAINT `payment_records_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of payment_records
-- ----------------------------

-- ----------------------------
-- Table structure for permissions
-- ----------------------------
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions`  (
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
                                `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限编码',
                                `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限类型: menu-菜单 button-按钮 api-接口',
                                `parent_id` bigint NULL DEFAULT 0 COMMENT '父权限ID,0为顶级',
                                `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径',
                                `component` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端组件路径',
                                `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
                                `sort` int NULL DEFAULT 0 COMMENT '排序',
                                `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
                                `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE INDEX `code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 95 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permissions
-- ----------------------------
INSERT INTO `permissions` VALUES (1, '系统管理', 'system', 'menu', 0, '/system', 'Layout', 'Setting', 100, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (2, '用户管理', 'system:user', 'menu', 1, '/system/user', 'system/User', 'User', 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (3, '角色管理', 'system:role', 'menu', 1, '/system/role', 'system/Role', 'Role', 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (4, '权限管理', 'system:permission', 'menu', 1, '/system/permission', 'system/Permission', 'Lock', 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (5, '菜单管理', 'system:menu', 'menu', 1, '/system/menu', 'system/Menu', 'Menu', 4, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (6, '新增用户', 'system:user:add', 'button', 2, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (7, '编辑用户', 'system:user:edit', 'button', 2, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (8, '删除用户', 'system:user:delete', 'button', 2, NULL, NULL, NULL, 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (9, '分配角色', 'system:user:assign', 'button', 2, NULL, NULL, NULL, 4, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (10, '新增角色', 'system:role:add', 'button', 3, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (11, '编辑角色', 'system:role:edit', 'button', 3, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (12, '删除角色', 'system:role:delete', 'button', 3, NULL, NULL, NULL, 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (13, '分配权限', 'system:role:assign', 'button', 3, NULL, NULL, NULL, 4, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (14, '新增权限', 'system:permission:add', 'button', 4, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (15, '编辑权限', 'system:permission:edit', 'button', 4, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (16, '商品管理', 'product', 'menu', 0, '/product', 'Layout', 'Goods', 90, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (17, '商品列表', 'product:list', 'menu', 16, '/product/list', 'product/ProductList', 'List', 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (18, '商品分类', 'product:category', 'menu', 16, '/product/category', 'product/Category', 'Category', 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (19, '商品品牌', 'product:brand', 'menu', 16, '/product/brand', 'product/Brand', 'Brand', 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (20, '商品评价', 'product:review', 'menu', 16, '/product/review', 'product/Review', 'Comment', 4, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (21, '新增商品', 'product:add', 'button', 17, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (22, '编辑商品', 'product:edit', 'button', 17, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (23, '删除商品', 'product:delete', 'button', 17, NULL, NULL, NULL, 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (24, '上架商品', 'product:onShelf', 'button', 17, NULL, NULL, NULL, 4, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (25, '下架商品', 'product:offShelf', 'button', 17, NULL, NULL, NULL, 5, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (26, '新增分类', 'product:category:add', 'button', 18, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (27, '编辑分类', 'product:category:edit', 'button', 18, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (28, '删除分类', 'product:category:delete', 'button', 18, NULL, NULL, NULL, 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (29, '新增品牌', 'product:brand:add', 'button', 19, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (30, '编辑品牌', 'product:brand:edit', 'button', 19, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (31, '删除品牌', 'product:brand:delete', 'button', 19, NULL, NULL, NULL, 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (32, '回复评价', 'product:review:reply', 'button', 20, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (33, '删除评价', 'product:review:delete', 'button', 20, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (34, 'SPU管理', 'spu', 'menu', 16, '/product/spu', 'product/SPU', 'Document', 5, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (35, '新增SPU', 'spu:add', 'button', 34, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (36, '编辑SPU', 'spu:edit', 'button', 34, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (37, '删除SPU', 'spu:delete', 'button', 34, NULL, NULL, NULL, 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (38, 'SKU管理', 'sku', 'menu', 16, '/product/sku', 'product/SKU', 'Tickets', 6, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (39, '新增SKU', 'sku:add', 'button', 38, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (40, '编辑SKU', 'sku:edit', 'button', 38, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (41, '订单管理', 'order', 'menu', 0, '/order', 'Layout', 'Order', 80, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (42, '订单列表', 'order:list', 'menu', 41, '/order/list', 'order/OrderList', 'List', 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (43, '待处理订单', 'order:pending', 'menu', 41, '/order/pending', 'order/Pending', 'Clock', 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (44, '售后管理', 'order:afterSale', 'menu', 41, '/order/afterSale', 'order/AfterSale', 'Service', 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (45, '查看订单', 'order:view', 'button', 42, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (46, '发货', 'order:ship', 'button', 42, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (47, '修改订单', 'order:edit', 'button', 42, NULL, NULL, NULL, 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (48, '取消订单', 'order:cancel', 'button', 42, NULL, NULL, NULL, 4, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (49, '退款', 'order:refund', 'button', 43, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (50, '同意售后', 'order:afterSale:agree', 'button', 44, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (51, '拒绝售后', 'order:afterSale:reject', 'button', 44, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (52, '会员管理', 'member', 'menu', 0, '/member', 'Layout', 'User', 70, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (53, '会员列表', 'member:list', 'menu', 52, '/member/list', 'member/MemberList', 'List', 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (54, '会员等级', 'member:level', 'menu', 52, '/member/level', 'member/Level', 'Medal', 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (55, '会员地址', 'member:address', 'menu', 52, '/member/address', 'member/Address', 'Location', 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (56, '查看会员', 'member:view', 'button', 53, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (57, '编辑会员', 'member:edit', 'button', 53, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (58, '禁用会员', 'member:disable', 'button', 53, NULL, NULL, NULL, 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (59, '新增等级', 'member:level:add', 'button', 54, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (60, '编辑等级', 'member:level:edit', 'button', 54, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (61, '数据统计', 'statistics', 'menu', 0, '/statistics', 'Layout', 'DataAnalysis', 60, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (62, '销售统计', 'statistics:sales', 'menu', 61, '/statistics/sales', 'statistics/Sales', 'TrendCharts', 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (63, '商品统计', 'statistics:product', 'menu', 61, '/statistics/product', 'statistics/Product', 'Goods', 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (64, '用户统计', 'statistics:user', 'menu', 61, '/statistics/user', 'statistics/User', 'User', 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (65, '导出数据', 'statistics:export', 'button', 61, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (66, '内容管理', 'content', 'menu', 0, '/content', 'Layout', 'Document', 50, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (67, '轮播图管理', 'content:banner', 'menu', 66, '/content/banner', 'content/Banner', 'Picture', 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (68, '公告管理', 'content:notice', 'menu', 66, '/content/notice', 'content/Notice', 'Bell', 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (69, '帮助中心', 'content:help', 'menu', 66, '/content/help', 'content/Help', 'QuestionFilled', 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (70, '新增轮播图', 'content:banner:add', 'button', 67, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (71, '编辑轮播图', 'content:banner:edit', 'button', 67, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (72, '删除轮播图', 'content:banner:delete', 'button', 67, NULL, NULL, NULL, 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (73, '新增公告', 'content:notice:add', 'button', 68, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (74, '编辑公告', 'content:notice:edit', 'button', 68, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (75, '删除公告', 'content:notice:delete', 'button', 68, NULL, NULL, NULL, 3, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (76, '客服管理', 'service', 'menu', 0, '/service', 'Layout', 'Service', 40, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (77, '会话管理', 'service:chat', 'menu', 76, '/service/chat', 'service/Chat', 'ChatDotRound', 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (78, '评价管理', 'service:evaluate', 'menu', 76, '/service/evaluate', 'service/Evaluate', 'Star', 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (79, '查看会话', 'service:chat:view', 'button', 77, NULL, NULL, NULL, 1, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (80, '结束会话', 'service:chat:close', 'button', 77, NULL, NULL, NULL, 2, 1, '2026-05-13 15:19:19', '2026-05-13 15:19:19');
INSERT INTO `permissions` VALUES (81, '订单发货', 'order:deliver', 'api', 41, NULL, NULL, NULL, 10, 1, '2026-05-16 19:58:05', '2026-05-16 19:58:05');
INSERT INTO `permissions` VALUES (82, '新增发货记录', 'order:delivery:add', 'api', 41, NULL, NULL, NULL, 11, 1, '2026-05-16 19:58:05', '2026-05-16 19:58:05');
INSERT INTO `permissions` VALUES (83, '编辑发货记录', 'order:delivery:edit', 'api', 41, NULL, NULL, NULL, 12, 1, '2026-05-16 19:58:05', '2026-05-16 19:58:05');
INSERT INTO `permissions` VALUES (84, '查询发货记录', 'order:delivery:query', 'api', 41, NULL, NULL, NULL, 13, 1, '2026-05-16 19:58:05', '2026-05-16 19:58:05');
INSERT INTO `permissions` VALUES (85, '物流公司管理', 'system:logistics', 'menu', 1, NULL, NULL, NULL, 5, 1, '2026-05-16 19:58:05', '2026-05-16 19:58:05');
INSERT INTO `permissions` VALUES (86, '新增物流公司', 'system:logistics:add', 'api', 81, NULL, NULL, NULL, 1, 1, '2026-05-16 19:58:05', '2026-05-16 19:58:05');
INSERT INTO `permissions` VALUES (87, '编辑物流公司', 'system:logistics:edit', 'api', 81, NULL, NULL, NULL, 2, 1, '2026-05-16 19:58:05', '2026-05-16 19:58:05');
INSERT INTO `permissions` VALUES (88, '删除物流公司', 'system:logistics:delete', 'api', 81, NULL, NULL, NULL, 3, 1, '2026-05-16 19:58:05', '2026-05-16 19:58:05');
INSERT INTO `permissions` VALUES (89, '查询物流公司', 'system:logistics:query', 'api', 81, NULL, NULL, NULL, 4, 1, '2026-05-16 19:58:05', '2026-05-16 19:58:05');
INSERT INTO `permissions` VALUES (90, '店铺管理', 'store:manage', NULL, 0, NULL, NULL, NULL, 0, 1, '2026-05-17 22:21:35', '2026-05-17 22:21:35');
INSERT INTO `permissions` VALUES (91, '商品管理', 'store:product', NULL, 0, NULL, NULL, NULL, 0, 1, '2026-05-17 22:21:35', '2026-05-17 22:21:35');
INSERT INTO `permissions` VALUES (92, '订单管理', 'store:order', NULL, 0, NULL, NULL, NULL, 0, 1, '2026-05-17 22:21:35', '2026-05-17 22:21:35');
INSERT INTO `permissions` VALUES (93, '客服管理', 'store:customer', NULL, 0, NULL, NULL, NULL, 0, 1, '2026-05-17 22:21:35', '2026-05-17 22:21:35');
INSERT INTO `permissions` VALUES (94, '财务管理', 'store:finance', NULL, 0, NULL, NULL, NULL, 0, 1, '2026-05-17 22:21:35', '2026-05-17 22:21:35');

-- ----------------------------
-- Table structure for refund_records
-- ----------------------------
DROP TABLE IF EXISTS `refund_records`;
CREATE TABLE `refund_records`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
                                   `refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '退款号',
                                   `refund_amount` decimal(10, 2) NOT NULL COMMENT '退款金额',
                                   `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退款原因',
                                   `status` tinyint NULL DEFAULT 0 COMMENT '退款状态: 0-待处理 1-处理中 2-已完成 3-已拒绝',
                                   `handle_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理备注',
                                   `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
                                   `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `order_no`(`order_no` ASC) USING BTREE,
                                   CONSTRAINT `refund_records_ibfk_1` FOREIGN KEY (`order_no`) REFERENCES `orders` (`order_no`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退款记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of refund_records
-- ----------------------------

-- ----------------------------
-- Table structure for reviews
-- ----------------------------
DROP TABLE IF EXISTS `reviews`;
CREATE TABLE `reviews`  (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `user_id` bigint NOT NULL,
                            `order_item_id` bigint NOT NULL COMMENT '订单明细ID',
                            `spu_id` bigint NOT NULL,
                            `rating` tinyint NOT NULL COMMENT '评分: 1-5',
                            `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
                            `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价图片(JSON格式)',
                            `is_anonymous` tinyint NULL DEFAULT 0 COMMENT '是否匿名: 1-是 0-否',
                            `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商家回复',
                            `reply_time` datetime NULL DEFAULT NULL COMMENT '回复时间',
                            `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-显示 0-隐藏',
                            `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`) USING BTREE,
                            INDEX `user_id`(`user_id` ASC) USING BTREE,
                            INDEX `spu_id`(`spu_id` ASC) USING BTREE,
                            CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                            CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`spu_id`) REFERENCES `spu` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reviews
-- ----------------------------

-- ----------------------------
-- Table structure for role_permissions
-- ----------------------------
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions`  (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `role_id` bigint NOT NULL,
                                     `permission_id` bigint NOT NULL,
                                     `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                     PRIMARY KEY (`id`) USING BTREE,
                                     UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE,
                                     INDEX `permission_id`(`permission_id` ASC) USING BTREE,
                                     CONSTRAINT `role_permissions_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                     CONSTRAINT `role_permissions_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 228 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permissions
-- ----------------------------
INSERT INTO `role_permissions` VALUES (1, 1, 1, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (2, 1, 2, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (3, 1, 3, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (4, 1, 4, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (5, 1, 5, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (6, 1, 6, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (7, 1, 7, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (8, 1, 8, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (9, 1, 9, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (10, 1, 10, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (11, 1, 11, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (12, 1, 12, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (13, 1, 13, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (14, 1, 14, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (15, 1, 15, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (16, 1, 16, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (17, 1, 17, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (18, 1, 18, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (19, 1, 19, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (20, 1, 20, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (21, 1, 21, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (22, 1, 22, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (23, 1, 23, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (24, 1, 24, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (25, 1, 25, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (26, 1, 26, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (27, 1, 27, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (28, 1, 28, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (29, 1, 29, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (30, 1, 30, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (31, 1, 31, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (32, 1, 32, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (33, 1, 33, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (34, 1, 34, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (35, 1, 35, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (36, 1, 36, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (37, 1, 37, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (38, 1, 38, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (39, 1, 39, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (40, 1, 40, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (41, 1, 41, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (42, 1, 42, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (43, 1, 43, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (44, 1, 44, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (45, 1, 45, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (46, 1, 46, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (47, 1, 47, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (48, 1, 48, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (49, 1, 49, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (50, 1, 50, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (51, 1, 51, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (52, 1, 52, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (53, 1, 53, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (54, 1, 54, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (55, 1, 55, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (56, 1, 56, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (57, 1, 57, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (58, 1, 58, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (59, 1, 59, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (60, 1, 60, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (61, 1, 61, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (62, 1, 62, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (63, 1, 63, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (64, 1, 64, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (65, 1, 65, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (66, 1, 66, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (67, 1, 67, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (68, 1, 68, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (69, 1, 69, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (70, 1, 70, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (71, 1, 71, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (72, 1, 72, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (73, 1, 73, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (74, 1, 74, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (75, 1, 75, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (76, 1, 76, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (77, 1, 77, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (78, 1, 78, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (79, 1, 79, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (80, 1, 80, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (81, 2, 16, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (82, 2, 17, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (83, 2, 18, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (84, 2, 19, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (85, 2, 20, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (86, 2, 21, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (87, 2, 22, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (88, 2, 23, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (89, 2, 24, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (90, 2, 25, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (91, 2, 26, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (92, 2, 27, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (93, 2, 28, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (94, 2, 29, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (95, 2, 30, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (96, 2, 31, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (97, 2, 32, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (98, 2, 33, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (99, 2, 34, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (100, 2, 35, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (101, 2, 36, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (102, 2, 37, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (103, 2, 38, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (104, 2, 39, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (105, 2, 40, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (106, 2, 41, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (107, 2, 42, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (108, 2, 43, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (109, 2, 44, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (110, 2, 45, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (111, 2, 46, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (112, 2, 47, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (113, 2, 48, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (114, 2, 49, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (115, 2, 50, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (116, 2, 51, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (117, 2, 52, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (118, 2, 53, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (119, 2, 54, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (120, 2, 55, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (121, 2, 56, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (122, 2, 57, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (123, 2, 58, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (124, 2, 59, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (125, 2, 60, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (126, 2, 61, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (127, 2, 62, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (128, 2, 63, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (129, 2, 64, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (130, 2, 65, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (131, 2, 66, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (132, 2, 67, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (133, 2, 68, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (134, 2, 69, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (135, 2, 70, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (136, 2, 71, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (137, 2, 72, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (138, 2, 73, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (139, 2, 74, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (140, 2, 75, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (141, 3, 16, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (142, 3, 17, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (143, 3, 21, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (144, 3, 22, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (145, 3, 23, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (146, 3, 24, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (147, 3, 25, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (148, 3, 34, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (149, 3, 35, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (150, 3, 36, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (151, 3, 37, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (152, 3, 38, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (153, 3, 39, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (154, 3, 40, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (155, 4, 41, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (156, 4, 42, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (157, 4, 45, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (158, 4, 46, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (159, 4, 47, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (160, 4, 48, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (161, 5, 41, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (162, 5, 42, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (163, 5, 43, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (164, 5, 44, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (165, 5, 45, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (166, 5, 49, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (167, 5, 50, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (168, 5, 51, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (169, 5, 76, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (170, 5, 77, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (171, 5, 78, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (172, 5, 79, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (173, 5, 80, '2026-05-13 15:20:54');
INSERT INTO `role_permissions` VALUES (174, 1, 81, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (175, 1, 82, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (176, 1, 83, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (177, 1, 84, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (178, 1, 86, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (179, 1, 88, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (180, 1, 87, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (181, 1, 89, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (189, 2, 81, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (190, 2, 82, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (191, 2, 83, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (192, 2, 84, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (193, 2, 86, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (194, 2, 88, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (195, 2, 87, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (196, 2, 89, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (204, 3, 81, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (205, 5, 84, '2026-05-16 19:58:21');
INSERT INTO `role_permissions` VALUES (206, 3, 93, '2026-05-17 22:21:53');
INSERT INTO `role_permissions` VALUES (207, 3, 94, '2026-05-17 22:21:53');
INSERT INTO `role_permissions` VALUES (208, 3, 90, '2026-05-17 22:21:53');
INSERT INTO `role_permissions` VALUES (209, 3, 92, '2026-05-17 22:21:53');
INSERT INTO `role_permissions` VALUES (210, 3, 91, '2026-05-17 22:21:53');
INSERT INTO `role_permissions` VALUES (211, 6, 16, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (212, 6, 17, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (213, 6, 21, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (214, 6, 22, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (215, 6, 24, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (216, 6, 25, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (217, 6, 41, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (218, 6, 42, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (219, 6, 43, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (220, 6, 44, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (221, 6, 45, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (222, 6, 46, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (223, 6, 50, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (224, 6, 51, '2026-05-20 15:32:10');
INSERT INTO `role_permissions` VALUES (226, 5, 20, '2026-05-20 15:35:30');
INSERT INTO `role_permissions` VALUES (227, 5, 32, '2026-05-20 15:35:30');

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
                          `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
                          `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '角色描述',
                          `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
                          `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`) USING BTREE,
                          UNIQUE INDEX `name`(`name` ASC) USING BTREE,
                          UNIQUE INDEX `code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, '超级管理员', 'SUPER_ADMIN', '系统最高权限，管理所有功能', 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `roles` VALUES (2, '运营管理员', 'ADMIN', '负责日常运营管理', 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `roles` VALUES (3, '普通卖家', 'SELLER', '商家用户，管理自己的商品', 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `roles` VALUES (4, '普通用户', 'USER', '前台注册用户', 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `roles` VALUES (5, '客服人员', 'CUSTOMER_SERVICE', '处理订单售后和咨询', 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `roles` VALUES (6, '店铺管理员', 'STORE_ADMIN', '店铺日常运营管理，可管理商品和订单', 1, '2026-05-20 15:23:38', '2026-05-20 15:23:38');

-- ----------------------------
-- Table structure for sku
-- ----------------------------
DROP TABLE IF EXISTS `sku`;
CREATE TABLE `sku`  (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `spu_id` bigint NOT NULL COMMENT 'SPU ID',
                        `price` decimal(10, 2) NOT NULL COMMENT '价格',
                        `market_price` decimal(10, 2) NOT NULL COMMENT '市场价',
                        `cost_price` decimal(10, 2) NOT NULL COMMENT '成本价',
                        `stock` int NULL DEFAULT 0 COMMENT '库存',
  `frozen_stock` int NULL DEFAULT 0 COMMENT '冻结库存（预扣）',
  `warn_stock` int NULL DEFAULT 10 COMMENT '预警库存',
                        `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SKU图片',
                        `weight` decimal(10, 3) NULL DEFAULT NULL COMMENT '重量(kg)',
                        `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
                        `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
                        `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`) USING BTREE,
                        INDEX `idx_sku_spu`(`spu_id` ASC) USING BTREE,
                        INDEX `idx_sku_price`(`price` ASC) USING BTREE,
                        CONSTRAINT `sku_ibfk_1` FOREIGN KEY (`spu_id`) REFERENCES `spu` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 257 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品SKU表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sku
-- ----------------------------
INSERT INTO `sku` VALUES (23, 3, 4299.00, 4999.00, 3200.00, 98, 10, NULL, 0.193, 1, 0, '2026-05-08 16:57:08', '2026-06-01 10:10:48');
INSERT INTO `sku` VALUES (24, 3, 4999.00, 5999.00, 4000.00, 80, 10, NULL, 0.193, 1, 0, '2026-05-08 16:57:08', '2026-06-01 10:10:48');
INSERT INTO `sku` VALUES (25, 3, 5999.00, 6999.00, 5000.00, 45, 10, NULL, 0.193, 1, 0, '2026-05-08 16:57:08', '2026-06-01 10:10:48');
INSERT INTO `sku` VALUES (26, 3, 4299.00, 4999.00, 3300.00, 90, 10, NULL, 0.193, 1, 0, '2026-05-08 16:57:08', '2026-06-01 10:10:48');
INSERT INTO `sku` VALUES (27, 3, 4999.00, 5999.00, 4000.00, 70, 10, NULL, 0.193, 1, 0, '2026-05-08 16:57:08', '2026-06-01 10:10:48');
INSERT INTO `sku` VALUES (28, 3, 5999.00, 6999.00, 5000.00, 37, 10, NULL, 0.193, 1, 0, '2026-05-08 16:57:08', '2026-06-01 10:10:49');
INSERT INTO `sku` VALUES (29, 3, 4299.00, 4999.00, 3200.00, 60, 10, NULL, 0.193, 1, 0, '2026-05-08 16:57:08', '2026-06-01 10:10:49');
INSERT INTO `sku` VALUES (30, 3, 4999.00, 5999.00, 4000.00, 49, 10, NULL, 0.193, 1, 0, '2026-05-08 16:57:08', '2026-06-01 10:10:49');
INSERT INTO `sku` VALUES (31, 3, 5999.00, 6999.00, 5000.00, 30, 10, NULL, 0.193, 1, 0, '2026-05-08 16:57:08', '2026-06-01 10:10:49');
INSERT INTO `sku` VALUES (32, 3, 4299.00, 4999.00, 3200.00, 55, 10, NULL, 0.193, 1, 0, '2026-05-08 16:57:08', '2026-06-01 10:10:49');
INSERT INTO `sku` VALUES (33, 3, 4999.00, 5999.00, 4000.00, 45, 10, NULL, 0.193, 1, 0, '2026-05-08 16:57:08', '2026-06-01 10:10:49');
INSERT INTO `sku` VALUES (34, 3, 5999.00, 6999.00, 5000.00, 19, 10, NULL, 0.193, 1, 0, '2026-05-08 16:57:08', '2026-06-01 10:10:49');
INSERT INTO `sku` VALUES (223, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (224, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (225, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (226, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (227, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (228, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (229, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (230, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (231, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (232, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (233, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (234, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (235, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (236, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (237, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (238, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (239, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (240, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (241, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (242, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (243, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (244, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (245, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (246, 47, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku` VALUES (247, 45, 3499.00, 3600.00, 2000.00, 91, 10, NULL, NULL, 1, 0, '2026-05-28 13:52:07', '2026-05-31 22:04:42');
INSERT INTO `sku` VALUES (248, 45, 4000.00, 4200.00, 2300.00, 97, 10, NULL, NULL, 1, 0, '2026-05-28 13:52:07', '2026-05-30 21:05:48');
INSERT INTO `sku` VALUES (249, 45, 3500.00, 3600.00, 2000.00, 98, 10, NULL, NULL, 1, 0, '2026-05-28 13:52:07', '2026-05-30 21:05:48');
INSERT INTO `sku` VALUES (250, 45, 4000.00, 4200.00, 2300.00, 95, 10, NULL, NULL, 1, 0, '2026-05-28 13:52:07', '2026-05-30 21:05:48');
INSERT INTO `sku` VALUES (251, 45, 3500.00, 3600.00, 2000.00, 100, 10, NULL, NULL, 1, 0, '2026-05-28 13:52:07', '2026-05-30 21:05:48');
INSERT INTO `sku` VALUES (252, 45, 4000.00, 4200.00, 2300.00, 99, 10, NULL, NULL, 1, 0, '2026-05-28 13:52:07', '2026-05-30 21:05:48');
INSERT INTO `sku` VALUES (253, 45, 3500.00, 3600.00, 2000.00, 100, 10, NULL, NULL, 1, 0, '2026-05-28 13:52:07', '2026-05-30 21:05:48');
INSERT INTO `sku` VALUES (254, 45, 4000.00, 4200.00, 2300.00, 100, 10, NULL, NULL, 0, 0, '2026-05-28 13:52:07', '2026-05-30 21:05:48');
INSERT INTO `sku` VALUES (255, 52, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 15:55:38', '2026-05-28 15:55:38');
INSERT INTO `sku` VALUES (256, 52, 0.00, 0.00, 0.00, 0, 0, NULL, NULL, 1, 0, '2026-05-28 15:55:38', '2026-05-28 15:55:38');

-- ----------------------------
-- Table structure for sku_sale_attr_values
-- ----------------------------
DROP TABLE IF EXISTS `sku_sale_attr_values`;
CREATE TABLE `sku_sale_attr_values`  (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `sku_id` bigint NOT NULL COMMENT 'SKU ID',
                                         `attr_value_id` bigint NOT NULL COMMENT '属性值ID（指向attribute_values）',
                                         `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                         `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         PRIMARY KEY (`id`) USING BTREE,
                                         UNIQUE INDEX `uk_sku_attr_value`(`sku_id` ASC, `attr_value_id` ASC) USING BTREE,
                                         INDEX `idx_sku_id`(`sku_id` ASC) USING BTREE,
                                         INDEX `idx_attr_value_id`(`attr_value_id` ASC) USING BTREE,
                                         CONSTRAINT `fk_sku_sale_attr_sku` FOREIGN KEY (`sku_id`) REFERENCES `sku` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                         CONSTRAINT `fk_sku_sale_attr_value` FOREIGN KEY (`attr_value_id`) REFERENCES `attribute_values` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 373 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SKU与销售属性值关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sku_sale_attr_values
-- ----------------------------
INSERT INTO `sku_sale_attr_values` VALUES (1, 23, 1, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (2, 23, 5, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (3, 23, 7, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (4, 24, 1, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (5, 24, 6, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (6, 24, 8, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (7, 25, 1, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (8, 25, 6, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (9, 25, 9, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (10, 26, 2, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (11, 26, 5, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (12, 26, 7, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (13, 27, 2, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (14, 27, 6, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (15, 27, 8, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (16, 28, 2, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (17, 28, 6, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (18, 28, 9, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (19, 29, 3, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (20, 29, 5, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (21, 29, 7, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (22, 30, 3, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (23, 30, 6, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (24, 30, 8, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (25, 31, 3, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (26, 31, 6, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (27, 31, 9, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (28, 32, 4, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (29, 32, 5, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (30, 32, 7, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (31, 33, 4, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (32, 33, 6, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (33, 33, 8, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (34, 34, 4, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (35, 34, 6, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (36, 34, 9, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `sku_sale_attr_values` VALUES (274, 224, 1, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (275, 224, 5, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (276, 224, 8, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (277, 225, 1, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (278, 225, 5, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (279, 225, 9, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (280, 226, 1, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (281, 226, 6, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (282, 226, 7, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (283, 227, 1, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (284, 227, 6, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (285, 227, 8, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (286, 228, 1, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (287, 228, 6, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (288, 228, 9, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (289, 229, 2, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (290, 229, 5, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (291, 229, 7, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (292, 230, 2, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (293, 230, 5, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (294, 230, 8, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (295, 231, 2, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (296, 231, 5, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (297, 231, 9, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (298, 232, 2, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (299, 232, 6, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (300, 232, 7, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (301, 233, 2, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (302, 233, 6, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (303, 233, 8, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (304, 234, 2, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (305, 234, 6, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (306, 234, 9, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (307, 235, 3, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (308, 235, 5, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (309, 235, 7, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (310, 236, 3, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (311, 236, 5, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (312, 236, 8, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (313, 237, 3, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (314, 237, 5, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (315, 237, 9, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (316, 238, 3, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (317, 238, 6, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (318, 238, 7, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (319, 239, 3, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (320, 239, 6, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (321, 239, 8, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (322, 240, 3, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (323, 240, 6, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (324, 240, 9, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (325, 241, 4, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (326, 241, 5, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (327, 241, 7, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (328, 242, 4, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (329, 242, 5, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (330, 242, 8, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (331, 243, 4, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (332, 243, 5, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (333, 243, 9, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (334, 244, 4, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (335, 244, 6, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (336, 244, 7, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (337, 245, 4, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (338, 245, 6, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (339, 245, 8, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (340, 246, 4, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (341, 246, 6, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (342, 246, 9, '2026-05-28 09:04:47', '2026-05-28 09:04:47');
INSERT INTO `sku_sale_attr_values` VALUES (343, 247, 1, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (344, 247, 6, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (345, 247, 8, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (346, 248, 1, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (347, 248, 6, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (348, 248, 9, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (349, 249, 2, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (350, 249, 6, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (351, 249, 8, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (352, 250, 2, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (353, 250, 6, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (354, 250, 9, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (355, 251, 3, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (356, 251, 6, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (357, 251, 8, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (358, 252, 3, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (359, 252, 6, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (360, 252, 9, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (361, 253, 4, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (362, 253, 6, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (363, 253, 8, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (364, 254, 4, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (365, 254, 6, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (366, 254, 9, '2026-05-28 13:52:07', '2026-05-28 13:52:07');
INSERT INTO `sku_sale_attr_values` VALUES (367, 255, 4, '2026-05-28 15:55:38', '2026-05-28 15:55:38');
INSERT INTO `sku_sale_attr_values` VALUES (368, 255, 6, '2026-05-28 15:55:38', '2026-05-28 15:55:38');
INSERT INTO `sku_sale_attr_values` VALUES (369, 255, 9, '2026-05-28 15:55:38', '2026-05-28 15:55:38');
INSERT INTO `sku_sale_attr_values` VALUES (370, 256, 4, '2026-05-28 15:55:38', '2026-05-28 15:55:38');
INSERT INTO `sku_sale_attr_values` VALUES (371, 256, 6, '2026-05-28 15:55:38', '2026-05-28 15:55:38');
INSERT INTO `sku_sale_attr_values` VALUES (372, 256, 8, '2026-05-28 15:55:38', '2026-05-28 15:55:38');

-- ----------------------------
-- Table structure for spu
-- ----------------------------
DROP TABLE IF EXISTS `spu`;
CREATE TABLE `spu`  (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
                        `category_id` bigint NOT NULL COMMENT '分类ID',
                        `brand_id` bigint NULL DEFAULT NULL COMMENT '品牌ID',
                        `seller_id` bigint NOT NULL DEFAULT 0 COMMENT '商家ID',
                        `store_id` bigint NULL DEFAULT NULL COMMENT '店铺ID',
                        `min_price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '最低SKU售价（用于首页列表展示）',
                        `sales` int NULL DEFAULT 0 COMMENT '销量',
                        `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
                        `main_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主图',
                        `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位',
                        `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品图片集(JSON格式)',
                        `keywords` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关键词',
                        `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-上架 0-下架',
                        `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
                        `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`) USING BTREE,
                        INDEX `idx_spu_category`(`category_id` ASC) USING BTREE,
                        INDEX `idx_spu_brand`(`brand_id` ASC) USING BTREE,
                        INDEX `idx_spu_status`(`status` ASC) USING BTREE,
                        INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE,
                        INDEX `idx_store_id`(`store_id` ASC) USING BTREE,
                        CONSTRAINT `spu_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                        CONSTRAINT `spu_ibfk_2` FOREIGN KEY (`brand_id`) REFERENCES `brands` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                        CONSTRAINT `spus_ibfk_store` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 54 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品SPU表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of spu
-- ----------------------------
INSERT INTO `spu` VALUES (3, '小米14 Pro1', 27, 3, 2, NULL, 4299.00, 0, '小米旗舰手机', '2026/05/08/451cf586-ee5e-4c6d-9072-f92dc46b16a0_小米14主图_.webp', '台', '[\"2026/05/08/a4699d83-3f33-41bb-9872-31fd72199f33_小米14_.webp\",\"2026/05/08/2772e4d9-a6df-40cc-817e-03266ee5283a_小米14类型2_.webp\",\"2026/05/08/fb59cd73-4e9e-4768-8f43-f110b8ccbe91_小米14类型3.webp\"]', '小米,14,手机', 1, 0, '2026-05-08 16:51:43', NULL);
INSERT INTO `spu` VALUES (20, '华为 Mate 60 Pro', 27, 1, 0, NULL, 0.00, 1523, '华为旗舰手机，搭载麒麟9000S芯片，支持卫星通信', NULL, '台', NULL, '华为,Mate60,手机,5G', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (21, 'iPhone 15 Pro Max', 27, 2, 0, NULL, 0.00, 2841, '苹果旗舰手机，A17 Pro芯片，钛金属边框', NULL, '台', NULL, '苹果,iPhone15,手机', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (22, '小米14 Ultra', 27, 3, 2, NULL, 0.00, 892, '小米影像旗舰，徕卡四摄，骁龙8Gen3', '', '台', '', '小米14,Ultra,手机,徕卡', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (23, '三星 S24 Ultra', 27, 4, 0, NULL, 0.00, 645, '三星旗舰手机，AI智能，SPen触控笔', NULL, '台', NULL, '三星,S24,手机,AI', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (24, 'OPPO Find X7 Ultra', 27, 7, 0, NULL, 0.00, 456, 'OPPO影像旗舰，双潜望四主摄', NULL, '台', NULL, 'OPPO,FindX7,手机', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (25, 'vivo X100 Pro', 27, 8, 0, NULL, 0.00, 378, 'vivo旗舰手机，蔡司影像，天玑9300', NULL, '台', NULL, 'vivo,X100,手机,蔡司', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (26, '荣耀 Magic6 Pro', 27, 9, 0, NULL, 0.00, 567, '荣耀旗舰手机，鸿燕通信，巨犀玻璃', NULL, '台', NULL, '荣耀,Magic6,手机', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (27, '联想 小新Pro16', 31, 10, 0, NULL, 0.00, 234, '联想轻薄本，13代i7，2.5K高刷屏', NULL, '台', NULL, '联想,小新,笔记本,轻薄本', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (28, '戴尔 XPS 15', 31, 11, 0, NULL, 0.00, 123, '戴尔高端创作本，3.5K OLED触控屏', NULL, '台', NULL, '戴尔,XPS,笔记本,创作本', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (29, '华为 MateBook X Pro', 31, 1, 0, NULL, 0.00, 345, '华为旗舰轻薄本，3.1K原色屏，超级终端', NULL, '台', NULL, '华为,MateBook,笔记本', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (30, 'Apple Watch Series 9', 12, 2, 0, NULL, 0.00, 789, '苹果智能手表，S9芯片，全天候显示', NULL, '只', NULL, '苹果,手表,AppleWatch', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (31, '华为 Watch 4 Pro', 12, 1, 0, NULL, 0.00, 456, '华为智能手表，支持血糖研究，钛金属表壳', NULL, '只', NULL, '华为,手表,Watch4', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (32, '小米手表 S3', 12, 3, 2, NULL, 0.00, 234, '小米智能手表，eSIM独立通话，百变表圈', '2026/05/19/587a8d5d-dd9b-4c0b-ab6d-c52e109364b2_小米14类型3.webp', '只', '[\"2026/05/19/587a8d5d-dd9b-4c0b-ab6d-c52e109364b2_小米14类型3.webp\"]', '小米,手表,小米手表', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (33, '耐克 Air Jordan 1', 46, 5, 0, NULL, 0.00, 567, '耐克经典篮球鞋，复古高帮设计', NULL, '双', NULL, '耐克,AJ1,篮球鞋,乔丹', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (34, '阿迪达斯 Yeezy 350', 46, 6, 0, NULL, 0.00, 345, '阿迪达斯椰子鞋，Boost中底', NULL, '双', NULL, '阿迪达斯,Yeezy,椰子鞋', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (35, '安踏 海沃德4代', 46, 13, 0, NULL, 0.00, 123, '安踏篮球鞋，氮科技中底', NULL, '双', NULL, '安踏,海沃德,篮球鞋', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (36, '李宁 驭帅15', 46, 14, 0, NULL, 0.00, 234, '李宁篮球鞋，䨻科技缓震', NULL, '双', NULL, '李宁,驭帅,篮球鞋', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (37, '小米电视 S Pro 65寸', 35, 3, 2, NULL, 0.00, 567, '小米MiniLED电视，4K 144Hz高刷', '', '台', '', '小米,电视,4K,智能电视', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (38, '海尔 65寸智能电视', 35, 12, 0, NULL, 0.00, 234, '海尔4K超高清电视，智能语音控制', NULL, '台', NULL, '海尔,电视,智能电视', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (39, '格力 云锦三代 1.5匹', 38, 14, 0, NULL, 0.00, 789, '格力变频空调，新一级能效，自清洁', NULL, '台', NULL, '格力,空调,变频,节能', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (40, '美的 酷省电 1.5匹', 38, 13, 0, NULL, 0.00, 456, '美的变频空调，新一级能效，ECO节能', NULL, '台', NULL, '美的,空调,节能,变频', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (41, '三只松鼠 每日坚果', 59, NULL, 0, NULL, 0.00, 2345, '混合坚果礼盒，营养均衡', NULL, '盒', NULL, '坚果,每日坚果,零食', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (42, '茅台 飞天茅台', 62, NULL, 0, NULL, 0.00, 89, '酱香型白酒，53度500ml', NULL, '瓶', NULL, '茅台,飞天,白酒,酱香', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (43, '五粮液 第八代普五', 62, NULL, 0, NULL, 0.00, 156, '浓香型白酒，52度500ml', NULL, '瓶', NULL, '五粮液,白酒,浓香', 0, 0, '2026-05-08 17:09:57', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (44, '小米14 Pro1', 27, 3, 2, NULL, 0.00, 0, '小米旗舰手机', '2026/05/22/1c93170e-e424-4074-a78a-bc8947f04fd3_小米14_.webp', '台', '[\"2026/05/22/1c93170e-e424-4074-a78a-bc8947f04fd3_小米14_.webp\",\"2026/05/22/01c0ede1-092f-44ff-95fb-9b2b434bd4db_小米14类型3.webp\",\"2026/05/22/4a7036c2-e86e-414c-846b-ae3dc5c25643_小米14类型2_.webp\"]', '小米,14,手机', 0, 0, '2026-05-19 22:00:49', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (45, '小米14 Pro2', 27, 3, 2, NULL, 3499.00, 0, '小米旗舰手机66666', '2026/05/26/537f9754-aed2-4988-bfbb-754068c2c0c6_dog111.jpg', '台', '[\"2026/05/20/b47ca256-bd2c-4c66-88f4-efd5e45c20d8_小米14类型3.webp\",\"2026/05/21/ca5a4e29-fcc0-4fde-b7f0-07247017cd4f_小米14_.webp\",\"2026/05/26/7334a201-e02e-42bb-a406-a85e1912b7ec_小米14主图_.webp\"]', '小米,14,手机', 1, 0, '2026-05-19 22:01:20', '2026-05-31 23:06:19');
INSERT INTO `spu` VALUES (46, 'ddd', 27, 1, 2, NULL, 0.00, 0, '手机', NULL, '个', NULL, '手机', 0, 1, '2026-05-20 22:29:48', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (47, 'bb', 27, 3, 2, NULL, 0.00, 0, '嘎嘎嘎', '2026/05/21/8e761d5c-3e03-4aaf-b3a2-79a158ca25c8_dog111.jpg', '个', '[\"2026/05/21/87783a5e-4d8c-42e4-8cc9-ec3a0432b08b_小米14类型3.webp\",\"2026/05/21/8e761d5c-3e03-4aaf-b3a2-79a158ca25c8_dog111.jpg\",\"2026/05/21/26153697-3395-4245-a593-a85a5c219e2c_小米14_.webp\",\"2026/05/21/0d2894f8-7a93-490a-bfdc-f7e88049b2dc_qie1.webp\"]', '手机55', 1, 1, '2026-05-20 22:50:23', '2026-05-31 23:06:27');
INSERT INTO `spu` VALUES (48, '小米', 27, 3, 2, NULL, 0.00, 0, '手机666', NULL, '个', '[\"2026/05/25/65bbe741-7d9f-439c-a279-505e26d6f117_小米14主图_.webp\",\"2026/05/25/a19f339a-6061-4bb9-aa8a-d08ad2a20a95_小米14类型3.webp\",\"2026/05/25/2490c43f-ece5-43c3-8ed0-d666d713ac0d_小米14_.webp\"]', NULL, 0, 1, '2026-05-25 10:51:59', '2026-05-25 14:32:14');
INSERT INTO `spu` VALUES (49, '小米', 27, 3, 2, NULL, 0.00, 0, '手机666', NULL, '个', '[\"2026/05/25/812d03af-2c02-4615-933b-07ccf3f793f3_小米14主图_.webp\",\"2026/05/25/bde49b6e-2393-41a8-b014-461488d63b06_小米14类型3.webp\",\"2026/05/25/fbb653e6-f4e5-46b0-8d43-a3716daea78b_小米14_.webp\"]', NULL, 0, 1, '2026-05-25 10:52:15', '2026-05-25 14:32:09');
INSERT INTO `spu` VALUES (50, '手机55', 27, 3, 2, NULL, 0.00, 0, '45', '2026/05/25/83ddcdb6-22e3-4e93-832b-abb069a0ee5e_dog111.jpg', '个', '[\"2026/05/25/9b535f28-5572-40c5-bd65-4d987a7feea7_小米14主图_.webp\",\"2026/05/25/83ddcdb6-22e3-4e93-832b-abb069a0ee5e_dog111.jpg\",\"2026/05/25/0a2c137a-531a-4b9b-9556-d001e968858b_dog111.jpg\"]', NULL, 0, 1, '2026-05-25 14:33:04', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (51, '小米16', 27, 3, 2, NULL, 0.00, 0, '是个手机', '2026/05/28/b06d08e6-fdb3-48eb-aee4-eb5f10ab005a_小米14_.webp', '个', '[\"2026/05/28/814d64f8-d3f9-4dcf-a167-be4e16f255ef_小米14主图_.webp\",\"2026/05/28/6624ba2c-f203-4ee9-adc2-e149610df0e5_小米14类型3.webp\",\"2026/05/28/ec714f8b-5f1d-4d00-8beb-a3c48f788f00_小米14类型2_.webp\"]', '手机', 0, 1, '2026-05-28 14:44:21', '2026-05-31 23:05:44');
INSERT INTO `spu` VALUES (52, '小米16', 27, 3, 2, NULL, 0.00, 0, '是个手机', '2026/05/28/f9b76691-d75e-4118-bc68-2c2858fc9c0b_小米14_.webp', '个', '[\"2026/05/28/bafa49cf-edc5-4f53-9544-3001a77bd1c6_小米14主图_.webp\",\"2026/05/28/b7137da7-21fb-406e-8db1-245e4e3ff8ca_小米14类型3.webp\",\"2026/05/28/b86c18c9-5b86-4453-89db-debf670093ba_小米14类型2_.webp\"]', '手机', 1, 1, '2026-05-28 14:45:00', '2026-05-31 23:06:35');
INSERT INTO `spu` VALUES (53, 'xiaomi', 27, NULL, 2, NULL, 0.00, 0, '是个手机', '2026/05/28/b36a9698-cf9a-4360-b369-1d75b759835c_小米14主图_.webp', '台', '[\"2026/05/28/58aad40f-cc06-4fd9-a0c8-b703a0286057_小米14类型3.webp\"]', NULL, 0, 1, '2026-05-28 15:58:12', '2026-05-31 23:05:44');

-- ----------------------------
-- Table structure for spu_basic_attr_values
-- ----------------------------
DROP TABLE IF EXISTS `spu_basic_attr_values`;
CREATE TABLE `spu_basic_attr_values`  (
                                          `id` bigint NOT NULL AUTO_INCREMENT,
                                          `spu_id` bigint NOT NULL COMMENT 'SPU ID',
                                          `attr_id` bigint NOT NULL COMMENT '基本属性ID',
                                          `attr_value_id` bigint NULL DEFAULT NULL COMMENT '当input_type为单选/多选时，关联attribute_values.id',
                                          `manual_value` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当input_type为手动输入时，存储用户填写的值',
                                          `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                          `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          PRIMARY KEY (`id`) USING BTREE,
                                          UNIQUE INDEX `uk_spu_attr`(`spu_id` ASC, `attr_id` ASC) USING BTREE,
                                          INDEX `idx_spu_id`(`spu_id` ASC) USING BTREE,
                                          INDEX `idx_attr_id`(`attr_id` ASC) USING BTREE,
                                          INDEX `fk_spu_basic_attr_value`(`attr_value_id` ASC) USING BTREE,
                                          CONSTRAINT `fk_spu_basic_attr_attr` FOREIGN KEY (`attr_id`) REFERENCES `attributes` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                          CONSTRAINT `fk_spu_basic_attr_spu` FOREIGN KEY (`spu_id`) REFERENCES `spu` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                          CONSTRAINT `fk_spu_basic_attr_value` FOREIGN KEY (`attr_value_id`) REFERENCES `attribute_values` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SPU基本属性值表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of spu_basic_attr_values
-- ----------------------------
INSERT INTO `spu_basic_attr_values` VALUES (1, 3, 4, 10, NULL, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `spu_basic_attr_values` VALUES (2, 3, 5, NULL, '高通骁龙8 Gen 3', '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `spu_basic_attr_values` VALUES (3, 3, 6, 11, NULL, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `spu_basic_attr_values` VALUES (4, 3, 7, NULL, '徕卡三摄：50MP超动态主摄+50MP超广角+50MP长焦', '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `spu_basic_attr_values` VALUES (9, 47, 4, 10, NULL, NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (10, 47, 5, NULL, 'cc', NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (11, 47, 6, 11, NULL, NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (12, 47, 7, 12, NULL, NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (13, 44, 4, 10, NULL, NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (14, 44, 5, NULL, '666', NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (15, 44, 6, 11, NULL, NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (16, 44, 7, 12, NULL, NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (17, 49, 4, 10, NULL, NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (18, 49, 6, 11, NULL, NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (19, 49, 7, 12, NULL, NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (20, 50, 4, 10, NULL, NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (21, 50, 6, 11, NULL, NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (22, 50, 7, 12, NULL, NULL, NULL);
INSERT INTO `spu_basic_attr_values` VALUES (23, 45, 4, NULL, '6000mAh', '2026-05-28 13:51:08', '2026-05-28 14:43:01');
INSERT INTO `spu_basic_attr_values` VALUES (24, 45, 5, NULL, '66', '2026-05-28 13:51:08', '2026-05-28 14:43:01');
INSERT INTO `spu_basic_attr_values` VALUES (25, 45, 6, 11, NULL, '2026-05-28 13:51:08', '2026-05-28 14:43:01');
INSERT INTO `spu_basic_attr_values` VALUES (26, 45, 7, 12, NULL, '2026-05-28 13:51:08', '2026-05-28 14:43:01');
INSERT INTO `spu_basic_attr_values` VALUES (27, 52, 4, 10, NULL, '2026-05-28 15:55:28', '2026-05-28 15:55:28');
INSERT INTO `spu_basic_attr_values` VALUES (28, 52, 5, NULL, '55', '2026-05-28 15:55:28', '2026-05-28 15:55:28');
INSERT INTO `spu_basic_attr_values` VALUES (29, 52, 6, 11, NULL, '2026-05-28 15:55:28', '2026-05-28 15:55:28');
INSERT INTO `spu_basic_attr_values` VALUES (30, 52, 7, 13, NULL, '2026-05-28 15:55:28', '2026-05-28 15:55:28');
INSERT INTO `spu_basic_attr_values` VALUES (31, 53, 4, 10, NULL, '2026-05-28 15:59:53', '2026-05-28 15:59:53');
INSERT INTO `spu_basic_attr_values` VALUES (32, 53, 5, NULL, '11', '2026-05-28 15:59:53', '2026-05-28 15:59:53');
INSERT INTO `spu_basic_attr_values` VALUES (33, 53, 6, 11, NULL, '2026-05-28 15:59:53', '2026-05-28 15:59:53');
INSERT INTO `spu_basic_attr_values` VALUES (34, 53, 7, 13, NULL, '2026-05-28 15:59:53', '2026-05-28 15:59:53');

-- ----------------------------
-- Table structure for spu_sale_attr_choice
-- ----------------------------
DROP TABLE IF EXISTS `spu_sale_attr_choice`;
CREATE TABLE `spu_sale_attr_choice`  (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `spu_id` bigint NOT NULL COMMENT 'SPU ID',
                                         `attr_id` bigint NOT NULL COMMENT '销售属性ID',
                                         `selected_values` json NULL COMMENT '该SPU启用的属性值ID列表，如[101,102]',
                                         `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                         `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         PRIMARY KEY (`id`) USING BTREE,
                                         UNIQUE INDEX `uk_spu_attr`(`spu_id` ASC, `attr_id` ASC) USING BTREE,
                                         INDEX `idx_spu_id`(`spu_id` ASC) USING BTREE,
                                         INDEX `idx_attr_id`(`attr_id` ASC) USING BTREE,
                                         CONSTRAINT `fk_spu_sale_choice_attr` FOREIGN KEY (`attr_id`) REFERENCES `attributes` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                         CONSTRAINT `fk_spu_sale_choice_spu` FOREIGN KEY (`spu_id`) REFERENCES `spu` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SPU选择哪些销售属性（及可选值）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of spu_sale_attr_choice
-- ----------------------------
INSERT INTO `spu_sale_attr_choice` VALUES (1, 3, 1, '[1, 2, 3, 4]', '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `spu_sale_attr_choice` VALUES (2, 3, 2, '[5, 6]', '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `spu_sale_attr_choice` VALUES (3, 3, 3, '[7, 8, 9]', '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `spu_sale_attr_choice` VALUES (7, 47, 1, '[1, 2, 3, 4]', NULL, NULL);
INSERT INTO `spu_sale_attr_choice` VALUES (8, 47, 2, '[5, 6]', NULL, NULL);
INSERT INTO `spu_sale_attr_choice` VALUES (9, 47, 3, '[7, 8, 9]', NULL, NULL);
INSERT INTO `spu_sale_attr_choice` VALUES (10, 44, 1, '[1, 2]', NULL, NULL);
INSERT INTO `spu_sale_attr_choice` VALUES (11, 44, 2, '[6]', NULL, NULL);
INSERT INTO `spu_sale_attr_choice` VALUES (12, 44, 3, '[8, 9]', NULL, NULL);
INSERT INTO `spu_sale_attr_choice` VALUES (13, 49, 1, '[1, 2]', NULL, NULL);
INSERT INTO `spu_sale_attr_choice` VALUES (14, 49, 2, '[5, 6]', NULL, NULL);
INSERT INTO `spu_sale_attr_choice` VALUES (15, 49, 3, '[8, 9]', NULL, NULL);
INSERT INTO `spu_sale_attr_choice` VALUES (16, 50, 1, '[1]', NULL, NULL);
INSERT INTO `spu_sale_attr_choice` VALUES (17, 50, 2, '[6]', NULL, NULL);
INSERT INTO `spu_sale_attr_choice` VALUES (18, 50, 3, '[8, 9]', NULL, NULL);
INSERT INTO `spu_sale_attr_choice` VALUES (19, 45, 1, '[1, 2, 3, 4]', '2026-05-28 13:51:28', '2026-05-28 13:57:48');
INSERT INTO `spu_sale_attr_choice` VALUES (20, 45, 2, '[6]', '2026-05-28 13:51:28', '2026-05-28 13:57:48');
INSERT INTO `spu_sale_attr_choice` VALUES (21, 45, 3, '[8, 9]', '2026-05-28 13:51:28', '2026-05-28 13:57:48');
INSERT INTO `spu_sale_attr_choice` VALUES (22, 52, 1, '[4]', '2026-05-28 15:55:31', '2026-05-28 15:55:31');
INSERT INTO `spu_sale_attr_choice` VALUES (23, 52, 2, '[6]', '2026-05-28 15:55:31', '2026-05-28 15:55:31');
INSERT INTO `spu_sale_attr_choice` VALUES (24, 52, 3, '[9, 8]', '2026-05-28 15:55:31', '2026-05-28 15:55:31');

-- ----------------------------
-- Table structure for store_admins
-- ----------------------------
DROP TABLE IF EXISTS `store_admins`;
CREATE TABLE `store_admins`  (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `store_id` bigint NOT NULL COMMENT '店铺ID',
                                 `user_id` bigint NOT NULL COMMENT '管理员用户ID',
                                 `role` tinyint NULL DEFAULT 1 COMMENT '角色: 1-店长 2-管理员 3-客服',
                                 `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
                                 `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`id`) USING BTREE,
                                 UNIQUE INDEX `uk_store_user`(`store_id` ASC, `user_id` ASC) USING BTREE,
                                 INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
                                 CONSTRAINT `store_admins_ibfk_1` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                 CONSTRAINT `store_admins_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '店铺管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of store_admins
-- ----------------------------
INSERT INTO `store_admins` VALUES (1, 1, 2, 1, 1, '2026-05-17 22:21:47');
INSERT INTO `store_admins` VALUES (2, 2, 3, 1, 1, '2026-05-17 22:21:47');

-- ----------------------------
-- Table structure for stores
-- ----------------------------
DROP TABLE IF EXISTS `stores`;
CREATE TABLE `stores`  (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '店铺ID',
                           `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '店铺名称',
                           `seller_id` bigint NOT NULL COMMENT '商家用户ID',
                           `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺Logo',
                           `banner` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺横幅',
                           `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺描述',
                           `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
                           `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺地址',
                           `business_license` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '营业执照',
                           `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-正常 0-禁用 2-审核中 3-审核失败',
                           `sort` int NULL DEFAULT 0 COMMENT '排序',
                           `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`) USING BTREE,
                           UNIQUE INDEX `uk_seller_id`(`seller_id` ASC) USING BTREE,
                           INDEX `idx_status`(`status` ASC) USING BTREE,
                           CONSTRAINT `stores_ibfk_1` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '店铺表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of stores
-- ----------------------------
INSERT INTO `stores` VALUES (1, '卖家张三的店铺', 2, '2026/05/19/04ff3fe9-7085-4dbd-8ee5-50c179291c89_小米14_.webp', NULL, '111111111111111111', '15738971365', '中国', NULL, 1, 0, '2026-05-17 22:21:44', '2026-05-19 11:03:58');
INSERT INTO `stores` VALUES (2, '卖家李四的店铺', 3, NULL, NULL, NULL, NULL, NULL, NULL, 1, 0, '2026-05-17 22:21:44', '2026-05-17 22:21:44');

-- ----------------------------
-- Table structure for user_roles
-- ----------------------------
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles`  (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `user_id` bigint NOT NULL,
                               `role_id` bigint NOT NULL,
                               `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
                               INDEX `role_id`(`role_id` ASC) USING BTREE,
                               CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                               CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_roles
-- ----------------------------
INSERT INTO `user_roles` VALUES (1, 1, 1, '2026-04-30 22:35:36');
INSERT INTO `user_roles` VALUES (2, 2, 3, '2026-04-30 22:35:36');
INSERT INTO `user_roles` VALUES (3, 3, 3, '2026-04-30 22:35:36');
INSERT INTO `user_roles` VALUES (4, 4, 4, '2026-04-30 22:35:36');
INSERT INTO `user_roles` VALUES (5, 5, 4, '2026-04-30 22:35:36');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
                          `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码(加密存储)',
                          `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
                          `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
                          `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
                          `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
                          `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
                          `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
                          `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录IP',
                          `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          `deleted_at` datetime NULL DEFAULT NULL COMMENT '软删除',
                          PRIMARY KEY (`id`) USING BTREE,
                          UNIQUE INDEX `username`(`username` ASC) USING BTREE,
                          UNIQUE INDEX `email`(`email` ASC) USING BTREE,
                          UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
                          INDEX `idx_users_username`(`username` ASC) USING BTREE,
                          INDEX `idx_users_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '$2b$10$n.9FfRTizHBnifhyf414GeJdyZythG2rrzeNp8ShgalotpcsRzwXK', 'admin@test.com', '13800138000', NULL, '系统管理员', 1, NULL, NULL, '2026-04-30 22:35:35', '2026-05-13 15:05:06', NULL);
INSERT INTO `users` VALUES (2, 'seller1', '$2b$10$n.9FfRTizHBnifhyf414GeJdyZythG2rrzeNp8ShgalotpcsRzwXK', 'seller1@test.com', '13800138001', NULL, '卖家张三', 1, '2026-05-31 21:48:38', NULL, '2026-04-30 22:35:35', '2026-05-13 15:06:50', NULL);
INSERT INTO `users` VALUES (3, 'seller2', '$2b$10$n.9FfRTizHBnifhyf414GeJdyZythG2rrzeNp8ShgalotpcsRzwXK', 'seller2@test.com', '13800138002', NULL, '卖家李四', 1, NULL, NULL, '2026-04-30 22:35:35', '2026-05-13 15:06:50', NULL);
INSERT INTO `users` VALUES (4, 'user1', '$2a$10$TCp1rF60m90HqocR9ahn3O9.V/Fdw8qdjKXcyqA6rMADiqdhkE0d.', '3338305331@qq.com', '15738971365', NULL, '用户王五', 1, '2026-05-31 22:07:51', NULL, '2026-04-30 22:35:35', '2026-05-18 12:41:03', NULL);
INSERT INTO `users` VALUES (5, 'user2', '$2b$10$n.9FfRTizHBnifhyf414GeJdyZythG2rrzeNp8ShgalotpcsRzwXK', 'user2@test.com', '13800138004', NULL, '用户赵六', 1, NULL, NULL, '2026-04-30 22:35:35', '2026-05-13 15:06:50', NULL);

SET FOREIGN_KEY_CHECKS = 1;
