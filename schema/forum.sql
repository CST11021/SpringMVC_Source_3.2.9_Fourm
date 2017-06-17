/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50621
Source Host           : localhost:3306
Source Database       : sampledb17

Target Server Type    : MYSQL
Target Server Version : 50621
File Encoding         : 65001

Date: 2016-12-19 18:29:51
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_board
-- ----------------------------
DROP TABLE IF EXISTS `t_board`;
CREATE TABLE `t_board` (
  `board_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '论坛版块ID',
  `board_name` varchar(150) NOT NULL DEFAULT '' COMMENT '论坛版块名',
  `board_desc` varchar(255) DEFAULT NULL COMMENT '论坛版块描述',
  `topic_num` int(11) NOT NULL DEFAULT '0' COMMENT '帖子数目',
  PRIMARY KEY (`board_id`),
  KEY `AK_Board_NAME` (`board_name`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_board
-- ----------------------------
INSERT INTO `t_board` VALUES ('10', '论坛板块一', '板块一简介。。。', '6');
INSERT INTO `t_board` VALUES ('13', '22222222', '222222222222222222', '7');

-- ----------------------------
-- Table structure for t_board_manager
-- ----------------------------
DROP TABLE IF EXISTS `t_board_manager`;
CREATE TABLE `t_board_manager` (
  `board_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`board_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='论坛管理员';

-- ----------------------------
-- Records of t_board_manager
-- ----------------------------

-- ----------------------------
-- Table structure for t_login_log
-- ----------------------------
DROP TABLE IF EXISTS `t_login_log`;
CREATE TABLE `t_login_log` (
  `login_log_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `ip` varchar(30) NOT NULL DEFAULT '',
  `login_datetime` datetime NOT NULL,
  PRIMARY KEY (`login_log_id`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_login_log
-- ----------------------------
INSERT INTO `t_login_log` VALUES ('1', '4', '127.0.0.1', '2016-04-23 00:00:00');
INSERT INTO `t_login_log` VALUES ('2', '4', '127.0.0.1', '2016-04-30 00:00:00');
INSERT INTO `t_login_log` VALUES ('3', '4', '127.0.0.1', '2016-04-30 00:00:00');
INSERT INTO `t_login_log` VALUES ('4', '4', '127.0.0.1', '2016-04-30 00:00:00');
INSERT INTO `t_login_log` VALUES ('5', '2', '127.0.0.1', '2016-05-02 00:00:00');
INSERT INTO `t_login_log` VALUES ('6', '2', '127.0.0.1', '2016-05-02 00:00:00');
INSERT INTO `t_login_log` VALUES ('7', '1', '127.0.0.1', '2016-05-02 00:00:00');
INSERT INTO `t_login_log` VALUES ('8', '1', '127.0.0.1', '2016-05-02 00:00:00');
INSERT INTO `t_login_log` VALUES ('9', '1', '127.0.0.1', '2016-05-03 00:00:00');
INSERT INTO `t_login_log` VALUES ('10', '1', '127.0.0.1', '2016-05-03 00:00:00');
INSERT INTO `t_login_log` VALUES ('11', '1', '127.0.0.1', '2016-05-03 00:00:00');
INSERT INTO `t_login_log` VALUES ('12', '1', '127.0.0.1', '2016-05-03 00:00:00');
INSERT INTO `t_login_log` VALUES ('13', '1', '127.0.0.1', '2016-05-03 00:00:00');
INSERT INTO `t_login_log` VALUES ('14', '1', '127.0.0.1', '2016-05-03 00:00:00');
INSERT INTO `t_login_log` VALUES ('15', '1', '127.0.0.1', '2016-05-03 00:00:00');
INSERT INTO `t_login_log` VALUES ('16', '1', '127.0.0.1', '2016-05-03 00:00:00');
INSERT INTO `t_login_log` VALUES ('17', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('18', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('19', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('20', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('21', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('22', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('23', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('24', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('25', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('26', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('27', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('28', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('29', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('30', '1', '127.0.0.1', '2016-05-19 00:00:00');
INSERT INTO `t_login_log` VALUES ('31', '1', '127.0.0.1', '2016-05-20 00:00:00');
INSERT INTO `t_login_log` VALUES ('32', '1', '127.0.0.1', '2016-05-20 00:00:00');
INSERT INTO `t_login_log` VALUES ('33', '1', '127.0.0.1', '2016-05-20 00:00:00');
INSERT INTO `t_login_log` VALUES ('34', '1', '127.0.0.1', '2016-05-20 00:00:00');
INSERT INTO `t_login_log` VALUES ('35', '1', '127.0.0.1', '2016-05-20 00:00:00');
INSERT INTO `t_login_log` VALUES ('36', '1', '0:0:0:0:0:0:0:1', '2016-05-20 00:00:00');
INSERT INTO `t_login_log` VALUES ('37', '1', '127.0.0.1', '2016-05-20 00:00:00');
INSERT INTO `t_login_log` VALUES ('38', '1', '127.0.0.1', '2016-05-20 00:00:00');
INSERT INTO `t_login_log` VALUES ('39', '1', '127.0.0.1', '2016-05-20 00:00:00');
INSERT INTO `t_login_log` VALUES ('40', '1', '127.0.0.1', '2016-05-20 00:00:00');
INSERT INTO `t_login_log` VALUES ('41', '1', '127.0.0.1', '2016-05-20 00:00:00');
INSERT INTO `t_login_log` VALUES ('42', '1', '127.0.0.1', '2016-05-20 00:00:00');
INSERT INTO `t_login_log` VALUES ('43', '1', '127.0.0.1', '2016-05-21 00:00:00');
INSERT INTO `t_login_log` VALUES ('44', '1', '127.0.0.1', '2016-05-21 00:00:00');
INSERT INTO `t_login_log` VALUES ('45', '1', '127.0.0.1', '2016-05-21 09:00:03');
INSERT INTO `t_login_log` VALUES ('46', '1', '127.0.0.1', '2016-05-21 10:30:53');
INSERT INTO `t_login_log` VALUES ('47', '1', '127.0.0.1', '2016-05-21 10:31:30');
INSERT INTO `t_login_log` VALUES ('48', '2', '127.0.0.1', '2016-05-21 10:34:25');
INSERT INTO `t_login_log` VALUES ('49', '1', '127.0.0.1', '2016-05-21 12:00:56');
INSERT INTO `t_login_log` VALUES ('50', '1', '127.0.0.1', '2016-05-21 13:41:23');
INSERT INTO `t_login_log` VALUES ('51', '1', '127.0.0.1', '2016-05-22 11:32:07');
INSERT INTO `t_login_log` VALUES ('52', '3', '0:0:0:0:0:0:0:1', '2016-12-07 17:44:51');
INSERT INTO `t_login_log` VALUES ('53', '3', '0:0:0:0:0:0:0:1', '2016-12-07 17:45:13');
INSERT INTO `t_login_log` VALUES ('54', '3', '0:0:0:0:0:0:0:1', '2016-12-07 17:46:34');
INSERT INTO `t_login_log` VALUES ('55', '3', '0:0:0:0:0:0:0:1', '2016-12-07 19:01:57');
INSERT INTO `t_login_log` VALUES ('56', '3', '0:0:0:0:0:0:0:1', '2016-12-07 19:08:52');
INSERT INTO `t_login_log` VALUES ('57', '1', '0:0:0:0:0:0:0:1', '2016-12-07 19:12:08');
INSERT INTO `t_login_log` VALUES ('58', '3', '0:0:0:0:0:0:0:1', '2016-12-09 10:45:31');
INSERT INTO `t_login_log` VALUES ('59', '3', '0:0:0:0:0:0:0:1', '2016-12-09 10:58:32');
INSERT INTO `t_login_log` VALUES ('60', '3', '0:0:0:0:0:0:0:1', '2016-12-09 11:03:34');
INSERT INTO `t_login_log` VALUES ('61', '3', '0:0:0:0:0:0:0:1', '2016-12-09 11:06:31');
INSERT INTO `t_login_log` VALUES ('62', '3', '0:0:0:0:0:0:0:1', '2016-12-09 13:12:24');
INSERT INTO `t_login_log` VALUES ('63', '3', '0:0:0:0:0:0:0:1', '2016-12-09 16:41:54');
INSERT INTO `t_login_log` VALUES ('64', '1', '0:0:0:0:0:0:0:1', '2016-12-09 16:45:49');
INSERT INTO `t_login_log` VALUES ('65', '3', '0:0:0:0:0:0:0:1', '2016-12-09 16:46:27');
INSERT INTO `t_login_log` VALUES ('66', '1', '0:0:0:0:0:0:0:1', '2016-12-09 16:48:23');
INSERT INTO `t_login_log` VALUES ('67', '3', '0:0:0:0:0:0:0:1', '2016-12-12 09:23:35');
INSERT INTO `t_login_log` VALUES ('68', '3', '0:0:0:0:0:0:0:1', '2016-12-12 13:50:01');
INSERT INTO `t_login_log` VALUES ('69', '3', '0:0:0:0:0:0:0:1', '2016-12-19 13:23:26');
INSERT INTO `t_login_log` VALUES ('70', '3', '0:0:0:0:0:0:0:1', '2016-12-19 14:03:56');

-- ----------------------------
-- Table structure for t_post
-- ----------------------------
DROP TABLE IF EXISTS `t_post`;
CREATE TABLE `t_post` (
  `post_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `board_id` int(11) NOT NULL DEFAULT '0' COMMENT '论坛ID',
  `topic_id` int(11) NOT NULL DEFAULT '0' COMMENT '话题ID',
  `user_id` int(11) NOT NULL DEFAULT '0' COMMENT '发表者ID',
  `post_type` tinyint(4) NOT NULL DEFAULT '2' COMMENT '帖子类型 1:主帖子 2:回复帖子',
  `post_title` varchar(50) NOT NULL COMMENT '帖子标题',
  `post_text` text NOT NULL COMMENT '帖子内容',
  `create_time` date NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`post_id`),
  KEY `IDX_POST_TOPIC_ID` (`topic_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8 COMMENT='帖子';

-- ----------------------------
-- Records of t_post
-- ----------------------------
INSERT INTO `t_post` VALUES ('1', '13', '7', '1', '1', '沙发', '撒的发生的的高度认同我个人提问', '2016-05-21');
INSERT INTO `t_post` VALUES ('2', '13', '7', '1', '2', '阿斯蒂芬', '沙发沙发等发达', '2016-05-21');
INSERT INTO `t_post` VALUES ('3', '13', '7', '1', '2', '是的发生', '是的发生', '2016-05-21');
INSERT INTO `t_post` VALUES ('4', '13', '7', '1', '2', '山东法萨芬', '沙发上说的发生', '2016-05-21');
INSERT INTO `t_post` VALUES ('5', '13', '7', '1', '2', '是打发斯蒂芬', '是的发送到发送', '2016-05-21');
INSERT INTO `t_post` VALUES ('6', '13', '7', '1', '2', '是的发送到发送', '是的发送到发送', '2016-05-21');
INSERT INTO `t_post` VALUES ('7', '13', '7', '1', '2', '都是范德萨范德萨', '发打发第三方', '2016-05-21');
INSERT INTO `t_post` VALUES ('8', '13', '7', '1', '2', '盛大发售的', '受到', '2016-05-21');
INSERT INTO `t_post` VALUES ('9', '13', '8', '1', '1', '1', '111111111111111111111111111', '2016-05-21');
INSERT INTO `t_post` VALUES ('10', '13', '9', '1', '1', '2', '2222222222222222222', '2016-05-21');
INSERT INTO `t_post` VALUES ('11', '13', '10', '1', '1', '3', '3333333333333333', '2016-05-21');
INSERT INTO `t_post` VALUES ('12', '13', '11', '1', '1', '4', '4444444444444444444444444', '2016-05-21');
INSERT INTO `t_post` VALUES ('13', '13', '12', '1', '1', '3', '33333333333333333333333', '2016-05-21');
INSERT INTO `t_post` VALUES ('14', '13', '13', '1', '1', '的所发生的', '是的发送到发送到发送到', '2016-05-21');
INSERT INTO `t_post` VALUES ('15', '10', '14', '1', '1', '标题一', '1111111111111111111111111111111111111', '2016-05-21');
INSERT INTO `t_post` VALUES ('16', '10', '15', '1', '1', '标题二', '222222222222222222222222222222', '2016-05-21');
INSERT INTO `t_post` VALUES ('17', '10', '16', '1', '1', '标题三', '333333333333333333333333333', '2016-05-21');
INSERT INTO `t_post` VALUES ('18', '10', '17', '1', '1', '标题四', '4444444444444444444444444444444444', '2016-05-21');
INSERT INTO `t_post` VALUES ('19', '10', '18', '1', '1', '标题五', '555555555555555555555555555555555', '2016-05-21');
INSERT INTO `t_post` VALUES ('20', '10', '19', '1', '1', '标题六', '666666666666666666666666666', '2016-05-21');
INSERT INTO `t_post` VALUES ('21', '10', '19', '1', '2', 'sdfas', 'asdfsadfsadf', '2016-05-21');
INSERT INTO `t_post` VALUES ('22', '10', '16', '1', '2', 'tgt', 'retertwwewerw', '2016-05-21');
INSERT INTO `t_post` VALUES ('23', '10', '19', '1', '2', 'ceshi', '测试沙发沙发是的发送到发送到', '2016-05-21');
INSERT INTO `t_post` VALUES ('24', '10', '19', '2', '2', 'sdfas ', 'sdfsdfsfsdf', '2016-05-21');
INSERT INTO `t_post` VALUES ('25', '10', '19', '2', '2', 'sdf盛大发售的fs', 'sfsdfsdfcxzw3', '2016-05-21');
INSERT INTO `t_post` VALUES ('26', '10', '19', '1', '2', '是打发斯蒂芬', '沙发沙发案说法', '2016-05-21');
INSERT INTO `t_post` VALUES ('27', '10', '19', '3', '2', '', '', '2016-05-23');

-- ----------------------------
-- Table structure for t_sys_parameter
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_parameter`;
CREATE TABLE `t_sys_parameter` (
  `PARAM_NAME` varchar(30) NOT NULL,
  `PARAM_VALUE` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`PARAM_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_sys_parameter
-- ----------------------------
INSERT INTO `t_sys_parameter` VALUES ('TEST1', '测试String');
INSERT INTO `t_sys_parameter` VALUES ('TEST2', 'false');
INSERT INTO `t_sys_parameter` VALUES ('TEST3', '100');

-- ----------------------------
-- Table structure for t_topic
-- ----------------------------
DROP TABLE IF EXISTS `t_topic`;
CREATE TABLE `t_topic` (
  `topic_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `board_id` int(11) NOT NULL COMMENT '所属论坛',
  `topic_title` varchar(100) NOT NULL DEFAULT '' COMMENT '帖子标题',
  `user_id` int(11) NOT NULL DEFAULT '0' COMMENT '发表用户',
  `create_time` date NOT NULL COMMENT '发表时间',
  `last_post` date NOT NULL COMMENT '最后回复时间',
  `topic_views` int(11) NOT NULL DEFAULT '1' COMMENT '浏览数',
  `topic_replies` int(11) NOT NULL DEFAULT '0' COMMENT '回复数',
  `digest` int(11) NOT NULL COMMENT '0:不是精华话题 1:是精华话题',
  PRIMARY KEY (`topic_id`),
  KEY `IDX_TOPIC_USER_ID` (`user_id`),
  KEY `IDX_TOPIC_TITLE` (`topic_title`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8 COMMENT='话题';

-- ----------------------------
-- Records of t_topic
-- ----------------------------
INSERT INTO `t_topic` VALUES ('7', '13', '沙发', '1', '2016-05-21', '2016-05-21', '0', '7', '0');
INSERT INTO `t_topic` VALUES ('8', '13', '1', '1', '2016-05-21', '2016-05-21', '0', '0', '0');
INSERT INTO `t_topic` VALUES ('9', '13', '2', '1', '2016-05-21', '2016-05-21', '0', '0', '0');
INSERT INTO `t_topic` VALUES ('10', '13', '3', '1', '2016-05-21', '2016-05-21', '0', '0', '0');
INSERT INTO `t_topic` VALUES ('11', '13', '4', '1', '2016-05-21', '2016-05-21', '0', '0', '0');
INSERT INTO `t_topic` VALUES ('12', '13', '3', '1', '2016-05-21', '2016-05-21', '0', '0', '0');
INSERT INTO `t_topic` VALUES ('13', '13', '的所发生的', '1', '2016-05-21', '2016-05-21', '0', '0', '0');
INSERT INTO `t_topic` VALUES ('14', '10', '标题一', '1', '2016-05-21', '2016-05-21', '0', '0', '0');
INSERT INTO `t_topic` VALUES ('15', '10', '标题二', '1', '2016-05-21', '2016-05-21', '0', '0', '0');
INSERT INTO `t_topic` VALUES ('16', '10', '标题三', '1', '2016-05-21', '2016-05-21', '0', '1', '0');
INSERT INTO `t_topic` VALUES ('17', '10', '标题四', '1', '2016-05-21', '2016-05-21', '0', '0', '0');
INSERT INTO `t_topic` VALUES ('18', '10', '标题五', '1', '2016-05-21', '2016-05-21', '0', '0', '0');
INSERT INTO `t_topic` VALUES ('19', '10', '标题六', '1', '2016-05-21', '2016-05-23', '0', '6', '0');

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户Id',
  `user_name` varchar(30) NOT NULL COMMENT '用户名',
  `password` varchar(30) NOT NULL DEFAULT '' COMMENT '密码',
  `user_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '1:普通用户 2:管理员',
  `locked` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0:未锁定 1:锁定',
  `credit` int(11) DEFAULT NULL COMMENT '积分',
  `last_visit` datetime DEFAULT NULL COMMENT '最后登陆时间',
  `last_ip` varchar(20) DEFAULT NULL COMMENT '最后登陆IP',
  PRIMARY KEY (`user_id`),
  KEY `AK_AK_USER_USER_NAME` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES ('1', 'admin', '123', '2', '0', '430', '2016-12-09 16:48:23', '0:0:0:0:0:0:0:1');
INSERT INTO `t_user` VALUES ('2', 'w', '123', '1', '0', '145', '2016-05-21 10:34:25', '127.0.0.1');
INSERT INTO `t_user` VALUES ('3', 'a', 'a', '1', '0', '65', '2016-12-19 14:03:56', '0:0:0:0:0:0:0:1');
