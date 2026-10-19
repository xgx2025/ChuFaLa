/*
 Navicat Premium Data Transfer

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80041
 Source Host           : localhost:3306
 Source Schema         : chufala_db

 Target Server Type    : MySQL
 Target Server Version : 80041
 File Encoding         : 65001

 Date: 18/10/2026 23:07:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_conversation
-- ----------------------------
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation`  (
  `id` bigint NOT NULL COMMENT '主键，会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '会话标题',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0-存在，1-删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ai_message
-- ----------------------------
DROP TABLE IF EXISTS `ai_message`;
CREATE TABLE `ai_message`  (
  `id` bigint NOT NULL COMMENT '主键，消息ID',
  `conversation_id` bigint NOT NULL COMMENT '主键，会话ID',
  `role` enum('user','assistant') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for attachment
-- ----------------------------
DROP TABLE IF EXISTS `attachment`;
CREATE TABLE `attachment`  (
  `id` bigint NOT NULL COMMENT '主键',
  `message_id` bigint NOT NULL COMMENT '会话消息中的消息ID',
  `file_type` enum('image','audio','video','document') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '默认text,如有其他文件则为该文件类型，暂只支持上传一种文件类型',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件存储地址',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称是展示给用户的（与实际保存在后端的文件名不同，后端保存的是唯一文件名）',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for attraction
-- ----------------------------
DROP TABLE IF EXISTS `attraction`;
CREATE TABLE `attraction`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '景点名称',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在城市',
  `country` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所在国家',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `adult_ticket_price` decimal(8, 2) NOT NULL COMMENT '成人票价',
  `child_ticket_price` decimal(8, 2) NOT NULL COMMENT '儿童票价',
  `location` geometry NOT NULL COMMENT '地理位置',
  `longitude` decimal(11, 8) NOT NULL COMMENT '经度',
  `latitude` decimal(10, 8) NOT NULL COMMENT '纬度',
  `open_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '开放时间（08:30-19:30）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述信息',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `stars` int NULL DEFAULT NULL COMMENT '几个A',
  `rating` decimal(8, 1) NULL DEFAULT 5.0 COMMENT '评分',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '运营状态（1-正常开放，0-暂停开放）',
  `tags` set('历史古迹','自然景观','文化场馆','宗教场所','主题公园','城市地标','休闲度假','购物街区','美食街区','红色旅游','户外探险','动物园','植物园','峡谷','溶洞','温泉','商圈街巷','亲子乐园','极限运动','江河湖泊','名人故居','瀑布','综合度假区','漂流') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '景点类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`, `create_time`) USING BTREE,
  SPATIAL INDEX `idx_attraction_location`(`location`) COMMENT '地理位置',
  INDEX `idx_attraction_rating_id`(`rating` DESC, `id` DESC) USING BTREE,
  INDEX `idx_attraction_city_rating_id`(`city` ASC, `rating` DESC, `id` DESC) USING BTREE,
  INDEX `idx_attraction_stars_rating_id`(`stars` ASC, `rating` DESC, `id` DESC) USING BTREE,
  INDEX `idx_attraction_city_stars_rating_id`(`city` ASC, `stars` ASC, `rating` DESC, `id` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for attraction_image
-- ----------------------------
DROP TABLE IF EXISTS `attraction_image`;
CREATE TABLE `attraction_image`  (
  `attraction_id` bigint NOT NULL COMMENT '景点ID',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片地址',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序序列',
  INDEX `idx_attraction_image_attraction_sort`(`attraction_id` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for attraction_order
-- ----------------------------
DROP TABLE IF EXISTS `attraction_order`;
CREATE TABLE `attraction_order`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `attraction_id` bigint NOT NULL COMMENT '景点ID',
  `context` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单内容',
  `total_price` decimal(10, 2) NOT NULL COMMENT '总价',
  `actual_price` decimal(10, 2) NOT NULL COMMENT '实际支付金额',
  `order_status` enum('待支付','已支付','已取消') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '已支付' COMMENT '订单状态',
  `time` datetime NOT NULL COMMENT '有效时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for content_comment
-- ----------------------------
DROP TABLE IF EXISTS `content_comment`;
CREATE TABLE `content_comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content_id` bigint NOT NULL COMMENT '帖子ID',
  `parent_comment_id` bigint NULL DEFAULT NULL COMMENT '父级评论ID',
  `comment_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `comment_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_connent_id`(`content_id` ASC) USING BTREE COMMENT '帖子ID'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for content_like
-- ----------------------------
DROP TABLE IF EXISTS `content_like`;
CREATE TABLE `content_like`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content_id` int NOT NULL COMMENT '用户帖子ID',
  `like_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_user_content`(`user_id` ASC, `content_id` ASC) USING BTREE COMMENT '保证一人一赞'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for hotel
-- ----------------------------
DROP TABLE IF EXISTS `hotel`;
CREATE TABLE `hotel`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '酒店名称',
  `type` int NOT NULL COMMENT '酒店类型',
  `address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '地址',
  `city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在城市',
  `price` decimal(8, 2) NOT NULL COMMENT '基本价格',
  `original_price` decimal(8, 2) NULL DEFAULT NULL COMMENT '原始基本价格',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `facilities` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设施（设施之间使用英文逗号隔开）',
  `longitude` decimal(11, 8) NOT NULL COMMENT '经度',
  `latitude` decimal(10, 8) NOT NULL COMMENT '纬度',
  `location` geometry NOT NULL COMMENT '地理位置',
  `open_year` year NULL DEFAULT NULL COMMENT '开业年份',
  `overall_rating` decimal(2, 1) NULL DEFAULT NULL COMMENT '评分',
  `stars` int NULL DEFAULT 0 COMMENT '星级',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `review_count` bigint NULL DEFAULT 0 COMMENT '评论数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_city`(`city` ASC) USING BTREE COMMENT '城市索引',
  INDEX `idx_hotel_rating_id`(`overall_rating` DESC, `id` ASC) USING BTREE,
  INDEX `idx_hotel_city_rating_id`(`city` ASC, `overall_rating` DESC, `id` ASC) USING BTREE,
  INDEX `idx_hotel_price_id`(`price` ASC, `id` ASC) USING BTREE,
  INDEX `idx_hotel_city_price_id`(`city` ASC, `price` ASC, `id` ASC) USING BTREE,
  INDEX `idx_hotel_stars_rating_id`(`stars` ASC, `overall_rating` DESC, `id` ASC) USING BTREE,
  INDEX `idx_hotel_city_stars_rating_id`(`city` ASC, `stars` ASC, `overall_rating` DESC, `id` ASC) USING BTREE,
  INDEX `idx_hotel_stars_price_id`(`stars` ASC, `price` ASC, `id` ASC) USING BTREE,
  INDEX `idx_hotel_city_stars_price_id`(`city` ASC, `stars` ASC, `price` ASC, `id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1975458044141768714 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for hotel_image
-- ----------------------------
DROP TABLE IF EXISTS `hotel_image`;
CREATE TABLE `hotel_image`  (
  `hotel_id` bigint NOT NULL COMMENT '酒店ID',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片地址',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '顺序',
  INDEX `idx_hotel_image_hotel_sort`(`hotel_id` ASC, `sort_order` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for hotel_order
-- ----------------------------
DROP TABLE IF EXISTS `hotel_order`;
CREATE TABLE `hotel_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `hotel_id` bigint NOT NULL COMMENT '酒店ID',
  `room_type_id` bigint NOT NULL COMMENT '房间类型ID',
  `room_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '房间类型',
  `room_count` int NOT NULL DEFAULT 1 COMMENT '预定房间数量',
  `night_num` int NOT NULL COMMENT '订购天数（单位：晚）',
  `check_in` date NOT NULL COMMENT '入住日期和时间',
  `check_out` date NOT NULL COMMENT '退房日期和时间',
  `total_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '总价 (原价）',
  `actual_price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '实际支付金额 (包含优惠后)',
  `order_status` enum('待支付','已支付','已取消') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '待支付' COMMENT '订单状态',
  `check_in_status` enum('未入住','已入住','已离店') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '未入住' COMMENT '入住状态',
  `payment_method` enum('支付宝','微信','银行卡','现金') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付方式',
  `coupon_id` bigint NULL DEFAULT NULL COMMENT '使用的优惠券ID',
  `discount_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `cancellation_policy` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取消政策描述',
  `cancellation_deadline` datetime NULL DEFAULT NULL COMMENT '取消截止时间',
  `cancellation_fee_ratio` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '取消费用比例',
  `guest_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '入住人姓名',
  `guest_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '入住人手机号',
  `guest_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '入住人邮箱',
  `arrival_time` varchar(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预计到达时间',
  `special_request` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '特别要求',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单标题',
  `book_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',
  `paid_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `cancellation_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '0-存在，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_number`(`order_id` ASC) USING BTREE,
  INDEX `idx_hotel_order_user_book`(`user_id` ASC, `is_deleted` ASC, `book_time` DESC, `id` DESC) USING BTREE,
  INDEX `idx_hotel_order_room_dates`(`room_type_id` ASC, `check_in` ASC, `check_out` ASC, `order_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2108464824369852419 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for hotel_review
-- ----------------------------
DROP TABLE IF EXISTS `hotel_review`;
CREATE TABLE `hotel_review`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `hotel_id` bigint NOT NULL COMMENT '酒店ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `rating` int NOT NULL COMMENT '评分（1-5）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `review_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_hotel_id`(`hotel_id` ASC) USING BTREE COMMENT '酒店ID'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pay_record
-- ----------------------------
DROP TABLE IF EXISTS `pay_record`;
CREATE TABLE `pay_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `biz_type` enum('hotel','ticket','vip') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型(1-酒店)',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `status` enum('WAIT_PAY','SUCCESS','FAIL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'WAIT_PAY' COMMENT '支付状态',
  `trade_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '第三方支付交易号（支付宝trade_no）',
  `money` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `absolute_expiry_enabled` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pay_record_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_pay_record_order_id_id`(`order_id` ASC, `id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2108464830069911554 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for plan_history
-- ----------------------------
DROP TABLE IF EXISTS `plan_history`;
CREATE TABLE `plan_history`  (
  `id` bigint NOT NULL COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `start_date` date NOT NULL COMMENT '旅游开始日期',
  `destination` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目的地',
  `day_num` int NOT NULL COMMENT '天数',
  `people` int NOT NULL COMMENT '人数',
  `budget` decimal(10, 2) NOT NULL COMMENT '预算',
  `plan_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '旅游文字计划',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  `plan_result` json NOT NULL COMMENT '旅游规划结果',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for room
-- ----------------------------
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `hotel_id` bigint NOT NULL COMMENT '所属酒店ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '房间类型名称',
  `price` decimal(10, 2) NOT NULL COMMENT '房间价格',
  `status` int NOT NULL DEFAULT 0 COMMENT '房间状态 0-不可订，1-可订',
  `area` int NULL DEFAULT NULL COMMENT '房间大小（单位：平米）',
  `bed` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '床的描述',
  `people` int NOT NULL COMMENT '可住人数',
  `floor` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在楼层',
  `stock` int NOT NULL COMMENT '库存',
  `facilities` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设施',
  `breakfast` tinyint NULL DEFAULT NULL COMMENT '0-没有提供，1-提供',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for room_daily_stock
-- ----------------------------
DROP TABLE IF EXISTS `room_daily_stock`;
CREATE TABLE `room_daily_stock`  (
  `room_type_id` bigint NOT NULL,
  `stay_date` date NOT NULL,
  `available_stock` int NOT NULL,
  PRIMARY KEY (`room_type_id`, `stay_date`) USING BTREE,
  CONSTRAINT `chk_room_daily_stock_nonnegative` CHECK (`available_stock` >= 0)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for room_image
-- ----------------------------
DROP TABLE IF EXISTS `room_image`;
CREATE TABLE `room_image`  (
  `id` bigint NOT NULL COMMENT '主键',
  `room_type_id` bigint NOT NULL COMMENT '房间类型ID',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片地址',
  `sort` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '序号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for spring_ai_chat_memory
-- ----------------------------
DROP TABLE IF EXISTS `spring_ai_chat_memory`;
CREATE TABLE `spring_ai_chat_memory`  (
  `conversation_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` enum('USER','ASSISTANT','SYSTEM','TOOL') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `timestamp` timestamp NOT NULL,
  INDEX `SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX`(`conversation_id` ASC, `timestamp` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ticket
-- ----------------------------
DROP TABLE IF EXISTS `ticket`;
CREATE TABLE `ticket`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `attraction_id` bigint NOT NULL COMMENT '景点ID',
  `ticket_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '门票类型（如成人票、儿童票、团体票）',
  `price` decimal(10, 2) NOT NULL COMMENT '门票单价',
  `stock` int NOT NULL COMMENT '门票库存',
  `valid_time` int NOT NULL COMMENT '有效时长（单位：小时）',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '门票说明',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '销售状态（1-可售，0-停售）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ticket_calendar_stock
-- ----------------------------
DROP TABLE IF EXISTS `ticket_calendar_stock`;
CREATE TABLE `ticket_calendar_stock`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL COMMENT '门票产品ID',
  `date` date NOT NULL COMMENT '日期',
  `price` decimal(10, 2) NOT NULL COMMENT '当日售价（可覆盖product表价格）',
  `stock` int NOT NULL COMMENT '当日可用库存',
  `status` tinyint NULL DEFAULT 1 COMMENT '1-可售 0-售罄/停售',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_date`(`product_id` ASC, `date` ASC) USING BTREE,
  INDEX `idx_date`(`date` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ticket_order_item
-- ----------------------------
DROP TABLE IF EXISTS `ticket_order_item`;
CREATE TABLE `ticket_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '快照：票种名称',
  `quantity` int NOT NULL,
  `unit_price` decimal(10, 2) NOT NULL COMMENT '下单时单价（价格快照）',
  `visit_date` date NOT NULL COMMENT '游玩日期（冗余，便于查询）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ticket_product
-- ----------------------------
DROP TABLE IF EXISTS `ticket_product`;
CREATE TABLE `ticket_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attraction_id` bigint NOT NULL COMMENT '关联景点',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '票种名称，如\"成人票\"',
  `ticket_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型: adult, child, student, senior...',
  `price` decimal(10, 2) NOT NULL COMMENT '挂牌价',
  `sale_price` decimal(10, 2) NOT NULL COMMENT '实际售价（支持折扣）',
  `stock_type` tinyint NOT NULL DEFAULT 1 COMMENT '库存模式: 1-无限 2-有限',
  `total_stock` int NULL DEFAULT NULL COMMENT '总库存（stock_type=2 时有效）',
  `min_quantity` int NULL DEFAULT 1 COMMENT '最少购买数量',
  `max_quantity` int NULL DEFAULT 10 COMMENT '最多购买数量',
  `valid_days` int NULL DEFAULT 1 COMMENT '有效期（天），如1天有效',
  `usage_rules` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '使用规则（JSON 或自由文本）',
  `status` tinyint NULL DEFAULT 1 COMMENT '1-上架 0-下架',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_attraction_id`(`attraction_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ticket_voucher
-- ----------------------------
DROP TABLE IF EXISTS `ticket_voucher`;
CREATE TABLE `ticket_voucher`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_item_id` bigint NOT NULL COMMENT '关联订单明细',
  `voucher_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '取票码/核销码',
  `status` tinyint NULL DEFAULT 0 COMMENT '0-未使用 1-已使用 2-已过期',
  `used_at` datetime NULL DEFAULT NULL,
  `expired_at` datetime NOT NULL COMMENT '过期时间 = visit_date + valid_days',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `voucher_code`(`voucher_code` ASC) USING BTREE,
  INDEX `idx_voucher_code`(`voucher_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for uploaded_file
-- ----------------------------
DROP TABLE IF EXISTS `uploaded_file`;
CREATE TABLE `uploaded_file`  (
  `id` bigint NOT NULL COMMENT '主键',
  `file_type` enum('image','audio','video','document') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件类型',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件存储地址',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称（展示给用户的）对于展示的图片可以选择不存',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL COMMENT '用户ID,主键',
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户邮箱',
  `password` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户密码（加密）',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '保密' COMMENT '用户性别：男、女、保密',
  `bio` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户简介',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `role` int NOT NULL DEFAULT 0 COMMENT '用户角色，0-普通用户，1-editor，2-admin，3-super_admin',
  `status` int NOT NULL DEFAULT 0 COMMENT '帐号状态，0-正常，1-禁用',
  `vip` int NOT NULL DEFAULT 0 COMMENT '会员状态，0-非会员，1-会员',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_delete` int NOT NULL DEFAULT 0 COMMENT '0-账号存在，1-账号已注销',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id`(`id` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_content
-- ----------------------------
DROP TABLE IF EXISTS `user_content`;
CREATE TABLE `user_content`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `context_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容',
  `location_tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地点标签',
  `post_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发表时间',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞量',
  `comment_count` int NULL DEFAULT 0 COMMENT '评论数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_location`(`location_tag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for vip_payment_record
-- ----------------------------
DROP TABLE IF EXISTS `vip_payment_record`;
CREATE TABLE `vip_payment_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `out_trade_no` bigint NOT NULL COMMENT '商户订单号(本地生成的唯一ID)',
  `trade_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付宝交易凭证号',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `subject` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'VIP会员购买' COMMENT '订单标题',
  `duration_days` int NULL DEFAULT 30 COMMENT '购买的会员时长(天)',
  `pay_status` tinyint NULL DEFAULT 0 COMMENT '支付状态: 0-待支付, 1-支付成功, 2-支付失败',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付完成时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_out_trade_no`(`out_trade_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'VIP会员支付记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for voucher
-- ----------------------------
DROP TABLE IF EXISTS `voucher`;
CREATE TABLE `voucher`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `attraction_id` bigint NOT NULL COMMENT '景点ID',
  `pay_value` bigint NOT NULL COMMENT '支付金额，单位分，100分-1元',
  `actual_value` bigint NOT NULL COMMENT '抵扣金额，单位分',
  `type` tinyint NOT NULL COMMENT '优惠券类型 1-景点 2-酒店',
  `status` tinyint NOT NULL COMMENT '1-上架，2-下架',
  `start_time` datetime NOT NULL COMMENT '抢购开始时间',
  `end_time` datetime NOT NULL COMMENT '抢购结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for voucher_order
-- ----------------------------
DROP TABLE IF EXISTS `voucher_order`;
CREATE TABLE `voucher_order`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `voucher_id` bigint NOT NULL COMMENT '代金券ID',
  `pay_type` tinyint NOT NULL COMMENT '支付方式 1-微信；2-支付宝',
  `status` tinyint NOT NULL COMMENT '订单状态：1-未支付 2-已支付 3-已使用 4-已取消',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `use_time` datetime NOT NULL COMMENT '使用时间',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- View structure for user_view
-- ----------------------------
DROP VIEW IF EXISTS `user_view`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `user_view` AS select `user`.`id` AS `id`,`user`.`username` AS `username`,`user`.`email` AS `email` from `user`;

-- ----------------------------
-- Triggers structure for table attraction
-- ----------------------------
DROP TRIGGER IF EXISTS `attraction_set_location_before_insert`;
delimiter ;;
CREATE TRIGGER `attraction_set_location_before_insert` BEFORE INSERT ON `attraction` FOR EACH ROW begin
    IF NEW.longitude IS NOT NULL AND NEW.latitude IS NOT NULL THEN
        SET NEW.location = POINT(NEW.longitude, NEW.latitude);
    END IF;
end
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table attraction
-- ----------------------------
DROP TRIGGER IF EXISTS `attraction_set_location_before_update`;
delimiter ;;
CREATE TRIGGER `attraction_set_location_before_update` BEFORE UPDATE ON `attraction` FOR EACH ROW BEGIN
    IF NEW.longitude != OLD.longitude OR NEW.latitude != OLD.latitude THEN
        IF NEW.longitude IS NOT NULL AND NEW.latitude IS NOT NULL THEN
            SET NEW.location = POINT(NEW.longitude, NEW.latitude);
        END IF;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table hotel
-- ----------------------------
DROP TRIGGER IF EXISTS `hotel_set_location_before_insert`;
delimiter ;;
CREATE TRIGGER `hotel_set_location_before_insert` BEFORE INSERT ON `hotel` FOR EACH ROW begin
    IF NEW.longitude IS NOT NULL AND NEW.latitude IS NOT NULL THEN
        SET NEW.location = POINT(NEW.longitude, NEW.latitude);
    END IF;
end
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
