# Headwind MDM - a platform for corporate Android applications

Headwind MDM is a Mobile Device Management platform for Android devices, designed for corporate app developers and IT managers.

(c) 2020 h-mdm.com [https://h-mdm.com](https://h-mdm.com)

## Features

 - Enrollment to Android 7+ devices through scanning a QR-code
 - Work in "Application mode" without enrollment
 - Customize the mobile desktop design and available applications
 - Automatic deployment of applications through the web panel
 - Mobile device management: groups, configurations, device status
 - Setup the available mobile device capabilities (GPS, Wi-Fi, Bluetooth etc.)
 - Manage the automatic OS update mode on the mobile device
 - Extensible platform design allowing the custom plugin development
 - Collection of application logs in the web panel
 - Centralized configuration of corporate applications

The *Enterprise edition* of the platform has more features:

 - Restriction of mobile user functions ("kid's shell" for corporate users)
 - Disable to change the mobile device settings
 - Kiosk mode (COSU, single-task mode)
 - Sending images from mobile device to server
 - Cloud-based or self-hosted server setup
 - Premium support of enterprise users
 - Custom plugin development services

The enterprise edition may be ordered on the [project website](https://h-mdm.com).

## Quick start

Headwind MDM control panel is cross-platform (it is written in Java and uses Tomcat web server). However the best OS for the deployment of Headwind MDM control panel is Ubuntu Linux.

 - Clone the project and build it (see [BUILD](#build) section for details)
 - Install the web panel to the server by using the installer script
 - Open the web panel and follow the hints to generate a QR code
 - Perform the factory reset on your Android device, tap 7 times on the welcome screen
 - Follow the instructions to scan a QR code and enroll the mobile agent

## Build

This instruction has been tested on Ubuntu Linux 22.04 LTS/24.04 LTS.

IMPORTANT: This project requires Java 21+ and Tomcat 10.1+ (Jakarta EE 9+).

1. Install required software

    For Ubuntu Linux 24.04 LTS (recommended):
    ```bash
    sudo apt install git aapt tomcat10 maven postgresql openjdk-21-jdk
    ```

    Set Java 21 as default:
    ```bash
    sudo update-alternatives --config java
    ```
    (select the Java 21 option)

2. Verify Java version

    ```bash
    java -version
    ```
    (should show version 21 or higher)

    ```bash
    mvn -version
    ```
    (should show Java 21 under "Java version")

3. Make sure Tomcat 10.1+ is running

    ```bash
    curl localhost:8080
    ```
    (if you get "Failed to connect" error, fix the installation issue)

4. Clone the repository

    ```bash
    git clone https://github.com/h-mdm/hmdm-server
    cd hmdm-server
    ```

5. If you are planning to run or debug Headwind MDM in IDE, create the properties
file from the sample

    ```bash
    cp server/build.properties.example server/build.properties
    ```

    and update the contents of the server/build.properties file.

6. Build the source code

    ```bash
    mvn install
    ```

7. Follow [INSTALL INSTRUCTIONS FOR THE WEB PANEL](#install-instructions-for-the-web-panel) below

## INSTALL INSTRUCTIONS FOR THE WEB PANEL

1. Make sure Tomcat is running

    ```bash
    telnet localhost 8080
    Trying ::1...
    Connected to localhost.
    Escape character is '^]'.
    ```

    (if you get "Connection refused" error, fix the installation issue)

2. Create the PostgreSQL database and user

    ```bash
    sudo su postgres
    psql
    postgres=# CREATE USER hmdm WITH PASSWORD 'topsecret';
    postgres=# CREATE DATABASE hmdm WITH OWNER=hmdm;
    postgres=# \q
    ```

3. Run the installer script from the project directory (as root)

    ```bash
    sudo ./hmdm_install.sh
    ```

4. On success, the installer script provides you with the URL. Open Headwind MDM in browser.

### HTTPS configuration

The installer offers certificate provisioning through Certbot. Its deploy hook
publishes certificate files under `/etc/hmdm/tls/current` with permissions for
Tomcat and the embedded MQTT broker.

Automatic Tomcat configuration is **opt-in**: it replaces the entire `server.xml`
with [install/server_template.xml](install/server_template.xml), which declares the
HTTPS connector on port 8443 and Tomcat's built-in TLS certificate reload listener.
The template uses the listener's default settings. Certificate loading and HTTPS
reloads are handled by Tomcat, not by a shell certificate parser.

Use automatic replacement only for a dedicated, uncustomized Tomcat installation.
Custom connectors, virtual hosts and realms are not preserved. Otherwise, decline
replacement and merge the template's HTTPS connector into the appropriate
`<Service>` and its TLS reload listener directly under `<Server>`, then restart
Tomcat and verify HTTPS. Declining replacement does not complete HTTPS setup.

The installer saves the previous configuration as `server.xml~`, stages and
syntax-checks the template, preserves file ownership and permissions, and replaces
the live file before requesting a restart. A failed restart stops installation;
there is no automatic rollback. See [TLS recovery](#tls-recovery) below.

### TLS recovery

Run recovery from the project directory, keeping the original domain and scripts
directory. Both modes skip database setup and WAR deployment:

```bash
# Resume configuration using the certificate already published under current.
sudo env HMDM_SKIP_ISSUANCE=1 ./hmdm_install.sh

# Alternatively, retry publication from the existing Certbot lineage.
sudo env HMDM_REPUBLISH_ONLY=1 ./hmdm_install.sh
```

Choose one mode according to the failure; do not run both commands routinely.
The renewal script's status 2 means publication succeeded but its recovery restart
failed. Check Tomcat's logs before resuming with skip-issuance mode. Status 3 means
publication failed; resolve the deploy-hook error and use republish mode.

Other failures, including interruptions, do not establish whether publication
finished. Inspect the preceding errors, `/etc/hmdm/tls/current`, and the lineage
recorded in `/etc/hmdm/tls/hook.env` before choosing recovery. If a certificate is
already available, do not request another just to retry configuration.

#### Failed Tomcat restart

Automatic setup leaves the new `server.xml` in place on restart failure and exits
non-zero. Tomcat may be unavailable. It does not restore the backup or attempt
another restart. Inspect the service logs (replace `tomcat10` if needed):

```bash
sudo journalctl -u tomcat10 -n 50
```

Correct the configuration, or manually restore `server.xml~` beside `server.xml`
and restart the service. The backup is not guaranteed to be valid or HTTP-only.
Restoring configuration does not change the published certificate. Save any backup
you want to retain before another automatic setup, which overwrites `server.xml~`.
When resuming with skip-issuance mode, decline template replacement if you want to
retain your manual configuration changes.

Template and installer-flow checks can be run without issuing certificates:

```bash
bash install/tests/server-template-test.sh
bash install/tests/tls-config-flow-test.sh
```

These require `xmllint`. The flow test mocks service and ownership operations;
passing it does not verify live Tomcat or MQTT certificate reloads.

## REST push API

The server exposes a private endpoint for sending push messages to devices:

```text
POST /rest/private/push
```

The request must be authenticated as an admin user with the `push_api` permission. Use the same session cookie or token authentication mechanism used by your deployment (the `Authorization: Bearer ...` header is accepted). If `ui.allowed.address` is configured in your deployment, the caller's IP must also be on that whitelist, since `/rest/private/*` is IP-filtered.

Example: notify one device that its configuration has changed:

```bash
curl -X POST "https://your-domain.com/rest/private/push" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "messageType": "configUpdated",
    "deviceNumbers": ["DEVICE_NUMBER"]
  }'
```

This does not carry an APK in the push message. It tells the mobile client to sync `/rest/public/sync/configuration/{deviceId}` and apply the applications returned by the configuration response.

The endpoint can also send custom message types. For example, if your Android client implements an `installApp` handler, you can send an APK URL like this:

```bash
curl -X POST "https://your-domain.com/rest/private/push" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "messageType": "installApp",
    "payload": "{\"url\":\"https://example.com/app.apk\",\"pkg\":\"com.example.app\"}",
    "deviceNumbers": ["DEVICE_NUMBER"]
  }'
```

`installApp` is not a built-in APK installation command in the stock client. The mobile client must implement this message type, download the APK, validate it, and perform the install. Silent installation still requires the client to have the required Android device-owner, profile-owner, system, or OEM install privileges.

The request body accepts five fields: `messageType`, `payload`, `deviceNumbers`, `groups` and `broadcast`. Other targeting options:

```json
{
  "messageType": "configUpdated",
  "groups": ["GROUP_NAME"]
}
```

```json
{
  "messageType": "configUpdated",
  "broadcast": true
}
```

Note that this is not the same endpoint the Push plugin's web panel uses. The plugin has its own
`POST /rest/plugins/push/private/send` (permission `plugin_push_send`), which targets devices by
`scope`/`deviceNumber`/`groupId`/`configurationId` and records the message in the plugin's history.
The endpoint documented above is the server-level API and is the one intended for external callers.

## Maintenance: plugin data retention

Some plugins store time-series data that grows over time. Retention is handled differently per plugin:

- **Device Log** and **Device Info** purge old records **automatically**, once a day, per tenant. Each plugin has a storage-period setting (Device Log's "Log storage time (days)", Device Info's "Storage time (days)"), and each customer's records are purged against that customer's own setting.
- **Push** and **Messaging** have **no automatic purge**. They expose a manual endpoint that deletes records older than a given number of days. There is no built-in scheduler, so if you use these plugins, call the endpoint periodically (e.g. from `cron`) to keep the tables from growing unbounded:

  | Plugin | Endpoint | Deletes | Permission |
  |---|---|---|---|
  | Push | `GET /rest/plugins/push/private/purge/{days}` | push messages older than `{days}` days | `plugin_push_delete` |
  | Messaging | `GET /rest/plugins/messaging/private/purge/{days}` | messages older than `{days}` days | `plugin_messaging_delete` |

  Both endpoints require an authenticated admin session or token holding the listed permission (the same login the web panel uses).

  Like the automatic purge above, these two endpoints are scoped to the customer of the calling admin: they only delete records belonging to that customer. On a multi-tenant install you must therefore run the job once per tenant, with the credentials of an admin of that tenant, to keep the tables bounded.

  **Warning:** `{days}` must be **>= 1**. A value of `0` or less is treated as "no cutoff" and **deletes every record of the calling admin's customer**.

  Example — purge push messages older than 30 days, daily via cron. Use a JWT bearer token rather than a saved session cookie: the plugin's private paths go through the JWT filter, and a token does not expire mid-job the way a panel session does.

  ```bash
  # crontab -e
  17 3 * * *  curl -fsS -H "Authorization: Bearer $TOKEN" \
    "https://your-domain.com/rest/plugins/push/private/purge/30" >/dev/null
  ```

## Troubleshooting

Problem: QR code isn't opening
Reason: The project URL is not accessible from the local machine.
Solution:
Make the project URL accessible from the local machine.
If you're using iptables to redirect port 80 to 8080, redirect also for loopback interface:

```bash
iptables -A OUTPUT -o lo -p tcp -m tcp --dport 80 -j REDIRECT --to-ports 8080
```

## Contributing

Headwind MDM is a platform making corporate app development easier. We are happy to get more powerful plugins related to mobile device management.

Please contact us on the [project website](https://h-mdm.com) if you'd like to:

 - develop a public plugin for Headwind MDM
 - suggest a feature
 - order the custom development
 - report a bug
