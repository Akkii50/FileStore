import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileStoreClient {
	private static final String BASE_URL = "http://localhost:8080/store";
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
            System.out.println("Usage: java FileStoreClient <command> <file>");
            return;
        }
		
		String command = args[0];
        String filePath = args.length > 1 ? args[1] : null; // If file path is provided, assign it


        switch (command) {
            case "add":
                addFile(filePath);
                break;
            case "ls":
                listFiles();
                break;
            case "rm":
                removeFile(filePath);
                break;
            case "update":
                updateFile(filePath);
                break;
            default:
                System.out.println("Unknown command");
        }

	}

	private static void addFile(String filePath) throws IOException {
	    File file = new File(filePath);
	    if (!file.exists()) {
	        System.out.println("File not found");
	        return;
	    }

	    String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
	    HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/add").openConnection();
	    connection.setDoOutput(true);
	    connection.setRequestMethod("POST");
	    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

	    try (OutputStream os = connection.getOutputStream();
	         PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"))) {
	        writer.append("--" + boundary).append("\r\n");
	        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
	        writer.append("Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n"); // You can use a more accurate content type here
	        writer.append("\r\n");
	        writer.flush();

	      
	        Files.copy(file.toPath(), os);
	        os.flush(); 

	        writer.append("\r\n").flush();
	        writer.append("--" + boundary + "--").append("\r\n");
	        writer.flush();
	    }

	    int responseCode = connection.getResponseCode();
	    System.out.println("Response Code: " + responseCode);

	    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
	        String inputLine;
	        StringBuilder response = new StringBuilder();
	        while ((inputLine = in.readLine()) != null) {
	            response.append(inputLine);
	        }
	        System.out.println("Response: " + response.toString());
	    } catch (IOException e) {
	        System.out.println("Error reading response: " + e.getMessage());
	    }
	}



    private static void listFiles() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/ls").openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
        }
        in.close();
    }

    private static void removeFile(String filename) throws IOException {
        System.out.println(filename);
        String urlString = BASE_URL + "/rm?filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8);
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setRequestMethod("DELETE");

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("File removed successfully.");
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            System.out.println("File not found.");
        } else {
            System.out.println("Error: " + responseCode);
        }
    }


    private static void updateFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found");
            return;
        }

        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gWn"; 
        
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + "/update").openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream os = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"))) {

            writer.append("--" + boundary).append("\r\n");
            
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
            writer.append("Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n");  // Content type, you can make it more specific
            writer.append("\r\n");
            writer.flush();

            Files.copy(file.toPath(), os);
            os.flush();  

            writer.append("\r\n").flush();
            writer.append("--" + boundary + "--").append("\r\n");
            writer.flush();
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            System.out.println("Response: " + response.toString());
        } catch (IOException e) {
            System.out.println("Error reading response: " + e.getMessage());
        }
    }


	
}
