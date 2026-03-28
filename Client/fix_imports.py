files = [
    "src/components/page/personalInfo/sections/ExperienceSection.tsx",
    "src/components/page/personalInfo/sections/LanguagesSection.tsx",
    "src/components/page/personalInfo/sections/SkillsSection.tsx"
]

for filepath in files:
    with open(filepath, 'r') as f:
        content = f.read()

    content = content.replace('normalizeOptionId, ', '')
    content = content.replace(', normalizeOptionId', '')
    content = content.replace('import { normalizeOptionId } from \'../personalInfoUtils\';\\n', '')
    # just in case it is solitary
    if 'import { normalizeOptionId }' in content:
        content = content.replace("import { normalizeOptionId } from '../personalInfoUtils';\n", '')

    with open(filepath, 'w') as f:
        f.write(content)

