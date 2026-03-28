import os
import re

sections = [
    "src/components/page/personalInfo/sections/EducationSection.tsx",
    "src/components/page/personalInfo/sections/ExperienceSection.tsx",
    "src/components/page/personalInfo/sections/SkillsSection.tsx",
    "src/components/page/personalInfo/sections/LanguagesSection.tsx"
]

for filepath in sections:
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
    # For Education/Experience/Skills, it's <div className="relative flex flex-col sm:flex-row gap-3 items-start"> or similar.
    # We can just replace `<div className="relative ` with `<div ref={containerRef} className="relative ` (assuming there's only one main relative div for the input)
    content = content.replace('<div className="relative flex flex-col', '<div ref={containerRef} className="relative flex flex-col', 1)

    # 4. Update the condition to include isDropdownOpen &&
    # In LanguagesSection: {(isLanguagesLoading || languageSuggestions.length > 0) && (
    # In EducationSection: {(isUniversitiesLoading || universitySuggestions.length > 0) && (
    # In SkillsSection: {(isSkillsLoading || skillSuggestions.length > 0) && (
    # In ExperienceSection: {(isPositionsLoading || positionSuggestions.length > 0) && (
    content = re.sub(r'\{\(is([A-Za-z]+)Loading \|\| ([A-Za-z]+)Suggestions\.length > 0\) && \(',
                     r'{isDropdownOpen && (is\1Loading || \2Suggestions.length > 0) && (', content)

    # 5. Add onFocus to inputs
    # Let's find AnimatedInput or input and add onFocus={() => setIsDropdownOpen(true)}
    content = content.replace('onChange={(e) => {', 'onFocus={() => setIsDropdownOpen(true)}\n          onChange={(e) => {\n            setIsDropdownOpen(true);')

    with open(filepath, 'w') as f:
        f.write(content)
