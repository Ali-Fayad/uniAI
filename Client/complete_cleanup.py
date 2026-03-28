import os
import re

files_to_clean_props = [
    "src/components/page/personalInfo/sections/CertificatesSection.tsx",
    "src/components/page/personalInfo/sections/EducationSection.tsx",
    "src/components/page/personalInfo/sections/ExperienceSection.tsx",
    "src/components/page/personalInfo/sections/LanguagesSection.tsx",
    "src/components/page/personalInfo/sections/ProjectsSection.tsx",
    "src/components/page/personalInfo/sections/SkillsSection.tsx",
    "src/components/page/personalInfo/PersonalInfoPageShell.tsx",
    "src/components/page/personalInfo/usePersonalInfoController.ts",
    "src/components/page/personalInfo/usePersonalInfoDraftState.ts",
]

def remove_editing_lines(content):
    lines = content.split('\n')
    new_lines = []
    for line in lines:
        if re.search(r'\b(editing[A-Z]\w+|setEditing[A-Z]\w+)\b', line):
            continue
        new_lines.append(line)
    return '\n'.join(new_lines)


def remove_secondary_lists(content):
    # Regex to find `<div className="space-y-2` inside the `PersonalInfoSectionCard`
    # and remove it, up to `    </PersonalInfoSectionCard>`
    start_str = '      <div className="space-y-2'
    end_str = '    </PersonalInfoSectionCard>'
    
    start_idx = content.find(start_str)
    if start_idx != -1:
        end_idx = content.find(end_str, start_idx)
        if end_idx != -1:
            content = content[:start_idx] + content[end_idx:]
    return content

for filepath in files_to_clean_props:
    with open(filepath, 'r') as f:
        content = f.read()

    # Step 1: Remove imports of moveItem
    content = re.sub(r'import\s+\{.*?\bmoveItem\b.*?\}\s+from\s+[\'"].*?[\'\"];?\n?', '', content)
    
    # Actually wait moveItem might be alongside other imports like `import { createClientId, moveItem }`
    # If so, we need a smarter regex for just removing `moveItem` or the whole thing.
    content = content.replace('moveItem, ', '')
    content = content.replace(', moveItem', '')
    content = content.replace('moveItem', '') # if lone import
    # This might leave `import { } from '../personalInfoUtils';`. We can clean empty imports:
    content = re.sub(r'import\s*\{\s*\}\s*from\s*[\'"][^\'"]*[\'"];?\n?', '', content)
    
    # Step 2: remove all editing* props and lines
    content = remove_editing_lines(content)
    
    # Step 3: Remove secondary lists
    if 'sections/' in filepath:
        content = remove_secondary_lists(content)
        
    with open(filepath, 'w') as f:
        f.write(content)

