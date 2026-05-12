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

 Date: 12/05/2026 14:13:25
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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收货地址表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of addresses
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '属性值表' ROW_FORMAT = Dynamic;

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
                           `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品品牌表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of brands
-- ----------------------------
INSERT INTO `brands` VALUES (1, '华为', '2026/05/07/5f748651-aa40-4c90-b865-aabe8430da18_华为logo.webp', '华为技术有限公司', NULL, 1, 1, '2026-04-30 22:35:36', '2026-05-07 21:08:38');
INSERT INTO `brands` VALUES (2, '苹果', NULL, '苹果公司', NULL, 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `brands` VALUES (3, '小米', NULL, '小米科技有限公司', NULL, 3, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `brands` VALUES (4, '三星', NULL, '三星电子', NULL, 4, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `brands` VALUES (5, '耐克', NULL, '耐克公司', NULL, 5, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `brands` VALUES (6, '阿迪达斯', NULL, '阿迪达斯公司', NULL, 6, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `brands` VALUES (7, 'OPPO', NULL, 'OPPO 广东移动通信有限公司', 'https://www.oppo.com', 7, 1, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (8, 'vivo', NULL, '维沃移动通信有限公司', 'https://www.vivo.com', 8, 1, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (9, '荣耀', NULL, '荣耀终端有限公司', 'https://www.honor.com', 9, 1, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (10, '联想', NULL, '联想集团', 'https://www.lenovo.com', 10, 1, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (11, '戴尔', NULL, '戴尔科技集团', 'https://www.dell.com', 11, 1, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (12, '海尔', NULL, '海尔集团', 'https://www.haier.com', 12, 1, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (13, '美的', NULL, '美的集团', 'https://www.midea.com', 13, 1, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (14, '格力', NULL, '格力电器股份有限公司', 'https://www.gree.com', 14, 1, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (15, '安踏', NULL, '安踏体育用品有限公司', 'https://www.anta.com', 15, 1, '2026-05-08 17:09:39', '2026-05-08 17:09:39');
INSERT INTO `brands` VALUES (16, '李宁', NULL, '李宁体育用品有限公司', 'https://www.lining.com', 16, 1, '2026-05-08 17:09:39', '2026-05-08 17:09:39');

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
                               `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                               `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`) USING BTREE,
                               UNIQUE INDEX `uk_user_sku`(`user_id` ASC, `sku_id` ASC) USING BTREE,
                               INDEX `sku_id`(`sku_id` ASC) USING BTREE,
                               INDEX `idx_cart_items_user`(`user_id` ASC) USING BTREE,
                               CONSTRAINT `cart_items_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                               CONSTRAINT `cart_items_ibfk_2` FOREIGN KEY (`sku_id`) REFERENCES `sku` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cart_items
-- ----------------------------

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
                               `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                               `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, '电子产品', 0, 1, NULL, 1, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (2, '服装鞋帽', 0, 1, NULL, 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (3, '家居用品', 0, 1, NULL, 3, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (4, '食品饮料', 0, 1, NULL, 4, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (5, '手机数码', 1, 2, NULL, 1, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (6, '电脑办公', 1, 2, NULL, 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (7, '智能设备', 1, 2, NULL, 3, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (8, '男装', 2, 2, NULL, 1, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (9, '女装', 2, 2, NULL, 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `categories` VALUES (10, '鞋靴', 2, 2, NULL, 3, 1, '2026-04-30 22:35:36', '2026-05-06 21:51:39');
INSERT INTO `categories` VALUES (11, '智能手环', 7, 3, NULL, 1, 1, '2026-05-06 15:49:28', '2026-05-06 15:49:28');
INSERT INTO `categories` VALUES (12, '智能手表', 7, 3, NULL, 2, 1, '2026-05-06 15:49:28', '2026-05-06 15:49:28');
INSERT INTO `categories` VALUES (13, '智能家居', 7, 3, NULL, 3, 1, '2026-05-06 15:49:28', '2026-05-06 15:49:28');
INSERT INTO `categories` VALUES (14, '大家电', 1, 2, NULL, 4, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (15, '厨卫电器', 1, 2, NULL, 5, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (16, '童装', 2, 2, NULL, 4, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (17, '内衣睡衣', 2, 2, NULL, 5, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (18, '箱包配饰', 2, 2, NULL, 6, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (19, '床上用品', 3, 2, NULL, 1, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (20, '厨房用具', 3, 2, NULL, 2, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (21, '收纳整理', 3, 2, NULL, 3, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (22, '家装软饰', 3, 2, NULL, 4, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (23, '休闲食品', 4, 2, NULL, 1, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (24, '生鲜食品', 4, 2, NULL, 2, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (25, '酒水饮料', 4, 2, NULL, 3, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (26, '营养保健', 4, 2, NULL, 4, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (27, '手机', 5, 3, NULL, 1, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (28, '手机配件', 5, 3, NULL, 2, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (29, '数码相机', 5, 3, NULL, 3, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (30, '耳机音响', 5, 3, NULL, 4, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (31, '笔记本电脑', 6, 3, NULL, 1, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (32, '台式电脑', 6, 3, NULL, 2, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (33, '电脑外设', 6, 3, NULL, 3, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (34, '办公用品', 6, 3, NULL, 4, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (35, '电视', 14, 3, NULL, 1, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (36, '冰箱', 14, 3, NULL, 2, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (37, '洗衣机', 14, 3, NULL, 3, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (38, '空调', 14, 3, NULL, 4, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (39, '上衣', 8, 3, NULL, 1, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (40, '裤子', 8, 3, NULL, 2, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (41, '外套', 8, 3, NULL, 3, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (42, '连衣裙', 9, 3, NULL, 1, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (43, '上衣', 9, 3, NULL, 2, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (44, '半身裙', 9, 3, NULL, 3, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (45, '裤子', 9, 3, NULL, 4, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (46, '运动鞋', 10, 3, NULL, 1, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (47, '皮鞋', 10, 3, NULL, 2, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (48, '休闲鞋', 10, 3, NULL, 3, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (49, '凉鞋/拖鞋', 10, 3, NULL, 4, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (50, '背包', 18, 3, NULL, 1, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (51, '钱包', 18, 3, NULL, 2, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (52, '饰品', 18, 3, NULL, 3, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (53, '四件套', 19, 3, NULL, 1, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (54, '被芯', 19, 3, NULL, 2, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (55, '枕芯', 19, 3, NULL, 3, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (56, '锅具', 20, 3, NULL, 1, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (57, '刀具砧板', 20, 3, NULL, 2, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (58, '餐具', 20, 3, NULL, 3, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (59, '坚果炒货', 23, 3, NULL, 1, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (60, '饼干糕点', 23, 3, NULL, 2, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (61, '巧克力糖果', 23, 3, NULL, 3, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (62, '白酒', 25, 3, NULL, 1, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (63, '啤酒', 25, 3, NULL, 2, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');
INSERT INTO `categories` VALUES (64, '饮料', 25, 3, NULL, 3, 1, '2026-05-06 22:00:00', '2026-05-06 22:00:00');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorites
-- ----------------------------

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
                                PRIMARY KEY (`id`) USING BTREE,
                                INDEX `sku_id`(`sku_id` ASC) USING BTREE,
                                INDEX `spu_id`(`spu_id` ASC) USING BTREE,
                                INDEX `idx_order_items_order`(`order_id` ASC) USING BTREE,
                                CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
                                CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`sku_id`) REFERENCES `sku` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                                CONSTRAINT `order_items_ibfk_3` FOREIGN KEY (`spu_id`) REFERENCES `spu` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_items
-- ----------------------------

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
                           PRIMARY KEY (`id`) USING BTREE,
                           UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
                           INDEX `idx_orders_user`(`user_id` ASC) USING BTREE,
                           INDEX `idx_orders_order_no`(`order_no` ASC) USING BTREE,
                           INDEX `idx_orders_status`(`status` ASC) USING BTREE,
                           INDEX `idx_orders_created`(`created_at` ASC) USING BTREE,
                           CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------

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
                                    PRIMARY KEY (`id`) USING BTREE,
                                    INDEX `order_no`(`order_no` ASC) USING BTREE,
                                    CONSTRAINT `payment_records_ibfk_1` FOREIGN KEY (`order_no`) REFERENCES `orders` (`order_no`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付记录表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permissions
-- ----------------------------
INSERT INTO `permissions` VALUES (1, '首页', 'dashboard', 'menu', 0, '/dashboard', 'Layout/Dashboard', 'dashboard', 1, 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `permissions` VALUES (2, '系统管理', 'system', 'menu', 0, '/system', 'Layout/System', 'system', 2, 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `permissions` VALUES (3, '用户管理', 'user', 'menu', 0, '/user', 'Layout/User', 'user', 3, 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `permissions` VALUES (4, '商品管理', 'product', 'menu', 0, '/product', 'Layout/Product', 'product', 4, 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `permissions` VALUES (5, '订单管理', 'order', 'menu', 0, '/order', 'Layout/Order', 'order', 5, 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `permissions` VALUES (6, '营销管理', 'marketing', 'menu', 0, '/marketing', 'Layout/Marketing', 'marketing', 6, 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `permissions` VALUES (7, '数据统计', 'report', 'menu', 0, '/report', 'Layout/Report', 'chart', 7, 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `permissions` VALUES (8, '角色管理', 'system:role:list', 'menu', 2, 'role', 'System/Role', 'role', 1, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (9, '权限管理', 'system:permission:list', 'menu', 2, 'permission', 'System/Permission', 'permission', 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (10, '菜单管理', 'system:menu:list', 'menu', 2, 'menu', 'System/Menu', 'menu', 3, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (11, '操作日志', 'system:log:list', 'menu', 2, 'log', 'System/Log', 'log', 4, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (12, '用户列表', 'user:list', 'menu', 3, 'list', 'User/List', 'user-list', 1, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (13, '用户统计', 'user:stats', 'menu', 3, 'stats', 'User/Stats', 'stats', 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (14, '商品列表', 'product:list', 'menu', 4, 'list', 'Product/List', 'product-list', 1, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (15, '分类管理', 'product:category', 'menu', 4, 'category', 'Product/Category', 'category', 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (16, '品牌管理', 'product:brand', 'menu', 4, 'brand', 'Product/Brand', 'brand', 3, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (17, '规格管理', 'product:spec', 'menu', 4, 'spec', 'Product/Spec', 'spec', 4, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (18, '订单列表', 'order:list', 'menu', 5, 'list', 'Order/List', 'order-list', 1, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (19, '退款管理', 'order:refund', 'menu', 5, 'refund', 'Order/Refund', 'refund', 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (20, '优惠券管理', 'marketing:coupon', 'menu', 6, 'coupon', 'Marketing/Coupon', 'coupon', 1, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (21, '活动管理', 'marketing:activity', 'menu', 6, 'activity', 'Marketing/Activity', 'activity', 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (22, '销售统计', 'report:sales', 'menu', 7, 'sales', 'Report/Sales', 'sales', 1, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (23, '用户分析', 'report:user', 'menu', 7, 'user-analysis', 'Report/UserAnalysis', 'analysis', 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (24, '添加角色', 'system:role:add', 'button', 2, NULL, NULL, NULL, 1, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (25, '编辑角色', 'system:role:edit', 'button', 2, NULL, NULL, NULL, 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (26, '删除角色', 'system:role:delete', 'button', 2, NULL, NULL, NULL, 3, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (27, '添加用户', 'user:add', 'button', 3, NULL, NULL, NULL, 1, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (28, '编辑用户', 'user:edit', 'button', 3, NULL, NULL, NULL, 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (29, '删除用户', 'user:delete', 'button', 3, NULL, NULL, NULL, 3, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (30, '添加商品', 'product:add', 'button', 4, NULL, NULL, NULL, 1, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (31, '编辑商品', 'product:edit', 'button', 4, NULL, NULL, NULL, 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (32, '删除商品', 'product:delete', 'button', 4, NULL, NULL, NULL, 3, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (33, '上架商品', 'product:shelve', 'button', 4, NULL, NULL, NULL, 4, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (34, '下架商品', 'product:unshelve', 'button', 4, NULL, NULL, NULL, 5, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (35, '发货', 'order:deliver', 'button', 5, NULL, NULL, NULL, 1, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (36, '取消订单', 'order:cancel', 'button', 5, NULL, NULL, NULL, 2, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');
INSERT INTO `permissions` VALUES (37, '处理退款', 'order:refund:handle', 'button', 5, NULL, NULL, NULL, 3, 1, '2026-04-30 22:35:36', '2026-04-30 22:35:36');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退款记录表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品评价表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 111 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permissions
-- ----------------------------
INSERT INTO `role_permissions` VALUES (1, 1, 1, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (2, 1, 6, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (3, 1, 21, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (4, 1, 20, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (5, 1, 5, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (6, 1, 36, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (7, 1, 35, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (8, 1, 18, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (9, 1, 19, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (10, 1, 37, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (11, 1, 4, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (12, 1, 30, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (13, 1, 16, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (14, 1, 15, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (15, 1, 32, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (16, 1, 31, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (17, 1, 14, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (18, 1, 33, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (19, 1, 17, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (20, 1, 34, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (21, 1, 7, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (22, 1, 22, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (23, 1, 23, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (24, 1, 2, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (25, 1, 11, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (26, 1, 10, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (27, 1, 9, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (28, 1, 24, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (29, 1, 26, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (30, 1, 25, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (31, 1, 8, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (32, 1, 3, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (33, 1, 27, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (34, 1, 29, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (35, 1, 28, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (36, 1, 12, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (37, 1, 13, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (64, 2, 1, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (65, 2, 6, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (66, 2, 20, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (67, 2, 5, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (68, 2, 36, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (69, 2, 35, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (70, 2, 18, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (71, 2, 19, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (72, 2, 37, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (73, 2, 4, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (74, 2, 30, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (75, 2, 15, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (76, 2, 14, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (77, 2, 33, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (78, 2, 34, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (79, 2, 7, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (80, 2, 22, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (81, 2, 2, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (82, 2, 11, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (83, 2, 8, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (84, 2, 3, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (85, 2, 27, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (86, 2, 28, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (87, 2, 12, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (95, 3, 1, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (96, 3, 5, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (97, 3, 35, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (98, 3, 18, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (99, 3, 4, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (100, 3, 30, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (101, 3, 31, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (102, 3, 14, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (103, 3, 33, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (104, 3, 34, '2026-04-30 22:35:36');
INSERT INTO `role_permissions` VALUES (110, 4, 1, '2026-04-30 22:35:36');

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
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, '超级管理员', 'SUPER_ADMIN', '系统最高权限，管理所有功能', 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `roles` VALUES (2, '运营管理员', 'ADMIN', '负责日常运营管理', 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `roles` VALUES (3, '普通卖家', 'SELLER', '商家用户，管理自己的商品', 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `roles` VALUES (4, '普通用户', 'USER', '前台注册用户', 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');
INSERT INTO `roles` VALUES (5, '客服人员', 'CUSTOMER_SERVICE', '处理订单售后和咨询', 1, '2026-04-30 22:35:35', '2026-04-30 22:35:35');

-- ----------------------------
-- Table structure for sku
-- ----------------------------
DROP TABLE IF EXISTS `sku`;
CREATE TABLE `sku`  (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `spu_id` bigint NOT NULL COMMENT 'SPU ID',
                        `sku_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SKU编码',
                        `price` decimal(10, 2) NOT NULL COMMENT '价格',
                        `market_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '市场价',
                        `cost_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '成本价',
                        `stock` int NULL DEFAULT 0 COMMENT '库存',
                        `warn_stock` int NULL DEFAULT 10 COMMENT '预警库存',
                        `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SKU图片',
                        `weight` decimal(10, 3) NULL DEFAULT NULL COMMENT '重量(kg)',
                        `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
                        `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`) USING BTREE,
                        UNIQUE INDEX `sku_code`(`sku_code` ASC) USING BTREE,
                        INDEX `idx_sku_spu`(`spu_id` ASC) USING BTREE,
                        INDEX `idx_sku_price`(`price` ASC) USING BTREE,
                        CONSTRAINT `sku_ibfk_1` FOREIGN KEY (`spu_id`) REFERENCES `spu` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品SKU表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sku
