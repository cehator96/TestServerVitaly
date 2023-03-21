package ru.test;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
public class Server {
    public static String dir = "tmp/"; // задаем директорию
    public static void main(String[] args) throws IOException
    {
        int serverPort = 8000;
        System.out.println("serv OK");
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        server.createContext("/api/hello", (exchange ->
        {
            OutputStream output = exchange.getResponseBody();
            if ("GET".equals(exchange.getRequestMethod())) {
                File[] files = new File(dir).listFiles();
                if (!exchange.getRequestURI().toString().contains("?")) {
                    List<String> lstFileNames = new ArrayList<>();
                    if (files != null) {
                        for (File file : files) {
                            lstFileNames.add(file.getName());
                        }
                    }
                    byte[] bytes = getBytesFromArray(lstFileNames);
                    exchange.sendResponseHeaders(200, bytes.length);
                    output.write(bytes);
                    output.flush();
                } else
                {
                    Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                    File file = new File(dir + params.get("name"));
                    if (file.exists())
                    {
                        try
                        {
                            Headers headers = exchange.getResponseHeaders();
                            headers.add("content-disposition" , "form-data;name=\"filefield\";filename="+file.getName());
                            headers.add("Content-Type", "application/octet-stream");
                            FileInputStream fis = new FileInputStream(file);
                            byte[] bytes = fis.readAllBytes();
                            if (bytes.length > 0)
                            {
                                exchange.sendResponseHeaders(200, bytes.length);
                                output.write(bytes);
                                output.flush();
                            }
                            else
                            {
                                String err = "File is not found";
                                exchange.sendResponseHeaders(404, err.getBytes(StandardCharsets.UTF_8).length);
                                output.write(err.getBytes(StandardCharsets.UTF_8));
                                output.flush();
                            }
                            //   byte[] array = Files.readAllBytes(Path.of(file.getAbsolutePath()));
                        }
                        catch (Exception e)
                        {

                        }
                    } else
                    {
                        String err = "File not found";
                        exchange.sendResponseHeaders(404, err.getBytes(StandardCharsets.UTF_8).length);
                        output.write(err.getBytes(StandardCharsets.UTF_8));
                        output.flush();
                    }
                }
                //обработка 2-го GET
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                FormDataAnalys formData = new FormDataAnalys(getFormDataBytes(exchange));
                String filename = formData.getParams().get("filename");
                File fileServer = new File(dir + filename);
                if (fileServer.exists())
                {
                    ByteArrayInputStream bis = new ByteArrayInputStream(formData.getFileData().clone());
                    String md5S = getHash(new FileInputStream(fileServer));
                    String md5 = getHash(bis);
                    if (!Objects.equals(md5, md5S))
                    {
                        FileOutputStream fos = new FileOutputStream(fileServer);
                        fos.write(formData.getFileData().clone());
                        fos.close();
                        String msg = "File updated succesfully";
                        exchange.sendResponseHeaders(200, msg.getBytes().length);
                        output.write(msg.getBytes());
                        output.flush();
                    } else
                    {
                        String msg = "Files is equal";
                        exchange.sendResponseHeaders(200, msg.getBytes().length);
                        output.write(msg.getBytes());
                        output.flush();
                    }
                } else
                {
                    String msg = "File not found";
                    exchange.sendResponseHeaders(404, msg.getBytes(StandardCharsets.UTF_8).length);
                    output.write(msg.getBytes());
                    output.flush();
                }
            }
            if ("PUT".equals(exchange.getRequestMethod())) {

                FormDataAnalys formData = new FormDataAnalys(getFormDataBytes(exchange));
                String filename = formData.getParams().get("filename");
                File fileClient = new File(dir + filename);

//                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
//                System.out.println("param A=" + params.get("name"));
//                File file = new File(dir + params.get("name"));

                if (fileClient.exists())
                {
                    String err = "File is already exists";
                    exchange.sendResponseHeaders(500, err.getBytes(StandardCharsets.UTF_8).length);
                    output.write(err.getBytes(StandardCharsets.UTF_8));
                    output.flush();
                }
                else
                {
                    try(FileOutputStream fos = new FileOutputStream(fileClient))
                {
                    fos.write(formData.getFileData());
                }
                    String msg = "File created successfully";
                    exchange.sendResponseHeaders(200, msg.getBytes(StandardCharsets.UTF_8).length);
                    output.write(msg.getBytes(StandardCharsets.UTF_8));
                    output.flush();
                }

            }
            if ("DELETE".equals(exchange.getRequestMethod())) {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());

                System.out.println("param A=" + params.get("name"));

                File file = new File(dir + params.get("name"));

                if (file.exists()) {
                    file.delete();
                    String err = "File deleted successfully";
                    exchange.sendResponseHeaders(200, err.getBytes(StandardCharsets.UTF_8).length);
                    output.write(err.getBytes(StandardCharsets.UTF_8));
                    output.flush();
                } else {
                    String err = "File not found";
                    exchange.sendResponseHeaders(404, err.getBytes(StandardCharsets.UTF_8).length);
                    output.write(err.getBytes(StandardCharsets.UTF_8));
                    output.flush();
                }
            }
            exchange.close();
        }));
        server.setExecutor(null); // creates a default executor
        server.start();
    }
    @SneakyThrows
    private static byte[] getBytesFromArray(List<String> array) {
        HashMap<String, String> myHashMap = new HashMap<String, String>();
        List<Map<String, String>> myMap = new ArrayList<>();
        for (String item : array)
        {
            File file = new File(dir + item);
            FileInputStream fis = new FileInputStream(file);
            String md5 = getHash(fis);
            myHashMap.put("name", item);
            myHashMap.put("hash", md5);
            myMap.add(myHashMap);
        }
        return myMap.toString().getBytes();
    }
    private static Map<String, String> queryToMap(String query) {
        if (query == null) {
            return null;
        }

        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }
    @SneakyThrows
    private static String getHash(InputStream fis) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        String hex = checksum(fis, md);
        return hex;
    }
private static String checksum(InputStream fis, MessageDigest md) throws IOException {
        byte[] buffer = new byte[1024];
        int nread;
        while ((nread = fis.read(buffer)) != -1)
        {
            md.update(buffer, 0, nread);
        }
// байты в гекс
    StringBuilder result = new StringBuilder();
    for (byte b : md.digest()) {
        result.append(String.format("%02x", b));
    }
    return result.toString();
}
    public static byte[] getFormDataBytes(HttpExchange request) throws IOException {
        InputStream is = request.getRequestBody();
        int size = -1;

        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while ((size = is.read(buffer)) != -1) {
            bis.write(buffer, 0, size);
        }
        buffer = bis.toByteArray();
        bis.close();
        return buffer;

    }
}
