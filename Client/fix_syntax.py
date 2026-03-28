import os
import re

dir_path = "src/components/page/personalInfo/sections"

def fix_file(filepath):
    with open(filepath, 'r') as file:
        content = file.read()

    # Find the closing bracket of the destructured parameters to the component `}) => {`
    # The previous script might have accidentally removed `}) => {`

    # Regex to find where the `({` parameter list ends and regular code begins
    pattern = re.compile(r'(const \w+: React\.FC<.*?> = \(\{\n(?:[\s\S]*?))\s+const containerRef', re.MULTILINE)

    def replacement(match):
        return match.group(1).rstrip() + "\n}) => {\n  const containerRef"

    new_content = pattern.sub(replacement, content)
    
    # Also CertificatesSection has a syntax error
    if 'CertificatesSection.tsx' in filepath:
         new_content = new_content.replace('addCertificate,\n};', 'addCertificate,\n}) => {')
         new_content = new_content.replace('addCertificate,\n  setError,\n};', 'addCertificate,\n  setError,\n}) => {')

    
    with open(filepath, 'w') as file:
        file.write(new_content)

for filename in os.listdir(dir_path):
    if filename.endswith(".tsx"):
        fix_file(os.path.join(dir_path, filename))