-- ----------------------------
INSERT INTO `sku` VALUES (23, 3, 'XIAOMI14PRO-BK-12-256', 4299.00, 4999.00, 3800.00, 100, 10, NULL, 0.193, 1, '2026-05-08 16:57:08', '2026-05-08 16:57:08');
INSERT INTO `sku` VALUES (24, 3, 'XIAOMI14PRO-BK-16-512', 4999.00, 5999.00, 4400.00, 80, 10, NULL, 0.193, 1, '2026-05-08 16:57:08', '2026-05-08 16:57:08');
INSERT INTO `sku` VALUES (25, 3, 'XIAOMI14PRO-BK-16-1TB', 5999.00, 6999.00, 5300.00, 50, 10, NULL, 0.193, 1, '2026-05-08 16:57:08', '2026-05-08 16:57:08');
INSERT INTO `sku` VALUES (26, 3, 'XIAOMI14PRO-WH-12-256', 4299.00, 4999.00, 3800.00, 90, 10, NULL, 0.193, 1, '2026-05-08 16:57:08', '2026-05-08 16:57:08');
INSERT INTO `sku` VALUES (27, 3, 'XIAOMI14PRO-WH-16-512', 4999.00, 5999.00, 4400.00, 70, 10, NULL, 0.193, 1, '2026-05-08 16:57:08', '2026-05-08 16:57:08');
INSERT INTO `sku` VALUES (28, 3, 'XIAOMI14PRO-WH-16-1TB', 5999.00, 6999.00, 5300.00, 40, 10, NULL, 0.193, 1, '2026-05-08 16:57:08', '2026-05-08 16:57:08');
INSERT INTO `sku` VALUES (29, 3, 'XIAOMI14PRO-GN-12-256', 4299.00, 4999.00, 3800.00, 60, 10, NULL, 0.193, 1, '2026-05-08 16:57:08', '2026-05-08 16:57:08');
INSERT INTO `sku` VALUES (30, 3, 'XIAOMI14PRO-GN-16-512', 4999.00, 5999.00, 4400.00, 50, 10, NULL, 0.193, 1, '2026-05-08 16:57:08', '2026-05-08 16:57:08');
INSERT INTO `sku` VALUES (31, 3, 'XIAOMI14PRO-GN-16-1TB', 5999.00, 6999.00, 5300.00, 30, 10, NULL, 0.193, 1, '2026-05-08 16:57:08', '2026-05-08 16:57:08');
INSERT INTO `sku` VALUES (32, 3, 'XIAOMI14PRO-PK-12-256', 4299.00, 4999.00, 3800.00, 55, 10, NULL, 0.193, 1, '2026-05-08 16:57:08', '2026-05-08 16:57:08');
INSERT INTO `sku` VALUES (33, 3, 'XIAOMI14PRO-PK-16-512', 4999.00, 5999.00, 4400.00, 45, 10, NULL, 0.193, 1, '2026-05-08 16:57:08', '2026-05-08 16:57:08');
INSERT INTO `sku` VALUES (34, 3, 'XIAOMI14PRO-PK-16-1TB', 5999.00, 6999.00, 5300.00, 25, 10, NULL, 0.193, 1, '2026-05-08 16:57:08', '2026-05-08 16:57:08');

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
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SKU与销售属性值关联表' ROW_FORMAT = Dynamic;

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

