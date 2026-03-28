import re
import os

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


def process_section_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Clean moveItem safely
    content = content.replace('moveItem, ', '')
    content = content.replace(', moveItem', '')
    content = content.replace('moveItem', '') 
    content = re.sub(r'import\s*\{\s*\}\s*from\s*[\'"][^\'"]*[\'"];?\n?', '', content)

    # Clean editing lines
    content = remove_editing_lines(content)

    # Replace space-y-2
    start_str = '      <div className="space-y-2'
    end_str = '    </PersonalInfoSectionCard>'
    
    start_idx = content.find(start_str)
    if start_idx != -1:
        end_idx = content.find(end_str, start_idx)
        if end_idx != -1:
            if 'CertificatesSection.tsx' in filepath:
                chip_view = """      <div className="flex flex-wrap gap-2 mt-4">
        {certificates.map((item) => (
          <span
            key={item.id}
            className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] px-3 py-1 text-sm text-[var(--color-textPrimary)]"
          >
            {item.name}{item.issuer ? ` · ${item.issuer}` : ''}
            <button
              type="button"
              onClick={() => setCertificates((prev) => prev.filter((entry) => entry.id !== item.id))}
              className="text-[var(--color-textSecondary)] hover:text-[var(--color-textPrimary)]"
            >
              ×
            </button>
          </span>
        ))}
      </div>
"""
                content = content[:start_idx] + chip_view + content[end_idx:]
            elif 'ProjectsSection.tsx' in filepath:
                chip_view = """      <div className="flex flex-wrap gap-2 mt-4">
        {projects.map((item) => (
          <span
            key={item.id}
            className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] px-3 py-1 text-sm text-[var(--color-textPrimary)]"
          >
            {item.name}
            <button
              type="button"
              onClick={() => setProjects((prev) => prev.filter((entry) => entry.id !== item.id))}
              className="text-[var(--color-textSecondary)] hover:text-[var(--color-textPrimary)]"
            >
              ×
            </button>
          </span>
        ))}
      </div>
"""
                content = content[:start_idx] + chip_view + content[end_idx:]
            else:
                content = content[:start_idx] + content[end_idx:]

    with open(filepath, 'w') as f:
        f.write(content)

for filepath in files_to_clean_props:
    if 'sections/' in filepath:
        process_section_file(filepath)
    else:
        with open(filepath, 'r') as f:
             content = f.read()
             content = remove_editing_lines(content)
             with open(filepath, 'w') as f:
                 f.write(content)

