package me.moonsoo.travelerapplication.deserialize;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Page {
    private Integer size;
    private Integer totalElements;
    private Integer totalPages;
    private Integer number;
}
