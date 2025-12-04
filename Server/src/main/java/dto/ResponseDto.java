package dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {
    private String username;
    private  String firstName;
    private  String lastName;
    private String email;
    private boolean isVerified = false;
    private boolean isTwoFacAuth =  false;
}
