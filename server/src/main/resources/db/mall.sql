/*
 Navicat Premium Data Transfer

 Source Server         : root
 Source Server Type    : MySQL
 Source Server Version : 80019
 Source Host           : localhost:3306
 Source Schema         : mall

 Target Server Type    : MySQL
 Target Server Version : 80019
 File Encoding         : 65001

 Date: 15/06/2026 15:28:05
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for album
-- ----------------------------
DROP TABLE IF EXISTS `album`;
CREATE TABLE `album`  (
  `picture_id` int NOT NULL AUTO_INCREMENT COMMENT '图片id',
  `commodity_id` int NOT NULL COMMENT '所属商品id',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片路径',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`picture_id`) USING BTREE,
  INDEX `fk_album_commodity`(`commodity_id`) USING BTREE,
  CONSTRAINT `fk_album_commodity` FOREIGN KEY (`commodity_id`) REFERENCES `commodity` (`commodity_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 91 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of album
-- ----------------------------
INSERT INTO `album` VALUES (1, 1, '/upload/sp/qi1.webp', '2026-04-09 10:25:26');
INSERT INTO `album` VALUES (2, 1, '/upload/sp/qi2.webp', '2026-04-09 10:25:41');
INSERT INTO `album` VALUES (3, 1, '/upload/sp/qi3.webp', '2026-04-09 10:25:55');
INSERT INTO `album` VALUES (4, 2, '/upload/sp/hei.webp', '2026-04-09 10:26:05');
INSERT INTO `album` VALUES (5, 2, '/upload/sp/hei2.webp', '2026-04-09 10:26:19');
INSERT INTO `album` VALUES (6, 2, '/upload/sp/hei3.webp', '2026-04-09 10:26:33');
INSERT INTO `album` VALUES (7, 2, '/upload/sp/hei4.webp', '2026-04-09 10:26:41');
INSERT INTO `album` VALUES (8, 2, '/upload/sp/hei5.webp', '2026-04-09 10:26:53');
INSERT INTO `album` VALUES (9, 3, '/upload/sp/ban.avif', '2026-04-12 14:03:27');
INSERT INTO `album` VALUES (10, 4, '/upload/sp/d1.avif', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (11, 4, '/upload/sp/d2.avif', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (12, 4, '/upload/sp/d3.avif', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (13, 4, '/upload/sp/d4.avif', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (14, 4, '/upload/sp/d5.avif', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (15, 4, '/upload/sp/d6.avif', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (16, 5, '/upload/sp/pg1.webp', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (17, 5, '/upload/sp/pg2.webp', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (18, 6, '/upload/sp/xj1.avif', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (19, 7, '/upload/sp/b1.webp', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (20, 7, '/upload/sp/b2.webp', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (21, 8, '/upload/sp/ys1.webp', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (22, 9, '/upload/sp/ch1.webp', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (23, 9, '/upload/sp/ch2.webp', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (24, 9, '/upload/sp/ch3.webp', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (25, 10, '/upload/sp/ak1.avif', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (26, 11, '/upload/sp/cp1.avif', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (27, 11, '/upload/sp/cp2.webp', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (28, 11, '/upload/sp/cp3.webp', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (29, 12, '/upload/sp/tm1.webp', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (30, 13, '/upload/sp/yg1.avif', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (31, 14, '/upload/sp/zz1.avif', '2026-04-12 14:07:57');
INSERT INTO `album` VALUES (32, 15, '/upload/sp/zuo1.avif', '2026-04-12 14:52:16');
INSERT INTO `album` VALUES (33, 16, '/upload/sp/qh1.webp', '2026-04-12 14:52:16');
INSERT INTO `album` VALUES (34, 16, '/upload/sp/qh2.webp', '2026-04-12 14:52:16');
INSERT INTO `album` VALUES (35, 17, '/upload/sp/ry.webp', '2026-04-12 14:52:16');
INSERT INTO `album` VALUES (36, 17, '/upload/sp/ry1.webp', '2026-04-12 14:52:16');
INSERT INTO `album` VALUES (37, 18, '/upload/sp/bj.avif', '2026-04-12 14:52:16');
INSERT INTO `album` VALUES (38, 19, '/upload/sp/lq.webp', '2026-04-12 14:52:16');
INSERT INTO `album` VALUES (39, 20, '/upload/sp/fl.avif', '2026-04-12 14:52:16');
INSERT INTO `album` VALUES (40, 20, '/upload/sp/fl2.avif', '2026-04-12 14:52:16');
INSERT INTO `album` VALUES (41, 21, '/upload/sp/lp.avif', '2026-04-12 14:52:16');
INSERT INTO `album` VALUES (42, 21, '/upload/sp/lp2.avif', '2026-04-12 14:52:16');
INSERT INTO `album` VALUES (43, 22, '/upload/sp/lf.avif', '2026-04-12 14:52:16');
INSERT INTO `album` VALUES (44, 22, '/upload/sp/lf2.avif', '2026-04-12 14:52:16');
INSERT INTO `album` VALUES (45, 23, '/upload/sp/mh.avif', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (46, 23, '/upload/sp/mh2.avif', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (47, 24, '/upload/sp/tn.avif', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (48, 25, '/upload/sp/ls.avif', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (49, 26, '/upload/sp/yw.webp', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (50, 27, '/upload/sp/tz.avif', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (51, 28, '/upload/sp/ml.webp', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (52, 28, '/upload/sp/ml2.webp', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (53, 29, '/upload/sp/mg.avif', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (54, 29, '/upload/sp/mg2.avif', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (55, 30, '/upload/sp/sz.avif', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (56, 30, '/upload/sp/sz2.avif', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (57, 31, '/upload/sp/sm.webp', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (58, 31, '/upload/sp/sm2.webp', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (59, 32, '/upload/sp/sp.webp', '2026-04-14 08:27:20');
INSERT INTO `album` VALUES (60, 33, '/upload/sp/cz.webp', '2026-04-14 11:22:03');
INSERT INTO `album` VALUES (61, 35, '/upload/sp/sb.webp', '2026-04-14 11:22:03');
INSERT INTO `album` VALUES (62, 35, '/upload/sp/sb.webp', '2026-04-14 11:22:03');
INSERT INTO `album` VALUES (73, 39, '/upload/sp/cbaeaf74cd7b464fae2d45792652dfe8.webp', '2026-05-11 11:20:56');
INSERT INTO `album` VALUES (74, 39, '/upload/sp/c2eb8eafe7e44bcc9c990113d2291fd9.webp', '2026-05-11 11:20:56');
INSERT INTO `album` VALUES (75, 39, '/upload/sp/1643f1a271df49249b0efb39306250a9.webp', '2026-05-11 11:20:56');
INSERT INTO `album` VALUES (76, 40, '/upload/sp/b3fcf4f3e87d482dba17fe87403dc478.webp', '2026-05-11 11:23:30');
INSERT INTO `album` VALUES (77, 40, '/upload/sp/cb07cf8d509b413db79242cf992fa5c1.webp', '2026-05-11 11:23:30');
INSERT INTO `album` VALUES (89, 44, '/upload/sp/a38d8d9d4b6f41a28f59d2fca5e0bdab.webp', '2026-06-11 08:54:11');
INSERT INTO `album` VALUES (90, 44, '/upload/sp/e8e3a938338d4c2ea6dcf1ab8eca966a.webp', '2026-06-11 08:54:11');

-- ----------------------------
-- Table structure for browse_history
-- ----------------------------
DROP TABLE IF EXISTS `browse_history`;
CREATE TABLE `browse_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `commodity_id` int NOT NULL,
  `browse_count` int NULL DEFAULT 1,
  `last_browse_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_commodity`(`user_id`, `commodity_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of browse_history
-- ----------------------------
INSERT INTO `browse_history` VALUES (1, 1, 4, 2, '2026-06-14 14:03:03');
INSERT INTO `browse_history` VALUES (2, 1, 10, 2, '2026-06-14 14:03:15');
INSERT INTO `browse_history` VALUES (3, 1, 23, 2, '2026-06-14 14:08:45');
INSERT INTO `browse_history` VALUES (4, 1, 29, 1, '2026-06-14 14:08:49');
INSERT INTO `browse_history` VALUES (5, 1, 26, 1, '2026-06-14 14:08:56');
INSERT INTO `browse_history` VALUES (6, 1, 12, 1, '2026-06-14 14:21:01');
INSERT INTO `browse_history` VALUES (7, 1, 1, 1, '2026-06-14 14:35:26');
INSERT INTO `browse_history` VALUES (8, 2, 17, 1, '2026-06-15 15:08:17');

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `type_id` int NOT NULL AUTO_INCREMENT COMMENT '商品类型id',
  `type_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型名称',
  `type_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型描述',
  `img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '装饰图片路径',
  `color` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '背景色',
  `en` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '英文装饰',
  PRIMARY KEY (`type_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '服装衣饰', '时尚美衣低价购', '/upload/category/fzys.png', '#ffe60f', 'clothes');
INSERT INTO `category` VALUES (3, '数码设备', '数智好物省心买', '/upload/category/smsb.png', '#85efff', 'digital');
INSERT INTO `category` VALUES (4, '鞋包配饰', '美鞋靓包超值配', '/upload/category/xbps.png', '#86fb9e', 'shoe');
INSERT INTO `category` VALUES (5, '美妆护肤', '美妆尖货底价抢', '/upload/category/mzhf.png', '#ffc0e5', 'cosmetics');
INSERT INTO `category` VALUES (6, '家具家电', '居家良品底价淘', '/upload/category/jjjd.png', '	#FFB38B', 'furniture');
INSERT INTO `category` VALUES (7, '礼品艺品', '雅致礼赠省心买', '/upload/category/lpyp.png', '#FFA6A6', 'gift');
INSERT INTO `category` VALUES (8, '食品饮料', '舌尖美味低价享', '/upload/category/spyl.png', '	#FFB347', 'food');
INSERT INTO `category` VALUES (9, '图书用具', '学识装备不贵', '/upload/category/tsyj.png', '#C7A4E0', 'book');
INSERT INTO `category` VALUES (10, '宠物用品', '主子满意省钱囤', '/upload/category/cwyp.png', '#E8C89A', 'pet');
INSERT INTO `category` VALUES (11, '鲜花绿植', '绿意鲜氧超值购', '/upload/category/xhlz.png', '	#A8D94D', 'plant');
INSERT INTO `category` VALUES (12, '省钱卡券', '开卡即省省省', '/upload/category/sqkq.png', '	#6EC8E0', 'coupon');
INSERT INTO `category` VALUES (18, '其他更多', '万物低价淘', '/upload/category/qtgd.png', '#F8F9FA', 'other');

-- ----------------------------
-- Table structure for commodity
-- ----------------------------
DROP TABLE IF EXISTS `commodity`;
CREATE TABLE `commodity`  (
  `commodity_id` int NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `user_id` int NOT NULL COMMENT '所属用户id',
  `status` int NULL DEFAULT 1 COMMENT '商品状态 0：下架，1：上架, 2:售出',
  `commodity_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `commodity_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品介绍',
  `brand` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌',
  `quality` int NULL DEFAULT NULL COMMENT '成色1-10分',
  `use_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '使用状态',
  `price` decimal(10, 2) NOT NULL COMMENT '商品价格',
  `commodity_type` int NOT NULL COMMENT '商品类型',
  `browse` int NULL DEFAULT 0 COMMENT '浏览量',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`commodity_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of commodity
-- ----------------------------
INSERT INTO `commodity` VALUES (1, 1, 1, '高雅人士企鹅连体睡衣女', '高雅人士企鹅连体睡衣女冬季天珊瑚绒加厚搞怪抽象男生家居服套装，表演穿过一次，已进行过干洗！保证干净。尺码看图 三。7 套打包 130包邮！', '\r\nDisney/迪士尼', 8, '轻微穿着痕迹', 20.00, 1, 2, '2026-04-09 08:24:52');
INSERT INTO `commodity` VALUES (2, 2, 1, '百事可乐联名361°运动卫衣', '百事可乐联名361°运动卫衣，黑色拼色款，165/88A码，男女都能穿，宽松版型，春秋冬都能穿，面料67.1%棉+32.9%聚酯纤维，穿着舒服，袖子和下摆有百事联名logo，细节都在图里啦～\r\n保存很好，无污渍无瑕疵，细节都在图里啦～\r\n包邮！喜欢直接拍，随时问我细节～\r\n胸围108\r\n衣长56', '\r\n361°', 8, '轻微穿着痕迹', 50.00, 1, 1, '2026-04-09 09:38:45');
INSERT INTO `commodity` VALUES (3, 1, 1, '出血小板衣服一套 m 码', '出血小板衣服一套 m 码\r\n只在室内穿过一次\r\n包道具 包邮', '', 7, '几乎全新', 40.00, 1, 0, '2026-04-09 09:39:25');
INSERT INTO `commodity` VALUES (4, 2, 1, 'RTX3050显卡游戏本', 'RTX3050显卡游戏本 火影T5C笔记本电脑联神舟神州战神炫龙笔记本游戏本 神舟炫龙 联想拯救者 微星 惠普戴尔宏碁 火影 机械革命 机械师 雷神 华硕 天选飞行堡垒 战神z8 战神TX系列\r\n\r\ni5-10400六核台式处理器 硬件跑分实测\r\nRTX 3050 4G显卡 一般游戏无压力\r\n8G DDR4 3200内存条 还有卡槽\r\n512G M.2固态硬盘 还有两个硬盘位\r\n电池健康度75%多 如图鲁大师配置图\r\n15.5寸144HZ高清屏幕 亮度高 色彩好\r\n\r\n成色如图 实物拍摄 图片怎么样发出就怎么样\r\n\r\n本子检测功能都正常 无暗病 无毛病\r\n外观酒精消毒 内部已经清灰 CPU和显卡上了信越9721散热硅脂 不怕炸机 放心使用 ______________________\r\n附带原装充电器 快递发顺丰邮费到付 验好货再签收 除电池外整机质保6个月 有质量问题我这边出邮费退回 放心有质量问题我这边出邮费退回 放心有质量问题我这边出邮费退回 放心有质量问题我这边出邮费退回 放心', '火影', 5, '明显磕碰划痕', 2160.00, 3, 2, '2026-04-09 09:40:01');
INSERT INTO `commodity` VALUES (5, 1, 1, '苹果iphone14Pro', '苹果iphone14Pro\r\n原装无拆修 电池74 暗紫色 内存256g\r\n屏幕无瑕疵 边框无磕碰 国行原装\r\n手机随意升级系统和退出id\r\n店铺质保一年七天内质量问题支持退换', 'Apple/苹果', 8, '几乎全新', 2499.00, 3, 0, '2026-04-09 09:40:34');
INSERT INTO `commodity` VALUES (6, 2, 1, '松下LUMIX fz47长焦数码相机｜黑色款', '松下LUMIX fz47长焦数码相机｜黑色款\r\n功能正常，成色还可以，有点使用痕迹，按键灵敏，屏幕轻微划伤没黄斑，镜头微微灰，适合拍月亮、演唱会、风景、人像啥的，操作简单，新手也能用\r\n包邮，支持自提，价格可聊，喜欢直接拍或者私聊我，细节图可以再发～配件有镜头盖，双点池原装充电器', 'Panasonic/松下', 8, '几乎全新', 425.00, 3, 0, '2026-04-12 14:14:19');
INSERT INTO `commodity` VALUES (7, 1, 1, '全新正品kipling x EMILY IN PARI联名系列手提包', '全新正品kipling x EMILY IN PARI联名系列手提包 靓丽紫\r\n女款轻便帆布包新款休闲潮流单肩包托特包，KI3186，\r\n小号尺寸：19X24X12cm', 'Kipling', 10, '全新', 199.00, 4, 0, '2026-04-12 14:18:53');
INSERT INTO `commodity` VALUES (8, 2, 1, 'Jamie福袋包', 'Jamie福袋包，牛皮版本，经典黑金配色，菱格纹缝线，金色YSL金属logo，链条可收紧，抽绳包型设计。尺寸14.5*15.5cm，容量日常够用，手机卡包钥匙都能装，单肩斜挎手提都可以，背法多样，百搭实用。\r\n\r\n全新带原装盒子和防尘袋，配件齐全，皮质柔软细腻，做工精致，质感很好。送礼自用都很合适。\r\n\r\n包邮，喜欢的小姐妹欢迎私聊～', 'other/其他', 10, '全新', 178.00, 4, 0, '2026-04-12 14:23:59');
INSERT INTO `commodity` VALUES (9, 1, 1, 'COACH蔻驰女士包高仿版', 'COACH蔻驰女士包高仿版，全新，保存很好，米白色配色，容量大，单肩斜挎都可以，日常通勤、出门都很实用。包身有COACH皮标，内里干净，拉链顺滑，做工细致\r\n\r\n包邮，同城可自提，价格可聊，喜欢的朋友可以私聊看细节图～二手商品售出不退不换。', '\r\nCOACH/蔻驰', 10, '全新', 299.00, 4, 0, '2026-04-12 14:26:35');
INSERT INTO `commodity` VALUES (10, 2, 1, 'AKF轻透柔焦散粉', '全新未拆封，品牌方授权拿货，假一赔十！\r\nAKF轻透柔焦散粉，04号色，10g装，镭射包装，日期新鲜，保质期到2027年。\r\n控油定妆，适合各种肤质，妆效自然，百搭不挑人。\r\n全国包邮，支持自提。\r\n喜欢直接拍，细节可私聊～', 'AKF', 10, '全新', 8.00, 5, 2, '2026-04-12 14:30:49');
INSERT INTO `commodity` VALUES (11, 1, 1, 'cs11', '16元便宜出16色眼影，全新包邮的，偏远地区除外\r\n新娘跟妆化妆店倒闭，囤了好多眼影盘，现在全部出掉，用过的我就留着自用了，出的都是全新的\r\n\r\n16色眼影盘，大地色珠光哑光眼影盘。\r\n腮红，高光，眼影合一，上色显色，不飞粉。\r\n今年囤的，保质期还有三年，可以放心囤货\r\n\r\n线下彩妆店买的，原价200，现在16一套全新包邮出了（全都是运费啊，亏本出），懂得捡漏的来哈', 'myho', 10, '全新', 15.88, 5, 0, '2026-04-12 14:33:09');
INSERT INTO `commodity` VALUES (12, 2, 1, 'cs12', '婷美绿BB多效无暇修颜霜 经典款升级款色号齐全 40g正装一支\r\n1号色小麦色-【适合黑皮肤】\r\n2号色自然色_适合黄皮自然肤色【痘痘痘印也能遮】\r\n3号色象牙白_适合白皮白皙肤色【脸部瑕疵都能遮】\r\n（库存充足 多买优惠）\r\n\r\n线下美妆店低价处理 正品保证\r\n拆封未使用过 瓶身完好\r\n保质期2027年左右', '\r\n婷美', 10, '全新', 19.80, 5, 1, '2026-04-12 14:36:09');
INSERT INTO `commodity` VALUES (13, 1, 1, 'cs13', '衣柜二手【全新清仓】衣柜现代简约实木组装家用卧室出租房用简易挂衣柜木\r\n质收纳大衣橱\r\n\r\n家具厂仓库升级搬迁，库存一批全新衣柜清仓处理，数量不多，售完即止\r\n\r\n【尺寸规格】\r\n长80 高180 厚40 【2门】198元\r\n长120 高180 厚40【3门】 298元\r\n长140 高180 厚40 【4门】368元\r\n\r\n【颜色】纯白色，浅胡桃色，\r\n白枫色， 柚木色', NULL, 8, '轻微使用痕迹', 368.00, 6, 0, '2026-04-12 14:38:02');
INSERT INTO `commodity` VALUES (14, 2, 1, 'cs14', '床头柜子小茶几小桌子出沙发边几床头桌置物架\r\n\r\n店铺到期，生意难做，现有原外贸出口单滞留，商品亏本出售，回笼资金。发出的商品请放心都是全新未拆封的。\r\n暖白和原木两色可选\r\n圆角款30*30*30cm￥14.9\r\n单层圆桌30*30*29cm ￥16.9\r\n双层小款40*24*53cm ￥26.9\r\n双层40*40*53cm ￥32.9\r\n双层60*30*53cm ￥46.9\r\n双层48*48*53cm￥49.9\r\n双层60*40*53cm￥56.9\r\n双层80*30*53cm￥59.9', NULL, 8, '轻微使用痕迹', 14.90, 6, 0, '2026-04-12 14:40:01');
INSERT INTO `commodity` VALUES (15, 1, 1, 'cs15', '宜家家居玻璃茶几，宜家KLINGSBO边桌，黑色铁艺+钢化玻璃，双层设计，带下层收纳。尺寸49cm宽，62cm高。桌面玻璃可以单独拆下来，下层镂空，放杂物方便。整体结实，承重没问题。实物9成新，放久有点灰尘，很容易洗干净，跟图片同款。只支持自提，不退不换。30元一张，55元2张，共有2张。需要的私聊。', '\r\nIKEA/宜家', 8, '轻微使用痕迹', 27.50, 6, 0, '2026-04-12 14:41:48');
INSERT INTO `commodity` VALUES (16, 2, 1, 'cs16', '全新未用！文韵陶瓷青花斗笠杯6件套，带原包装盒～釉下青花手绘，6款花色不重样：鱼跃龙门、莲花、古建筑等，寓意好又好看，瓷质细腻釉面亮。口径约8.5cm，高4cm，泡茶喝茶都合适，家用送礼都有面儿！包邮到家（新疆西藏除外），价格可小刀，喜欢的宝子直接拍，细节随时问～', '文韵陶瓷', 10, '全新', 28.00, 7, 0, '2026-04-14 08:10:52');
INSERT INTO `commodity` VALUES (17, 1, 1, 'cs17', '汝窑金竹茶具套装\r\n高档礼盒，全新的，品质很棒，包邮', '文韵陶瓷', 10, '全新', 60.00, 7, 1, '2026-04-14 08:10:52');
INSERT INTO `commodity` VALUES (18, 2, 1, 'cs18', '【全新未拆封】第五人格汐月之时系列精美盲盒礼盒\r\n\r\n全套45件，黑色高档礼盒包装，内容齐全：明信片4张、卡膜20张、小卡10张、拍立得10张、7.5cm吧唧徽章1个，包装完整，收藏送礼都很合适～\r\n\r\n关注收藏后找我，优先发货', NULL, 10, '全新未拆封', 15.88, 7, 0, '2026-04-14 08:15:02');
INSERT INTO `commodity` VALUES (19, 1, 1, 'cs19', '临期未拆封零食大礼包，家里囤多了低价转，到期26年五月一号五月十几号，时间不等，正品保证～混装19.9元包邮12袋，\r\n\r\n好丽友薯愿非油炸薯片、旺旺雪饼、乐事薯片、独立小包装，包装完好，放心吃！', '\r\nORION/好丽友', 10, '全新', 19.90, 8, 0, '2026-04-14 08:16:51');
INSERT INTO `commodity` VALUES (20, 2, 1, 'cs20', '60包无蔗糖坚果茯苓八珍糕无糖食品黑米芝麻传统糕点零食大全各', NULL, 10, '全新', 8.80, 8, 0, '2026-04-14 08:19:41');
INSERT INTO `commodity` VALUES (21, 1, 1, 'cs21', '良品铺子柠檬酸辣鸡脚筋100g网红零食解馋追剧宿舍小零食鸡爪零食\r\n\r\n柠檬酸辣鸡脚筋100克 好原料造就好味道 柠檬酸辣鸡脚筋 清新柠檬酸辣 清新不腻!大个脆弹鸡脚筋 脆弹爽口', NULL, 10, '全新', 15.80, 8, 0, '2026-04-14 08:22:05');
INSERT INTO `commodity` VALUES (22, 2, 1, 'cs22', '全8册雷锋的故事一二年级红色经典励志故事书注音小英雄雨来\r\n\r\n全新正版现货，实拍图，无破损无缺页。', NULL, 8, '轻微使用', 6.51, 9, 0, '2026-04-14 08:24:16');
INSERT INTO `commodity` VALUES (23, 1, 1, 'cs23', '漫画讲透道德经+渔樵问对\r\n\r\n全新正版现货，实拍图，无破损无缺页。\r\n', NULL, 10, '全新', 15.00, 9, 2, '2026-04-14 08:26:22');
INSERT INTO `commodity` VALUES (24, 2, 1, 'cs24', '二手 读经典名著：童年 9787530662595\r\n\r\n【购买须知】\r\n- 商品信息】正版书籍读经典名著：童年 9787530662595\r\n- 本店书籍品相经人工核验、描述不符包售后、套装书籍默认只有单本，一般当日发货\r\n- 正版二手书，成色85，展示图片视频仅供参考，有笔记划线折痕，单本销售不含光盘或附件\r\n- 如有特殊要求请提前联系客服', NULL, 8, '轻微使用', 7.99, 9, 0, '2026-04-14 08:29:26');
INSERT INTO `commodity` VALUES (25, 1, 1, 'cs25', '西班牙睡鼠，拇指鼠，学生，宿舍小宠物，可爱温顺不咬人，个个都是肥肥的幼崽，拍下即默认。\r\n\r\n到家就可以亲人上手，体质强，亲人度高，饲养简单。一只25.9元包邮包活。有拇指猴子，迷你小龙猫之称\r\n\r\n想好再下单，毕竟是活物，不是物品，鸽子勿扰。宠物发货严格，需要安排', '鼠类', 10, '宠物', 25.90, 10, 0, '2026-04-14 08:31:30');
INSERT INTO `commodity` VALUES (26, 2, 1, 'cs26', '鹦鹉活鸟虎皮鹦鹉小型鸟类手养宠物会学说话一公一母\r\n\r\n绿色/蓝色/黄色/ 白色， 宝石蓝\r\n共五个颜色可选 公/母可选，可消息我备注。包邮包活的！', '鹦鹉', 10, '宠物', 39.90, 10, 1, '2026-04-14 08:33:01');
INSERT INTO `commodity` VALUES (27, 1, 1, 'cs27', '普通的小兔子，小时候可以当宠物，长大了可以当食物不是那种养几天一个星期就挂了的，皮实得很，各种蔬菜都吃，不用买兔粮牧草之类的，好养活。可以滴血认主了。养到十万年还能触发隐藏技能，变身柔骨兔，那可是相当值得！现在二十一只买不了吃亏买不了上当。只支持本地自提哦～', '兔子', 10, '宠物', 25.00, 10, 0, '2026-04-14 08:35:14');
INSERT INTO `commodity` VALUES (28, 2, 1, 'cs28', '【带10-13花剑】梦香兰文心兰盆栽带花苞花卉浓香型植物室内客厅易养殖\r\n\r\n梦香文心兰盆栽，带花苞，香味浓，室内开花效果好，新手也能养活。现在正是开花季，花剑多，香味扑鼻，桌面摆放很合适。\r\n\r\n这一批都是大爆盆文心兰，建议选择10支花剑以上，马上就可以赏花。', '兰花', 10, '全新', 24.80, 11, 0, '2026-04-14 08:39:28');
INSERT INTO `commodity` VALUES (29, 1, 1, 'cs29', '【3棵9.8包邮】处理玫瑰花苗老桩四季开花大花月季浓香盆栽花卉室内\r\n\r\n云南基地清货，处理混色玫瑰老桩特价出！\r\n精品嫁接苗，货量不多，喜欢不要错过哦！种植3个月左右开花，四季可种植！', '玫瑰', 10, '全新', 9.80, 11, 1, '2026-04-14 11:09:28');
INSERT INTO `commodity` VALUES (30, 2, 1, 'cs30', '发财树盆栽室内植物净化空气办公室花卉桌面绿植好养\r\n发财树整体高度20-25厘米，都是精选的哦～', '发财树', 10, '全新', 6.80, 11, 0, '2026-04-14 11:11:30');
INSERT INTO `commodity` VALUES (31, 1, 1, 'cs31', '办了山姆卡，一年去不了几次，挂到咸鱼，随缘代购。不嫌麻烦，不怕辛苦，随时问我。我的坐标在深圳，辛苦费3元！一单3元！\r\n山姆app下单。', '山姆', 5, '已使用', 3.00, 12, 0, '2026-04-14 11:15:22');
INSERT INTO `commodity` VALUES (32, 2, 1, 'cs32', '新开88VIP权益，网易云黑胶vip，饿了么吃货年卡，夸克网盘vip，高德打车，出了不退。\r\n1⃣️网易会员年卡50\r\n2⃣️优酷／芒果年卡40\r\n3⃣️饿了么18r\r\n4⃣️夸克10r\r\n5⃣️高德打车会员10r\r\n6⃣️飞猪省钱卡8r\r\n7⃣️希尔顿会员5r\r\n8⃣️万豪尊贵金卡5r\r\n淘票票10r\r\n点击我想要私聊会员+改价\r\n请自行确认手机号无误，售出绑定成功后不退不换', '', 10, '未使用', 0.77, 12, 0, '2026-04-14 11:17:57');
INSERT INTO `commodity` VALUES (33, 1, 1, '1000块钱带绿牌直出', '需要的来，1000块钱带绿牌直出，可小刀，仅支持货到付款当面交易，成都市龙泉驿区师大花园', NULL, 5, '饱经风霜', 800.00, 18, 0, '2026-04-14 11:20:39');
INSERT INTO `commodity` VALUES (35, 1, 1, 'gpw3稀有白色', 'gpw3稀有白色，刚入手99新', 'Logitech/罗技', 8, '轻微使用', 600.00, 18, 0, '2026-04-14 11:23:22');
INSERT INTO `commodity` VALUES (39, 1, 1, '华硕RedolBook14EP 14寸笔记本', '华硕RedolBook14EP 14寸笔记本\ni5-10210U处理器 8G内存 512G固态\n性能稳定 办公学习追剧都没问题\n成色几乎全新 外观无明显痕迹 具体看图\n包邮 可自提\n售出不退不换\n喜欢直接拍 细节私聊～', ' Asus/华硕', NULL, '轻微使用痕迹', 1200.00, 3, 0, '2026-05-11 11:20:56');
INSERT INTO `commodity` VALUES (40, 1, 1, '沐浴露纯白柠檬清香720ml男,大牌正品红石榴山茶花家用女泡沫丰富，大瓶家庭装无刺激儿童通用沐浴液持久留香。', '沐浴露纯白柠檬清香720ml男,大牌正品红石榴山茶花家用女泡沫丰富，大瓶家庭装无刺激儿童通用沐浴液持久留香。\n\n超市老款临期半价处理，数量不多，均在保质期内。有纯白、柠檬和芦荟三种可以选择。本店商品保正品包售后。', '其他', NULL, '全新', 8.80, 5, 0, '2026-05-11 11:23:30');
INSERT INTO `commodity` VALUES (44, 1, 1, '个人自用高配台式电脑主机转让，原价四千多，现低价出。  ', '个人自用高配台式电脑主机转让，原价四千多，现低价出。  \n直播、剪辑、渲染、电商办公、吃鸡游戏全能跑，比七百多的办公机强好几倍，流畅玩游戏完全没问题。  \n\n支持现场自提试机，也发快递。二手贩子勿扰，纯个人闲置。  \n\n单主机，连接线全套都有。加190元可配24寸液晶显示器。  \n\n【主板】英特尔X99固态豪华千兆主板  \n【处理器】英特尔2666v3 10核20线程  \n【显卡】R7 350 Series 4G高级独立显卡（别拿那些960和580大矿卡来比）  \n【内存】16G大内存，多开稳定  \n【固态】250G高速固态  \n【系统】Windows 10 专业版', '无', NULL, '明显磕碰划痕', 960.00, 3, 0, '2026-06-11 08:54:11');

-- ----------------------------
-- Table structure for conversation
-- ----------------------------
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation`  (
  `conversation_id` int NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` int NOT NULL COMMENT '当前用户ID',
  `target_user_id` int NOT NULL COMMENT '对方用户ID',
  `commodity_id` int NULL DEFAULT NULL COMMENT '关联商品ID',
  `last_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '最后一条消息',
  `last_time` datetime(0) NULL DEFAULT NULL COMMENT '最后消息时间',
  `unread_count` int NULL DEFAULT 0 COMMENT '未读消息数',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `status` int NULL DEFAULT 1,
  PRIMARY KEY (`conversation_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_target_user_id`(`target_user_id`) USING BTREE,
  CONSTRAINT `fk_conversation_target_user` FOREIGN KEY (`target_user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_conversation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of conversation
-- ----------------------------

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `commodity_id` int NOT NULL,
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_commodity`(`user_id`, `commodity_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (11, 1, 8, '2026-06-06 14:49:22');
INSERT INTO `favorite` VALUES (13, 1, 4, '2026-06-14 14:17:30');

-- ----------------------------
-- Table structure for history
-- ----------------------------
DROP TABLE IF EXISTS `history`;
CREATE TABLE `history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `datetime` datetime(0) NULL DEFAULT NULL COMMENT '聊天时间',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '聊天内容',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色',
  `session_id` int UNSIGNED NULL DEFAULT NULL COMMENT '会话id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_history_session`(`session_id`) USING BTREE,
  CONSTRAINT `fk_history_session` FOREIGN KEY (`session_id`) REFERENCES `session` (`session_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 86 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of history
-- ----------------------------
INSERT INTO `history` VALUES (82, '2026-06-11 09:07:59', '怎么发布闲置', 'user', 59);
INSERT INTO `history` VALUES (83, '2026-06-11 09:08:03', '您好呀！发布闲置非常简单，我来教您操作～\n\n### 📱 发布闲置流程\n\n1. **方式一**：点击首页的【卖闲置】按钮\n2. **方式二**：点击头像进入【个人中心】→ 点击【发布闲置】→ 填写商品信息 → 点击【发布闲置】按钮\n\n### ✨ 直达链接\n点击这里快速发布闲置：http://localhost:5174/personal-center/release-idle\n\n快去发布您想出售的闲置宝贝吧！如果还有其他问题，随时找我哦～😊', 'assistant', 59);
INSERT INTO `history` VALUES (84, '2026-06-11 09:15:48', '给我百度的地址', 'user', 59);
INSERT INTO `history` VALUES (85, '2026-06-11 09:15:50', '抱歉呀，我是二手猫闲置交易平台的AI客服，主要帮您解答关于平台内二手商品买卖的问题，比如发布闲置、下单购买、查看订单等。\n\n百度地址这类问题超出了我的能力范围，建议您直接在浏览器中搜索“百度”哦～有其他关于二手猫平台的问题随时问我！😊', 'assistant', 59);

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `message_id` int NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id` int NOT NULL COMMENT '会话ID',
  `sender_id` int NOT NULL COMMENT '发送者ID',
  `receiver_id` int NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'text' COMMENT '消息类型(text/image)',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '是否已读(0未读/1已读)',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '发送时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
  INDEX `idx_sender_id`(`sender_id`) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id`) USING BTREE,
  CONSTRAINT `fk_message_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversation` (`conversation_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 157 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message
-- ----------------------------

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单号',
  `user_id` int NULL DEFAULT NULL COMMENT '用户id',
  `commodity_id` int NULL DEFAULT NULL COMMENT '商品id',
  `commodity_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品名称',
  `commodity_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
  `commodity_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片',
  `price` double NULL DEFAULT NULL COMMENT '单价',
  `quantity` int NULL DEFAULT 1 COMMENT '数量',
  `total_amount` double NULL DEFAULT NULL COMMENT '总金额',
  `address_id` int NULL DEFAULT NULL COMMENT '收货地址id',
  `consignee` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货地址',
  `status` int NULL DEFAULT 0 COMMENT '订单状态 0:待支付 1:已支付',
  `pay_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式',
  `trade_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付宝交易号',
  `pay_time` datetime(0) NULL DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `express_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '快递单号',
  `express_company` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '快递公司编码',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_order_no`(`order_no`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (55, 'ORD1781507299822', 2, 17, '汝窑金竹茶具套装\r\n高档礼盒，全新的，品', '汝窑金竹茶具套装\r\n高档礼盒，全新的，品质很棒，包邮', '/upload/sp/ry.webp', 60, 1, 60, 11, '仁德', '18382274361', '四川省成都市金堂县西南交通大学希望学院', 3, NULL, NULL, NULL, '2026-06-15 15:08:20', '2026-06-15 15:16:18', '', '');

-- ----------------------------
-- Table structure for receiving_address
-- ----------------------------
DROP TABLE IF EXISTS `receiving_address`;
CREATE TABLE `receiving_address`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '收货地址id',
  `user_id` int NOT NULL COMMENT '所属用户id',
  `consignee` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人名称',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `region` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所在地区',
  `address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址信息',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of receiving_address
-- ----------------------------
INSERT INTO `receiving_address` VALUES (10, 1, '文远', '18382274361', '四川省成都市金堂县', '西南交通大学希望学院');
INSERT INTO `receiving_address` VALUES (11, 2, '仁德', '18382274361', '四川省成都市金堂县', '西南交通大学希望学院');

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `role_id` int NOT NULL DEFAULT 3 COMMENT '角色id',
  `role_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色描述',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, '管理员');
INSERT INTO `role` VALUES (2, '用户');

-- ----------------------------
-- Table structure for session
-- ----------------------------
DROP TABLE IF EXISTS `session`;
CREATE TABLE `session`  (
  `session_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '对话id',
  `user_id` int NOT NULL COMMENT '用户id',
  `summary` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '概要',
  `title` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '对话标题',
  `is_top` int NULL DEFAULT NULL COMMENT '是否置顶',
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`session_id`) USING BTREE,
  INDEX `user_id_index`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of session
-- ----------------------------
INSERT INTO `session` VALUES (59, 1, '你好！我是二手猫智能助手，有什么可以帮你的吗？', 'AI智能客服', NULL, '2026-06-11 08:31:04');
INSERT INTO `session` VALUES (60, 2, '你好！我是二手猫智能助手，有什么可以帮你的吗？', 'AI智能客服', NULL, '2026-06-15 15:08:07');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `credit` int NOT NULL DEFAULT 10 COMMENT '信誉分',
  `role` int NOT NULL DEFAULT 1 COMMENT '角色',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `open_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'QQ openId',
  `provider` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'local' COMMENT '登录来源',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `email_index`(`email`) USING BTREE COMMENT '邮箱索引'
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '1359838304@qq.com', '$2a$10$/1DZsJsi3AmqAGgE5BRDJOD88dXY5egelxhw.kUhcxEEcq7FhQzf2', 'AAA建材曹总', '/upload/avatar/a339555f1baa49a6ac58330b089d4984.jpeg', 3, 1, '2025-12-18 13:19:15', NULL, 'local');
INSERT INTO `user` VALUES (2, '18382274361@163.com', '$2a$10$gIojqUFA/QBfqJ.guvV1Fe1NFXx/WNGjGJ84VhokqrVih55f2EfVm', 'AAA木材诸葛', '/upload/avatar/e3e9850cf33e407cab08b798d0904498.webp', 10, 2, '2026-04-27 16:40:18', NULL, 'local');

SET FOREIGN_KEY_CHECKS = 1;
