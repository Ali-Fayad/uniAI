import re

file_path = "/home/afayad2/uniAI/Server/src/main/java/com/uniai/cvbuilder/presentation/controller/PersonalInfoController.java"

with open(file_path, 'r') as f:
    content = f.read()

new_method = '''
    @GetMapping("/status")
    public ResponseEntity<java.util.Map<String, Object>> getPersonalInfoStatus() {
        String email = jwtFacade.getAuthenticatedUserEmail();
        PersonalInfoResponse info = personalInfoUseCase.getPersonalInfo(email);
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("isFilled", info.isFilled());
        
        java.util.List<String> missingFields = new java.util.ArrayList<>();
        if (info.getPhone() == null || info.getPhone().trim().isEmpty()) missingFields.add("phone");
        if (info.getAddress() == null || info.getAddress().trim().isEmpty()) missingFields.add("address");
        if (info.getSummary() == null || info.getSummary().trim().isEmpty()) missingFields.add("summary");
        if (info.getSkills() == null || info.getSkills().isEmpty()) missingFields.add("skills");
        
        response.put("missingFields", missingFields);
        return ResponseEntity.ok(response);
    }
}'''

content = re.sub(r'\}\s*$', new_method, content)

with open(file_path, 'w') as f:
    f.write(content)
