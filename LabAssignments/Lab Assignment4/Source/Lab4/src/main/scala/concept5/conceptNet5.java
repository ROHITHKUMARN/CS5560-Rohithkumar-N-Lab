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

        File myfile_rk= new File("output/conceptnet5");
        myfile_rk.createNewFile();
        FileWriter fileWriter_rk= new FileWriter(myfile_rk);
        HttpClient httpClient_rk = new DefaultHttpClient();
        String line_rk = "";
        try {
            HttpGet httpGetRequest = new HttpGet("http://conceptnet5.media.mit.edu/data/5.4/search?rel=/r/PartOf&end=/c/en/aeroplane&limit=10");
            HttpResponse httpResponse = httpClient_rk.execute(httpGetRequest);

            System.out.println("----------------------------------------");
            System.out.println(httpResponse.getStatusLine());
            System.out.println("----------------------------------------");

            HttpEntity entity_rk = httpResponse.getEntity();

            byte[] buffer = new byte[1024];
            if (entity_rk != null) {
                InputStream inputStream = entity_rk.getContent();
                int bytesRead_rk = 0;
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                while ((bytesRead_rk = bis.read(buffer)) != -1) {
                    String chunk_rk = new String(buffer, 0, bytesRead_rk);
                    System.out.println(chunk_rk);
                    line_rk += chunk_rk;
                }
                inputStream.close();
            }
            JSONParser parser_rk = new JSONParser();
            Object object_rk = parser_rk.parse(line_rk);
            JSONObject jsonObject_rk = (JSONObject) object_rk;
            JSONArray jsonArray_rk = (JSONArray) jsonObject_rk.get("edges");
            for (int i_rk = 0; i_rk < jsonArray_rk.size(); i_rk++) {
                JSONObject object = (JSONObject) jsonArray_rk.get(i_rk);
                //System.out.println(ob.get("surfaceText"));
                fileWriter_rk.write(object.get("surfaceText").toString());
                fileWriter_rk.write("\n");
                fileWriter_rk.flush();
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpClient_rk.getConnectionManager().shutdown();
            fileWriter_rk.close();
        }

    }
}


/*
output:
http://conceptnet5.media.mit.edu/data/5.4/search?rel=/r/PartOf&end=/c/en/aeroplane&limit=10

 */