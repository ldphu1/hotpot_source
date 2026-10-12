package com.fourspicehotpot.backend.modules.cuoctrochuyen.page;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
public class CursorPageResponse<T> {
    List<T> data;
    LocalDateTime nextCursor;
    boolean hasNext;
}