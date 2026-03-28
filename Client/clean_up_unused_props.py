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

    # Generic removal of editing props from interfaces
    content = re.sub(r'\s*editing[a-zA-Z]+Id[^;]+;', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Id[^;]+;', '', content)
    content = re.sub(r'\s*editing[a-zA-Z]+Value[^;]+;', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Value[^;]+;', '', content)
    content = re.sub(r'\s*editing[a-zA-Z]+Name[^;]+;', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Name[^;]+;', '', content)
    content = re.sub(r'\s*editing[a-zA-Z]+Issuer[^;]+;', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Issuer[^;]+;', '', content)
    content = re.sub(r'\s*editing[a-zA-Z]+Description[^;]+;', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Description[^;]+;', '', content)
    content = re.sub(r'\s*editing[a-zA-Z]+Position[^;]+;', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Position[^;]+;', '', content)
    content = re.sub(r'\s*editing[a-zA-Z]+Company[^;]+;', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Company[^;]+;', '', content)

    # Generic removal from component destructured arguments
    content = re.sub(r'\s*editing[a-zA-Z]+Id,', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Id,', '', content)
    content = re.sub(r'\s*editing[a-zA-Z]+Value,', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Value,', '', content)
    content = re.sub(r'\s*editing[a-zA-Z]+Name,', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Name,', '', content)
    content = re.sub(r'\s*editing[a-zA-Z]+Issuer,', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Issuer,', '', content)
    content = re.sub(r'\s*editing[a-zA-Z]+Description,', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Description,', '', content)
    content = re.sub(r'\s*editing[a-zA-Z]+Position,', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Position,', '', content)
    content = re.sub(r'\s*editing[a-zA-Z]+Company,', '', content)
    content = re.sub(r'\s*setEditing[a-zA-Z]+Company,', '', content)

    # Remove unused imports
    content = re.sub(r'import AnimatedInput from \'../../../common/AnimatedInput\';\n', '', content)
    content = re.sub(r", moveItem", "", content)
    content = re.sub(r"moveItem, ", "", content)
    content = re.sub(r"\{ moveItem \} from '\.\./personalInfoUtils';", "'../personalInfoUtils';", content)
    content = re.sub(r", normalizeOptionId", "", content)
    content = re.sub(r"normalizeOptionId, ", "", content)
    content = re.sub(r"\{ normalizeOptionId \} from '\.\./personalInfoUtils';", "'../personalInfoUtils';", content)

    with open(file, 'w') as f:
        f.write(content)
