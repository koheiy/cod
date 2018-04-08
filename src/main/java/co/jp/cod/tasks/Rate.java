package co.jp.cod.tasks;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Rate {
    private String currency;
    private String currencyCode;
    private String tts;
    private String ttb;
    private String ttm;
}
