import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {
    private static final String URL = "https://api.nasa.gov/planetary/apod?api_key=aMIG936v01LxMZlQ2eyBbZrKOeqHf4hMCIfqQhEu";

    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(30000)
                        .setRedirectsEnabled(false)
                        .build())
                .build();

        //запрос: URL фото дня
        HttpGet request1 = new HttpGet(URL);
        request1.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        CloseableHttpResponse response1 = httpClient.execute(request1);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Post post = gson.fromJson(new String(response1.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8), Post.class);
        response1.close();

        //если материалом дня является видео, а не фото, то записываем ссылку на видео в файл
        if(post.getMedia_type().equals("video")) {
            String explanation = post.getExplanation() + "\n\n" + "[link to video]" + "(";
            try (FileOutputStream fos = new FileOutputStream(post.getDate() + "_link_to_video.md")) {
                fos.write(explanation.getBytes(StandardCharsets.UTF_8));
                fos.write(post.getUrl().getBytes(StandardCharsets.UTF_8));
                fos.write(")".getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpClient.close();
            return;
        }

        //парсинг имени фотоснимка
        String[] names = post.getUrl().split("/");
        String photoName = names[names.length - 1].split("\\.")[0];

        //запрос: получение фотоснимка
        HttpGet request2 = new HttpGet(post.getUrl());
        request2.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        CloseableHttpResponse response2 = httpClient.execute(request2);
        var body = response2.getEntity().getContent().readAllBytes();

        //создание файла и запись в него фотоснимка
        try (FileOutputStream fos = new FileOutputStream(photoName + ".jpg")) {
            fos.write(body, 0, body.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        response2.close();
        httpClient.close();
    }
}

