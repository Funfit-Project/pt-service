package funfit.pt.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadPostResponse {


    private String name;
    private String content;
    private String category;
    private List<String> imageUrls;
    private List<CommentDto> comments;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentDto {
        private String name;
        private String content;
    }
}
