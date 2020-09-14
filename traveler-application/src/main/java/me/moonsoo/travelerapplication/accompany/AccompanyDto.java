package me.moonsoo.travelerapplication.accompany;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccompanyDto {

    @NotBlank
    private String title;
    @NotBlank
    private String article;
    @NotNull
    private LocalDateTime startDate;
    @NotNull
    private LocalDateTime endDate;
    @NotBlank
    private String location;
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;

}
