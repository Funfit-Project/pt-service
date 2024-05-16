package funfit.pt.diary.entity;

import funfit.pt.exception.ErrorCode;
import funfit.pt.exception.customException.BusinessException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum Category {

    PT_LOG("수업일지"),
    DIARY("다이어리");

    private final String name;

    public static Category find(String name) {
        return Arrays.stream(Category.values())
                .filter(category -> category.name.equals(name))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CATEGORY));
    }
}
