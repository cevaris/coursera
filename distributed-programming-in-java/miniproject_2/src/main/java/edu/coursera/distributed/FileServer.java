package edu.coursera.distributed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {

  static class FileRequest {
    private final String method;
    private final String filePath;
    private final String protocol;

    public FileRequest(String method, String filePath, String protocol) {
      this.method = method;
      this.filePath = filePath;
      this.protocol = protocol;
    }
  }

  private final IOException emptyRequest = new IOException("empty request");

  /**
   * Main entrypoint for the basic file server.
   *
   * @param socket Provided socket to accept connections on.
   * @param fs A proxy filesystem to serve files from. See the PCDPFilesystem
   * class for more detailed documentation of its usage.
   * @throws IOException If an I/O error is detected on the server. This
   * should be a fatal error, your file server
   * implementation is not expected to ever throw
   * IOExceptions during normal operation.
   */
  public void run(final ServerSocket socket, final PCDPFilesystem fs)
      throws IOException {

    /*
     * Enter a spin loop for handling client requests to the provided
     * ServerSocket object.
     */
    while (true) {
      // TODO 1) Use socket.accept to get a Socket object
      Socket s = socket.accept();

      /*
       * TODO 2) Using Socket.getInputStream(), parse the received HTTP
       * packet. In particular, we are interested in confirming this
       * message is a GET and parsing out the path to the file we are
       * GETing. Recall that for GET HTTP packets, the first line of the
       * received packet will look something like:
       *
       *     GET /path/to/file HTTP/1.1
       */
      BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
      FileRequest request = parseFileRequest(reader.readLine());

      /*
       * TODO 3) Using the parsed path to the target file, construct an
       * HTTP reply and write it to Socket.getOutputStream(). If the file
       * exists, the HTTP reply should be formatted as follows:
       *
       *   HTTP/1.0 200 OK\r\n
       *   Server: FileServer\r\n
       *   \r\n
       *   FILE CONTENTS HERE\r\n
       *
       * If the specified file does not exist, you should return a reply
       * with an error code 404 Not Found. This reply should be formatted
       * as:
       *
       *   HTTP/1.0 404 Not Found\r\n
       *   Server: FileServer\r\n
       *   \r\n
       *
       * Don't forget to close the output stream.
       */

      PrintWriter writer = new PrintWriter(s.getOutputStream());
      String content = fs.readFile(new PCDPPath(request.filePath));

      if (content == null) {
        writer.write(String.format("%s 404 Not Found\r\n", request.protocol));
        writer.write("Server: FileServer\r\n");
        writer.write("\r\n");
      } else {
        writer.write(String.format("%s 200 OK\r\n", request.protocol));
        writer.write("Server: FileServer\r\n");
        writer.write("\r\n");
        writer.write(String.format("%s\r\n", content));
      }

      writer.flush();
      writer.close();
      s.close();
    }

  }

  private FileRequest parseFileRequest(String line) throws IOException {
    if (line != null && !line.trim().isEmpty()) {
      String[] firstLineWords = line.split(" ");
      if (firstLineWords.length == 3) {
        String command = firstLineWords[0];
        String filePath = firstLineWords[1];
        String protocol = firstLineWords[2];
        return new FileRequest(command, filePath, protocol);
      } else {
        throw new IOException(String.format("malformed request: %s", line));
      }
    }
    throw emptyRequest;
  }
}
