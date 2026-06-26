-- 本地 MySQL 初始化脚本
-- 用途：为直播借样项目创建数据库、用户和授权
-- 执行前请使用有权限的账号登录 MySQL（如 root）

CREATE DATABASE IF NOT EXISTS live_borrow_sample
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'ycf'@'localhost' IDENTIFIED BY 'ycf012!';
CREATE USER IF NOT EXISTS 'ycf'@'127.0.0.1' IDENTIFIED BY 'ycf012!';
CREATE USER IF NOT EXISTS 'ycf'@'%' IDENTIFIED BY 'ycf012!';

GRANT ALL PRIVILEGES ON live_borrow_sample.* TO 'ycf'@'localhost';
GRANT ALL PRIVILEGES ON live_borrow_sample.* TO 'ycf'@'127.0.0.1';
GRANT ALL PRIVILEGES ON live_borrow_sample.* TO 'ycf'@'%';

FLUSH PRIVILEGES;
