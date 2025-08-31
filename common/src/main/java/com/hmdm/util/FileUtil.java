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

import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.Customer;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * <p>An utility class for managing the files on local file system.</p>
 *
 * @author isv
 */
public final class FileUtil {
    private static final String TEMP_FILE_DELIMITER = "1111111";

    /**
     * <p>Constructs new <code>FileUtil</code> instance. This implementation does nothing.</p>
     */
    private FileUtil() {
    }

    public static String adjustFileName(String fileName) {
        return fileName
                .replace(' ', '_')
                .replace('+', '_')          // Not valid in URL
                .replace('%', '_')          // Not valid in URL
                .replace("(", "")           // These characters are used by Windows when a file is downloaded twice
                .replace(")", "");
    }

    public static File createTempFile(String fileName) throws IOException {
        return File.createTempFile(fileName + TEMP_FILE_DELIMITER, ".temp");
    }

    public static void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
        try (FileOutputStream out = new FileOutputStream(new File(uploadedFileLocation))) {
            byte[] bytes = new byte[1024];

            int read;
            while((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getNameFromTmpPath(String tmpFilePath) {
        File localFile = new File(tmpFilePath);
        if (!localFile.getName().contains(TEMP_FILE_DELIMITER)) {
            throw new RuntimeException("Temp file should contain the delimiter: " + tmpFilePath);
        }
        return localFile.getName().split(TEMP_FILE_DELIMITER)[0];
    }

    /**
     * <p>Moves the specified uploaded file to desired location related to specified customer account.
     * The target file name is determined from the tmp file name</p>
     *
     * @param customer a customer account which the file belongs to.
     * @param filesDirectory a directory which holds all the files maintained by the application.
     * @param localPath an optional local path to move file to.
     * @param tmpFilePath a path to a temorary file to be moved.
     * @return a file referencing the moved file if operation was successful; <code>null</code> otherwise.
     * @throws FileExistsException if file already exists in
     */
    public static File moveFile(Customer customer, String filesDirectory, String localPath, String tmpFilePath) {
        return moveFile(customer, filesDirectory, localPath, tmpFilePath, null);
    }

    /**
     * <p>Moves the specified uploaded file to desired location related to specified customer account.
     * The target file name is explicitly specified</p>
     *
     * @param customer a customer account which the file belongs to.
     * @param filesDirectory a directory which holds all the files maintained by the application.
     * @param localPath an optional local path to move file to.
     * @param tmpFilePath a path to a temorary file to be moved.
     * @return a file referencing the moved file if operation was successful; <code>null</code> otherwise.
     * @throws FileExistsException if file already exists in
     */
    public static File moveFile(Customer customer, String filesDirectory, String localPath, String tmpFilePath, String newName) {
        File localFile = new File(tmpFilePath);
        String fileName = newName != null ? newName : getNameFromTmpPath(tmpFilePath);
        while (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }

        String filePath;
        if (localPath == null || localPath.isEmpty()) {
            filePath = String.format("%s/%s/%s", filesDirectory, customer.getFilesDir(), fileName);
        } else {
            filePath = String.format("%s/%s/%s/%s", filesDirectory, customer.getFilesDir(), localPath, fileName);
        }
        File file = new File(filePath.replace("/", File.separator));
        file.getParentFile().mkdirs();

        if (file.exists()) {
            throw new FileExistsException(customer, file.getName());
        }

        final boolean success = localFile.renameTo(file);
        if (success) {
            return file;
        } else {
            // Try to copy and delete because rename can fail due to different file systems
            // For example, on Tomcat 9 renaming from /tmp to /var/lib/tomcat9/work will fail due to sandbox restrictions
            try {
                FileInputStream inputStream = new FileInputStream(localFile);
                writeToFile(inputStream, file.getAbsolutePath());
                inputStream.close();
                localFile.delete();
                return file;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    public static String translateURLToLocalFilePath(Customer customer, String url, String baseUrl) {
        final String prefixWithoutCustomer = baseUrl + "/files/";
        String prefix = prefixWithoutCustomer;
        if (customer.getFilesDir() != null && !customer.getFilesDir().isEmpty()) {
            prefix += customer.getFilesDir() + "/";
        }
        if (url.startsWith(prefix)) {
            final String path = url.substring(prefix.length());
            return path.replace("/", File.separator);
        }

        return null;
    }

    /**
     * <p>Deletes the specified file on local file system.</p>
     *
     * @param baseDirectory a path to a base directory where all application files are stored..
     * @param path a path to a file to delete.
     * @return <code>true</code> if file was deleted successfully; <code>false</code> otherwise.
     */
    public static boolean deleteFile(Customer customer, String baseDirectory, String path) {
        String filePath = String.format("%s/%s", baseDirectory, customer.getFilesDir()).replace("/", File.separator);
        final File fileToDelete = new File(filePath, path);
        return fileToDelete.delete();
    }

    public static String createFileUrl(String baseUrl, String customerDir, String fileName) {
        // TODO: Use files.directory from XML config!
        String url = baseUrl + "/files/";
        if (customerDir != null && !customerDir.equals("")) {
            url += customerDir + "/";
        }
        url += fileName;
        return url;
    }

    public static String downloadTextFile(URL url) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        StringBuffer stringBuffer = new StringBuffer();
        byte[] buffer = new byte[1024];
        int count=0;
        while((count = bis.read(buffer,0,1024)) != -1) {
            stringBuffer.append(new String(buffer, StandardCharsets.UTF_8));
        }
        bis.close();
        return stringBuffer.toString();
    }

    public static void downloadFile(URL url, String directory, String name) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        File file = new File(directory, name);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int count=0;
        while((count = bis.read(buffer,0,1024)) != -1) {
            fos.write(buffer, 0, count);
        }
        bis.close();
        fos.close();
    }

    public static boolean isSafePath(String path) {
        return path == null || !path.contains("..");
    }
}
