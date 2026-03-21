package com.uniai.cvbuilder.domain.builder;

import com.uniai.cvbuilder.domain.model.PersonalInfo;

public final class PersonalInfoBuilder {

    private final PersonalInfo.PersonalInfoBuilder builder;

    private PersonalInfoBuilder(Long userId) {
        this.builder = PersonalInfo.builder().userId(userId);
    }

    public static PersonalInfoBuilder forUser(Long userId) {
        return new PersonalInfoBuilder(userId);
    }

    public PersonalInfoBuilder phone(String phone) {
        builder.phone(phone);
        return this;
    }

    public PersonalInfoBuilder address(String address) {
        builder.address(address);
        return this;
    }

    public PersonalInfoBuilder linkedin(String linkedin) {
        builder.linkedin(linkedin);
        return this;
    }

    public PersonalInfoBuilder github(String github) {
        builder.github(github);
        return this;
    }

    public PersonalInfoBuilder portfolio(String portfolio) {
        builder.portfolio(portfolio);
        return this;
    }

    public PersonalInfoBuilder summary(String summary) {
        builder.summary(summary);
        return this;
    }

    public PersonalInfoBuilder jobTitle(String jobTitle) {
        builder.jobTitle(jobTitle);
        return this;
    }

    public PersonalInfoBuilder company(String company) {
        builder.company(company);
        return this;
    }

    public PersonalInfo build() {
        return builder.build();
    }
}
