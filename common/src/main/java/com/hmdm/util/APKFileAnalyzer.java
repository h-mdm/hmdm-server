/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.inject.Named;
import com.hmdm.persistence.ApplicationDAO;
import com.hmdm.persistence.domain.Application;
import com.hmdm.rest.json.APKFileDetails;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * <p>An analyzer for uploaded APK-files.</p>
 *
 * @author isv
 */
@Singleton
public class APKFileAnalyzer {

    /**
     * <p>A logger for the encountered events.</p>
     */
    private static final Logger log = LoggerFactory.getLogger(ApplicationDAO.class);

    /**
     * <p>A command line string to call the <code>aapt</code> command.</p>
     */
    private final String aaptCommand;

    /**
     * <p>Constructs new <code>APKFileAnalyzer</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public APKFileAnalyzer(@Named("aapt.command") String aaptCommand) {
        this.aaptCommand = aaptCommand;
    }

    /**
     * <p>Analyzes the specified file (APK or XAPK).</p>
     *
     * @param filePath an absolute path to an file to be analyzed.
     * @throws APKFileAnalyzerException if an unexpected error occurs or external <code>aapt</code> command reported an
     *         error.
     */
    public APKFileDetails analyzeFile(String filePath) {
        String realFileName = filePath.endsWith(".temp") ? FileUtil.getNameFromTmpPath(filePath) : filePath;
        if (realFileName.endsWith(".xapk")) {
            return analyzeXapkFile(filePath);
        } else {
            return analyzeApkFile(filePath);
        }
    }

    /**
     * <p>Analyzes the specified APK file.</p>
     *
     * @param filePath an absolute path to an APK-file to be analyzed.
     * @throws APKFileAnalyzerException if an unexpected error occurs or external <code>aapt</code> command reported an
     *         error.
     */
    private APKFileDetails analyzeApkFile(String filePath) {
        try {
            final String[] commands = {this.aaptCommand, "dump", "badging", filePath};
            log.debug("Executing shell-commands: {}", Arrays.toString(commands));
            final Process exec = Runtime.getRuntime().exec(commands);

            final AtomicReference<String> appPkg = new AtomicReference<>();
            final AtomicReference<String> appVersion = new AtomicReference<>();
            final AtomicReference<Integer> appVersionCode = new AtomicReference<>();
            final AtomicReference<String> appArch = new AtomicReference<>();
            final List<String> errorLines = new ArrayList<>();

            // Process the error stream by collecting all the error lines for further logging
            StreamGobbler errorGobbler = new StreamGobbler(exec.getErrorStream(), "ERROR", errorLines::add);

            // Process the output by analyzing the line starting with "package:"
            StreamGobbler outputGobbler = new StreamGobbler(exec.getInputStream(), "APK-file DUMP", line -> {
                if (line.startsWith("package:")) {
                    parseInfoLine(line, appPkg, appVersion, appVersionCode);
                    if (appPkg.get() == null || appVersion.get() == null) {
                        // AAPT output is wrong!
                        log.warn("Failed to parse aapt output: '" + line + "', trying legacy parser");
                        parseInfoLineLegacy(line, appPkg, appVersion, appVersionCode);
                    }
                } else if (line.startsWith("native-code:")) {
                    // This is an optional line, so do not care for errors
                    parseNativeCodeLine(line, appArch);
                }
            });

            // Get ready to consume input and error streams from the process
            errorGobbler.start();
            outputGobbler.start();

            final int exitCode = exec.waitFor();

            outputGobbler.join();
            errorGobbler.join();

            if (exitCode == 0) {
                log.debug("Parsed application name and version from APK-file {}: {} {} {}",
                        filePath, appPkg, appVersion, appVersionCode);
                APKFileDetails result = new APKFileDetails();

                result.setPkg(appPkg.get());
                result.setVersion(appVersion.get());
                Integer versionCode = appVersionCode.get();
                result.setVersionCode(versionCode != null ? versionCode : 0);
                result.setArch(appArch.get());

                return result;
            } else {
                log.error("Could not analyze the .apk-file {}. The system process returned: {}. " +
                        "The error message follows:", filePath, exitCode);
                errorLines.forEach(log::error);
                throw new APKFileAnalyzerException("Could not analyze the .apk-file");
            }
        } catch (IOException | InterruptedException e) {
            log.error("Unexpected error while analyzing APK-file: {}", filePath, e);
            throw new APKFileAnalyzerException("Unexpected error while analyzing APK-file", e);
        }
    }

    // This function deals with an issue when the version name contains a space or even an apostrophe
    // It presumes the following format of the line:
    // package: name='xxxxx' versionCode='xxxxx' versionName='xxxxx' compileSdkVersion='xxx' compileSdkVersionCodename='xxx'
    private void parseInfoLine(final String line,
                               final AtomicReference<String> appPkg,
                               final AtomicReference<String> appVersion,
                               final AtomicReference<Integer> appVersionCode) {
        String l = line;
        final String namePrefix = "package: name='";
        final String versionCodePrefix = "' versionCode='";
        final String versionNamePrefix = "' versionName='";
        final String sdkVersionPrefix = "' compileSdkVersion='";
        final String platformBuildPrefix = "' platformBuildVersionName='";
        int pos;

        pos = l.indexOf(namePrefix);
        if (pos == -1) {
            return;
        }
        l = l.substring(pos + namePrefix.length());

        pos = l.indexOf(versionCodePrefix);
        if (pos == -1) {
            return;
        }
        appPkg.set(l.substring(0, pos));
        l = l.substring(pos + versionCodePrefix.length());

        pos = l.indexOf(versionNamePrefix);
        if (pos == -1) {
            return;
        }
        // Here we get the version code
        String versionCode = l.substring(0, pos);
        try {
            appVersionCode.set(Integer.parseInt(versionCode));
        } catch (NumberFormatException e) {
        }
        l = l.substring(pos + versionNamePrefix.length());

        // Different versions of aapt may give different output
        pos = l.indexOf(platformBuildPrefix);
        if (pos == -1) {
            pos = l.indexOf(sdkVersionPrefix);
        }
        if (pos == -1) {
            return;
        }
        appVersion.set(l.substring(0, pos));
    }

