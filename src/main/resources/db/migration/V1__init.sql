BEGIN TRANSACTION;

CREATE TABLE `denied` (
    `plot_id_x` INT NOT NULL,
    `plot_id_z` INT NOT NULL,
    `denied_username` VARCHAR(16) NOT NULL,
    `reason` VARCHAR(64) DEFAULT NULL,
     PRIMARY KEY (`plot_id_x`,`plot_id_z`,`denied_username`)
);

CREATE TABLE `plots` (
    `id_x` INT NOT NULL,
    `id_z` INT NOT NULL,
    `greeting` VARCHAR(1024) DEFAULT NULL,
    `farewall` VARCHAR(1024) DEFAULT NULL,
    `biome` VARCHAR(32) DEFAULT NULL,
    `music` VARCHAR(32) DEFAULT NULL,
    `team` VARCHAR(32) DEFAULT NULL,
    `owner_username` VARCHAR(32) NOT NULL,
    `gamemode_id` VARCHAR(24) DEFAULT NULL,
    `date_claimed` TIMESTAMP NOT NULL,
    `chat_style_id` VARCHAR(32) DEFAULT NULL,
    PRIMARY KEY (`id_x`,`id_z`)
);

CREATE TABLE `reportreasons` (
    `reason_id` VARCHAR(24) NOT NULL,
    `report_id` BIGINT NOT NULL,
    PRIMARY KEY (`reason_id`, `report_id`)
);

CREATE TABLE `reports` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `plot_id_x` INT NOT NULL,
    `plot_id_z` INT NOT NULL,
    `reporting_user` VARCHAR(16) NOT NULL,
    `admin_username` VARCHAR(16) DEFAULT NULL,
    `date_reported` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `date_moderated` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `is_moderated` BOOLEAN DEFAULT 0,
    PRIMARY KEY (`id`)
);

CREATE TABLE `trusted` (
    `plot_id_x` INT NOT NULL,
    `plot_id_z` INT NOT NULL,
    `trusted_username` VARCHAR(16) NOT NULL,
    `permission_id` VARCHAR(32) NOT NULL,
    PRIMARY KEY (`plot_id_x`,`plot_id_z`,`trusted_username`, `permission_id`)
);

CREATE TABLE `players` (
    `username` VARCHAR(16) NOT NULL,
    `plot_chat_enabled` BOOLEAN DEFAULT 0,
     PRIMARY KEY (`username`)
);

ALTER TABLE `denied` ADD CONSTRAINT `fk_denied_players` FOREIGN KEY (`denied_username`) REFERENCES `players` (`username`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `denied` ADD CONSTRAINT `fk_denied_plots` FOREIGN KEY (`plot_id_x`,`plot_id_z`) REFERENCES `plots` (`id_x`, `id_z`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `plots` ADD CONSTRAINT `fk_plots_players` FOREIGN KEY (`owner_username`) REFERENCES `players` (`username`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `reportreasons` ADD CONSTRAINT `fk_reportreasons_reports` FOREIGN KEY (`report_id`) REFERENCES `reports` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `reports` ADD CONSTRAINT `fk_reported_plot` FOREIGN KEY (`plot_id_x`,`plot_id_z`) REFERENCES `plots` (`id_x`, `id_z`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `reports` ADD CONSTRAINT `fk_reporting_user` FOREIGN KEY (`reporting_user`) REFERENCES `players` (`username`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `trusted` ADD CONSTRAINT `fk_trusted_players` FOREIGN KEY (`trusted_username`) REFERENCES `players` (`username`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `trusted` ADD CONSTRAINT `fk_trusted_plots` FOREIGN KEY (`plot_id_x`,`plot_id_z`) REFERENCES `plots` (`id_x`, `id_z`) ON DELETE CASCADE ON UPDATE CASCADE;

COMMIT;
