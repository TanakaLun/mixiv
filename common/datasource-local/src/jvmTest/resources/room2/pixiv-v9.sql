-- Frozen pre-upgrade Room 2 schema for PixivDatabase version 9.
-- Source: createAllTables() in the existing Room 2 generated PixivDatabase_Impl.kt,
-- captured before switching this project to Room 3. The exact Room 2 compiler
-- patch version is not known. This fixture must not be regenerated using Room 3.
-- SHA-256 of the captured generated Kotlin file: 41d7379afbf828e3676e0824bd0b654c4b4b04296039b4dfdd0e81b544040f0c
-- The 11 entity tables, 3 indexes and room_master_table statements below are
-- copied verbatim from that implementation, including its original identity hash.
-- PRAGMA user_version reproduces the database version from its RoomOpenDelegate.

CREATE TABLE IF NOT EXISTS `download` (`illustId` INTEGER NOT NULL, `index` INTEGER NOT NULL, `title` TEXT NOT NULL, `userId` INTEGER NOT NULL, `userName` TEXT NOT NULL, `thumbnailUrl` TEXT NOT NULL, `originalUrl` TEXT NOT NULL, `subFolder` TEXT, `status` INTEGER NOT NULL, `progress` REAL NOT NULL, `filePath` TEXT NOT NULL, `fileUri` TEXT NOT NULL, `createTime` INTEGER NOT NULL, PRIMARY KEY(`illustId`, `index`));
CREATE TABLE IF NOT EXISTS `novel_reading_progress` (`novelId` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `paragraphIndex` INTEGER NOT NULL, `charIndex` INTEGER NOT NULL, `paragraphHash` INTEGER NOT NULL, `updatedAtMillis` INTEGER NOT NULL, PRIMARY KEY(`novelId`, `userId`));
CREATE TABLE IF NOT EXISTS `novel_translation` (`novelId` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `targetLanguage` TEXT NOT NULL, `provider` TEXT NOT NULL, `model` TEXT NOT NULL, `configFingerprint` TEXT NOT NULL, `sourceMd5` TEXT NOT NULL, `translatedText` TEXT NOT NULL, `updatedAtMillis` INTEGER NOT NULL, `translatedTitle` TEXT NOT NULL DEFAULT '', `translatedCaption` TEXT NOT NULL DEFAULT '', `metadataSourceMd5` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`novelId`, `userId`, `targetLanguage`));
CREATE TABLE IF NOT EXISTS `novel_read_later` (`novelId` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `targetLanguage` TEXT NOT NULL, `novelTitle` TEXT NOT NULL, `novelCaption` TEXT NOT NULL, `novelAuthorName` TEXT NOT NULL, `coverUrl` TEXT NOT NULL, `novelTagsJson` TEXT NOT NULL, `addedAtMillis` INTEGER NOT NULL, `provider` TEXT NOT NULL, `model` TEXT NOT NULL, `endpoint` TEXT NOT NULL, `responseApi` INTEGER NOT NULL, `extraBody` TEXT NOT NULL, `configFingerprint` TEXT NOT NULL, `sourceMd5` TEXT NOT NULL, `state` TEXT NOT NULL, `attemptToken` TEXT NOT NULL, `retryCount` INTEGER NOT NULL, `lastError` TEXT, `updatedAtMillis` INTEGER NOT NULL, PRIMARY KEY(`novelId`, `userId`, `targetLanguage`));
CREATE INDEX IF NOT EXISTS `index_novel_read_later_userId_state_addedAtMillis` ON `novel_read_later` (`userId`, `state`, `addedAtMillis`);
CREATE TABLE IF NOT EXISTS `block_illust` (`illustId` INTEGER NOT NULL, `title` TEXT NOT NULL, PRIMARY KEY(`illustId`));
CREATE TABLE IF NOT EXISTS `block_novel` (`novelId` INTEGER NOT NULL, `title` TEXT NOT NULL, PRIMARY KEY(`novelId`));
CREATE TABLE IF NOT EXISTS `block_tag` (`tag` TEXT NOT NULL, `isRegex` INTEGER NOT NULL, PRIMARY KEY(`tag`));
CREATE TABLE IF NOT EXISTS `block_comment` (`commentId` INTEGER NOT NULL, `commentJson` TEXT NOT NULL, PRIMARY KEY(`commentId`));
CREATE TABLE IF NOT EXISTS `block_user` (`userId` INTEGER NOT NULL, `name` TEXT NOT NULL, PRIMARY KEY(`userId`));
CREATE TABLE IF NOT EXISTS `browsing_history_illust` (`illustId` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `viewedAtMillis` INTEGER NOT NULL, `illustJson` TEXT NOT NULL, PRIMARY KEY(`illustId`, `userId`));
CREATE INDEX IF NOT EXISTS `index_browsing_history_illust_userId_viewedAtMillis` ON `browsing_history_illust` (`userId`, `viewedAtMillis`);
CREATE TABLE IF NOT EXISTS `browsing_history_novel` (`novelId` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `viewedAtMillis` INTEGER NOT NULL, `novelJson` TEXT NOT NULL, PRIMARY KEY(`novelId`, `userId`));
CREATE INDEX IF NOT EXISTS `index_browsing_history_novel_userId_viewedAtMillis` ON `browsing_history_novel` (`userId`, `viewedAtMillis`);
CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT);
INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '115367479a96f680e52d2ee7d6dc6421');
PRAGMA user_version = 9;
