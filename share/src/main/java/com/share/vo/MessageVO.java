package com.share.vo;

import com.share.entity.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class MessageVO extends Message {
    private String avatar;
    private String nickname;
}
