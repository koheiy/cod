package co.jp.cod.tasks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RateFileDownloader {

    @Value("${target.url}")
    private String targetUrl;

    @Value("${target.savedirectory}")
    private String saveDirectory;

    @Scheduled(cron = "0 0 13 * * 1-5", zone = "Asia/Tokyo")
    public void download() throws JsonProcessingException {
        downloadFor(LocalDate.now());
    }

    public void downloadFor(LocalDate targetDate) throws JsonProcessingException {
        String targetDateFormat = targetDate.format(DateTimeFormatter.ofPattern("uuuuMMdd"));
        String accessTargetUrl = targetUrl.replace("yyyyMMdd", targetDateFormat);

        RestTemplate template = new RestTemplateBuilder().build();
        template.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("Shift_JIS")));
        String text = template.getForObject(accessTargetUrl, String.class, new Object[]{});

        String[] parsedStrings = text.split("\r\n");
        List<String> jsonBase= new ArrayList<>(parsedStrings.length);
        Arrays.stream(parsedStrings).forEach(t -> {
            jsonBase.add((t.replaceAll(" +", " ")));
        });

        List<Rate> rateList = new ArrayList<>(jsonBase.size());
        for (int i=4; i<jsonBase.size(); i++) {
            String line = jsonBase.get(i);
            if (Strings.isEmpty(line)) break;
            String[] l = line.split(" ");
            rateList.add(Rate.builder().currency(l[0].replaceAll("　", "")).currencyCode(l[1]).tts(l[2]).ttb(l[3]).ttm(l[4]).build());
        }

        Rates rates = new Rates();
        rates.setRates(rateList);

        String json = new ObjectMapper().writeValueAsString(rates);

        try (BufferedWriter br = Files.newBufferedWriter(Paths.get(saveDirectory + "quote_" + targetDateFormat + ".json"), StandardCharsets.UTF_8)) {
            br.write(json);
            br.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
