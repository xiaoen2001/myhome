package com.share.vo;

import com.share.entity.History;
import com.share.entity.Note;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;


@EqualsAndHashCode(callSuper = true)
@Data
public class HistoryVO extends Note {
    private LocalDateTime browsedAt;
}
