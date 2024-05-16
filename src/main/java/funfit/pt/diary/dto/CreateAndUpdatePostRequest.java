package funfit.pt.diary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAndUpdatePostRequest {

    @NotBlank
    private String content;

    private List<String> imageUrls;
}
