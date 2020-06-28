/*
 * Copyright 2018 Preferred.AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.preferred.venom.storage;

import ai.preferred.venom.fetcher.Callback;
import ai.preferred.venom.request.Request;
import ai.preferred.venom.response.Response;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.tika.mime.MimeTypeException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This class implements a FileManager that writes response content to a
 * file on the file system and a record in MySQL database pointing to the
 * record and allows retrieving the file using an id or request.
 *
 * @author Maksim Tkachenko
 * @author Truong Quoc Tuan
 * @author Ween Jiann Lee
 */
public class MysqlFileManager implements FileManager<Integer> {

  /**
   * Logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(MysqlFileManager.class);

  /**
   * Default content type of response if not given.
   */
  private static final ContentType DEFAULT_CONTENT_TYPE = ContentType.APPLICATION_OCTET_STREAM;

  /**
   * The DataSource to use for connecting to database.
   */
  private final DataSource dataSource;

  /**
   * The name of the table in the database to use for record storage.
   */
  private final String table;

  /**
   * The storage path on the file system to use for content storage.
   */
  private final File storagePath;

  /**
   * The callback to trigger upon response.
   */
  private final Callback callback;

  /**
   * Constructs an instance of MysqlFileManager.
   *
   * @param url        a JDBC URL to the database
   * @param table      table in the database to use for record storage
   * @param username   username for the database
   * @param password   password for the database
   * @param storageDir storage directory to use for content storage
   */
  public MysqlFileManager(final String url, final String table, final String username, final String password,
                          final String storageDir) {
    this(url, table, username, password, new File(storageDir));
  }

  /**
   * Constructs an instance of MysqlFileManager.
   *
   * @param url         a JDBC URL to the database
   * @param table       name of table in the database to use for record storage
   * @param username    username for the database
   * @param password    password for the database
   * @param storagePath storage path to use for content storage
   */
  public MysqlFileManager(final String url, final String table, final String username, final String password,
                          final File storagePath) {
    this(url, table, username, password, storagePath, 10);
  }

  /**
   * Constructs an instance of MysqlFileManager.
   *
   * @param url         a JDBC URL to the database
   * @param table       name of table in the database to use for record storage
   * @param username    username for the database
   * @param password    password for the database
   * @param storagePath storage path to use for content storage
   * @param maxPoolSize maximum connection pool size
   */
  public MysqlFileManager(final String url, final String table, final String username, final String password,
                          final File storagePath, final int maxPoolSize) {
    this.dataSource = setupDataSource(url, username, password, maxPoolSize);
    ensureTable(table);
    this.table = table;
    this.storagePath = storagePath;
    this.callback = new CompletedThreadedCallback(this);
  }

  /**
   * Creates a Hikari DataSource.
   *
   * @param url      a JDBC URL to the database
   * @param username username for the database
   * @param password password for the database
   * @return an instance of DataSource
   */
  private DataSource setupDataSource(final String url, final String username, final String password,
                                     final int maxPoolSize) {
    final HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(url);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    dataSource.setAutoCommit(false);
    dataSource.setMaximumPoolSize(maxPoolSize);
    return dataSource;
  }

  /**
   * Check if table exists, if not create it.
   *
   * @param table name of table in the database to use for record storage
   */
  private void ensureTable(final String table) {
    try (Connection conn = dataSource.getConnection();
         Statement statement = conn.createStatement()) {
      final String sql = "CREATE TABLE IF NOT EXISTS `" + table + "` ("
          + "`id` int(11) NOT NULL AUTO_INCREMENT,\n"
          + "`url` varchar(1024) NOT NULL,\n"
          + "`method` ENUM('GET', 'POST', 'HEAD', 'PUT', 'DELETE', 'OPTIONS') NOT NULL,\n"
          + "`request_headers` JSON DEFAULT NULL,\n"
          + "`request_body` JSON DEFAULT NULL,\n"
          + "`status_code` int(3) NOT NULL DEFAULT 200,\n"
          + "`response_headers` JSON DEFAULT NULL,\n"
          + "`mime_type` varchar(255) NOT NULL,\n"
          + "`encoding` varchar(255) NULL DEFAULT NULL,\n"
          + "`md5` varchar(32) NOT NULL,\n"
          + "`location` varchar(3) NOT NULL,\n"
          + "`date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,\n"
          + "PRIMARY KEY (`id`),\n"
          + "INDEX `url_idx` (`url` ASC)\n"
          + ") CHARACTER SET latin1 COLLATE latin1_swedish_ci;";
      statement.execute(sql);
      conn.commit();
    } catch (SQLException e) {
      LOGGER.error("Unable to execute ensure table query", e);
    }
  }

