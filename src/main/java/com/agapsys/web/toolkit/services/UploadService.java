/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.agapsys.web.toolkit.services;

import com.agapsys.web.toolkit.Service;
import com.agapsys.web.toolkit.utils.FileUtils;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Upload service.
 */
public class UploadService extends Service {
    // CLASS SCOPE =============================================================
    protected static final long DEFAULT_TOTAL_MAX_SIZE = -1; // No limit
    protected static final long DEFAULT_MAX_FILE_SIZE  = -1; // No limit

    private static final String ATTR_SESSION_FILES = "com.agapsys.agrest.sessionFiles";

    public static interface OnFormFieldListener {
        public void onFormField(String name, String value);
    }
    // =========================================================================

    // INSTANCE SCOPE ==========================================================
    /**
     * Returns the directory used to store uploaded files
     * @return the directory used to store uploaded files
     * Default implementation returns {@linkplain FileUtils#DEFAULT_TEMPORARY_FOLDER}
     */
    protected File getOutputDirectory() {
        return FileUtils.DEFAULT_TEMPORARY_FOLDER;
    }

    /**
     * Returns the maximum size of an uploaded file.
     * @return the maximum size of an upload file or -1 if there is no limit.
     * Default implementation returns {@link UploadService#DEFAULT_MAX_FILE_SIZE}
     */
    protected long getMaxFileSize() {
        return DEFAULT_MAX_FILE_SIZE;
    }


    /**
     * Returns the maximum size of upload bundle (when multiple files are uploaded).
     * @return the maximum size of upload bundle (when multiple files are uploaded) or -1 if there is no limit.
     * Default implementation returns {@link UploadService#DEFAULT_TOTAL_MAX_SIZE}
     */
    protected long getTotalMaxSize() {
        return DEFAULT_TOTAL_MAX_SIZE;
    }

    /**
     * Returns a coma-delimited list of accepted content-types
     * @return a coma-delimited list of accepted content-types or '*' for any content-type.
     * Default implementation returns '*'
     */
    protected  String getAllowedContentTypes() {
        return "*";
    }

    /**
     * Returns the encoding used for form fields
     * @return the encoding used for form fields. Default implementation returns "utf-8".
     */
    protected String getFieldEncoding() {
        return "utf-8";
    }

    /**
     * @return a boolean indicating if session file list contains a file with given filename.
     * @param req HTTP request
     * @param filename filename to be searched.
     */
    private boolean sessionContainsFilename(HttpServletRequest req, String filename) {
        for (File file : getSessionFiles(req)) {
            if (file.getName().equals(filename)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Process an uploaded file and returns a modified one
     * @param req HTTP request
     * @param filename the original filename in the client's file system.
     * @param file temporary file
     * @return processed file.
     */
    private File processFile(HttpServletRequest req, String filename, File file) {
        String baseFilename = new File(filename).getName();
        String sessionId = req.getSession().getId();

        String tmpFilename = String.format("%s_%s", sessionId, baseFilename);
        int counter = 1;
        while (sessionContainsFilename(req, tmpFilename)) {
            tmpFilename = String.format("%s_%s_%d", sessionId, baseFilename, counter);
            counter++;
        }

        File newFile = new File(getOutputDirectory(), tmpFilename);
        file.renameTo(newFile);
        getSessionFiles(req).add(newFile);
        return newFile;
    }

    /**
     * Returns a list of files stored in session
     * @param req HTTP request
     * @return list of files stored in session. If there is no files, return an empty list.
     */
    public List<File> getSessionFiles(HttpServletRequest req) {
        List<File> sessionFiles = (List<File>) req.getSession().getAttribute(ATTR_SESSION_FILES);

        if (sessionFiles == null) {
            sessionFiles = new LinkedList<>();
            req.getSession().setAttribute(ATTR_SESSION_FILES, sessionFiles);
        }

        return sessionFiles;
    }

    /**
     * Removes all files stored in session.
     * @param req HTTP request
     */
    public void clearSessionFile(HttpServletRequest req) {
        List<File> sessionFiles = getSessionFiles(req);

        while(!sessionFiles.isEmpty()) {
            File sessionFile = sessionFiles.get(0);
            sessionFiles.remove(0);
            if (!sessionFile.delete()) throw new RuntimeException("Failiure removing session file: " + sessionFile.getAbsolutePath());
        }
    }

    /**
     * Process a request to receive files
     * @param req HTTP request
     * @param resp HTTP response
     * @param onFormFieldListener listener called when a form field is received
     * @throws IllegalArgumentException if given request if not multipart/form-data
     */
    public void receiveFiles(HttpServletRequest req, HttpServletResponse resp, OnFormFieldListener onFormFieldListener) throws IllegalArgumentException {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(0); // All files will be written to disk
        factory.setRepository(getOutputDirectory());

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(getTotalMaxSize());
        upload.setFileSizeMax(getMaxFileSize());

        if (!ServletFileUpload.isMultipartContent(req)) throw new IllegalArgumentException("Request is not multipart/form-data");

        try {
            List<FileItem> fileItems = upload.parseRequest(req);
            for (FileItem fi : fileItems) {
                if (fi.isFormField()) {
                    if (onFormFieldListener != null)
                        onFormFieldListener.onFormField(fi.getFieldName(), fi.getString(getFieldEncoding()));
                } else {
                    boolean acceptRequest = getAllowedContentTypes().equals("*");

                    if (!acceptRequest) {
                        String[] acceptedContentTypes = getAllowedContentTypes().split(Pattern.quote("."));
                        for (String acceptedContentType : acceptedContentTypes) {
                            if (fi.getContentType().equals(acceptedContentType.trim())) {
                                acceptRequest = true;
                                break;
                            }
                        }
                    }

                    if (!acceptRequest) throw new IllegalArgumentException("Unsupported content-type: " + fi.getContentType());

                    File tmpFile = ((DiskFileItem)fi).getStoreLocation();
                    processFile(req, fi.getName(), tmpFile);
                }
            }

        } catch(FileUploadException ex) {
            if (ex instanceof FileUploadBase.SizeLimitExceededException)
                throw new IllegalArgumentException("Size limit exceeded");
            else
                throw new RuntimeException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Process a request to receive files.
     * This is a convenience method for receiveFiles(req, resp, null).
     * @param req HTTP request
     * @param resp HTTP response
     * @throws IllegalArgumentException if given request if not multipart/form-data
     */
    public void receiveFiles(HttpServletRequest req, HttpServletResponse resp) throws IllegalArgumentException {
        receiveFiles(req, resp, null);
    }
}