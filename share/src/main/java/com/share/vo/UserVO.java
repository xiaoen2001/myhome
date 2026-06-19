package com.share.vo;

import com.share.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class UserVO extends User {
    private String noteCount;
    private String likeCount;
    private String collectionCount;
}
