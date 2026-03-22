package com.uniai.cvbuilder.application.port.in;

import com.uniai.cvbuilder.application.dto.command.UpdatePersonalInfoCommand;
import com.uniai.cvbuilder.application.dto.response.PersonalInfoResponse;

/**
 * Application boundary for retrieving and updating user-level personal information reused across CVs.
 */
public interface PersonalInfoUseCase {

    PersonalInfoResponse getPersonalInfo(String email);

    PersonalInfoResponse updatePersonalInfo(String email, UpdatePersonalInfoCommand command);
}
