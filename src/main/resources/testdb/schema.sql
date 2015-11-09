drop table if exists T_ACCOUNT;

create table T_ACCOUNT (ID bigint AUTO_INCREMENT primary key, NUMBER varchar(9),
                        NAME varchar(50) not null, BALANCE decimal(8,2), unique(NUMBER));
                        
ALTER TABLE T_ACCOUNT ALTER COLUMN BALANCE SET DEFAULT 0.0;
