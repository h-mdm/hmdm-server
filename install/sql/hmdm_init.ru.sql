UPDATE users SET email='info@h-mdm.com' WHERE id=1;

INSERT INTO settings (id, backgroundcolor, textcolor, backgroundimageurl, iconsize, desktopheader, customerid, usedefaultlanguage, language) VALUES (1, '#1c40e3', '#fcfcfc', NULL, 'SMALL', 'NO_HEADER', 1, true, NULL);

INSERT INTO userrolesettings (id, roleid, customerid, columndisplayeddevicestatus, columndisplayeddevicedate, columndisplayeddevicenumber, columndisplayeddevicemodel, columndisplayeddevicepermissionsstatus, columndisplayeddeviceappinstallstatus, columndisplayeddeviceconfiguration, columndisplayeddeviceimei, columndisplayeddevicephone, columndisplayeddevicedesc, columndisplayeddevicegroup, columndisplayedlauncherversion) VALUES 
(1, 1, 1, true, true, true, NULL, true, true, true, NULL, NULL, NULL, NULL, NULL),
(2, 2, 1, true, true, true, NULL, true, true, true, NULL, NULL, NULL, NULL, NULL),
(3, 3, 1, true, true, true, NULL, true, true, true, NULL, NULL, NULL, NULL, NULL),
(4, 100, 1, true, true, true, NULL, true, true, true, NULL, NULL, NULL, NULL, NULL);

SELECT pg_catalog.setval('public.settings_id_seq', 1, true);

ALTER TABLE applications DROP CONSTRAINT applications_latestversion_fkey;

INSERT INTO applications (id, pkg, name, showicon, customerid, system, latestversion) VALUES 
    (1, 'com.android.systemui', 'Системный интерфейс', false, 1, true, 10000),
    (2, 'com.android.bluetooth', 'Сервис Bluetooth', false, 1, true, 10001),
    (3, 'com.google.android.gms', 'Сервисы Google', false, 1, true, 10002),
    (34, 'com.android.email', 'Email клиент', true, 1, true, 10033),
    (7, 'org.kman.WifiManager', 'WiFi Менеджер', true, 1, false, 10006),
    (9, 'com.android.chrome', 'Браузер Chrome', true, 1, true, 10008),
    (10, 'com.sec.android.app.browser', 'Браузер (Samsung)', true, 1, true, 10009),
    (11, 'com.samsung.android.video', 'Samsung Video', false, 1, true, 10010),
    (12, 'com.android.providers.media', 'Media Service', false, 1, true, 10011),
    (13, 'com.android.gallery3d', 'Галерея', true, 1, true, 10012),
    (14, 'com.sec.android.gallery3d', 'Галерея (Samsung)', true, 1, true, 10013),
    (15, 'com.android.vending', 'Поддержка Google Payment', false, 1, true, 10014),
    (16, 'com.samsung.android.app.memo', 'Заметки (Samsung)', true, 1, true, 10015),
    (35, 'com.android.documentsui', 'Менеджер файлов - расширение', false, 1, true, 10034),
    (5, 'com.google.android.packageinstaller', 'Установщик пакетов (Google)', false, 1, true, 10004),
    (17, 'com.android.packageinstaller', 'Установщик пакетов', false, 1, true, 10016),
    (18, 'com.samsung.android.calendar', 'Календарь (Samsung)', true, 1, true, 10017),
    (19, 'com.android.calculator2', 'Калькулятор (generic)', true, 1, true, 10018),
    (20, 'com.sec.android.app.popupcalculator', 'Калькулятор (Samsung)', true, 1, true, 10019),
    (21, 'com.android.camera', 'Камера (generic)', true, 1, true, 10020),
    (22, 'com.huawei.camera', 'Камера (Huawei)', true, 1, true, 10021),
    (23, 'org.codeaurora.snapcam', 'Камера (Lenovo)', true, 1, true, 10022),
    (24, 'com.mediatek.camera', 'Камера (Mediatek)', true, 1, true, 10023),
    (25, 'com.sec.android.app.camera', 'Камера (Samsung, legacy)', true, 1, true, 10024),
    (26, 'com.sec.android.camera', 'Камера (Samsung)', true, 1, true, 10025),
    (27, 'com.google.android.apps.maps', 'Карты Google', true, 1, true, 10026),
    (28, 'com.touchtype.swiftkey', 'Расширение клавиатуры Swiftkey', false, 1, true, 10027),
    (29, 'com.android.contacts', 'Контакты', true, 1, true, 10028),
    (31, 'com.sec.android.app.myfiles', 'Менеджер файлов (Samsung)', true, 1, true, 10030),
    (32, 'com.android.settings', 'Настройки (запретите их!)', false, 1, true, 10031),
    (33, 'com.sec.android.inputmethod', 'Настройки клавиатуры (Samsung)', false, 1, true, 10032),
    (36, 'com.samsung.android.email.provider', 'Провайдер почты (Samsung)', false, 1, true, 10035),
    (37, 'android', 'Системный пакет Android', false, 1, true, 10036),
    (38, 'com.android.mms', 'Сообщения (generic)', true, 1, true, 10037),
    (39, 'com.google.android.apps.messaging', 'Сообщения (Google)', true, 1, true, 10038),
    (40, 'com.android.dialer', 'Телефон (generic UI)', true, 1, true, 10039),
    (41, 'com.sec.phone', 'Телефон (Samsung)', true, 1, true, 10040),
    (42, 'com.android.phone', 'Телефон (generic service)', true, 1, true, 10041),
    (43, 'com.huaqin.filemanager', 'Менеджер файлов (Lenovo)', true, 1, true, 10042),
    (6, 'com.google.android.apps.photos', 'Галерея (Google)', true, 1, true, 10005),
    (4, 'com.google.android.apps.docs', 'Google Drive', true, 1, true, 10003),
    (30, 'com.huawei.android.launcher', 'Штатная оболочка (Huawei)', false, 1, true, 10029),
    (8, 'com.android.browser', 'Браузер (generic)', true, 1, true, 10007),
    (46, 'com.hmdm.launcher', 'Headwind MDM', false, 1, false, 10045),
    (47, 'com.huawei.android.internal.app', 'Huawei - выбор лаунчера', false, 1, true, 10046);


