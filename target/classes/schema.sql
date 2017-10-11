CREATE DATABASE IF NOT EXISTS parser;

CREATE TABLE parser.access_log ( 
`id` int(10) NOT NULL AUTO_INCREMENT,
`access_date` DATETIME NOT NULL,
`ip` VARCHAR(15) NOT NULL,
`request` VARCHAR(2050) NOT NULL,
`status` int(10) not null,
`user_agent` VARCHAR(512) NOT NULL,
 PRIMARY KEY (`id`));