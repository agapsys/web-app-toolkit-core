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
    
    // <editor-fold desc="STATIC SCOPE" defaultstate="collapsed">
    public static final long DEFAULT_TOTAL_MAX_SIZE    = -1;        // No limit
    public static final long DEFAULT_MAX_FILE_SIZE     = -1;        // No limit

    protected static final String ATTR_SESSION_FILES = "com.agapsys.agrest.sessionFiles";

    public static interface OnFormFieldListener {
        public void onFormField(String name, String value);
    }
    
    public static class ReceivedFile {
        public final File tmpFile;
        public final String filename;
        
        private ReceivedFile(File tmpFile, String filename) {
            this.tmpFile = tmpFile;
            this.filename = filename;
        }
    }
    // </editor-fold>
    
    private ServletFileUpload uploadServlet;
    
    private synchronized void __int() {
        if (uploadServlet == null) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setSizeThreshold(0); // <--All files will be written to disk
            factory.setRepository(getTemporaryDirectory());

            uploadServlet = new ServletFileUpload(factory);
            uploadServlet.setSizeMax(getTotalMaxSize());
            uploadServlet.setFileSizeMax(getMaxFileSize());
        }
    }
    
    /**
     * Returns the directory used to store uploaded files.
     * 
     * @return the directory used to store uploaded files.
     * Default implementation returns {@linkplain FileUtils#DEFAULT_TEMPORARY_FOLDER}.
     */
    protected File getTemporaryDirectory() {
        return FileUtils.DEFAULT_TEMPORARY_FOLDER;
    }

    /**
     * Returns the maximum size of an uploaded file.
     * 
     * @return the maximum size of an upload file or -1 if there is no limit.
     * Default implementation returns {@link UploadService#DEFAULT_MAX_FILE_SIZE}
     */
    protected long getMaxFileSize() {
        return DEFAULT_MAX_FILE_SIZE;
    }

    /**
     * Returns the maximum size of upload bundle (when multiple files are uploaded).
     * 
     * @return the maximum size of upload bundle (when multiple files are uploaded) or -1 if there is no limit.
     * Default implementation returns {@link UploadService#DEFAULT_TOTAL_MAX_SIZE}
     */
    protected long getTotalMaxSize() {
        return DEFAULT_TOTAL_MAX_SIZE;
    }

    /**
     * Returns a coma-delimited list of accepted content-types.
     * 
     * @return a coma-delimited list of accepted content-types or '*' for any content-type.
     * Default implementation returns '*'
     */
    protected  String getAllowedContentTypes() {
        return "*";
    }

    /**
     * Returns the encoding used for form fields.
     * 
     * @return the encoding used for form fields. Default implementation returns "utf-8".
     */
    protected String getFieldEncoding() {
        return "utf-8";
    }

    
    /**
     * Instructs the service how to retrieve a list of received files associated with given request.
     * 
     * Default implementation uses container session to store this information.
     * @param req HTTP request.
     * @param resp HTTP response.
     * @return list of files stored in session. If there is no files, return an empty list.
     */
    public List<ReceivedFile> getSessionFiles(HttpServletRequest req, HttpServletResponse resp) {
        __int();

        List<ReceivedFile> sessionFiles = (List<ReceivedFile>) req.getSession().getAttribute(ATTR_SESSION_FILES);

        if (sessionFiles == null) {
            sessionFiles = new LinkedList<>();
            req.getSession().setAttribute(ATTR_SESSION_FILES, sessionFiles);
        }

        return sessionFiles;
    }
    
    /**
     * Instructs the service how to keep session file information.
     * 
     * Default implementation uses container session mechanism to persist information.
     * 
     * @param req HTTP request.
     * @param resp HTTP response.
     * @param receivedFiles received file list to be persisted.
     */
    public void persistSessionFiles(HttpServletRequest req, HttpServletResponse resp, List<ReceivedFile> receivedFiles) {
        __int();
        
        req.getSession().setAttribute(ATTR_SESSION_FILES, receivedFiles);
    }

    /**
     * Instructs the server how to remove all received files associated with given request.
     * @param req HTTP request.
     * @param resp HTTP response.
     */
    public void clearSessionFiles(HttpServletRequest req, HttpServletResponse resp) {
        __int();
        
        List<ReceivedFile> sessionFiles = getSessionFiles(req, resp);

        while(!sessionFiles.isEmpty()) {
            File sessionFile = sessionFiles.get(0).tmpFile;
            
            if (!sessionFile.delete()) 
                throw new RuntimeException("Failiure removing session file: " + sessionFile.getAbsolutePath());
            
            sessionFiles.remove(0);

        }
    }
    
    /**
     * Process a request to receive files.
     * 
     * @param req HTTP request.
     * @param resp HTTP response.
     * @param persistReceivedFiles indicates if received files should be persisted.
     * @param onFormFieldListener listener called when a form field is received.
     * @throws IllegalArgumentException if given request if not multipart/form-data.
     * @return a list of received file by given request.
     */
    public List<ReceivedFile> receiveFiles(HttpServletRequest req, HttpServletResponse resp, boolean persistReceivedFiles, OnFormFieldListener onFormFieldListener) throws IllegalArgumentException {
        __int();
        
        if (persistReceivedFiles && resp == null)
            throw new IllegalArgumentException("In order to persist information, response cannot be null");
        
        if (!ServletFileUpload.isMultipartContent(req)) 
            throw new IllegalArgumentException("Request is not multipart/form-data");

        try {
            List<ReceivedFile> recvFiles = new LinkedList<>();
            
            List<FileItem> fileItems = uploadServlet.parseRequest(req);
            
            for (FileItem fi : fileItems) {
                if (fi.isFormField()) {
                    if (onFormFieldListener != null)
                        onFormFieldListener.onFormField(fi.getFieldName(), fi.getString(getFieldEncoding()));
                } else {
                    boolean acceptRequest = getAllowedContentTypes().equals("*");

                    if (!acceptRequest) {
                        String[] acceptedContentTypes = getAllowedContentTypes().split(Pattern.quote(","));
                        for (String acceptedContentType : acceptedContentTypes) {
                            if (fi.getContentType().equals(acceptedContentType.trim())) {
                                acceptRequest = true;
                                break;
                            }
                        }
                    }

                    if (!acceptRequest)
                        throw new IllegalArgumentException("Unsupported content-type: " + fi.getContentType());

                    File tmpFile = ((DiskFileItem)fi).getStoreLocation();
                    String filename = fi.getName();
                    ReceivedFile recvFile = new ReceivedFile(tmpFile, filename);
                    recvFiles.add(recvFile);
                }
            }
            
            if (persistReceivedFiles) {
                List<ReceivedFile> sessionRecvFiles = getSessionFiles(req, resp);
                sessionRecvFiles.addAll(recvFiles);
                persistSessionFiles(req, resp, sessionRecvFiles);
            }
            
            return recvFiles;

        } catch(FileUploadException ex) {
            if (ex instanceof FileUploadBase.SizeLimitExceededException)
                throw new IllegalArgumentException("Size limit exceeded");
            else
                throw new RuntimeException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /** This is a convenience method for receiveFiles(req, resp, persistReceivedFiles, null). */
    public final List<ReceivedFile> receiveFiles(HttpServletRequest req, HttpServletResponse resp, boolean persistReceivedFiles) throws IllegalArgumentException {
        return receiveFiles(req, resp, persistReceivedFiles, null);
    }
    
    /** This is a convenience method for receiveFiles(req, resp, true, null). */
    public final List<ReceivedFile> receiveFiles(HttpServletRequest req, HttpServletResponse resp) {
        return receiveFiles(req, resp, true, null);
    }
    
    /** This is a convenience method for receiveFiles(req, null, false). */
    public final List<ReceivedFile> receiveFiles(HttpServletRequest req) {
        return receiveFiles(req, null, false);
    }
    
    /** This is a convenience method for return receiveFiles(req). */
    public final List<ReceivedFile> receiveFiles(HttpServletRequest req, OnFormFieldListener listener) {
        return receiveFiles(req);
    }

}