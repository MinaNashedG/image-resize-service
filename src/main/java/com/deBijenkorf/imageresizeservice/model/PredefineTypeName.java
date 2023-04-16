package com.deBijenkorf.imageresizeservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PredefineTypeName {
    THUMBNAIL("thumbnail"), DETAIL_LARGE("detail-large");
    final String value;
}