    private void parseInfoLineLegacy(final String line,
                                     final AtomicReference<String> appPkg,
                                     final AtomicReference<String> appVersion,
                                     final AtomicReference<Integer> appVersionCode) {
        Scanner scanner = new Scanner(line).useDelimiter(" ");
        while (scanner.hasNext()) {
            final String token = scanner.next();
            if (token.startsWith("name=")) {
                String appPkgLocal = token.substring("name=".length());
                if (appPkgLocal.startsWith("'") && appPkgLocal.endsWith("'")) {
                    appPkgLocal = appPkgLocal.substring(1, appPkgLocal.length() - 1);
                }
                appPkg.set(appPkgLocal);
            } else if (token.startsWith("versionCode=")) {
                String appVersionCodeLocal = token.substring("versionCode=".length());
                if (appVersionCodeLocal.startsWith("'") && appVersionCodeLocal.endsWith("'")) {
                    appVersionCodeLocal = appVersionCodeLocal.substring(1, appVersionCodeLocal.length() - 1);
                }
                try {
                    appVersionCode.set(Integer.parseInt(appVersionCodeLocal));
                } catch (NumberFormatException e) {
                }
            } else if (token.startsWith("versionName=")) {
                String appVersionLocal = token.substring("versionName=".length());
                if (appVersionLocal.startsWith("'") && appVersionLocal.endsWith("'")) {
                    appVersionLocal = appVersionLocal.substring(1, appVersionLocal.length() - 1);
                }
                appVersion.set(appVersionLocal);
            }
        }
    }

    // Parse a line containing the native code CPU architecture
    // It presumes the following format of the line:
    // native-code: 'xxxxx'
    private void parseNativeCodeLine(final String line, final AtomicReference<String> appArch) {
        if (line.indexOf("armeabi-v7a") != -1) {
            appArch.set(Application.ARCH_ARMEABI);
            if (line.indexOf("arm64-v8a") != -1) {
                // Native code for both architectures present, so it is a universal file
                appArch.set(null);
            }
        } else if (line.indexOf("arm64-v8a") != -1) {
            appArch.set(Application.ARCH_ARM64);
        }
    }


    /**
     * <p>A consumer for the stream contents. Outputs the line read from the stream and passes it to provided line
     * consumer.</p>
     */
    private class StreamGobbler extends Thread {
        private final InputStream is;
        private final String type;
        private final Consumer<String> lineConsumer;

        private StreamGobbler(InputStream is, String type, Consumer<String> lineConsumer) {
            this.is = is;
            this.type = type;
            this.lineConsumer = lineConsumer;
        }

        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    log.debug(type + "> " + line);
                    this.lineConsumer.accept(line);
                }
            } catch (Exception e) {
                log.error("An error in {} stream handler for external process {}", this.type, aaptCommand, e);
            }
        }
    }

    /**
     * <p>Analyzes the specified XAPK file.</p>
     *
     * @param filePath an absolute path to an XAPK-file to be analyzed.
     * @throws APKFileAnalyzerException if an unexpected error occurs
     */
    private APKFileDetails analyzeXapkFile(String filePath) {
        try {
            ZipFile zipFile = new ZipFile(filePath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while(entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().equalsIgnoreCase("manifest.json")) {
                    InputStream stream = zipFile.getInputStream(entry);
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();
                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null) {
                        responseStrBuilder.append(inputStr);
                    }
                    stream.close();
                    zipFile.close();
                    return analyzeXapkManifest(responseStrBuilder.toString());
                }

            }
            zipFile.close();
            throw new APKFileAnalyzerException("Missing manifest in XAPK-file", new Exception());

        } catch (Exception e) {
            log.error("Unexpected error while analyzing XAPK-file: {}", filePath, e);
            throw new APKFileAnalyzerException("Unexpected error while analyzing XAPK-file", e);
        }
    }

    private APKFileDetails analyzeXapkManifest(String manifest) {
        JSONObject jsonObject = new JSONObject(manifest);
        APKFileDetails fileDetails = new APKFileDetails();
        fileDetails.setPkg(jsonObject.getString("package_name"));
        fileDetails.setVersion(jsonObject.getString("version_name"));
        fileDetails.setVersionCode(jsonObject.optInt("version_code"));

        // XAPK manifest doesn't contain data about the native code
        // So we try to guess it by searching the keywords
        boolean hasArm64 = manifest.contains("arm64");
        boolean hasArmeabi = manifest.contains("armeabi");
        if (hasArm64 && !hasArmeabi) {
            fileDetails.setArch(Application.ARCH_ARM64);
        } else if (hasArmeabi && !hasArm64) {
            fileDetails.setArch(Application.ARCH_ARMEABI);
        }

        return fileDetails;
    }
}