SELECT pg_catalog.setval('public.applications_id_seq', 47, true);

INSERT INTO applicationversions (id, applicationid, version, url) VALUES 
    (10000, 1, '0', NULL),
    (10001, 2, '0', NULL),
    (10002, 3, '0', NULL),
    (10003, 4, '0', NULL),
    (10004, 5, '0', NULL),
    (10005, 6, '0', NULL),
    (10006, 7, '4.2.7-220', 'https://h-mdm.com/files/wifi-manager-4-2-7-220.apk'),
    (10007, 8, '0', NULL),
    (10008, 9, '0', NULL),
    (10009, 10, '0', NULL),
    (10010, 11, '0', NULL),
    (10011, 12, '0', NULL),
    (10012, 13, '0', NULL),
    (10013, 14, '0', NULL),
    (10014, 15, '0', NULL),
    (10015, 16, '0', NULL),
    (10016, 17, '0', NULL),
    (10017, 18, '0', NULL),
    (10018, 19, '0', NULL),
    (10019, 20, '0', NULL),
    (10020, 21, '0', NULL),
    (10021, 22, '0', NULL),
    (10022, 23, '0', NULL),
    (10023, 24, '0', NULL),
    (10024, 25, '0', NULL),
    (10025, 26, '0', NULL),
    (10026, 27, '0', NULL),
    (10027, 28, '0', NULL),
    (10028, 29, '0', NULL),
    (10029, 30, '0', NULL),
    (10030, 31, '0', NULL),
    (10031, 32, '0', NULL),
    (10032, 33, '0', NULL),
    (10033, 34, '0', NULL),
    (10034, 35, '0', NULL),
    (10035, 36, '0', NULL),
    (10036, 37, '0', NULL),
    (10037, 38, '0', NULL),
    (10038, 39, '0', NULL),
    (10039, 40, '0', NULL),
    (10040, 41, '0', NULL),
    (10041, 42, '0', NULL),
    (10042, 43, '0', NULL),
    (10045, 46, '_HMDM_VERSION_', 'https://h-mdm.com/files/_HMDM_APK_'),
    (10046, 47, '0', NULL);

