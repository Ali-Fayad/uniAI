package com.uniai.cvbuilder.application.mapper;

import com.uniai.cvbuilder.application.dto.ItemsOrderDto;
import com.uniai.cvbuilder.application.dto.SelectedItemsDto;
import com.uniai.cvbuilder.domain.model.ItemsOrder;
import com.uniai.cvbuilder.domain.model.SelectedItems;
import java.util.List;

/**
 * Maps CV item-selection DTOs into domain value objects used by the CV aggregate.
 */
public final class CVSelectionMapper {

    private CVSelectionMapper() {}

    public static SelectedItems toSelectedItems(SelectedItemsDto dto) {
        if (dto == null) {
            return new SelectedItems();
        }
        return SelectedItems.builder()
            .skillIds(orEmpty(dto.getSkillIds()))
            .languageIds(orEmpty(dto.getLanguageIds()))
            .educationIds(orEmpty(dto.getEducationIds()))
            .experienceIds(orEmpty(dto.getExperienceIds()))
            .projectIds(orEmpty(dto.getProjectIds()))
            .certificateIds(orEmpty(dto.getCertificateIds()))
            .build();
    }

    public static ItemsOrder toItemsOrder(ItemsOrderDto dto) {
        if (dto == null) {
            return new ItemsOrder();
        }
        return ItemsOrder.builder()
            .skillIds(orEmpty(dto.getSkillIds()))
            .languageIds(orEmpty(dto.getLanguageIds()))
            .educationIds(orEmpty(dto.getEducationIds()))
            .experienceIds(orEmpty(dto.getExperienceIds()))
            .projectIds(orEmpty(dto.getProjectIds()))
            .certificateIds(orEmpty(dto.getCertificateIds()))
            .build();
    }

    private static List<String> orEmpty(List<String> values) {
        return values != null ? values : List.of();
    }
}
