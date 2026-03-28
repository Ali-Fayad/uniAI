import re

files = [
    "src/components/page/personalInfo/sections/CertificatesSection.tsx",
    "src/components/page/personalInfo/sections/EducationSection.tsx",
    "src/components/page/personalInfo/sections/ExperienceSection.tsx",
    "src/components/page/personalInfo/sections/LanguagesSection.tsx",
    "src/components/page/personalInfo/sections/ProjectsSection.tsx",
    "src/components/page/personalInfo/sections/SkillsSection.tsx"
]

for file in files:
    with open(file, 'r') as f:
        content = f.read()
    
    # We want to remove everything from `<div className="space-y-2` to the end of the section card.
    # The end of the section card is `    </PersonalInfoSectionCard>`
    # We will slice out the div.
    match = re.search(r'      <div className="space-y-2[^\n]*\n', content)
    if match:
        start_idx = match.start()
        end_idx = content.find('    </PersonalInfoSectionCard>', start_idx)
        if end_idx != -1:
            content = content[:start_idx] + content[end_idx:]
            
    with open(file, 'w') as f:
        f.write(content)