  /**
   * Write stream to file.
   *
   * @param in         an instance of InputStream
   * @param recordDir  the directory to save the file
   * @param recordName the filename to write the file
   * @throws IOException if an I/O error occurs
   */
  private void createFile(final InputStream in, final File recordDir, final String recordName) throws IOException {
    if (!recordDir.exists() && !recordDir.mkdirs()) {
      throw new IOException("Cannot create the record dir: " + recordDir);
    }
    if (recordDir.exists() && !recordDir.isDirectory()) {
      throw new IOException("The record path is not a dir: " + recordDir);
    }
    final File recordFile = new File(recordDir, recordName);
    try (BufferedOutputStream out = new BufferedOutputStream(
        new GZIPOutputStream(new FileOutputStream(recordFile)))) {
      IOUtils.copy(in, out);
    }
  }

  /**
   * Convert request headers from JSON to map.
   *
   * @param json JSONObject of headers
   * @return aa map of headers
   */
  private Map<String, String> parseRequestHeaders(final JSONObject json) {
    final HashMap<String, String> map = new HashMap<>();
    Iterator<?> i = json.keys();
    while (i.hasNext()) {
      String key = (String) i.next();
      map.put(key, json.getString(key));
    }

    return map;
  }

  /**
   * Convert request headers from JSON to header array.
   *
   * @param json JSONObject of headers
   * @return an array of headers
   */
  private Header[] parseResponseHeaders(final JSONObject json) {
    final List<Header> headers = new ArrayList<>();
    Iterator<?> i = json.keys();
    while (i.hasNext()) {
      String key = (String) i.next();
      headers.add(new BasicHeader(key, json.getString(key)));
    }

    Header[] headersArray = new Header[headers.size()];
    return headers.toArray(headersArray);
  }

  /**
   * Convert request body into map.
   *
   * @param request the instance of request with body
   * @return a map of request body
   */
  private Map<String, String> prepareRequestBody(final Request request) {
    Map<String, String> requestBody = new HashMap<>();
    if (request.getBody() != null) {
      for (String pair : request.getBody().split("&")) {
        String[] nvp = pair.split("=");
        String value = nvp.length > 1 ? nvp[1] : "";
        requestBody.put(nvp[0], value);
      }
    }

    return requestBody;
  }

  /**
   * Get content type from record, if not found return default.
   *
   * @param mimeType name of mime type
   * @param encoding name of encoding
   * @return an instance of content type
   */
  private ContentType getContentType(final String mimeType, final String encoding) {
    final Charset charset;
    if (encoding != null) {
      charset = Charset.forName(encoding);
    } else {
      charset = null;
    }
    try {
      return ContentType.create(mimeType, charset);
    } catch (ParseException e) {
      LOGGER.warn("Could not parse content type", e);
    } catch (UnsupportedCharsetException e) {
      LOGGER.warn("Charset is not available in this instance of the Java virtual machine", e);
    }
    return DEFAULT_CONTENT_TYPE;
  }

