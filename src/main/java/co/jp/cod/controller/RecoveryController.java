package co.jp.cod.controller;

import static java.time.format.DateTimeFormatter.ofPattern;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.jp.cod.tasks.RateFileDownloader;

@RestController
@RequestMapping("/api/v1")
public class RecoveryController {

    @Autowired
    private RateFileDownloader downloader;

    @GetMapping("/rate/mizuho/{targetDate}")
    public ResponseEntity<String> download(@PathVariable("targetDate") String targetDate) {
        if (Strings.isEmpty(targetDate)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            LocalDate date = LocalDate.parse(
                targetDate,
                // http://tech.furyu.jp/blog/?p=4313
                ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT));

            downloader.downloadFor(date);
        } catch(DateTimeParseException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}