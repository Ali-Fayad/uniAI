import re

file_path = '/home/afayad2/uniAI/Client/src/components/page/personalInfo/usePersonalInfoController.ts'

with open(file_path, 'r') as f:
    content = f.read()

content = content.replace(
    '  error: string | null;',
    '  error: string | null;\n  missingFields: string[];'
)

content = content.replace(
    '  const [error, setError] = useState<string | null>(null);',
    '  const [error, setError] = useState<string | null>(null);\n  const [missingFields, setMissingFields] = useState<string[]>([]);'
)

old_save_start = '''  const saveChanges = async () => {
    const validationError = validatePersonalInfoState(personalInfoState);
    if (validationError) {
      setError(validationError);
      return;
    }'''

new_save_start = '''  const saveChanges = async () => {
    const validation = validatePersonalInfoState(personalInfoState);
    if (validation.error) {
      setError(validation.error);
      setMissingFields(validation.missingFields);
      return;
    }
    setMissingFields([]);'''

content = content.replace(old_save_start, new_save_start)

content = content.replace(
    '    error,\n    saveToast,',
    '    error,\n    missingFields,\n    saveToast,'
)

if 'missingFields,' not in content:
    pattern = r'(    error,)'
    replacement = r'\1\n    missingFields,'
    content = re.sub(pattern, replacement, content)

with open(file_path, 'w') as f:
    f.write(content)