  /**
   * Create an instance of Record using the result from database.
   *
   * @param rs an instance of result set from database
   * @return an instance of Record
   * @throws SQLException     if the columnLabel is not valid;
   *                          if a database access error occurs or this method is
   *                          called on a closed result set
   * @throws StorageException if file is not found
   */
  private StorageRecord<Integer> createRecord(final ResultSet rs) throws SQLException, StorageException {
    final Map<String, String> requestHeaders = parseRequestHeaders(new JSONObject(rs.getString("request_headers")));
    final Header[] responseHeaders = parseResponseHeaders(new JSONObject(rs.getString("response_headers")));
    final String location = rs.getString("location");
    String tryFileExtension;
    try {
      tryFileExtension = StorageUtil.getFileExtension(rs.getString("mime_type"));
    } catch (MimeTypeException e) {
      LOGGER.warn("Cannot find mime type defaulting to no extension");
      tryFileExtension = "";
    }
    final String fileExtension = tryFileExtension;
    final File file = new File(new File(storagePath, location), rs.getString("id") + fileExtension + ".gz");

    final ContentType contentType = getContentType(
        rs.getString("mime_type"), rs.getString("encoding"));

    final byte[] responseContent;
    try {
      responseContent = IOUtils.toByteArray(
          new BufferedInputStream(
              new GZIPInputStream(
                  new FileInputStream(file)
              )
          )
      );
    } catch (FileNotFoundException e) {
      throw new StorageException("Record found but file not found for " + rs.getString("url") + ".", e);
    } catch (IOException e) {
      throw new StorageException("Error reading file for " + rs.getString("url") + ".", e);
    }

    LOGGER.debug("Record found for request: {}", rs.getString("url"));

    return StorageRecord.builder(rs.getInt("id"))
        .setUrl(rs.getString("url"))
        .setRequestMethod(Request.Method.valueOf(rs.getString("method")))
        .setRequestHeaders(requestHeaders)
        .setResponseHeaders(responseHeaders)
        .setContentType(contentType)
        .setMD5(rs.getString("md5"))
        .setDateCreated(rs.getLong("date_created"))
        .setResponseContent(responseContent)
        .build();
  }

  @Override
  public final Callback getCallback() {
    return callback;
  }

