package concept5;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;


public class conceptNet5 {

    public final static void main(String[] args) throws IOException{

        File f= new File("output/conceptnet5");
        f.createNewFile();
        FileWriter fileWriter= new FileWriter(f);

        HttpClient httpClient = new DefaultHttpClient();
        String line = "";
        try {
            HttpGet httpGetRequest = new HttpGet("http://conceptnet5.media.mit.edu/data/5.4/search?rel=/r/PartOf&end=/c/en/aeroplane&limit=10");
            HttpResponse httpResponse = httpClient.execute(httpGetRequest);

            System.out.println("----------------------------------------");
            System.out.println(httpResponse.getStatusLine());
            System.out.println("----------------------------------------");

            HttpEntity entity = httpResponse.getEntity();

            byte[] buffer = new byte[1024];
            if (entity != null) {
                InputStream inputStream = entity.getContent();
                int bytesRead = 0;
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                while ((bytesRead = bis.read(buffer)) != -1) {
                    String chunk = new String(buffer, 0, bytesRead);
                    System.out.println(chunk);
                    line += chunk;
                }

                inputStream.close();
            }
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(line);
            JSONObject b = (JSONObject) obj;
            JSONArray ja = (JSONArray) b.get("edges");
            for (int i = 0; i < ja.size(); i++) {
                JSONObject ob = (JSONObject) ja.get(i);
                //System.out.println(ob.get("surfaceText"));
                fileWriter.write(ob.get("surfaceText").toString());
                fileWriter.write("\n");
                fileWriter.flush();
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();
            fileWriter.close();
        }

    }
}


/*
output:



 */