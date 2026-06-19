package com.share.vo;

import com.share.entity.Note;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class NoteVO extends Note {
    private boolean Liked;
    private boolean Collected;
    private String nickname;
    private String username;
    private String userAvatar;
}