SELECT pg_catalog.setval('public.applicationversions_id_seq', 10046, true);

ALTER TABLE applications ADD CONSTRAINT applications_latestversion_fkey FOREIGN KEY (latestversion) REFERENCES applicationversions(id) ON DELETE SET NULL;

DELETE FROM configurations;
INSERT INTO configurations (id, name, description, type, password, backgroundcolor, textcolor, backgroundimageurl, iconsize, desktopheader, usedefaultdesignsettings, customerid, gps, bluetooth, wifi, mobiledata, mainappid, eventreceivingcomponent, kioskmode, qrcodekey, contentappid,autoupdate, blockstatusbar, systemupdatetype, systemupdatefrom, systemupdateto) VALUES (1, 'По умолчанию', 'Подходит для большинства устройств; минимальный набор приложений на экране', 0, '12345678', '', '', NULL, 'SMALL', 'NO_HEADER', true, 1, NULL, NULL, NULL, NULL, 10045, 'com.hmdm.launcher.AdminReceiver', false, '6fb9c8dc81483173a0c0e9f8b2e46be1', NULL, false, false, 0, NULL, NULL);

SELECT pg_catalog.setval('public.configurations_id_seq', 1, true);

INSERT INTO configurationapplications (id, configurationid, applicationid, remove, showicon, applicationversionid) VALUES 
    (1, 1, 7, false, false, 10006),
    (2, 1, 8, false, true, 10007),
    (3, 1, 37, false, false, 10036),
    (4, 1, 2, false, false, 10001),
    (5, 1, 10, false, true, 10009),
    (6, 1, 19, false, false, 10018),
    (7, 1, 20, false, false, 10019),
    (8, 1, 18, false, false, 10017),
    (9, 1, 21, false, true, 10020),
    (10, 1, 22, false, true, 10021),
    (11, 1, 23, false, true, 10022),
    (12, 1, 24, false, true, 10023),
    (13, 1, 26, false, true, 10025),
    (14, 1, 25, false, true, 10024),
    (15, 1, 9, false, true, 10008),
    (16, 1, 29, false, true, 10028),
    (17, 1, 30, false, false, 10029),
    (18, 1, 34, false, true, 10033),
    (19, 1, 36, false, false, 10035),
    (20, 1, 35, false, false, 10034),
    (21, 1, 43, false, false, 10042),
    (22, 1, 31, false, false, 10030),
    (23, 1, 13, false, false, 10012),
    (24, 1, 6, false, false, 10005),
    (25, 1, 14, false, false, 10013),
    (26, 1, 4, false, false, 10003),
    (27, 1, 27, false, false, 10026),
    (28, 1, 15, false, false, 10014),
    (29, 1, 3, false, false, 10002),
    (30, 1, 33, false, false, 10032),
    (31, 1, 12, false, false, 10011),
    (32, 1, 38, false, true, 10037),
    (33, 1, 39, false, true, 10038),
    (34, 1, 16, false, false, 10015),
    (35, 1, 5, false, false, 10004),
    (36, 1, 17, false, false, 10016),
    (37, 1, 42, false, true, 10041),
    (38, 1, 40, false, true, 10039),
    (39, 1, 41, false, true, 10040),
    (40, 1, 11, false, false, 10010),
    (41, 1, 28, false, false, 10027),
    (42, 1, 1, false, false, 10000),
    (43, 1, 46, false, false, 10045),
    (44, 1, 47, false, false, 10046);
    
SELECT pg_catalog.setval('public.configurationapplications_id_seq', 44, true);

INSERT INTO devices (id, number, description, lastupdate, configurationid, oldconfigurationid, info, imei, phone, customerid) VALUES (1, 'h0001', 'Мое первое Android-устройство', 0, 1, NULL, NULL, NULL, NULL, 1);

SELECT pg_catalog.setval('public.devices_id_seq', 1, true);
