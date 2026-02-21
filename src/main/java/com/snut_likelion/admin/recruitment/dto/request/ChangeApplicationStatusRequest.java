package com.snut_likelion.admin.recruitment.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChangeApplicationStatusRequest {

    private Long recId;
    private List<Long> ids;

    public ChangeApplicationStatusRequest(Long recId, List<Long> ids) {
        this.recId = recId;
        this.ids = ids;
    }
}
