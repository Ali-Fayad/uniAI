package com.uniai.cvbuilder.application.port.in;

import com.uniai.cvbuilder.application.dto.command.UpdatePersonalInfoCommand;
import com.uniai.cvbuilder.application.dto.response.PersonalInfoResponse;

public interface PersonalInfoUseCase {

    PersonalInfoResponse getPersonalInfo(String email);

    PersonalInfoResponse updatePersonalInfo(String email, UpdatePersonalInfoCommand command);
}
