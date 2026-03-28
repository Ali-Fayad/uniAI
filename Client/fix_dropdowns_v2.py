import os
import re

sections = [
    ("src/components/page/personalInfo/sections/EducationSection.tsx", "isUniversitiesLoading", "universitySuggestions", "universityQuery"),
    ("src/components/page/personalInfo/sections/ExperienceSection.tsx", "isPositionsLoading", "positionSuggestions", "positionQuery"),
    ("src/components/page/personalInfo/sections/SkillsSection.tsx", "isSkillsLoading", "skillSuggestions", "skillQuery"),
    ("src/components/page/personalInfo/sections/LanguagesSection.tsx", "isLanguagesLoading", "languageSuggestions", "languageQuery")
]

for filepath, loading_state, suggestions_state, query_state in sections:
    with open(filepath, 'r') as f:
        content = f.read()

    # 1. Import hooks
    if "useOnClickOutside" not in content:
        content = content.replace("import React", "import React, { useState, useRef }")
        content = content.replace("from 'react';", "from 'react';\nimport { useOnClickOutside } from '../../../../hooks/useOnClickOutside';")

    # 2. Add state and ref inside component
    component_match = re.search(r'(const \w+: React\.FC<.*?> = \([^)]+\) => {)', content, re.DOTALL)
    if component_match and "const [isDropdownOpen" not in content:
        header_end = component_match.end()
        insertion = """
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  useOnClickOutside(containerRef, () => setIsDropdownOpen(false));
"""
        content = content[:header_end] + insertion + content[header_end:]

    # 3. Add ref to the wrapper div
    content = content.replace('<div className="relative flex flex-col sm:flex-row', '<div ref={containerRef} className="relative flex flex-col sm:flex-row', 1)

    # 4. Update the condition
    original_cond = f"{{({loading_state} || {suggestions_state}.length > 0) && ("
    new_cond = f"{{isDropdownOpen && ({loading_state} || {suggestions_state}.length > 0) && ("
    content = content.replace(original_cond, new_cond)

    # 5. Add onFocus to input
    # Find the specific query input block:
    # "value={universityQuery}\n          onChange="
    query_block = f"value={{{query_state}}}\n          onChange="
    new_query_block = f"value={{{query_state}}}\n          onFocus={{() => setIsDropdownOpen(true)}}\n          onChange="
    
    content = content.replace(query_block, new_query_block)

    # 6. Inside onChange add setIsDropdownOpen(true)
    # The onChange looks like: "onChange={(e) => {\n            set"
    onchange_block_1 = f"onChange={{(e) => {{\n            set{query_state[0].upper() + query_state[1:]}("
    new_onchange_block_1 = f"onChange={{(e) => {{\n            setIsDropdownOpen(true);\n            set{query_state[0].upper() + query_state[1:]}("
    
    # Also handle some single line onChanges if they exist, but they are block based:
    content = content.replace(onchange_block_1, new_onchange_block_1)

    with open(filepath, 'w') as f:
        f.write(content)