-- ----------------------------
-- Table structure for spu
-- ----------------------------
DROP TABLE IF EXISTS `spu`;
CREATE TABLE `spu`  (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
                        `category_id` bigint NOT NULL COMMENT '分类ID',
                        `brand_id` bigint NULL DEFAULT NULL COMMENT '品牌ID',
                        `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
                        `main_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主图',
                        `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品图片集(JSON格式)',
                        `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位',
                        `keywords` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关键词',
                        `sales` int NULL DEFAULT 0 COMMENT '销量',
                        `status` tinyint NULL DEFAULT 1 COMMENT '状态: 1-上架 0-下架',
                        `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`) USING BTREE,
                        INDEX `idx_spu_category`(`category_id` ASC) USING BTREE,
                        INDEX `idx_spu_brand`(`brand_id` ASC) USING BTREE,
                        INDEX `idx_spu_status`(`status` ASC) USING BTREE,
                        CONSTRAINT `spu_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
                        CONSTRAINT `spu_ibfk_2` FOREIGN KEY (`brand_id`) REFERENCES `brands` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品SPU表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of spu
-- ----------------------------
INSERT INTO `spu` VALUES (3, '小米14 Pro', 27, 3, '小米旗舰手机', '2026/05/08/451cf586-ee5e-4c6d-9072-f92dc46b16a0_小米14主图_.webp', '[\"2026/05/08/451cf586-ee5e-4c6d-9072-f92dc46b16a0_小米14主图_.webp\",\"2026/05/08/a4699d83-3f33-41bb-9872-31fd72199f33_小米14_.webp\",\"2026/05/08/2772e4d9-a6df-40cc-817e-03266ee5283a_小米14类型2_.webp\",\"2026/05/08/fb59cd73-4e9e-4768-8f43-f110b8ccbe91_小米14类型3.webp\"]', '台', '小米,14,手机', 0, 1, '2026-05-08 16:51:43', '2026-05-08 17:46:05');
INSERT INTO `spu` VALUES (20, '华为 Mate 60 Pro', 27, 1, '华为旗舰手机，搭载麒麟9000S芯片，支持卫星通信', NULL, NULL, '台', '华为,Mate60,手机,5G', 1523, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (21, 'iPhone 15 Pro Max', 27, 2, '苹果旗舰手机，A17 Pro芯片，钛金属边框', NULL, NULL, '台', '苹果,iPhone15,手机', 2841, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (22, '小米14 Ultra', 27, 3, '小米影像旗舰，徕卡四摄，骁龙8Gen3', NULL, NULL, '台', '小米14,Ultra,手机,徕卡', 892, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (23, '三星 S24 Ultra', 27, 4, '三星旗舰手机，AI智能，SPen触控笔', NULL, NULL, '台', '三星,S24,手机,AI', 645, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (24, 'OPPO Find X7 Ultra', 27, 7, 'OPPO影像旗舰，双潜望四主摄', NULL, NULL, '台', 'OPPO,FindX7,手机', 456, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (25, 'vivo X100 Pro', 27, 8, 'vivo旗舰手机，蔡司影像，天玑9300', NULL, NULL, '台', 'vivo,X100,手机,蔡司', 378, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (26, '荣耀 Magic6 Pro', 27, 9, '荣耀旗舰手机，鸿燕通信，巨犀玻璃', NULL, NULL, '台', '荣耀,Magic6,手机', 567, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (27, '联想 小新Pro16', 31, 10, '联想轻薄本，13代i7，2.5K高刷屏', NULL, NULL, '台', '联想,小新,笔记本,轻薄本', 234, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (28, '戴尔 XPS 15', 31, 11, '戴尔高端创作本，3.5K OLED触控屏', NULL, NULL, '台', '戴尔,XPS,笔记本,创作本', 123, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (29, '华为 MateBook X Pro', 31, 1, '华为旗舰轻薄本，3.1K原色屏，超级终端', NULL, NULL, '台', '华为,MateBook,笔记本', 345, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (30, 'Apple Watch Series 9', 12, 2, '苹果智能手表，S9芯片，全天候显示', NULL, NULL, '只', '苹果,手表,AppleWatch', 789, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (31, '华为 Watch 4 Pro', 12, 1, '华为智能手表，支持血糖研究，钛金属表壳', NULL, NULL, '只', '华为,手表,Watch4', 456, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (32, '小米手表 S3', 12, 3, '小米智能手表，eSIM独立通话，百变表圈', NULL, NULL, '只', '小米,手表,小米手表', 234, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (33, '耐克 Air Jordan 1', 46, 5, '耐克经典篮球鞋，复古高帮设计', NULL, NULL, '双', '耐克,AJ1,篮球鞋,乔丹', 567, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (34, '阿迪达斯 Yeezy 350', 46, 6, '阿迪达斯椰子鞋，Boost中底', NULL, NULL, '双', '阿迪达斯,Yeezy,椰子鞋', 345, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (35, '安踏 海沃德4代', 46, 13, '安踏篮球鞋，氮科技中底', NULL, NULL, '双', '安踏,海沃德,篮球鞋', 123, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (36, '李宁 驭帅15', 46, 14, '李宁篮球鞋，䨻科技缓震', NULL, NULL, '双', '李宁,驭帅,篮球鞋', 234, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (37, '小米电视 S Pro 65寸', 35, 3, '小米MiniLED电视，4K 144Hz高刷', NULL, NULL, '台', '小米,电视,4K,智能电视', 567, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (38, '海尔 65寸智能电视', 35, 12, '海尔4K超高清电视，智能语音控制', NULL, NULL, '台', '海尔,电视,智能电视', 234, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (39, '格力 云锦三代 1.5匹', 38, 14, '格力变频空调，新一级能效，自清洁', NULL, NULL, '台', '格力,空调,变频,节能', 789, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (40, '美的 酷省电 1.5匹', 38, 13, '美的变频空调，新一级能效，ECO节能', NULL, NULL, '台', '美的,空调,节能,变频', 456, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (41, '三只松鼠 每日坚果', 59, NULL, '混合坚果礼盒，营养均衡', NULL, NULL, '盒', '坚果,每日坚果,零食', 2345, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (42, '茅台 飞天茅台', 62, NULL, '酱香型白酒，53度500ml', NULL, NULL, '瓶', '茅台,飞天,白酒,酱香', 89, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');
INSERT INTO `spu` VALUES (43, '五粮液 第八代普五', 62, NULL, '浓香型白酒，52度500ml', NULL, NULL, '瓶', '五粮液,白酒,浓香', 156, 1, '2026-05-08 17:09:57', '2026-05-08 17:09:57');

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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SPU基本属性值表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of spu_basic_attr_values
-- ----------------------------
INSERT INTO `spu_basic_attr_values` VALUES (1, 3, 4, 10, NULL, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `spu_basic_attr_values` VALUES (2, 3, 5, NULL, '高通骁龙8 Gen 3', '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `spu_basic_attr_values` VALUES (3, 3, 6, 11, NULL, '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `spu_basic_attr_values` VALUES (4, 3, 7, NULL, '徕卡三摄：50MP超动态主摄+50MP超广角+50MP长焦', '2026-05-12 14:07:25', '2026-05-12 14:07:25');

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'SPU选择哪些销售属性（及可选值）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of spu_sale_attr_choice
-- ----------------------------
INSERT INTO `spu_sale_attr_choice` VALUES (1, 3, 1, '[1, 2, 3, 4]', '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `spu_sale_attr_choice` VALUES (2, 3, 2, '[5, 6]', '2026-05-12 14:07:25', '2026-05-12 14:07:25');
INSERT INTO `spu_sale_attr_choice` VALUES (3, 3, 3, '[7, 8, 9]', '2026-05-12 14:07:25', '2026-05-12 14:07:25');

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
INSERT INTO `users` VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 'admin@test.com', '13800138000', NULL, '系统管理员', 1, NULL, NULL, '2026-04-30 22:35:35', '2026-04-30 22:35:35', NULL);
INSERT INTO `users` VALUES (2, 'seller1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 'seller1@test.com', '13800138001', NULL, '卖家张三', 1, NULL, NULL, '2026-04-30 22:35:35', '2026-04-30 22:35:35', NULL);
INSERT INTO `users` VALUES (3, 'seller2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 'seller2@test.com', '13800138002', NULL, '卖家李四', 1, NULL, NULL, '2026-04-30 22:35:35', '2026-04-30 22:35:35', NULL);
INSERT INTO `users` VALUES (4, 'user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 'user1@test.com', '13800138003', NULL, '用户王五', 1, NULL, NULL, '2026-04-30 22:35:35', '2026-04-30 22:35:35', NULL);
INSERT INTO `users` VALUES (5, 'user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 'user2@test.com', '13800138004', NULL, '用户赵六', 1, NULL, NULL, '2026-04-30 22:35:35', '2026-04-30 22:35:35', NULL);

SET FOREIGN_KEY_CHECKS = 1;