  @Override
  public final String put(final Request request, final Response response) throws StorageException {
    Connection conn = null;
    try {
      conn = dataSource.getConnection();

      final ByteArrayInputStream content = new ByteArrayInputStream(response.getContent());
      final String md5 = DigestUtils.md5Hex(content);
      content.reset();

      final Map<String, String> responseHeaders = new HashMap<>();

      for (final Header header : response.getHeaders()) {
        responseHeaders.put(header.getName(), header.getValue());
      }

      final Map<String, String> requestBody = prepareRequestBody(request);

      final PreparedStatement pstmt = conn.prepareStatement(
          "INSERT INTO `" + table + "` (url, method, request_headers, request_body, "
              + "status_code, response_headers, mime_type, encoding, md5, location) "
              + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
          Statement.RETURN_GENERATED_KEYS
      );
      final String subDirName = md5.substring(0, 3);
      pstmt.setString(1, request.getUrl());
      pstmt.setString(2, request.getMethod().name());
      pstmt.setString(3, new JSONObject(request.getHeaders()).toString());
      pstmt.setString(4, new JSONObject(requestBody).toString());
      pstmt.setInt(5, response.getStatusCode());
      pstmt.setString(6, new JSONObject(responseHeaders).toString());
      pstmt.setString(7, response.getContentType().getMimeType());
      if (response.getContentType().getCharset() != null) {
        pstmt.setString(8, response.getContentType().getCharset().name());
      } else {
        pstmt.setString(8, null);
      }
      pstmt.setString(9, md5);
      pstmt.setString(10, subDirName);
      LOGGER.debug("Executing for: {}", request.getUrl());

      if (pstmt.executeUpdate() == 1) {
        final ResultSet rs = pstmt.getGeneratedKeys();
        if (rs.next()) {
          LOGGER.debug("MySQL insert successfully for: {}", request.getUrl());
          final int id = rs.getInt(1);
          final String sId = String.valueOf(id);

          String tryFileExtension;
          try {
            tryFileExtension = StorageUtil.getFileExtension(response);
          } catch (MimeTypeException e) {
            LOGGER.warn("Cannot find mime type defaulting to no extension");
            tryFileExtension = "";
          }
          final String fileExtension = tryFileExtension;

          LOGGER.debug("Using extension ({}) for: {}", fileExtension, request.getUrl());
          createFile(content, new File(storagePath, subDirName), sId + fileExtension + ".gz");
          conn.commit();
          pstmt.close();
          LOGGER.debug("Record stored successfully for: {}", request.getUrl());
          return sId;
        }
      }

      conn.rollback();
      throw new StorageException("Cannot store the record");
    } catch (SQLException | IOException e) {
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException e2) {
          e.addSuppressed(e2);
          throw new StorageException("Cannot store the record", e);
        }
      }
      throw new StorageException("Cannot store the record", e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          throw new StorageException("Unable to close the connection", e);
        }
      }
    }
  }

  @Override
  public final Record<Integer> get(final Integer id) throws StorageException {
    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM `" + table + "` WHERE id = ?")) {
      pstmt.setInt(1, id);
      final ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        return createRecord(rs);
      }

    } catch (SQLException e) {
      LOGGER.error("Record query failure for id: {}", id, e);
      throw new StorageException("Cannot retrieve the record", e);
    }
    LOGGER.debug("No record found for id: {}", id);
    return null;
  }

  @Override
  public final Record<Integer> get(final Request request) throws StorageException {
    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM `" + table + "` "
             + "WHERE url = ? "
             + "AND method = ? "
             + "AND request_headers = CAST(? AS JSON) "
             + "AND request_body = CAST(? AS JSON) "
             + "ORDER BY `date_created` DESC "
         )) {
      pstmt.setString(1, request.getUrl());
      pstmt.setString(2, request.getMethod().name());
      pstmt.setString(3, new JSONObject(request.getHeaders()).toString());
      pstmt.setString(4, new JSONObject(prepareRequestBody(request)).toString());
      final ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        return createRecord(rs);
      }

    } catch (SQLException e) {
      LOGGER.error("Record query failure for request: {}", request.getUrl(), e);
      throw new StorageException("Cannot retrieve the record for " + request.getUrl() + ".", e);
    }
    LOGGER.debug("No record found for request: {}", request.getUrl());
    return null;
  }

  @Override
  public final void close() throws SQLException {
    if (dataSource instanceof AutoCloseable) {
      try {
        ((AutoCloseable) dataSource).close();
      } catch (final SQLException e) {
        throw e;
      } catch (Exception e) {
        LOGGER.error("Unexpected exception during closing", e);
      }
    }
  }

  /**
   * A callback wrapper for to run complete multithreaded.
   */
  public static final class CompletedThreadedCallback implements Callback {

    /**
     * The executor used to submit tasks.
     */
    private final ExecutorService executorService;

    /**
     * The callback to trigger upon response.
     */
    private final FileManagerCallback fileManagerCallback;

    /**
     * Constructs an instance of ThreadedCallback.
     *
     * @param fileManager an instance of file manager used to store raw responses.
     */
    private CompletedThreadedCallback(final FileManager<?> fileManager) {
      this.fileManagerCallback = new FileManagerCallback(fileManager);
      this.executorService = Executors.newCachedThreadPool(
          new ThreadFactoryBuilder().setNameFormat("FileManager I/O %d").build());
    }

    @Override
    public void completed(final @NotNull Request request, final @NotNull Response response) {
      executorService.execute(() -> fileManagerCallback.completed(request, response));
    }

    @Override
    public void failed(final @NotNull Request request, final @NotNull Exception ex) {
      fileManagerCallback.failed(request, ex);
    }

    @Override
    public void cancelled(final @NotNull Request request) {
      fileManagerCallback.cancelled(request);
    }
  }

}
