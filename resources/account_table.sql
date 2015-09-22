USE `atm`;

delimiter $$

CREATE TABLE `account` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT 'User account is associated with',
  `name` varchar(45) NOT NULL COMMENT 'account name',
  `balance` double NOT NULL DEFAULT '0' COMMENT 'balance of the account',
  `last_update` datetime NOT NULL COMMENT 'Timestamp of last update. Provided by the caller',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
